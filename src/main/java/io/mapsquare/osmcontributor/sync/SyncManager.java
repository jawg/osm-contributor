/**
 * Copyright (C) 2015 eBusiness Information
 *
 * This file is part of OSM Contributor.
 *
 * OSM Contributor is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * OSM Contributor is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OSM Contributor.  If not, see <http://www.gnu.org/licenses/>.
 */
package io.mapsquare.osmcontributor.sync;

import android.app.Application;

import java.util.List;

import javax.inject.Inject;

import de.greenrobot.event.EventBus;
import io.mapsquare.osmcontributor.R;
import io.mapsquare.osmcontributor.core.PoiManager;
import io.mapsquare.osmcontributor.core.database.dao.PoiDao;
import io.mapsquare.osmcontributor.core.database.dao.PoiTypeDao;
import io.mapsquare.osmcontributor.core.events.PoiTypesLoaded;
import io.mapsquare.osmcontributor.core.events.PoisAndNotesDownloadedEvent;
import io.mapsquare.osmcontributor.core.model.Poi;
import io.mapsquare.osmcontributor.core.model.PoiType;
import io.mapsquare.osmcontributor.sync.assets.events.DbInitializedEvent;
import io.mapsquare.osmcontributor.sync.assets.events.InitDbEvent;
import io.mapsquare.osmcontributor.sync.events.SyncDownloadPoisAndNotesEvent;
import io.mapsquare.osmcontributor.sync.events.SyncDownloadWayEvent;
import io.mapsquare.osmcontributor.sync.events.SyncFinishUploadPoiEvent;
import io.mapsquare.osmcontributor.sync.events.error.SyncConflictingNodeErrorEvent;
import io.mapsquare.osmcontributor.sync.events.error.SyncNewNodeErrorEvent;
import io.mapsquare.osmcontributor.sync.events.error.SyncUploadRetrofitErrorEvent;
import io.mapsquare.osmcontributor.utils.Box;
import io.mapsquare.osmcontributor.utils.FlavorUtils;
import timber.log.Timber;

/**
 * Manage the synchronisation of POIs and Notes between the backend and the application.
 */
public class SyncManager {

    Application application;
    PoiManager poiManager;
    PoiDao poiDao;
    PoiTypeDao poiTypeDao;
    EventBus bus;
    Backend backend;
    SyncWayManager syncWayManager;
    SyncNoteManager syncNoteManager;

    @Inject
    public SyncManager(Application application, PoiManager poiManager, PoiDao poiDao, PoiTypeDao poiTypeDao, EventBus bus, Backend backend, SyncWayManager syncWayManager, SyncNoteManager syncNoteManager) {
        this.application = application;
        this.poiManager = poiManager;
        this.poiDao = poiDao;
        this.poiTypeDao = poiTypeDao;
        this.bus = bus;
        this.backend = backend;
        this.syncWayManager = syncWayManager;
        this.syncNoteManager = syncNoteManager;
    }


    // ********************************
    // ************ Events ************
    // ********************************

    public void onEventAsync(SyncDownloadPoisAndNotesEvent event) {
        syncDownloadPoiBox(event.getBox());
        syncNoteManager.syncDownloadNotesInBox(event.getBox());
        bus.post(new PoisAndNotesDownloadedEvent());
    }

    public void onEventAsync(SyncDownloadWayEvent event) {
        syncWayManager.syncDownloadWay(event.getBox());
    }


    /**
     * Initialize the database if the Flavor is PoiStorage.
     *
     * @param event The initialization event.
     */
    public void onEventBackgroundThread(InitDbEvent event) {
        if (FlavorUtils.isPoiStorage()) {
            Timber.d("Initializing database ...");
            syncDownloadPoiTypes();
            bus.postSticky(new DbInitializedEvent());
        }
    }

    // ********************************
    // ************ public ************
    // ********************************

    /**
     * Download the list of PoiType from the backend and actualize the database.
     * <br/>
     * If there was new, modified or a deleted PoiType, send a
     * {@link io.mapsquare.osmcontributor.core.events.PoiTypesLoaded} event containing the new list of PoiTypes.
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
            bus.postSticky(new PoiTypesLoaded(poiManager.loadPoiTypes()));
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

        List<Poi> pois = backend.getPoisInBox(box);
        if (pois.size() > 0) {
            Timber.d("Updating %d nodes", pois.size());
            poiManager.mergeFromOsmPois(pois);
        } else {
            Timber.d("No new node found in the area");
        }
    }

    /**
     * Call {@link io.mapsquare.osmcontributor.sync.SyncManager#remoteAddOrUpdateOrDeletePois(String)} with default comment.
     */
    public void remoteAddOrUpdateOrDeletePois() {
        remoteAddOrUpdateOrDeletePois(application.getResources().getString(R.string.comment_changeset));
    }

    /**
     * Send in a unique changeSet all the new POIs, modified and suppressed ones contained in the database.
     * <p/>
     * Send a {@link io.mapsquare.osmcontributor.sync.events.SyncFinishUploadPoiEvent} with the counts.
     *
     * @param comment The comment of the changeSet.
     */
    public void remoteAddOrUpdateOrDeletePois(String comment) {
        syncWayManager.downloadPoiForWayEdition();

        final List<Poi> updatedPois = poiDao.queryForAllUpdated();
        final List<Poi> newPois = poiDao.queryForAllNew();
        final List<Poi> toDeletePois = poiDao.queryToDelete();

        int successfullyAddedPoisCount = 0;
        int successfullyUpdatedPoisCount = 0;
        int successfullyDeletedPoisCount = 0;


        if (updatedPois.size() == 0 && newPois.size() == 0 && toDeletePois.size() == 0) {
            Timber.i("No new or updatable or to delete POIs to send to osm");
        } else {
            Timber.i("Found %d new, %d updated and %d to delete POIs to send to osm", newPois.size(), updatedPois.size(), toDeletePois.size());

            final String changeSetId = backend.initializeTransaction(comment);

            if (changeSetId != null) {
                successfullyAddedPoisCount = remoteAddPois(newPois, changeSetId);
                successfullyUpdatedPoisCount = remoteUpdatePois(updatedPois, changeSetId);
                successfullyDeletedPoisCount = remoteDeletePois(toDeletePois, changeSetId);
            }
        }
        bus.post(new SyncFinishUploadPoiEvent(successfullyAddedPoisCount, successfullyUpdatedPoisCount, successfullyDeletedPoisCount));
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
                poi.setUpdated(false);
                poiManager.savePoi(poi);
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
            if (remoteUpdatePoi(poi, changeSetId)) {
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
        switch (updateResult.getStatus()) {
            case SUCCESS:
                poi.setVersion(updateResult.getVersion());
                poi.setUpdated(false);
                poiManager.savePoi(poi);
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
        switch (modificationStatus) {
            case SUCCESS:
            case FAILURE_NOT_EXISTING:
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
