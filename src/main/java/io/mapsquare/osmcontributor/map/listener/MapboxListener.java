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
package io.mapsquare.osmcontributor.map.listener;

import android.animation.ValueAnimator;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.util.Log;

import com.mapbox.mapboxsdk.annotations.IconFactory;
import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapboxMap;

import org.greenrobot.eventbus.EventBus;

import javax.inject.Inject;

import io.mapsquare.osmcontributor.core.events.PleaseLoadNodeRefAround;
import io.mapsquare.osmcontributor.core.model.Note;
import io.mapsquare.osmcontributor.core.model.Poi;
import io.mapsquare.osmcontributor.core.model.PoiNodeRef;
import io.mapsquare.osmcontributor.map.BitmapHandler;
import io.mapsquare.osmcontributor.map.MapFragment;
import io.mapsquare.osmcontributor.map.MapMode;
import io.mapsquare.osmcontributor.map.OsmAnimatorUpdateListener;
import io.mapsquare.osmcontributor.map.marker.LocationMarker;


public class MapboxListener {
    private MapFragment mapFragment;
    private MapboxMap mapboxMap;
    private BitmapHandler bitmapHandler;


    @Inject
    EventBus eventBus;

    @Inject
    public MapboxListener(MapFragment mapFragment) {
        Log.i(MapboxListener.class.getSimpleName(), "MapboxListener Contructor");
        this.mapFragment = mapFragment;
    }

    public void listen(MapboxMap mapboxMap) {
        this.mapboxMap = mapboxMap;
        this.bitmapHandler = mapFragment.getBitmapHandler();
        // Listen on map click
        mapboxMap.setOnMapClickListener(new MapboxMap.OnMapClickListener() {
            @Override
            public void onMapClick(@NonNull LatLng point) {
                MapboxListener.this.onMapClick(point);
            }
        });

        // Listen on marker click
        mapboxMap.setOnMarkerClickListener(new MapboxMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(@NonNull Marker marker) {
                if (marker instanceof LocationMarker) {
                    LocationMarker locationMarker = (LocationMarker) marker;
                    MapboxListener.this.onMarkerClick(locationMarker);
                    return true;
                }
                return false;
            }
        });
    }

    /**
     * User click on map
     * @param point
     */
    private void onMapClick(LatLng point) {
        MapMode mapMode = mapFragment.getMapMode();
        if (mapMode == MapMode.DETAIL_POI || mapMode == MapMode.DETAIL_NOTE) {
            // it prevents to reselect the marker
            mapFragment.setMarkerSelectedId(-1L);
            mapFragment.switchMode(MapMode.DEFAULT);
        }
        if (mapMode == MapMode.WAY_EDITION) {
            eventBus.post(new PleaseLoadNodeRefAround(point.getLatitude(), point.getLongitude()));
        }
        if (mapMode == MapMode.DEFAULT && mapFragment.getAddPoiFloatingButton().isExpanded()) {
            mapFragment.getAddPoiFloatingButton().collapse();
        }
    }

    public void onMarkerClick(LocationMarker locationMarker) {
        MapMode mapMode = mapFragment.getMapMode();
        if (mapMode != MapMode.POI_POSITION_EDITION && mapMode != MapMode.POI_CREATION && mapMode != MapMode.WAY_EDITION && !mapFragment.isTuto()) {
            mapFragment.unselectIcon();
            mapFragment.setMarkerSelected(locationMarker);
            switch (locationMarker.getType()) {
                case POI:
                    onPoiMarkerClick(locationMarker);
                    break;
                case NOTE:
                    onNoteMarkerClick(locationMarker);
                    break;
                default:
                    break;
            }
        }
    }

    /**
     * Click on POI
     * @param marker
     */
    void onPoiMarkerClick(LocationMarker<Poi> marker) {

        if (bitmapHandler == null) {
            Log.i(MapboxListener.class.getSimpleName(), "onPoiMarkerClick: BitmapHandler NULL");
        } else {
            Bitmap bitmap = bitmapHandler.getMarkerBitmap(marker.getRelatedObject().getType(), Poi.computeState(true, false, false));
            if (bitmap != null) {
                marker.setIcon(IconFactory.getInstance(mapFragment.getActivity()).fromBitmap(bitmap));
            }
            mapFragment.switchMode(MapMode.DETAIL_POI);
            ValueAnimator valueAnimator = ValueAnimator.ofFloat(0, OsmAnimatorUpdateListener.STEPS_CENTER_ANIMATION);
            valueAnimator.setDuration(500);
            valueAnimator.addUpdateListener(new OsmAnimatorUpdateListener(mapboxMap.getCameraPosition().target, marker.getPosition(), mapboxMap));
            valueAnimator.start();
            mapFragment.setMarkerSelectedId(-1L);
        }
    }

    private void onNoteMarkerClick(LocationMarker<Note> marker) {
        Bitmap bitmap = bitmapHandler.getNoteBitmap(
                Note.computeState(marker.getRelatedObject(), true, false));
        if (bitmap != null) {
            marker.setIcon(IconFactory.getInstance(mapFragment.getActivity()).fromBitmap(bitmap));
        }

        mapFragment.switchMode(MapMode.DETAIL_NOTE);
        ValueAnimator valueAnimator = ValueAnimator.ofFloat(0, OsmAnimatorUpdateListener.STEPS_CENTER_ANIMATION);
        valueAnimator.setDuration(500);
        valueAnimator.addUpdateListener(new OsmAnimatorUpdateListener(mapboxMap.getCameraPosition().target, marker.getPosition(), mapboxMap));
        valueAnimator.start();
        mapFragment.setMarkerSelectedId(-1L);
    }

    public void onNodeRefClick(LocationMarker<PoiNodeRef> marker) {
        mapFragment.selectNodeRef();
        ValueAnimator valueAnimator = ValueAnimator.ofFloat(0, OsmAnimatorUpdateListener.STEPS_CENTER_ANIMATION);
        valueAnimator.setDuration(500);
        valueAnimator.addUpdateListener(new OsmAnimatorUpdateListener(mapboxMap.getCameraPosition().target, marker.getPosition(), mapboxMap));
        valueAnimator.start();
    }
}
