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
import io.jawg.osmcontributor.ui.events.map.PleaseCreateNoTagPoiEvent;
import io.jawg.osmcontributor.ui.events.map.PoiNoTypeCreated;
import io.jawg.osmcontributor.ui.managers.sync.PushToOSMService;
import timber.log.Timber;

public class EditPoiManager {

    private PoiManager poiManager;
    private PoiNodeRefDao poiNodeRefDao;
    private EventBus eventBus;
    private FirebaseJobDispatcher dispatcher;

    @Inject
    public EditPoiManager(PoiManager poiManager, PoiNodeRefDao poiNodeRefDao, EventBus eventBus, FirebaseJobDispatcher dispatcher) {
        this.poiManager = poiManager;
        this.poiNodeRefDao = poiNodeRefDao;
        this.eventBus = eventBus;
        this.dispatcher = dispatcher;
    }

    @Subscribe(threadMode = ThreadMode.ASYNC)
    public void onPleaseApplyPoiChanges(PleaseApplyPoiChanges event) {
        Timber.d("please apply poi changes");
        Poi editPoi = poiManager.queryForId(event.getPoiChanges().getId());

        if (editPoi.hasChanges(event.getPoiChanges().getTagsMap())) {

            editPoi.setOldPoiId(saveOldVersionOfPoi(editPoi));

            //this is the edition of a new poi or we already edited this poi
            editPoi.applyChanges(event.getPoiChanges().getTagsMap());
            editPoi.applyChanges(applyConstraints(editPoi));
            editPoi.setUpdated(true);
            poiManager.savePoi(editPoi);
            poiManager.updatePoiTypeLastUse(editPoi.getType().getId());
            PushToOSMService.schedulePushJob(dispatcher);
        }

        eventBus.post(new PoiChangesApplyEvent());
    }

    @Subscribe(threadMode = ThreadMode.ASYNC)
    public void onPleaseApplyPoiPositionChange(PleaseApplyPoiPositionChange event) {
        Timber.d("Please apply poi position change");
        Poi editPoi = poiManager.queryForId(event.getPoiId());

        editPoi.setOldPoiId(saveOldVersionOfPoi(editPoi));

        editPoi.setLatitude(event.getPoiPosition().getLatitude());
        editPoi.setLongitude(event.getPoiPosition().getLongitude());
        editPoi.setUpdated(true);
        poiManager.savePoi(editPoi);
        poiManager.updatePoiTypeLastUse(editPoi.getType().getId());
        PushToOSMService.schedulePushJob(dispatcher);
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
        PushToOSMService.schedulePushJob(dispatcher);
    }

    @Subscribe(threadMode = ThreadMode.ASYNC)
    public void onPleaseCreatePoiEvent(PleaseCreatePoiEvent event) {
        Timber.d("Please create poi");
        Poi poi = event.getPoi();
        poi.setUpdated(true);
        poi.applyChanges(event.getPoiChanges().getTagsMap());
        poi.applyChanges(applyConstraints(poi));
        poiManager.savePoi(poi);
        poiManager.updatePoiTypeLastUse(poi.getType().getId());
        eventBus.post(new PoiChangesApplyEvent());
        PushToOSMService.schedulePushJob(dispatcher);
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
        PushToOSMService.schedulePushJob(dispatcher);
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
        poi.setUpdated(true);

        poiManager.savePoi(poi);
        poiManager.updatePoiTypeLastUse(event.getPoiType().getId());

        eventBus.post(new PoiNoTypeCreated());
        PushToOSMService.schedulePushJob(dispatcher);
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
}
