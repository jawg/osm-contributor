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
package io.mapsquare.osmcontributor.map.vectorial;

import android.app.Application;
import android.graphics.Paint;

import com.mapbox.mapboxsdk.geometry.LatLng;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.inject.Inject;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import io.mapsquare.osmcontributor.R;
import io.mapsquare.osmcontributor.core.PoiManager;
import io.mapsquare.osmcontributor.core.database.dao.PoiNodeRefDao;
import io.mapsquare.osmcontributor.core.events.NodeRefAroundLoadedEvent;
import io.mapsquare.osmcontributor.core.model.Poi;
import io.mapsquare.osmcontributor.core.model.PoiNodeRef;
import io.mapsquare.osmcontributor.map.events.EditionVectorialTilesLoadedEvent;
import io.mapsquare.osmcontributor.map.events.PleaseLoadEditVectorialTileEvent;
import io.mapsquare.osmcontributor.map.events.PleaseSelectNodeRefByID;
import timber.log.Timber;

public class EditVectorialWayManager {

    private Set<VectorialObject> vectorialObjectsEdition = new HashSet<>();
    private Application application;
    private PoiManager poiManager;
    private PoiNodeRefDao poiNodeRefDao;
    private EventBus bus;

    private float scaledDensity;

    @Inject
    public EditVectorialWayManager(Application application, PoiManager poiManager, PoiNodeRefDao poiNodeRefDao, EventBus bus) {
        this.application = application;
        this.poiManager = poiManager;
        this.poiNodeRefDao = poiNodeRefDao;
        this.bus = bus;
        scaledDensity = application.getResources().getDisplayMetrics().scaledDensity;
        if (scaledDensity < 3) {
            scaledDensity = 3;
        }
    }

    public void buildEditionVectorialObject(List<Poi> pois, boolean isRefreshFromOverpass) {
        Timber.d("Showing nodesRefs : " + pois.size());
        vectorialObjectsEdition.clear();

        for (Poi poi : pois) {

            VectorialObject vectorialObject = new VectorialObject(false);
            vectorialObject.setId(poi.getBackendId());
            vectorialObject.setPriority(2);
            vectorialObject.getPaint().setStrokeWidth(5);
            for (PoiNodeRef nodeRef : poi.getNodeRefs()) {
                // Properties of a node in way edition
                VectorialObject point = new VectorialObject(false);
                point.setId(nodeRef.getNodeBackendId());
                point.setPriority(1);
                point.getPaint().setColor(application.getResources().getColor(R.color.colorCreation));
                point.getPaint().setStrokeWidth(6 * scaledDensity);
                point.getPaint().setStrokeCap(Paint.Cap.ROUND);
                point.setFilled(true);
                LatLng latLng = new LatLng(nodeRef.getLatitude(), nodeRef.getLongitude());
                double[] precomputed;

                precomputed = Projection.latLongToPixelXY(latLng.getLatitude(), latLng.getLongitude());
                vectorialObject.addPoint(new XY(precomputed[0], precomputed[1], nodeRef.getNodeBackendId()));

                point.addPoint(new XY(precomputed[0], precomputed[1], nodeRef.getNodeBackendId()));
                vectorialObjectsEdition.add(point);

            }
            vectorialObjectsEdition.add(vectorialObject);
        }

        TreeSet<Double> levels = new TreeSet<>();
        levels.add(0d);

        bus.post(new EditionVectorialTilesLoadedEvent(vectorialObjectsEdition, levels, isRefreshFromOverpass));
    }


    @Subscribe(threadMode = ThreadMode.ASYNC)
    public void onPleaseLoadEditVectorialTileEvent(PleaseLoadEditVectorialTileEvent event) {
        List<Poi> poisFromDB = poiManager.queryForAllWays();
        buildEditionVectorialObject(poisFromDB, event.isRefreshFromOverpass());
    }

    @Subscribe(threadMode = ThreadMode.ASYNC)
    public void onPleaseSelectNodeRefByID(PleaseSelectNodeRefByID event) {
        List<PoiNodeRef> poisFromDB = new ArrayList<>();
        poisFromDB.add(poiNodeRefDao.queryForId(event.getNodeRefSelectedId()));
        bus.post(new NodeRefAroundLoadedEvent(poisFromDB));
    }
}