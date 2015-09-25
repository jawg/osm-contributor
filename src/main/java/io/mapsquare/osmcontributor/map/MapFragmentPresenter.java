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
package io.mapsquare.osmcontributor.map;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;

import com.mapbox.mapboxsdk.geometry.BoundingBox;
import com.mapbox.mapboxsdk.overlay.Icon;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import de.greenrobot.event.EventBus;
import io.mapsquare.osmcontributor.OsmTemplateApplication;
import io.mapsquare.osmcontributor.core.ConfigManager;
import io.mapsquare.osmcontributor.core.events.NotesLoadedEvent;
import io.mapsquare.osmcontributor.core.events.PleaseLoadNotesEvent;
import io.mapsquare.osmcontributor.core.events.PleaseLoadPoiTypes;
import io.mapsquare.osmcontributor.core.events.PleaseLoadPoisEvent;
import io.mapsquare.osmcontributor.core.events.PoiTypesLoaded;
import io.mapsquare.osmcontributor.core.events.PoisLoadedEvent;
import io.mapsquare.osmcontributor.core.model.Note;
import io.mapsquare.osmcontributor.core.model.Poi;
import io.mapsquare.osmcontributor.core.model.PoiType;
import io.mapsquare.osmcontributor.map.events.PleaseChangeValuesDetailNoteFragmentEvent;
import io.mapsquare.osmcontributor.map.events.PleaseChangeValuesDetailPoiFragmentEvent;
import io.mapsquare.osmcontributor.map.events.PleaseInitializeDrawer;
import io.mapsquare.osmcontributor.map.events.PleaseLoadVectorialTileEvent;
import timber.log.Timber;

public class MapFragmentPresenter {

    MapFragment mapFragment;

    @Inject
    EventBus eventBus;

    @Inject
    ConfigManager configManager;

    private int zoomVectorial;
    private boolean forceRefreshPoi = false;
    private boolean forceRefreshNotes = false;

    public MapFragmentPresenter(MapFragment mapFragment) {
        this.mapFragment = mapFragment;
        ((OsmTemplateApplication) mapFragment.getActivity().getApplication()).getOsmTemplateComponent().inject(this);
        zoomVectorial = configManager.getZoomVectorial();
    }

    public void register() {
        eventBus.registerSticky(this);
    }

    public void unregister() {
        eventBus.unregister(this);
    }

    private Map<Long, PoiType> poiTypes = null;

    public int getNumberOfPoiTypes() {
        return poiTypes != null ? poiTypes.size() : 0;
    }

    public Collection<PoiType> getPoiTypes() {
        return poiTypes == null ? Collections.<PoiType>emptyList() : poiTypes.values();
    }

    public PoiType getPoiType(Long id) {
        return poiTypes.get(id);
    }

    public void onEventMainThread(PoiTypesLoaded event) {
        Timber.d("Received event PoiTypesLoaded");
        poiTypes = event.getPoiTypes();
        setForceRefreshPoi();
        setForceRefreshNotes();
        loadPoisIfNeeded();
        mapFragment.getBitmapHandler().setPoiTypes(poiTypes);
        eventBus.post(new PleaseInitializeDrawer(poiTypes, mapFragment.getPoiTypeHidden()));
        mapFragment.loadPoiTypeSpinner();
        mapFragment.loadPoiTypeFloatingBtn();
        mapFragment.displayTutorial(false);
        eventBus.removeStickyEvent(event);
    }

    private Float previousZoom;
    private BoundingBox triggerReloadPoiBoundingBox;

    public void loadPoisIfNeeded() {
        if (poiTypes == null) {
            Timber.v("PleaseLoadPoiTypes");
            eventBus.post(new PleaseLoadPoiTypes());
        }
        BoundingBox viewBoundingBox = mapFragment.getViewBoundingBox();
        if (viewBoundingBox != null) {
            if (mapFragment.getZoomLevel() > 15) {
                if (shouldReload(viewBoundingBox)) {
                    Timber.d("Reloading pois");
                    previousZoom = mapFragment.getZoomLevel();
                    triggerReloadPoiBoundingBox = enlarge(viewBoundingBox, 1.5);
                    eventBus.post(new PleaseLoadPoisEvent(enlarge(viewBoundingBox, 2)));
                    eventBus.post(new PleaseLoadNotesEvent(enlarge(viewBoundingBox, 2)));
                }
            } else {
                if (mapFragment.hasMarkers()) {
                    Timber.d("area displayed is too big, hiding pois");
                    previousZoom = mapFragment.getZoomLevel();
                    mapFragment.removeAllMarkers();
                }
            }
        }
    }

    public void setForceRefreshPoi() {
        this.forceRefreshPoi = true;
    }

    public void setForceRefreshNotes() {
        this.forceRefreshNotes = true;
    }


    // TODO remove start
    public boolean isForceRefreshPoi() {
        return forceRefreshPoi;
    }

    public boolean isForceRefreshNotes() {
        return forceRefreshNotes;
    }

    public void setForceRefreshPoi(boolean forceRefreshPoi) {
        this.forceRefreshPoi = forceRefreshPoi;
    }

    public void setForceRefreshNotes(boolean forceRefreshNotes) {
        this.forceRefreshNotes = forceRefreshNotes;
    }

    // TODO REMOVE END

    private boolean shouldReload(BoundingBox viewBoundingBox) {
        if (forceRefreshPoi || forceRefreshNotes) {
            forceRefreshPoi = false;
            forceRefreshNotes = false;
            return true;
        }
        if (previousZoom != null && previousZoom < 15) {
            return true;
        }
        return triggerReloadPoiBoundingBox == null
                || !triggerReloadPoiBoundingBox.union(viewBoundingBox).equals(triggerReloadPoiBoundingBox);
    }

    BoundingBox enlarge(BoundingBox viewBoundingBox, double factor) {
        double n = viewBoundingBox.getLatNorth();
        double e = viewBoundingBox.getLonEast();
        double s = viewBoundingBox.getLatSouth();
        double w = viewBoundingBox.getLonWest();
        double f = (factor - 1) / 2;
        return new BoundingBox(n + f * (n - s),
                e + f * (e - w),
                s - f * (n - s),
                w - f * (e - w));
    }

    private BoundingBox triggerReloadVectorialTiles;

    public void loadVectorialTilesIfNeeded() {
        BoundingBox viewBoundingBox = mapFragment.getViewBoundingBox();
        if (viewBoundingBox != null) {
            if (mapFragment.getZoomLevel() >= zoomVectorial) {
                if (shouldReloadVectorialTiles(viewBoundingBox)) {
                    Timber.d("Reloading vectorial tiles");
                    triggerReloadVectorialTiles = enlarge(viewBoundingBox, 1.25);
                    eventBus.post(new PleaseLoadVectorialTileEvent(enlarge(viewBoundingBox, 1.5)));
                }
            }
        }
    }

    private boolean shouldReloadVectorialTiles(BoundingBox viewBoundingBox) {
        return triggerReloadVectorialTiles == null
                || !triggerReloadVectorialTiles.union(viewBoundingBox).equals(triggerReloadVectorialTiles);
    }


    public void onEventMainThread(PoisLoadedEvent event) {
        Timber.d("Received event PoisLoaded  : " + event.getPois().size());
        List<Poi> pois = event.getPois();
        setForceRefreshPoi(false);
        LocationMarker marker;

        mapFragment.removeAllPoiMarkers();

        for (Poi poi : pois) {
            if (mapFragment.getMarkersPoi().get(poi.getId()) == null) {
                boolean selected = false;
                marker = new LocationMarker(poi);

                //is it the marker selected
                if (mapFragment.getSelectedMarkerType().equals(LocationMarker.MarkerType.POI) && poi.getId().equals(mapFragment.getMarkerSelectedId())) {
                    mapFragment.setMarkerSelected(marker);
                    selected = true;
                } else if (mapFragment.getSelectedMarkerType().equals(LocationMarker.MarkerType.POI) && mapFragment.getMarkerSelected() != null && poi.getId().equals(mapFragment.getMarkerSelected().getPoi().getId())) {
                    selected = true;
                }

                // Marker will draw the right icon color
                Bitmap bitmap = mapFragment.getBitmapHandler().getMarkerBitmap(poi.getType() != null ? poi.getType().getId() : null, Poi.computeState(selected, false, poi.getUpdated()));

                if (bitmap != null) {
                    marker.setIcon(new Icon(new BitmapDrawable(mapFragment.getResources(), bitmap)));
                }

                //the poi in edition should be hide
                if (!(mapFragment.getMarkerSelected() != null && mapFragment.getMapMode() == MapMode.POI_POSITION_EDITION) && !poi.getToDelete()) {
                    mapFragment.addMarker(marker);
                }

            } else {
                LocationMarker locationMarker = mapFragment.getMarkersPoi().get(poi.getId());
                locationMarker.setPoi(poi);
                boolean selected = false;

                if (mapFragment.getSelectedMarkerType().equals(LocationMarker.MarkerType.POI) && (poi.getId().equals(mapFragment.getMarkerSelectedId()) || mapFragment.getMarkerSelected() != null && poi.getId().equals(mapFragment.getMarkerSelected().getPoi().getId()))) {
                    selected = true;
                }

                //update the detail banner data
                if (selected && mapFragment.getMapMode() == MapMode.DETAIL_POI) {
                    eventBus.post(new PleaseChangeValuesDetailPoiFragmentEvent(getPoiType(poi.getType().getId()).getName(), poi.getName()));
                }

                Bitmap bitmap = mapFragment.getBitmapHandler().getMarkerBitmap(poi.getType() != null ? poi.getType().getId() : null, Poi.computeState(selected, false, poi.getUpdated()));

                if (bitmap != null) {
                    locationMarker.setIcon(new Icon(new BitmapDrawable(mapFragment.getResources(), bitmap)));
                }
            }
        }

        //use to click on the selected marker when the activity resume
        if (mapFragment.getMapMode() == MapMode.DEFAULT) {
            mapFragment.reselectMarker();
        }

        if (mapFragment.getSelectedMarkerType().equals(LocationMarker.MarkerType.POI) && mapFragment.getMarkerSelected() == null) {
            mapFragment.setMarkerSelectedId(-1L);
        }

        mapFragment.invalidateMap();
    }

    public void onEventMainThread(NotesLoadedEvent event) {
        Timber.d("Showing notes : " + event.getNotes().size());
        List<Note> notes = event.getNotes();
        setForceRefreshNotes(false);
        LocationMarker marker;

        for (Note note : notes) {
            if (mapFragment.getNote(note.getId()) == null) {

                marker = new LocationMarker(note);
                Bitmap bitmap;

                if (mapFragment.getSelectedMarkerType().equals(LocationMarker.MarkerType.NOTE) && note.getId().equals(mapFragment.getMarkerSelectedId())) {
                    mapFragment.setMarkerSelected(marker);
                    bitmap = mapFragment.getBitmapHandler().getNoteBitmap(Note.computeState(note, true, false));
                } else {
                    bitmap = mapFragment.getBitmapHandler().getNoteBitmap(Note.computeState(note, false, false));
                }

                if (bitmap != null) {
                    marker.setIcon(new Icon(new BitmapDrawable(mapFragment.getResources(), bitmap)));
                }
                mapFragment.addNote(marker);
            } else {
                boolean selected = false;
                LocationMarker currentMarker = mapFragment.getNote(note.getId());
                // refresh the note inside the marker the data may have changed
                currentMarker.setNote(note);
                //if it's the selected marker refresh the banner view

                if (mapFragment.getSelectedMarkerType().equals(LocationMarker.MarkerType.NOTE) && mapFragment.getMarkerSelected() != null && note.getId().equals(mapFragment.getMarkerSelected().getNote().getId())) {
                    //detailNoteFragment.setNote(mapFragment.getMarkerSelected().getNote());
                    selected = true;
                }

                //update the detail banner data
                if (selected && mapFragment.getMapMode() == MapMode.DETAIL_NOTE) {
                    eventBus.post(new PleaseChangeValuesDetailNoteFragmentEvent(note));
                }

                // refresh the icon
                Bitmap bitmap = mapFragment.getBitmapHandler().getNoteBitmap(Note.computeState(note, selected, false));
                if (bitmap != null) {
                    currentMarker.setIcon(new Icon(new BitmapDrawable(mapFragment.getResources(), bitmap)));
                }
            }

        }

        if ((mapFragment.getMapMode() == MapMode.DEFAULT || mapFragment.getMapMode() == MapMode.CREATION)) {
            mapFragment.reselectMarker();

        }

        if (mapFragment.getSelectedMarkerType().equals(LocationMarker.MarkerType.NOTE) && mapFragment.getMarkerSelected() == null) {
            mapFragment.setMarkerSelectedId(-1L);
        }
        mapFragment.invalidateMap();
    }
}
