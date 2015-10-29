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
package io.mapsquare.osmcontributor.edition;


import android.app.Application;

import com.mapbox.mapboxsdk.geometry.LatLng;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import de.greenrobot.event.EventBus;
import io.mapsquare.osmcontributor.core.PoiManager;
import io.mapsquare.osmcontributor.core.database.dao.PoiNodeRefDao;
import io.mapsquare.osmcontributor.core.events.PleaseCreatePoiEvent;
import io.mapsquare.osmcontributor.core.events.PleaseDeletePoiEvent;
import io.mapsquare.osmcontributor.core.model.Poi;
import io.mapsquare.osmcontributor.core.model.PoiNodeRef;
import io.mapsquare.osmcontributor.core.model.PoiTag;
import io.mapsquare.osmcontributor.core.model.PoiTypeTag;
import io.mapsquare.osmcontributor.edition.events.PleaseApplyNodeRefPositionChange;
import io.mapsquare.osmcontributor.edition.events.PleaseApplyPoiChanges;
import io.mapsquare.osmcontributor.edition.events.PleaseApplyPoiPositionChange;
import io.mapsquare.osmcontributor.edition.events.PoiChangesApplyEvent;
import io.mapsquare.osmcontributor.map.events.PleaseCreateNoTagPoiEvent;
import io.mapsquare.osmcontributor.map.events.PoiNoTypeCreated;
import timber.log.Timber;

public class EditPoiManager {

    PoiManager poiManager;
    Application application;
    PoiNodeRefDao poiNodeRefDao;
    EventBus eventBus;

    @Inject
    public EditPoiManager(PoiManager poiManager, PoiNodeRefDao poiNodeRefDao, Application application, EventBus eventBus) {
        this.poiManager = poiManager;
        this.application = application;
        this.poiNodeRefDao = poiNodeRefDao;
        this.eventBus = eventBus;
    }

    public void onEventAsync(PleaseApplyPoiChanges event) {
        Timber.d("please apply poi changes");
        Poi editPoi = poiManager.queryForId(event.getPoiChanges().getId());
        editPoi.applyChanges(event.getPoiChanges().getTagsMap());
        editPoi.setUpdated(true);
        poiManager.savePoi(editPoi);
        poiManager.updatePoiTypeLastUse(editPoi.getType().getId());
        eventBus.post(new PoiChangesApplyEvent());
    }

    public void onEventAsync(PleaseApplyPoiPositionChange event) {
        Timber.d("Please apply poi position change");
        Poi editPoi = poiManager.queryForId(event.getPoiId());
        editPoi.setLatitude(event.getPoiPosition().getLatitude());
        editPoi.setLongitude(event.getPoiPosition().getLongitude());
        editPoi.setUpdated(true);
        poiManager.savePoi(editPoi);
        poiManager.updatePoiTypeLastUse(editPoi.getType().getId());
    }

    public void onEventAsync(PleaseApplyNodeRefPositionChange event) {
        Timber.d("Please apply noderef position change");
        LatLng newLatLng = event.getPoiPosition();

        //apply changes on the noderef
        PoiNodeRef poiNodeRef = poiNodeRefDao.queryForId(event.getPoiId());
        poiNodeRef.setLongitude(newLatLng.getLongitude());
        poiNodeRef.setLatitude(newLatLng.getLatitude());
        poiNodeRef.setUpdated(true);
        poiNodeRefDao.createOrUpdate(poiNodeRef);
    }

    public void onEventAsync(PleaseCreatePoiEvent event) {
        Timber.d("Please create poi");
        Poi poi = event.getPoi();
        poi.setUpdated(true);
        poi.applyChanges(event.getPoiChanges().getTagsMap());
        poiManager.savePoi(poi);
        poiManager.updatePoiTypeLastUse(poi.getType().getId());
        eventBus.post(new PoiChangesApplyEvent());
    }

    public void onEventAsync(PleaseDeletePoiEvent event) {
        Timber.d("Please delete poi");
        Poi poi = event.getPoi();
        if (poi.getId() != null) {
            poi = poiManager.queryForId(poi.getId());
        }

        if (poi.getBackendId() == null) {
            poiManager.deletePoi(poi);
        } else {
            poi.setToDelete(true);
            poiManager.savePoi(poi);
        }
    }

    public void onEventAsync(PleaseCreateNoTagPoiEvent event) {

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
    }
}
