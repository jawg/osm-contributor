/**
 * Copyright (C) 2016 eBusiness Information
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
package io.jawg.osmcontributor.ui.managers;


import android.app.Application;
import android.content.SharedPreferences;

import com.firebase.jobdispatcher.FirebaseJobDispatcher;
import com.mapbox.mapboxsdk.geometry.LatLng;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import io.jawg.osmcontributor.R;
import io.jawg.osmcontributor.database.dao.PoiNodeRefDao;
import io.jawg.osmcontributor.model.entities.Action;
import io.jawg.osmcontributor.model.entities.Condition;
import io.jawg.osmcontributor.model.entities.Constraint;
import io.jawg.osmcontributor.model.entities.Poi;
import io.jawg.osmcontributor.model.entities.PoiNodeRef;
import io.jawg.osmcontributor.model.entities.PoiTag;
import io.jawg.osmcontributor.model.entities.PoiTypeTag;
import io.jawg.osmcontributor.model.entities.Source;
import io.jawg.osmcontributor.model.events.PleaseCreatePoiEvent;
import io.jawg.osmcontributor.model.events.PleaseDeletePoiEvent;
import io.jawg.osmcontributor.ui.events.edition.PleaseApplyNodeRefPositionChange;
import io.jawg.osmcontributor.ui.events.edition.PleaseApplyPoiChanges;
import io.jawg.osmcontributor.ui.events.edition.PleaseApplyPoiPositionChange;
import io.jawg.osmcontributor.ui.events.edition.PoiChangesApplyEvent;
import io.jawg.osmcontributor.ui.events.login.PleaseAskForLoginEvent;
import io.jawg.osmcontributor.ui.events.map.PleaseCreateNoTagPoiEvent;
import io.jawg.osmcontributor.ui.events.map.PoiNoTypeCreated;
import io.jawg.osmcontributor.ui.managers.executor.GenericSubscriber;
import io.jawg.osmcontributor.ui.managers.login.CheckUserLogged;
import io.jawg.osmcontributor.ui.managers.sync.PushToOSMService;
import timber.log.Timber;

public class EditPoiManager {

    private Application application;
    private PoiManager poiManager;
    private RelationManager relationManager;
    private PoiNodeRefDao poiNodeRefDao;
    private EventBus eventBus;
    private FirebaseJobDispatcher dispatcher;
    private SharedPreferences sharedPreferences;
    private CheckUserLogged checkUserLogged;

    @Inject
    public EditPoiManager(Application application, PoiManager poiManager, RelationManager relationManager, PoiNodeRefDao poiNodeRefDao, EventBus eventBus, FirebaseJobDispatcher dispatcher, SharedPreferences sharedPreferences, CheckUserLogged checkUserLogged) {
        this.application = application;
        this.poiManager = poiManager;
        this.relationManager = relationManager;
        this.poiNodeRefDao = poiNodeRefDao;
        this.eventBus = eventBus;
        this.dispatcher = dispatcher;
        this.sharedPreferences = sharedPreferences;
        this.checkUserLogged = checkUserLogged;
    }

    /**
     * Set poi updated when changes made to pois tags
     * Set poi relation updated when changes to the relations ids
     * @param event
     */
    @Subscribe(threadMode = ThreadMode.ASYNC)
    public void onPleaseApplyPoiChanges(PleaseApplyPoiChanges event) {
        Timber.d("please apply poi changes");
        Poi editPoi = poiManager.queryForId(event.getPoiChanges().getId());

        // if the poi has some changes on its tags or if some changes should be applied to its relations
        boolean poiHasChanges = editPoi.hasChanges(event.getPoiChanges().getTagsMap());
        if (poiHasChanges || !event.getRelationEditions().isEmpty()) {

            editPoi.setOldPoiId(saveOldVersionOfPoi(editPoi));

            if (poiHasChanges) {
                //this is the edition of a new poi or we already edited this poi
                editPoi.applyChanges(event.getPoiChanges().getTagsMap());
                editPoi.applyChanges(applyConstraints(editPoi));
                editPoi.setDetailsUpdated(true);
            }

            if (!event.getRelationEditions().isEmpty()) {
                editPoi.applyChangesOnRelationList(event.getRelationEditions());
                editPoi.setRelation_updated(true);
            }

            poiManager.savePoi(editPoi);
            poiManager.updatePoiTypeLastUse(editPoi.getType().getId());
            relationManager.saveRelationEditions(event.getRelationEditions(), editPoi);
            schedulePushJob();
        }

        checkIfLoggedIn();
    }

    @Subscribe(threadMode = ThreadMode.ASYNC)
    public void onPleaseApplyPoiPositionChange(PleaseApplyPoiPositionChange event) {
        Timber.d("Please apply poi position change");
        Poi editPoi = poiManager.queryForId(event.getPoiId());

        editPoi.setOldPoiId(saveOldVersionOfPoi(editPoi));

        editPoi.setLatitude(event.getPoiPosition().getLatitude());
        editPoi.setLongitude(event.getPoiPosition().getLongitude());
        editPoi.setDetailsUpdated(true);
        poiManager.savePoi(editPoi);
        poiManager.updatePoiTypeLastUse(editPoi.getType().getId());
        schedulePushJob();
        checkIfLoggedIn();
    }

    @Subscribe(threadMode = ThreadMode.ASYNC)
    public void onPleaseApplyNodeRefPositionChange(PleaseApplyNodeRefPositionChange event) {
        Timber.d("Please apply noderef position change");
        LatLng newLatLng = event.getPoiPosition();

        //apply changes on the noderef
        PoiNodeRef poiNodeRef = poiNodeRefDao.queryForId(event.getPoiId());

        poiNodeRef.setOldPoiId(saveOldVersionOfPoiNodeRef(poiNodeRef));

        poiNodeRef.setLongitude(newLatLng.getLongitude());
        poiNodeRef.setLatitude(newLatLng.getLatitude());
        poiNodeRef.setUpdated(true);
        poiNodeRefDao.createOrUpdate(poiNodeRef);
        schedulePushJob();
        checkIfLoggedIn();
    }

    @Subscribe(threadMode = ThreadMode.ASYNC)
    public void onPleaseCreatePoiEvent(PleaseCreatePoiEvent event) {
        Timber.d("Please create poi");
        Poi poi = event.getPoi();
        poi.setDetailsUpdated(true);
        poi.applyChanges(event.getPoiChanges().getTagsMap());
        poi.applyChanges(applyConstraints(poi));
        poiManager.savePoi(poi);
        poiManager.updatePoiTypeLastUse(poi.getType().getId());
        schedulePushJob();
        checkIfLoggedIn();
    }

    @Subscribe(threadMode = ThreadMode.ASYNC)
    public void onPleaseDeletePoiEvent(PleaseDeletePoiEvent event) {
        Timber.d("Please delete poi");
        Poi poi = event.getPoi();
        if (poi.getId() != null) {
            poi = poiManager.queryForId(poi.getId());
        }

        if (poi.getBackendId() == null) {
            poiManager.deletePoi(poi);
        } else {
            poi.setOldPoiId(saveOldVersionOfPoi(poi));
            poi.setToDelete(true);
            poiManager.savePoi(poi);
        }
        schedulePushJob();
        checkIfLoggedIn();
    }

    @Subscribe(threadMode = ThreadMode.ASYNC)
    public void onPleaseCreateNoTagPoiEvent(PleaseCreateNoTagPoiEvent event) {
        Poi poi = new Poi();
        LatLng latLng = event.getLatLng();

        poi.setLatitude(latLng.getLatitude());
        poi.setLongitude(latLng.getLongitude());
        poi.setType(event.getPoiType());

        List<PoiTag> defaultTags = new ArrayList<>();
        for (PoiTypeTag poiTypeTag : poi.getType().getTags()) {
            if (poiTypeTag.getValue() != null) { // default tags should be set in the corresponding POI
                PoiTag poiTag = new PoiTag();
                poiTag.setKey(poiTypeTag.getKey());
                poiTag.setValue(poiTypeTag.getValue());
                defaultTags.add(poiTag);
            }
        }

        poi.setTags(defaultTags);
        poi.setDetailsUpdated(true);

        poiManager.savePoi(poi);
        poiManager.updatePoiTypeLastUse(event.getPoiType().getId());

        eventBus.post(new PoiNoTypeCreated());
        schedulePushJob();
        checkIfLoggedIn();
    }

    private void checkIfLoggedIn() {
        checkUserLogged.unsubscribe();
        checkUserLogged.execute(new CheckUserLoggedSubscriber());
    }

    private void schedulePushJob() {
        if (sharedPreferences.getBoolean(application.getString(R.string.shared_prefs_auto_commit), false)) {
            PushToOSMService.schedulePushJob(dispatcher);
        }
    }

    private Long saveOldVersionOfPoi(Poi poi) {
        if (poi.getBackendId() == null) {
            return null;
        }
        if (poiManager.countForBackendId(poi.getBackendId()) == 1) {
            Poi old = poi.getCopy();
            old.setOld(true);
            poiManager.savePoi(old);
            return old.getId();
        }
        return poi.getOldPoiId();
    }

    private Long saveOldVersionOfPoiNodeRef(PoiNodeRef poiNodeRef) {
        if (poiNodeRefDao.countForBackendId(poiNodeRef.getNodeBackendId()) == 1) {
            PoiNodeRef old = poiNodeRef.getCopy();
            old.setOld(true);
            poiNodeRefDao.createOrUpdate(old);
            return old.getId();
        }
        return poiNodeRef.getOldPoiId();
    }

    private Map<String, String> applyConstraints(Poi poi) {
        Map<String, String> tagsMap = new HashMap<>();
        Collection<Constraint> constraints = poi.getType().getConstraints();
        if (constraints != null) {
            for (Constraint constraint : constraints) {
                Source source = constraint.getSource();
                Condition condition = constraint.getCondition();
                Action action = constraint.getAction();

                PoiTag poiTag = null;

                switch (source.getType()) {
                    case TAG:
                        poiTag = findTagByKey(poi, source.getKey());
                        break;
                }

                switch (condition.getType()) {
                    case EXISTS:
                        // if the value is true and tag exists
                        if (poiTag != null && condition.getValue()
                                .equalsIgnoreCase(Condition.ExistsValues.TRUE.getValue())) {
                            continue;
                            // if the value is false and tag doesn't exists
                        } else if (poiTag == null && condition.getValue()
                                .equalsIgnoreCase(Condition.ExistsValues.FALSE.getValue())) {
                            continue;
                            // If none is valid
                        } else {
                            break;
                        }
                    case EQUALS:
                        if (poiTag != null && poiTag.getValue()
                                .equalsIgnoreCase(condition.getValue())) {
                            break;
                        }
                    default:
                        continue;
                }

                switch (action.getType()) {
                    case SET_TAG_VALUE:
                        // Add to map of tags
                        tagsMap.put(action.getKey(), action.getValue());
                        break;
                    case REMOVE_TAG:
                        // Set value to empty string to remove
                        tagsMap.put(action.getKey(), "");
                }
            }
        }
        return tagsMap;
    }

    private PoiTag findTagByKey(Poi poi, String key) {
        Collection<PoiTag> poiTypeTags = poi.getTags();
        for (PoiTag poiTag : poiTypeTags) {
            if (poiTag.getKey().equals(key)) {
                return poiTag;
            }
        }
        return null;
    }

    private class CheckUserLoggedSubscriber extends GenericSubscriber<Boolean> {
        @Override
        public void onNext(Boolean isLogged) {
            if (!isLogged) {
                eventBus.post(new PleaseAskForLoginEvent());
            } else {
                // If we don't send the PleaseAskForLoginEvent, we need to send the
                // PoiChangesApplyEvent in order to let the EditPoiFragment that it can close itself.
                eventBus.post(new PoiChangesApplyEvent());
            }
        }

        @Override
        public void onError(Throwable e) {
            super.onError(e);
            eventBus.post(new PoiChangesApplyEvent());
        }
    }
}
