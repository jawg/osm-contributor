/**
 * Copyright (C) 2019 Takima
 * <p>
 * This file is part of OSM Contributor.
 * <p>
 * OSM Contributor is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * OSM Contributor is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with OSM Contributor.  If not, see <http://www.gnu.org/licenses/>.
 */
package io.jawg.osmcontributor.rest.managers;

import android.app.Application;
import android.util.Pair;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import io.jawg.osmcontributor.BuildConfig;
import io.jawg.osmcontributor.database.dao.PoiDao;
import io.jawg.osmcontributor.database.dao.PoiTypeDao;
import io.jawg.osmcontributor.database.dao.RelationEditionDao;
import io.jawg.osmcontributor.database.events.DbInitializedEvent;
import io.jawg.osmcontributor.database.events.InitDbEvent;
import io.jawg.osmcontributor.model.entities.Note;
import io.jawg.osmcontributor.model.entities.Poi;
import io.jawg.osmcontributor.model.entities.PoiType;
import io.jawg.osmcontributor.model.entities.RelationId;
import io.jawg.osmcontributor.model.entities.relation.FullOSMRelation;
import io.jawg.osmcontributor.model.entities.relation_save.RelationEdition;
import io.jawg.osmcontributor.model.events.PoiTypesLoaded;
import io.jawg.osmcontributor.model.events.PoisAndNotesDownloadedEvent;
import io.jawg.osmcontributor.rest.Backend;
import io.jawg.osmcontributor.rest.NetworkException;
import io.jawg.osmcontributor.rest.events.PleaseUploadPoiChangesByIdsEvent;
import io.jawg.osmcontributor.rest.events.SyncDownloadPoisAndNotesEvent;
import io.jawg.osmcontributor.rest.events.SyncDownloadWayEvent;
import io.jawg.osmcontributor.rest.events.SyncFinishUploadPoiEvent;
import io.jawg.osmcontributor.rest.events.SyncFinishUploadRelationEvent;
import io.jawg.osmcontributor.rest.events.error.SyncConflictingNodeErrorEvent;
import io.jawg.osmcontributor.rest.events.error.SyncConflictingRelationErrorEvent;
import io.jawg.osmcontributor.rest.events.error.SyncNewNodeErrorEvent;
import io.jawg.osmcontributor.rest.events.error.SyncUnauthorizedEvent;
import io.jawg.osmcontributor.rest.events.error.SyncUploadRetrofitErrorEvent;
import io.jawg.osmcontributor.ui.managers.LoginManager;
import io.jawg.osmcontributor.ui.managers.NoteManager;
import io.jawg.osmcontributor.ui.managers.PoiManager;
import io.jawg.osmcontributor.ui.managers.RelationManager;
import io.jawg.osmcontributor.utils.Box;
import io.jawg.osmcontributor.utils.FlavorUtils;
import io.jawg.osmcontributor.utils.OsmAnswers;
import rx.Observable;
import timber.log.Timber;

/**
 * Manage the synchronisation of POIs and Notes between the backend and the application.
 */
public class SyncManager {

    Application application;
    PoiManager poiManager;
    RelationManager relationManager;
    NoteManager noteManager;
    PoiDao poiDao;
    RelationEditionDao relationEditionDao;
    PoiTypeDao poiTypeDao;
    EventBus bus;
    Backend backend;
    SyncWayManager syncWayManager;
    SyncRelationManager syncRelationManager;
    SyncNoteManager syncNoteManager;
    LoginManager loginManager;

    @Inject
    public SyncManager(Application application, PoiManager poiManager, LoginManager loginManager, NoteManager noteManager, PoiDao poiDao, RelationEditionDao relationEditionDao, PoiTypeDao poiTypeDao, EventBus bus, Backend backend, SyncWayManager syncWayManager, SyncNoteManager syncNoteManager, SyncRelationManager syncRelationManager, RelationManager relationManager) {
        this.application = application;
        this.poiManager = poiManager;
        this.noteManager = noteManager;
        this.loginManager = loginManager;
        this.poiDao = poiDao;
        this.relationEditionDao = relationEditionDao;
        this.poiTypeDao = poiTypeDao;
        this.bus = bus;
        this.backend = backend;
        this.syncWayManager = syncWayManager;
        this.syncNoteManager = syncNoteManager;
        this.syncRelationManager = syncRelationManager;
        this.relationManager = relationManager;
    }


    // ********************************
    // ************ Events ************
    // ********************************

    @Subscribe(threadMode = ThreadMode.ASYNC)
    public void onSyncDownloadPoisAndNotesEvent(SyncDownloadPoisAndNotesEvent event) {
        syncDownloadPoiBox(event.getBox());
        List<Note> notes = syncNoteManager.syncDownloadNotesInBox(event.getBox());
        if (notes != null && notes.size() > 0) {
            noteManager.mergeBackendNotes(notes);
        }
        bus.post(new PoisAndNotesDownloadedEvent());
    }

    @Subscribe(threadMode = ThreadMode.ASYNC)
    public void onSyncDownloadWayEvent(SyncDownloadWayEvent event) {
        syncWayManager.syncDownloadWay(event.getBox());
    }

    @Subscribe(threadMode = ThreadMode.ASYNC)
    public void onPleaseUploadPoiChangesByIdsEvent(PleaseUploadPoiChangesByIdsEvent event) {
        if (loginManager.isUserLogged()) {
            remoteAddOrUpdateOrDeletePoisInTransaction(event.getComment(), event.getPoiIds(), event.getPoiNodeRefIds());
        } else {
            bus.post(new SyncUnauthorizedEvent());
        }
    }

    /**
     * Initialize the database if the Flavor is PoiStorage.
     *
     * @param event The initialization event.
     */
    @Subscribe(threadMode = ThreadMode.ASYNC)
    public void onInitDbEvent(InitDbEvent event) {
        if (FlavorUtils.isPoiStorage()) {
            Timber.d("Initializing database ...");
            syncDownloadPoiTypes();
            bus.postSticky(new DbInitializedEvent());
        }
    }

    // ********************************
    // ************ public ************
    // ********************************

    public Observable<Boolean> sync() {
        return Observable.unsafeCreate(subscriber -> {
            subscriber.onNext(remoteAddOrUpdateOrDeletePoisInTransaction(
                    BuildConfig.AUTO_COMMIT_CHANGESET,
                    poiDao.queryForAllUpdatedPois(),
                    poiDao.queryForAllNew(),
                    poiDao.queryToDelete()));
            subscriber.onCompleted();
        });
    }

    /**
     * Download the list of PoiType from the backend and actualize the database.
     * <br/>
     * If there was new, modified or a deleted PoiType, send a
     * {@link PoiTypesLoaded} event containing the new list of PoiTypes.
     */
    public void syncDownloadPoiTypes() {
        List<PoiType> poiTypes = backend.getPoiTypes();
        PoiType dbPoiType;
        boolean modified = false;

        List<PoiType> oldTypes = poiTypeDao.queryForAll();

        // Create and update the PoiTypes in database with the result from backend
        for (PoiType type : poiTypes) {
            dbPoiType = type.getBackendId() != null ? poiTypeDao.findByBackendId(type.getBackendId()) : null;

            if (dbPoiType != null) {
                type.setId(dbPoiType.getId());
            }

            type = poiManager.savePoiType(type);

            if (!modified && !type.equals(dbPoiType)) {
                modified = true;
            }
        }

        //Delete from database the PoiTypes who aren't in the new list of the backend.
        for (PoiType type : oldTypes) {
            if (!poiTypes.contains(type)) {
                poiManager.deletePoiType(type);
                modified = true;
            }
        }

        if (modified) {
            bus.postSticky(new PoiTypesLoaded(poiManager.getPoiTypesSortedByName()));
        }
    }

    /**
     * Download from backend the list of Poi contained in the box.
     * Update the database with the obtained list.
     *
     * @param box The Box to synchronize with the database.
     */
    public void syncDownloadPoiBox(final Box box) {
        if (FlavorUtils.isPoiStorage()) {
            // Sync PoiTypes before the Poi to be sure we have all PoiTypes
            syncDownloadPoiTypes();
        }

        List<Poi> pois = null;
        try {
            pois = backend.getPoisInBox(box);
            if (pois.size() > 0) {
                Timber.d("Updating %d nodes", pois.size());
                poiManager.mergeFromOsmPois(pois, box);
            } else {
                Timber.d("No new node found in the area");
            }
        } catch (NetworkException e) {
            Timber.w("There was a network error, it was impossible to load pois of the box");
        }
    }

    /**
     * Send in a unique changeSet all the new POIs, modified and suppressed ones from ids send in params.
     * <p/>
     * Send a {@link io.jawg.osmcontributor.rest.events.SyncFinishUploadPoiEvent} with the counts.
     *
     * @param comment       The comment of the changeSet.
     * @param poisId        Pois to upload
     * @param poiNodeRefsId PoisNodeRef to upload
     * @return Whether everything was correctly sent to the remote or not.
     */
    private boolean remoteAddOrUpdateOrDeletePoisInTransaction(String comment, List<Long> poisId, List<Long> poiNodeRefsId) {
        return remoteAddOrUpdateOrDeletePoisInTransaction(comment, poiDao.queryForAllUpdatedPois(), poiDao.queryForAllNew(), poiDao.queryToDelete());
    }

    private List<RelationEdition> getRelationEditions(List<Poi> pois) {
        return relationEditionDao.queryByPoisDescendingOrder(pois);
    }

    private List<FullOSMRelation> downloadRelationsToUpdate(List<RelationEdition> relationEditions) {
        List<FullOSMRelation> fullOSMRelationToUpdate = syncRelationManager.downloadRelationsForEdition(getBackendIdsOfRelations(relationEditions));
        return syncRelationManager.applyChangesToRelations(fullOSMRelationToUpdate, relationEditions);
    }

    private List<Long> getBackendIdsOfRelations(List<RelationEdition> list) {
        List<Long> result = new ArrayList<>();
        for (RelationEdition re : list) {
            if (re.getBackendId() != null) {
                result.add(Long.valueOf(re.getBackendId()));
            }
        }
        return result;
    }

    private boolean remoteAddOrUpdateOrDeletePoisInTransaction(String comment, List<Poi> updatedPois, List<Poi> newPois, List<Poi> toDeletePois) {
        boolean success = true;
        int successfullyAddedPoisCount = 0;
        int successfullyUpdatedPoisCount = 0;
        int successfullyDeletedPoisCount = 0;

        String changeSetId = null;

        if (updatedPois.isEmpty() && newPois.isEmpty() && toDeletePois.isEmpty()) {
            Timber.i("No new or to update or to delete POIs to send to osm");
        } else {
            Timber.i("Found %d new, %d updated and %d to delete POIs to send to osm", newPois.size(), updatedPois.size(), toDeletePois.size());

            changeSetId = backend.initializeTransaction(comment);

            if (changeSetId != null) {
                successfullyAddedPoisCount = remoteAddPois(newPois, changeSetId);
                successfullyUpdatedPoisCount = remoteUpdatePois(updatedPois, changeSetId);
                successfullyDeletedPoisCount = remoteDeletePois(toDeletePois, changeSetId);
            }
            success = changeSetId != null
                    && successfullyAddedPoisCount == newPois.size()
                    && successfullyUpdatedPoisCount == updatedPois.size()
                    && successfullyDeletedPoisCount == toDeletePois.size();
        }
        bus.post(new SyncFinishUploadPoiEvent(successfullyAddedPoisCount, successfullyUpdatedPoisCount, successfullyDeletedPoisCount));

        return success && remoteUpdateRelationsInTransaction(changeSetId, comment);
    }

    private boolean remoteUpdateRelationsInTransaction(String changeSetId, String comment) {
        //this request to the database has to be done after the previous operations on the pois are done
        List<RelationEdition> relationEditions = getRelationEditions(poiDao.queryForAllUpdatedPoisRelations());
        List<RelationEdition> filteredRelationEditions = filterForDuplicateOperations(relationEditions);
        List<FullOSMRelation> updatedFullOSMRelations = downloadRelationsToUpdate(filteredRelationEditions);
        List<Pair<RelationEdition, FullOSMRelation>> editions = getRelationEditionList(filteredRelationEditions, updatedFullOSMRelations);
        boolean success = true;
        int successfullyUpdatedRelationsCount = 0;

        if (filteredRelationEditions.isEmpty()) {
            Timber.i("No updatable relations to send to osm");
        } else {
            Timber.i("Found  %d update to apply to relations to send to osm", filteredRelationEditions.size());

            if (changeSetId == null) {
                changeSetId = backend.initializeTransaction(comment);
            }

            if (changeSetId != null) {
                successfullyUpdatedRelationsCount = remoteUpdateRelations(editions, changeSetId);
            }
            success = changeSetId != null && successfullyUpdatedRelationsCount == updatedFullOSMRelations.size();
        }
        bus.post(new SyncFinishUploadRelationEvent(successfullyUpdatedRelationsCount));

        return success;
    }

    private List<RelationEdition> filterForDuplicateOperations(List<RelationEdition> relationEditions) {
        Set<RelationEdition> uniqueEditions = new LinkedHashSet<>();
        for (RelationEdition relationEdition : relationEditions) {
            uniqueEditions.add(relationEdition);
        }
        return new ArrayList<>(uniqueEditions);
    }

    private List<Pair<RelationEdition, FullOSMRelation>> getRelationEditionList(List<RelationEdition> filteredRelationEditions, List<FullOSMRelation> updatedFUllOSMRelations) {
        List<Pair<RelationEdition, FullOSMRelation>> editions = new ArrayList<>();
        if (filteredRelationEditions.size() == updatedFUllOSMRelations.size()) {
            for (int i = 0; i != filteredRelationEditions.size(); i++) {
                editions.add(new Pair<>(filteredRelationEditions.get(i), updatedFUllOSMRelations.get(i)));
            }
        }
        return editions;
    }

    // *********************************
    // ************ private ************
    // *********************************

    /**
     * Add a List of POIs to the backend.
     *
     * @param pois        The List of POIs to add to the backend.
     * @param changeSetId The changeSet in which the POIs are sent.
     * @return The number of POIs who where successfully added.
     */
    private int remoteAddPois(List<Poi> pois, String changeSetId) {
        int count = 0;

        for (Poi poi : pois) {
            if (remoteAddPoi(poi, changeSetId)) {
                count++;
            }
        }
        return count;
    }

    /**
     * Add a Poi to the backend.
     *
     * @param poi         The Poi to add to the backend.
     * @param changeSetId The changeSet in which the Poi is sent.
     * @return Whether the addition was a success or not.
     */
    private boolean remoteAddPoi(final Poi poi, String changeSetId) {
        Backend.CreationResult creationResult = backend.addPoi(poi, changeSetId);
        switch (creationResult.getStatus()) {
            case SUCCESS:
                poi.setBackendId(creationResult.getBackendId());
                poi.setDetailsUpdated(false);
                poiManager.savePoi(poi);
                OsmAnswers.remotePoiAction(poi.getType().getTechnicalName(), "add");
                return true;
            case FAILURE_UNKNOWN:
            default:
                poiManager.deletePoi(poi);
                bus.post(new SyncNewNodeErrorEvent(poi.getName(), poi.getId()));
                return false;
        }
    }

    /**
     * Update a List of POIs to the backend.
     *
     * @param pois        The List of POIs to update to the backend.
     * @param changeSetId The changeSet in which the POIs are sent.
     * @return The number of POIs who where successfully updated.
     */
    private int remoteUpdatePois(List<Poi> pois, String changeSetId) {
        int count = 0;
        for (Poi poi : pois) {
            if (poi.getDetailsUpdated()) {
                if (remoteUpdatePoi(poi, changeSetId)) {
                    count++;
                }
            }

        }
        return count;
    }

    /**
     * Update a List of Relations to the backend.
     *
     * @param editions The List of Relations to update to the backend.
     * @param changeSetId      The changeSet in which the Relations are sent.
     * @return The number of fullOSMRelations who where successfully updated.
     */
    private int remoteUpdateRelations(List<Pair<RelationEdition, FullOSMRelation>> editions, String changeSetId) {
        int count = 0;
        for (Pair<RelationEdition, FullOSMRelation> edition : editions) {
            if (remoteUpdateRelation(edition, changeSetId)) {
                count++;
            }
        }
        return count;
    }

    /**
     * Update a Poi of the backend.
     *
     * @param poi         The Poi to update.
     * @param changeSetId The changeSet in which the Poi is sent.
     * @return Whether the update was a success or not.
     */
    private boolean remoteUpdatePoi(final Poi poi, String changeSetId) {
        Backend.UpdateResult updateResult = backend.updatePoi(poi, changeSetId);
        poiManager.deleteOldPoiAssociated(poi);

        switch (updateResult.getStatus()) {
            case SUCCESS:
                poi.setVersion(updateResult.getVersion());
                poi.setDetailsUpdated(false);
                poiManager.savePoi(poi);
                OsmAnswers.remotePoiAction(poi.getType().getTechnicalName(), "update");
                return true;
            case FAILURE_CONFLICT:
                bus.post(new SyncConflictingNodeErrorEvent(poi.getName(), poi.getId()));
                Timber.e("Couldn't update poi %s: conflict, redownloading last version of poi", poi);
                deleteAndRetrieveUnmodifiedPoi(poi);
                return false;
            case FAILURE_NOT_EXISTING:
                Timber.e("Couldn't update poi %s, it didn't exist. Deleting the incriminated poi", poi);
                poiManager.deletePoi(poi);
                bus.post(new SyncConflictingNodeErrorEvent(poi.getName(), poi.getId()));
                return false;
            case FAILURE_UNKNOWN:
            default:
                Timber.e("Couldn't update poi %s. Deleting the incriminated poi", poi);
                poiManager.deletePoi(poi);
                bus.post(new SyncUploadRetrofitErrorEvent(poi.getId()));
                return false;
        }
    }

    /**
     * Update a FullOSMRelation of the backend.
     *
     * @param edition     The FullOSMRelation to update.
     * @param changeSetId The changeSet in which the Relation is sent.
     * @return Whether the update was a success or not.
     */
    private boolean remoteUpdateRelation(final Pair<RelationEdition, FullOSMRelation> edition, String changeSetId) {
        Backend.UpdateResult updateResult = backend.updateRelation(edition.second, changeSetId);

        switch (updateResult.getStatus()) {
            case SUCCESS:
                edition.first.getPoi().setRelationsUpdated(false);
                poiManager.savePoi(edition.first.getPoi());
                relationManager.deleteFinishedEditions(edition.first);
                OsmAnswers.remoteRelationAction();
                return true;
            case FAILURE_CONFLICT:
                revertChangesInPoi(edition);
                relationManager.deleteFinishedEditions(edition.first);
                bus.post(new SyncConflictingRelationErrorEvent());
                Timber.e("Couldn't update fullOSMRelation %s: conflict, cancelling change", edition.second.getBackendId());
                return false;
            case FAILURE_NOT_EXISTING:
                revertChangesInPoi(edition);
                relationManager.deleteFinishedEditions(edition.first);
                Timber.e("Couldn't update fullOSMRelation %s, it didn't exist. Cancelling change", edition.second.getBackendId());
                bus.post(new SyncConflictingRelationErrorEvent());
                return false;
            case FAILURE_UNKNOWN:
            default:
                revertChangesInPoi(edition);
                relationManager.deleteFinishedEditions(edition.first);
                Timber.e("Couldn't update relation %s. Cancelling change", edition.second.getBackendId());
                bus.post(new SyncConflictingRelationErrorEvent());
                return false;
        }
    }

    private void revertChangesInPoi(Pair<RelationEdition, FullOSMRelation> edition) {
        Collection<RelationId> relationIds = poiManager.queryForId(edition.first.getPoi().getOldPoiId()).getRelationIds();
        edition.first.getPoi().setRelationIds(relationIds);
        poiManager.savePoi(edition.first.getPoi());
    }


    /**
     * Delete a List of POIs to the backend.
     *
     * @param pois        The List of POIs to delete to the backend.
     * @param changeSetId The changeSet in which the POIs are sent.
     * @return The number of POIs who where successfully deleted.
     */
    private int remoteDeletePois(List<Poi> pois, String changeSetId) {
        int count = 0;
        for (Poi poi : pois) {
            if (remoteDeletePoi(poi, changeSetId)) {
                count++;
            }
        }
        return count;
    }

    /**
     * Delete a Poi of the backend.
     *
     * @param poi         The Poi to delete.
     * @param changeSetId The changeSet in which the Poi is sent.
     * @return Whether the delete was a success or not.
     */
    private boolean remoteDeletePoi(final Poi poi, String changeSetId) {
        Backend.ModificationStatus modificationStatus = backend.deletePoi(poi, changeSetId);
        poiManager.deleteOldPoiAssociated(poi);

        switch (modificationStatus) {
            case SUCCESS:
            case FAILURE_NOT_EXISTING:
                OsmAnswers.remotePoiAction(poi.getType().getTechnicalName(), "delete");
                poiManager.deletePoi(poi);
                return true;
            case FAILURE_CONFLICT:
                bus.post(new SyncConflictingNodeErrorEvent(poi.getName(), poi.getId()));
                Timber.e("Couldn't update poi %s: conflict, redownloading last version of poi", poi);
                deleteAndRetrieveUnmodifiedPoi(poi);
                return false;
            case FAILURE_UNKNOWN:
            default:
                Timber.e("Couldn't delete poi %s", poi);
                bus.post(new SyncUploadRetrofitErrorEvent(poi.getId()));
                return false;
        }
    }

    /**
     * Delete the Poi from the database and re-download it from the backend.
     *
     * @param poi The Poi to delete and re-download.
     */
    // TODO manage way pois
    private void deleteAndRetrieveUnmodifiedPoi(final Poi poi) {
        poiManager.deletePoi(poi);
        if (poi.getBackendId() != null) {

            Poi backendPoi = backend.getPoiById(poi.getBackendId());
            if (backendPoi != null) {
                poiManager.savePoi(backendPoi);
            } else {
                Timber.w("The poi with id %s couldn't be found ", poi.getBackendId());
            }
        }
    }
}
