/**
 * Copyright (C) 2016 eBusiness Information
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
package io.mapsquare.osmcontributor.ui.managers;

import android.app.Application;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

import javax.inject.Inject;

import io.mapsquare.osmcontributor.database.PoiAssetLoader;
import io.mapsquare.osmcontributor.database.dao.PoiDao;
import io.mapsquare.osmcontributor.database.dao.PoiNodeRefDao;
import io.mapsquare.osmcontributor.database.dao.PoiTagDao;
import io.mapsquare.osmcontributor.database.dao.PoiTypeDao;
import io.mapsquare.osmcontributor.database.dao.PoiTypeTagDao;
import io.mapsquare.osmcontributor.database.events.DbInitializedEvent;
import io.mapsquare.osmcontributor.database.events.InitDbEvent;
import io.mapsquare.osmcontributor.database.helper.DatabaseHelper;
import io.mapsquare.osmcontributor.model.entities.Poi;
import io.mapsquare.osmcontributor.model.entities.PoiNodeRef;
import io.mapsquare.osmcontributor.model.entities.PoiTag;
import io.mapsquare.osmcontributor.model.entities.PoiType;
import io.mapsquare.osmcontributor.model.entities.PoiTypeTag;
import io.mapsquare.osmcontributor.model.events.DatabaseResetFinishedEvent;
import io.mapsquare.osmcontributor.model.events.PleaseLoadPoiForArpiEvent;
import io.mapsquare.osmcontributor.model.events.PleaseLoadPoiForCreationEvent;
import io.mapsquare.osmcontributor.model.events.PleaseLoadPoiForEditionEvent;
import io.mapsquare.osmcontributor.model.events.PleaseLoadPoiTypes;
import io.mapsquare.osmcontributor.model.events.PleaseLoadPoisEvent;
import io.mapsquare.osmcontributor.model.events.PleaseLoadPoisToUpdateEvent;
import io.mapsquare.osmcontributor.model.events.PleaseRevertPoiEvent;
import io.mapsquare.osmcontributor.model.events.PleaseRevertPoiNodeRefEvent;
import io.mapsquare.osmcontributor.model.events.PoiForEditionLoadedEvent;
import io.mapsquare.osmcontributor.model.events.PoiTypesLoaded;
import io.mapsquare.osmcontributor.model.events.PoisArpiLoadedEvent;
import io.mapsquare.osmcontributor.model.events.PoisLoadedEvent;
import io.mapsquare.osmcontributor.model.events.PoisToUpdateLoadedEvent;
import io.mapsquare.osmcontributor.model.events.ResetDatabaseEvent;
import io.mapsquare.osmcontributor.model.events.ResetTypeDatabaseEvent;
import io.mapsquare.osmcontributor.model.events.RevertFinishedEvent;
import io.mapsquare.osmcontributor.rest.dtos.dma.H2GeoDto;
import io.mapsquare.osmcontributor.ui.events.map.ChangesInDB;
import io.mapsquare.osmcontributor.ui.events.map.LastUsePoiTypeLoaded;
import io.mapsquare.osmcontributor.ui.events.map.PleaseLoadLastUsedPoiType;
import io.mapsquare.osmcontributor.ui.events.map.PleaseTellIfDbChanges;
import io.mapsquare.osmcontributor.ui.utils.BitmapHandler;
import io.mapsquare.osmcontributor.ui.utils.views.map.marker.LocationMarkerView;
import io.mapsquare.osmcontributor.utils.Box;
import io.mapsquare.osmcontributor.utils.ConfigManager;
import io.mapsquare.osmcontributor.utils.FlavorUtils;
import io.mapsquare.osmcontributor.utils.StringUtils;
import io.mapsquare.osmcontributor.utils.upload.PoiUpdateWrapper;
import timber.log.Timber;

import static io.mapsquare.osmcontributor.database.helper.DatabaseHelper.loadLazyForeignCollection;

/**
 * Manager class for POIs.
 * Provides a number of methods to manipulate the POIs in the database that should be used instead
 * of calling the {@link io.mapsquare.osmcontributor.database.dao.PoiDao}.
 */
public class PoiManager {

    Application application;
    BitmapHandler bitmapHandler;
    PoiDao poiDao;
    PoiTagDao poiTagDao;
    PoiNodeRefDao poiNodeRefDao;
    PoiTypeDao poiTypeDao;
    PoiTypeTagDao poiTypeTagDao;
    DatabaseHelper databaseHelper;
    ConfigManager configManager;
    EventBus bus;
    PoiAssetLoader poiAssetLoader;

    @Inject
    public PoiManager(Application application, BitmapHandler bitmapHandler, PoiDao poiDao, PoiTagDao poiTagDao, PoiNodeRefDao poiNodeRefDao, PoiTypeDao poiTypeDao, PoiTypeTagDao poiTypeTagDao, DatabaseHelper databaseHelper, ConfigManager configManager, EventBus bus, PoiAssetLoader poiAssetLoader) {
        this.application = application;
        this.bitmapHandler = bitmapHandler;
        this.poiDao = poiDao;
        this.poiTagDao = poiTagDao;
        this.poiNodeRefDao = poiNodeRefDao;
        this.poiTypeDao = poiTypeDao;
        this.poiTypeTagDao = poiTypeTagDao;
        this.databaseHelper = databaseHelper;
        this.configManager = configManager;
        this.bus = bus;
        this.poiAssetLoader = poiAssetLoader;
    }

    // ********************************
    // ************ Events ************
    // ********************************

    @Subscribe(threadMode = ThreadMode.ASYNC)
    public void onInitDbEvent(InitDbEvent event) {
        if (!FlavorUtils.isPoiStorage()) {
            Timber.d("Initializing database ...");
            initDb();
            bus.postSticky(new DbInitializedEvent());
        }
    }

    @Subscribe(threadMode = ThreadMode.ASYNC)
    public void onPleaseLoadPoiForEditionEvent(PleaseLoadPoiForEditionEvent event) {
        loadPoiForEdition(event.getPoiId());
    }

    @Subscribe(threadMode = ThreadMode.ASYNC)
    public void onPleaseLoadPoiForCreationEvent(PleaseLoadPoiForCreationEvent event) {
        loadPoiForCreation(event);
    }

    @Subscribe(threadMode = ThreadMode.ASYNC)
    public void onPleaseLoadPoisEvent(PleaseLoadPoisEvent event) {
        loadPois(event);
    }

    @Subscribe(threadMode = ThreadMode.ASYNC)
    public void onPleaseTellIfDbChanges(PleaseTellIfDbChanges event) {
        bus.post(new ChangesInDB(poiDao.countForAllChanges() > 0 || poiNodeRefDao.countAllToUpdate() > 0));
    }

    @Subscribe(threadMode = ThreadMode.ASYNC)
    public void onPleaseLoadPoisToUpdateEvent(PleaseLoadPoisToUpdateEvent event) {
        List<Poi> updatedPois = poiDao.queryForAllUpdated();
        List<Poi> newPois = poiDao.queryForAllNew();
        List<Poi> toDeletePois = poiDao.queryToDelete();
        List<PoiNodeRef> wayPoiNodeRef = poiNodeRefDao.queryAllToUpdate();
        List<PoiUpdateWrapper> allPois = new ArrayList<>();

        for (Poi p : updatedPois) {
            allPois.add(new PoiUpdateWrapper(true, p, poiDao.queryForId(p.getOldPoiId()), null, PoiUpdateWrapper.PoiAction.UPDATE));
        }
        for (Poi p : newPois) {
            allPois.add(new PoiUpdateWrapper(true, p, null, null, PoiUpdateWrapper.PoiAction.CREATE));
        }
        for (Poi p : toDeletePois) {
            allPois.add(new PoiUpdateWrapper(true, p, poiDao.queryForId(p.getOldPoiId()), null, PoiUpdateWrapper.PoiAction.DELETED));
        }
        for (PoiNodeRef p : wayPoiNodeRef) {
            allPois.add(new PoiUpdateWrapper(false, null, null, p, PoiUpdateWrapper.PoiAction.UPDATE));
        }

        bus.post(new PoisToUpdateLoadedEvent(allPois));
    }

    @Subscribe(threadMode = ThreadMode.ASYNC)
    public void onPleaseLoadPoiTypes(PleaseLoadPoiTypes event) {
        PoiTypesLoaded poiTypesLoaded = new PoiTypesLoaded(getPoiTypesSortedByName());
        poiTypesLoaded.setPreset(event.isPreset());
        bus.postSticky(poiTypesLoaded);
    }

    @Subscribe(threadMode = ThreadMode.ASYNC)
    public void onPleaseLoadLastUsedPoiType(PleaseLoadLastUsedPoiType event) {
        bus.post(new LastUsePoiTypeLoaded(getPoiTypesSortedByLastUse()));
    }

    @Subscribe(threadMode = ThreadMode.ASYNC)
    public void onResetDatabaseEvent(ResetDatabaseEvent event) {
        bus.post(new DatabaseResetFinishedEvent(resetDatabase()));
    }

    @Subscribe(threadMode = ThreadMode.ASYNC)
    public void onResetTypeDatabaseEvent(ResetTypeDatabaseEvent event) {
        if (event.isByDefault()) {
            bus.post(new DatabaseResetFinishedEvent(resetTypesByDefault()));
        } else if (event.getH2GeoDto() != null) {
            bus.post(new DatabaseResetFinishedEvent(resetTypes(event.getH2GeoDto())));
        }
    }

    @Subscribe(threadMode = ThreadMode.ASYNC)
    public void onPleaseRevertPoiEvent(PleaseRevertPoiEvent event) {
        Poi poi = revertPoi(event.getIdToRevert());
        bus.post(new RevertFinishedEvent(poi, LocationMarkerView.MarkerType.POI));
    }

    @Subscribe(threadMode = ThreadMode.ASYNC)
    public void onPleaseRevertPoiNodeRefEvent(PleaseRevertPoiNodeRefEvent event) {
        PoiNodeRef poiNodeRef = revertPoiNodeRef(event.getIdToRevert());
        bus.post(new RevertFinishedEvent(poiNodeRef, LocationMarkerView.MarkerType.NODE_REF));
    }

    @Subscribe(threadMode = ThreadMode.ASYNC)
    public void onPleaseLoadPoiForArpiEvent(PleaseLoadPoiForArpiEvent event) {
        List<Poi> pois = poiDao.queryForAllInRect(event.getBox());
        bus.post(new PoisArpiLoadedEvent(pois));
    }


    // ********************************
    // ************ public ************
    // ********************************

    /**
     * Initialize the database with the data from the assets files.
     */
    public void initDb() {
        if (!isDbInitialized()) {
            // No data, initializing from assets
            savePoiTypesByDefault();
            savePoisFromAssets();
        }
        Timber.d("Database initialized");
    }

    /**
     * Load poi types from assets and save them in the database.
     */
    public void savePoiTypesByDefault() {
        savePoiTypes(poiAssetLoader.loadPoiTypesByDefault());
    }

    /**
     * Load poi types from h2GeoDto and save them in the database.
     * @param h2GeoDto
     */
    public void savePoiTypesFromH2Geo(H2GeoDto h2GeoDto) {
        savePoiTypes(poiAssetLoader.loadPoiTypesFromH2GeoDto(h2GeoDto));
    }

    public void savePoiTypes(List<PoiType> poiTypes) {
        if (poiTypes != null) {
            Timber.d("Loaded %s poiTypes, trying to insert them", poiTypes.size());
            for (PoiType poiType : poiTypes) {
                Timber.d("saving poiType %s", poiType);
                savePoiType(poiType);
                Timber.d("poiType saved");
            }
        }
    }

    /**
     * Load pois from assets and save them in the database.
     */
    public void savePoisFromAssets() {
        List<Poi> pois = poiAssetLoader.loadPoisFromAssets();
        Timber.d("Loaded %s poi, trying to insert them", pois.size());
        for (Poi poi : pois) {
            Timber.d("saving poi %s", poi);
            savePoi(poi);
            Timber.d("poi saved");
        }
    }

    /**
     * Method saving a poi and all the associated foreign collections.
     * <p/>
     * Do not call the DAO directly to save a poi, use this method.
     *
     * @param poi The poi to save.
     * @return The saved poi.
     */
    public Poi savePoi(final Poi poi) {
        return databaseHelper.callInTransaction(new Callable<Poi>() {
            @Override
            public Poi call() throws Exception {
                return savePoiNoTransaction(poi);
            }
        });
    }

    /**
     * Method saving a List of POIs and all the associated foreign collections. The saving is done in a transaction.
     * <p/>
     * Do not call the DAO directly to save a List of POIs, use this method.
     *
     * @param pois The List of POIs to save.
     * @return The saved List.
     */
    public List<Poi> savePois(final List<Poi> pois) {
        return databaseHelper.callInTransaction(new Callable<List<Poi>>() {
            @Override
            public List<Poi> call() throws Exception {
                List<Poi> result = new ArrayList<>(pois.size());
                for (Poi poi : pois) {
                    result.add(savePoiNoTransaction(poi));
                }
                return result;
            }
        });
    }

    /**
     * Method saving a List of PoiNodeRefs.
     *
     * @param poiNodeRefs The List of PoiNodeReds to save.
     * @return The saved List.
     */
    public List<PoiNodeRef> savePoiNodeRefs(final List<PoiNodeRef> poiNodeRefs) {
        return databaseHelper.callInTransaction(new Callable<List<PoiNodeRef>>() {
            @Override
            public List<PoiNodeRef> call() throws Exception {
                List<PoiNodeRef> result = new ArrayList<>(poiNodeRefs.size());
                for (PoiNodeRef poiNodeRef : poiNodeRefs) {
                    poiNodeRefDao.createOrUpdate(poiNodeRef);
                    result.add(poiNodeRef);
                }
                return result;
            }
        });
    }

    /**
     * Method saving a poi and all the associated foreign collections without transaction management.
     * <p/>
     * Do not call the DAO directly to save a poi, use this method.
     *
     * @param poi The poi to save
     * @return The saved poi
     * @see #savePoi(Poi)
     */
    private Poi savePoiNoTransaction(Poi poi) {
        List<PoiTag> poiTagsToRemove = poiTagDao.queryByPoiId(poi.getId());
        poiTagsToRemove.removeAll(poi.getTags());
        for (PoiTag poiTag : poiTagsToRemove) {
            poiTagDao.delete(poiTag);
        }

        List<PoiNodeRef> poiNodeRefsToRemove = poiNodeRefDao.queryByPoiId(poi.getId());
        poiNodeRefsToRemove.removeAll(poi.getNodeRefs());
        for (PoiNodeRef poiNodeRef : poiNodeRefsToRemove) {
            poiNodeRefDao.delete(poiNodeRef);
        }

        poiDao.createOrUpdate(poi);

        if (poi.getTags() != null) {
            for (PoiTag poiTag : poi.getTags()) {
                poiTag.setPoi(poi);
                poiTagDao.createOrUpdate(poiTag);
            }
        }

        if (poi.getNodeRefs() != null) {
            for (PoiNodeRef poiNodeRef : poi.getNodeRefs()) {
                poiNodeRef.setPoi(poi);
                poiNodeRefDao.createOrUpdate(poiNodeRef);

            }
        }

        return poi;
    }

    /**
     * Method saving a PoiType and all the associated foreign collections.
     * <p/>
     * Do not call the DAO directly to save a PoiType, use this method.
     *
     * @param poiType The PoiType to save.
     * @return The saved PoiType.
     */
    public PoiType savePoiType(final PoiType poiType) {
        return databaseHelper.callInTransaction(new Callable<PoiType>() {
            @Override
            public PoiType call() throws Exception {
                poiTypeDao.createOrUpdate(poiType);
                List<PoiTypeTag> poiTypeTagsToDelete = poiTypeTagDao.queryByPoiTypeId(poiType.getId());
                poiTypeTagsToDelete.removeAll(poiType.getTags());
                poiTypeTagDao.delete(poiTypeTagsToDelete);
                // Save the PoiTypeTags
                for (PoiTypeTag poiTypeTag : poiType.getTags()) {
                    poiTypeTag.setPoiType(poiType);
                    poiTypeTagDao.createOrUpdate(poiTypeTag);
                }
                return poiType;
            }
        });
    }

    /**
     * Query for a Poi with a given id eagerly.
     *
     * @param id The id of the Poi to load.
     * @return The queried Poi.
     */
    public Poi queryForId(Long id) {
        Poi poi = poiDao.queryForId(id);
        if (poi == null) {
            return null;
        }
        poiTypeDao.refresh(poi.getType());
        poi.setTags(loadLazyForeignCollection(poi.getTags()));
        poi.getType().setTags(loadLazyForeignCollection(poi.getType().getTags()));
        return poi;
    }

    /**
     * Query for Poi with a given backendId.
     *
     * @param backendId The backendId of the Pois to load.
     * @return The queried Poi.
     */
    public List<Poi> queryForBackendId(String backendId) {
        return poiDao.queryForBackendId(backendId);
    }

    /**
     * Count for POIs with the same backend Id.
     *
     * @param backendId The backend id.
     * @return The count of pois.
     */
    public Long countForBackendId(String backendId) {
        return poiDao.countForBackendId(backendId);
    }

    /**
     * Delete a Poi and all PoiTags and PoiNodeRefs associated.
     * <p/>
     * Do not call the DAO directly to delete a Poi, use this method.
     *
     * @param poi The Poi to delete.
     */
    public void deletePoi(final Poi poi) {
        databaseHelper.callInTransaction(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                Poi poiToDelete = poiDao.queryForId(poi.getId());
                if (poiToDelete == null) {
                    return null;
                }
                List<PoiNodeRef> poiNodeRefsToDelete = poiNodeRefDao.queryByPoiId(poiToDelete.getId());
                List<PoiTag> poiTagsToDelete = poiTagDao.queryByPoiId(poiToDelete.getId());

                Timber.d("NodeRefs to delete : %d", poiNodeRefsToDelete.size());
                Timber.d("NodeTags to delete : %d", poiTagsToDelete.size());

                poiTagDao.delete(poiTagsToDelete);
                poiNodeRefDao.delete(poiNodeRefsToDelete);
                poiDao.delete(poiToDelete);

                Timber.i("Deleted Poi %d", poiToDelete.getId());
                return null;
            }
        });
    }

    /**
     * Delete a PoiType and all the PoiTypeTags and POIs associated.
     * <p/>
     * Do not call the DAO directly to delete a PoiType, use this method.
     *
     * @param poiType The PoiType to delete.
     */
    public void deletePoiType(PoiType poiType) {
        final Long id = poiType.getId();
        databaseHelper.callInTransaction(new Callable<PoiType>() {
            @Override
            public PoiType call() throws Exception {
                List<Long> poiIdsToDelete = poiDao.queryAllIdsByPoiTypeId(id);

                Timber.d("PoiTags deleted : %d", poiTagDao.deleteByPoiIds(poiIdsToDelete));
                Timber.d("PoiNodeRefs deleted : %d", poiNodeRefDao.deleteByPoiIds(poiIdsToDelete));
                Timber.d("POIs deleted : %d", poiDao.deleteIds(poiIdsToDelete));

                Timber.d("PoiTypeTags deleted : %d", poiTypeTagDao.deleteByPoiTypeId(id));
                poiTypeDao.deleteById(id);
                Timber.i("Deleted PoiType id=%d", id);
                return null;
            }
        });
    }

    /**
     * Delete all the POIs who are ways and have no PoiType ans the PoiNodeRefs associated.
     * <p/>
     * Do not call the DAO directly to delete a way, use this method.
     */
    public void deleteAllWays() {
        databaseHelper.callInTransaction(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                List<Poi> pois = poiDao.queryForAllWaysNoType();
                List<Poi> poisToDelete = new ArrayList<>();

                for (Poi poi : pois) {
                    boolean delete = true;
                    for (PoiNodeRef poiNodeRef : poi.getNodeRefs()) {
                        if (poiNodeRef.getUpdated()) {
                            delete = false;
                        }
                    }
                    if (delete) {
                        poisToDelete.add(poi);
                        poiNodeRefDao.delete(poi.getNodeRefs());
                    }
                }

                poiDao.delete(poisToDelete);
                return null;
            }
        });
    }

    /**
     * Delete all the POIs who are ways and have no PoiType except the POIs in parameters. Delete also the PoiNodeRefs associated.
     * <p/>
     * Do not call the DAO directly to delete a way, use this method.
     *
     * @param exceptions The POIs who shouldn't be deleted.
     */
    public void deleteAllWaysExcept(final List<Poi> exceptions) {
        databaseHelper.callInTransaction(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                List<Poi> pois = poiDao.queryForAllWaysNoType();
                pois.removeAll(exceptions);
                List<Poi> poisToDelete = new ArrayList<>();

                for (Poi poi : pois) {
                    boolean delete = true;
                    for (PoiNodeRef poiNodeRef : poi.getNodeRefs()) {
                        if (poiNodeRef.getUpdated()) {
                            delete = false;
                        }
                    }
                    if (delete) {
                        poisToDelete.add(poi);
                        poiNodeRefDao.delete(poi.getNodeRefs());
                    }
                }

                poiDao.delete(poisToDelete);
                return null;
            }
        });
    }

    /**
     * Merge POIs in parameters to those already in the database.
     *
     * @param remotePois The POIs to merge.
     */
    public void mergeFromOsmPois(List<Poi> remotePois) {
        List<Poi> toMergePois = new ArrayList<>();

        Map<String, Poi> remotePoisMap = new HashMap<>();
        // Map remote Poi backend Ids
        for (Poi poi : remotePois) {
            remotePoisMap.put(poi.getBackendId(), poi);
        }

        // List matching Pois
        List<Poi> localPois = poiDao.queryForBackendIds(remotePoisMap.keySet());


        Map<String, Poi> localPoisMap = new HashMap<>();
        // Map matching local Pois
        for (Poi localPoi : localPois) {
            localPoisMap.put(localPoi.getBackendId(), localPoi);
        }

        // Browse remote pois
        for (Poi remotePoi : remotePois) {
            Poi localPoi = localPoisMap.get(remotePoi.getBackendId());
            Long localVersion = -1L;
            // If localPoi is versioned
            if (localPoi != null && localPoi.getVersion() != null) {
                localVersion = Long.valueOf(localPoi.getVersion());
            }
            // Compute version delta
            if (Long.valueOf(remotePoi.getVersion()) > localVersion) {
                // Remote version is newer, override existing one
                if (localPoi != null) {
                    remotePoi.setId(localPoi.getId());
                }
                // This Poi should be updated
                toMergePois.add(remotePoi);
            }
        }

        // savePois of either new or existing Pois
        savePois(toMergePois);
    }

    /**
     * Get the date of the last update of POIs.
     *
     * @return The date of the last update.
     */
    public DateTime getLastUpdateDate() {
        return poiDao.queryForMostRecentChangeDate();
    }

    /**
     * Query for all the POIs contained in the bounds defined by the box.
     *
     * @param box Bounds of the search in latitude and longitude coordinates.
     * @return The POIs contained in the box.
     */
    public List<Poi> queryForAllInRect(Box box) {
        return poiDao.queryForAllInRect(box);
    }

    /**
     * Query for all POIs who are ways.
     *
     * @return The list of POIs who are ways.
     */
    public List<Poi> queryForAllWays() {
        return poiDao.queryForAllWays();
    }

    /**
     * Query for all the existing values of a given PoiTag.
     *
     * @param key The key of the PoiTag.
     * @return The list of values.
     */
    public List<String> suggestionsForTagValue(String key, Long poiTypeId) {
        return poiTagDao.existingValuesForTag(key, poiTypeId);
    }

    /**
     * Get a Map containing all the suggestions for each tags.
     * <br/>
     * Parse the possible values of a PoiTypeTag and if there are none, query the database for all
     * the values for the tag.
     *
     * @param poiTypeTags The list of PoiTypeTags.
     * @return The map of results.
     */
    public Map<String, List<String>> suggestionsForTagsValue(Collection<PoiTypeTag> poiTypeTags) {
        Map<String, List<String>> res = new HashMap<>();
        for (PoiTypeTag poiTypeTag : poiTypeTags) {
            // If there are no possible values, load all the values in the database for the given tag name
            if (StringUtils.isEmpty(poiTypeTag.getPossibleValues())) {
                res.put(poiTypeTag.getKey(), suggestionsForTagValue(poiTypeTag.getKey(), poiTypeTag.getPoiType().getId()));
            } else {
                // Split the possible values string to a list
                res.put(poiTypeTag.getKey(), Arrays.asList(poiTypeTag.getPossibleValues().split(Character.toString((char) 29))));
            }
        }
        return res;
    }

    /**
     * Get all the PoiTypes in the database.
     *
     * @return A ID,PoiType map with all the PoiTypes.
     */
    public Map<Long, PoiType> loadPoiTypes() {
        List<PoiType> poiTypes = poiTypeDao.queryForAll();
        Map<Long, PoiType> result = new HashMap<>();
        for (PoiType poiType : poiTypes) {
            result.put(poiType.getId(), poiType);
        }
        return result;
    }


    public List<String> loadPoiTypeKeysWithDefaultValues() {
        return poiTypeTagDao.queryForTagKeysWithDefaultValues();
    }

    /**
     * Get the PoiType with the given id.
     *
     * @param id The id of the PoiType.
     * @return The PoiType.
     */
    public PoiType getPoiType(Long id) {
        return poiTypeDao.queryForId(id);
    }

    /**
     * Get all the PoiTypes alphabetically sorted.
     *
     * @return The List of PoiTypes alphabetically sorted.
     */
    public List<PoiType> getPoiTypesSortedByName() {
        return poiTypeDao.queryAllSortedByName();
    }

    /**
     * Get all the PoiTypes last use sorted.
     *
     * @return The List of PoiTypes last use sorted.
     */
    public List<PoiType> getPoiTypesSortedByLastUse() {
        return poiTypeDao.queryAllSortedByLastUse();
    }

    /**
     * Check whether the database has been initialized.
     *
     * @return Whether the database has been initialized.
     */
    public boolean isDbInitialized() {
        long count = poiTypeDao.countOf();
        Timber.d("pois in database : %s", count);
        return count > 0;
    }

    /**
     * Update the PoiTypes in the database with the given List of PoiTypes.
     *
     * @param newPoiTypes The PoiTypes to update.
     */
    public void updatePoiTypes(List<PoiType> newPoiTypes) {
        for (PoiType newPoiType : newPoiTypes) {
            PoiType byBackendId = poiTypeDao.findByBackendId(newPoiType.getBackendId());
            if (byBackendId != null) {
                newPoiType.setId(byBackendId.getId());
            }
            savePoiType(newPoiType);
        }
    }

    /**
     * Update the date of last use of a PoiType.
     *
     * @param id The id of the PoiType to update.
     */
    public void updatePoiTypeLastUse(long id) {
        PoiType poiType = poiTypeDao.queryForId(id);
        if (poiType != null) {
            poiType.setLastUse(new DateTime());
            Timber.d("Update date of : %s", poiType);
            poiTypeDao.createOrUpdate(poiType);
        }
    }

    /**
     * Reset the database : delete all the Pois, PoiTags and PoiNodeRefs of the database.
     *
     * @return Whether the reset was successful.
     */
    public Boolean resetDatabase() {
        return databaseHelper.callInTransaction(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                poiDao.deleteAll();
                poiNodeRefDao.deleteAll();
                poiTagDao.deleteAll();
                return true;
            }
        });
    }

    /**
     * Reset the PoiTypes of the database : delete all the Pois, PoiTags, PoiNodeRefs, PoiTypes and PoiTypeTags of the database
     * then reload and save the PoiTypes from the assets.
     *
     * @return Whether the reset was successful.
     */
    public Boolean resetTypesByDefault() {
        return databaseHelper.callInTransaction(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                Timber.d("Resetting the PoiTypes of the database");
                poiDao.deleteAll();
                poiNodeRefDao.deleteAll();
                poiTagDao.deleteAll();
                poiTypeDao.deleteAll();
                poiTypeTagDao.deleteAll();
                Timber.d("All Pois en PoiTypes deleted from database");
                savePoiTypesByDefault();
                Timber.d("Finished reloading and saving PoiTypes from assets");
                return true;
            }
        });
    }

    /**
     * Reset the PoiTypes of the database : delete all the Pois, PoiTags, PoiNodeRefs, PoiTypes and PoiTypeTags of the database
     * then reload and save the PoiTypes from the assets.
     *
     * @return Whether the reset was successful.
     */
    public Boolean resetTypes(final H2GeoDto h2GeoDto) {
        return databaseHelper.callInTransaction(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                Timber.d("Resetting the PoiTypes of the database");
                poiDao.deleteAll();
                poiNodeRefDao.deleteAll();
                poiTagDao.deleteAll();
                poiTypeDao.deleteAll();
                poiTypeTagDao.deleteAll();
                Timber.d("All Pois en PoiTypes deleted from database");
                savePoiTypesFromH2Geo(h2GeoDto);
                Timber.d("Finished reloading and saving PoiTypes from assets");
                return true;
            }
        });
    }

    // *********************************
    // ************ private ************
    // *********************************

    /**
     * Send a {@link PoiForEditionLoadedEvent} containing the Poi to edit and the suggestions for the PoiTypeTags.
     *
     * @param id The id of the Poi to edit.
     */
    private void loadPoiForEdition(Long id) {
        Poi poi = queryForId(id);

        bus.post(new PoiForEditionLoadedEvent(poi, suggestionsForTagsValue(poi.getType().getTags())));
    }

    /**
     * Send a {@link PoiForEditionLoadedEvent} containing the suggestions for the PoiTypeTags and a new Poi to complete.
     *
     * @param event Event containing the position, level and PoiType of the Poi to create.
     */
    private void loadPoiForCreation(PleaseLoadPoiForCreationEvent event) {
        Poi poi = new Poi();
        Set<Double> level = new HashSet<>();
        level.add(event.getLevel());
        poi.setLevel(level);
        poi.setLatitude(event.getLat());
        poi.setLongitude(event.getLng());
        poi.setType(poiTypeDao.queryForId(event.getPoiType()));

        Map<String, String> defaultTags = new HashMap<>();
        for (PoiTypeTag poiTypeTag : poi.getType().getTags()) {
            if (poiTypeTag.getValue() != null) { // default tags should be set in the corresponding POI
                defaultTags.put(poiTypeTag.getKey(), poiTypeTag.getValue());
            }
        }
        poi.applyChanges(defaultTags);

        bus.post(new PoiForEditionLoadedEvent(poi, suggestionsForTagsValue(poi.getType().getTags())));
    }

    /**
     * Send a {@link PoisLoadedEvent} containing all the POIs
     * in the Box of the {@link PleaseLoadPoisEvent}.
     *
     * @param event Event containing the box to load.
     */
    private void loadPois(PleaseLoadPoisEvent event) {
        bus.post(new PoisLoadedEvent(event.getBox(), queryForAllInRect(event.getBox())));
    }

    /**
     * Get the backup data and put it back in the active Poi, at the end the backup Poi is deleted.
     *
     * @param poiId The Id of the Poi to revert.
     */
    private Poi revertPoi(Long poiId) {
        Poi poi = poiDao.queryForId(poiId);
        Poi backup = null;
        Long oldPoiId = poi.getOldPoiId();

        if (oldPoiId != null) {
            backup = poiDao.queryForId(oldPoiId);

            // we retrieve the backup data and put it back
            backup.setOld(false);
            backup.setId(poi.getId());
            savePoi(backup);

            //we prepare to delete the modifications
            poi.setId(oldPoiId);
        }

        deletePoi(poi);
        return backup;
    }

    /**
     * Get the backup data and put it back in the active NodeRefPoi
     *
     * @param poiNodeRefId The Id of the PoiNodeRef to revert.
     */
    private PoiNodeRef revertPoiNodeRef(Long poiNodeRefId) {
        PoiNodeRef poiNodeRef = poiNodeRefDao.queryForId(poiNodeRefId);
        PoiNodeRef backup;
        Long oldId = poiNodeRef.getOldPoiId();

        if (oldId != null) {
            backup = poiNodeRefDao.queryForId(oldId);
            poiNodeRef.setLatitude(backup.getLatitude());
            poiNodeRef.setLongitude(backup.getLongitude());
            poiNodeRefDao.deleteById(oldId);
        }

        poiNodeRef.setUpdated(false);
        poiNodeRef.setOld(false);
        poiNodeRef.setOldPoiId(null);
        poiNodeRefDao.createOrUpdate(poiNodeRef);
        return poiNodeRef;
    }

    public void deleteOldPoiAssociated(Poi poi) {
        Long oldId = poi.getOldPoiId();
        if (oldId != null) {
            Poi old = poiDao.queryForId(oldId);
            if (old != null) {
                deletePoi(old);
            }
            poi.setOldPoiId(null);
        }
    }
}
