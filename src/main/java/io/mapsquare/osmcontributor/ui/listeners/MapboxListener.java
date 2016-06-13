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
package io.mapsquare.osmcontributor.ui.listeners;

import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;

import com.mapbox.mapboxsdk.annotations.IconFactory;
import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapboxMap;

import org.greenrobot.eventbus.EventBus;

import java.math.RoundingMode;
import java.text.DecimalFormat;

import io.mapsquare.osmcontributor.model.entities.Note;
import io.mapsquare.osmcontributor.model.entities.Poi;
import io.mapsquare.osmcontributor.model.entities.PoiNodeRef;
import io.mapsquare.osmcontributor.ui.fragments.MapFragment;
import io.mapsquare.osmcontributor.ui.utils.MapMode;
import io.mapsquare.osmcontributor.ui.utils.views.map.marker.LocationMarker;
import timber.log.Timber;


public class MapboxListener {
    private MapFragment mapFragment;
    private MapboxMap mapboxMap;
    private CameraPosition position;
    private EventBus eventBus;

    private final DecimalFormat df;

    public MapboxListener(MapFragment mapFragment, EventBus eventBus) {
        this.mapFragment = mapFragment;
        this.eventBus = eventBus;
        df = new DecimalFormat("#.##");
        df.setRoundingMode(RoundingMode.DOWN);
    }

    /**
     * Register the listener for the map
     * @param mapboxMap
     */
    public void listen(MapboxMap mapboxMap) {
        this.mapboxMap = mapboxMap;
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
                    return false;
                }
                return false;
            }
        });

        // Listen on location and zoom change
        mapboxMap.setOnCameraChangeListener(new MapboxMap.OnCameraChangeListener() {
            @Override
            public void onCameraChange(CameraPosition position) {
                // Location change, call the listener method
                if (MapboxListener.this.position != null) {
                    if (!MapboxListener.this.position.target.equals(position.target)) {
                        onLocationChange(position.target);
                    }

                    // Zoom change, call the listener method
                    if (MapboxListener.this.position.zoom !=  position.zoom) {
                        onZoomChange(position.zoom);
                    }
                }

                MapboxListener.this.position = position;
            }
        });

        mapboxMap.setOnCameraChangeListener(new MapboxMap.OnCameraChangeListener() {
            @Override
            public void onCameraChange(CameraPosition position) {
                onCameraPositionChange();
            }
        });
    }

    /**
     * The camera position change
     */
    private void onCameraPositionChange() {
        if (mapFragment.getMapMode().equals(MapMode.NODE_REF_POSITION_EDITION)) {
            mapFragment.onCameraChangeUpdatePolyline();
        }
    }

    /**
     * The zoom change
     * @param zoom
     */
    private void onZoomChange(double zoom) {
        // For testing purpose
        mapFragment.setZoomLevelText(df.format(zoom));
        mapFragment.getPresenter().loadPoisIfNeeded();
        boolean isVectorial = mapFragment.isVectorial();

        Timber.v("new zoom : %s", zoom);

        if (zoom < mapFragment.getZoomVectorial()) {
            mapFragment.getLevelBar().setVisibility(View.INVISIBLE);
            mapFragment.getAddressView().setVisibility(View.INVISIBLE);
            if (isVectorial) {
                mapFragment.setVectorial(false);
                mapFragment.applyPoiFilter();
                mapFragment.applyNoteFilter();
            }
        } else {
            LatLng center = mapboxMap.getCameraPosition().target;
            mapFragment.getGeocoder().delayedReverseGeocoding(center.getLatitude(), center.getLongitude());
            if (mapFragment.getLevelBar().getLevels().length > 1) {
                mapFragment.getLevelBar().setVisibility(View.VISIBLE);
            }
            if (!isVectorial) {
                mapFragment.setVectorial(true);
                mapFragment.applyPoiFilter();
                mapFragment.applyNoteFilter();
            }
        }
    }


    private void onLocationChange(LatLng location) {

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
//            eventBus.post(new PleaseLoadNodeRefAround(point.getLatitude(), point.getLongitude()));
            mapFragment.unselectIcon();
        }
        if (mapMode == MapMode.DEFAULT && mapFragment.getAddPoiFloatingButton().isExpanded()) {
            mapFragment.getAddPoiFloatingButton().collapse();
        }
    }

    /**
     * Click on LocationMarker
     * @param locationMarker
     */
    public void onMarkerClick(LocationMarker locationMarker) {
        MapMode mapMode = mapFragment.getMapMode();
        if (mapMode != MapMode.POI_POSITION_EDITION && mapMode != MapMode.POI_CREATION && !mapFragment.isTuto()) {
            mapFragment.unselectIcon();
            mapFragment.setMarkerSelected(locationMarker);
            Log.i(MapboxListener.class.getSimpleName(), "onMarkerClick: selected " + locationMarker.getRelatedObject());
            switch (locationMarker.getType()) {
                case POI:
                    onPoiMarkerClick(locationMarker);
                    break;
                case NOTE:
                    onNoteMarkerClick(locationMarker);
                    break;
                case NODE_REF:
                    onNodeRefMarkerClick(locationMarker);
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
        Bitmap bitmap = mapFragment.getBitmapHandler().getMarkerBitmap(marker.getRelatedObject().getType(), Poi.computeState(true, false, false));
        if (bitmap != null) {
            marker.setIcon(IconFactory.getInstance(mapFragment.getActivity()).fromBitmap(bitmap));
        }
        mapFragment.switchMode(MapMode.DETAIL_POI);
        mapFragment.changeMapPositionSmooth(marker.getPosition());
        mapFragment.setMarkerSelectedId(-1L);
    }

    /**
     * Click on Note
     * @param marker
     */
    private void onNoteMarkerClick(LocationMarker<Note> marker) {
        Bitmap bitmap = mapFragment.getBitmapHandler().getNoteBitmap(
                Note.computeState(marker.getRelatedObject(), true, false));
        if (bitmap != null) {
            marker.setIcon(IconFactory.getInstance(mapFragment.getActivity()).fromBitmap(bitmap));
        }

        mapFragment.switchMode(MapMode.DETAIL_NOTE);
        mapFragment.changeMapPositionSmooth(marker.getPosition());
        mapFragment.setMarkerSelectedId(-1L);
    }


    /**
     * Click on NoteRef
     * @param marker
     */
    public void onNodeRefMarkerClick(LocationMarker<PoiNodeRef> marker) {
        Log.i(MapboxListener.class.getSimpleName(), "onNodeRefMarkerClick: ");
        mapFragment.selectNodeRefMarker();
    }
}
