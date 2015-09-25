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

import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.overlay.Marker;

import io.mapsquare.osmcontributor.core.model.Note;
import io.mapsquare.osmcontributor.core.model.Poi;
import io.mapsquare.osmcontributor.core.model.PoiNodeRef;


public class LocationMarker extends Marker {

    public enum MarkerType {
        POI,
        NODE_REF,
        NOTE,
        NONE
    }

    private MarkerType type = MarkerType.POI;

    public LocationMarker(Poi poi) {
        super(null, null, new LatLng(poi.getLatitude(), poi.getLongitude()));
        this.type = MarkerType.POI;
        setRelatedObject(poi);
    }

    public LocationMarker(Note note) {
        super(null, null, new LatLng(note.getLatitude(), note.getLongitude()));
        this.type = MarkerType.NOTE;
        setRelatedObject(note);
    }

    public LocationMarker(PoiNodeRef poiNodeRef) {
        super(null, null, new LatLng(poiNodeRef.getLatitude(), poiNodeRef.getLongitude()));
        this.type = MarkerType.NODE_REF;
        setRelatedObject(poiNodeRef);
    }

    public MarkerType getType() {
        return type;
    }

    public Poi getPoi() {
        return (Poi) getRelatedObject();
    }

    public PoiNodeRef getNodeRef() {
        return (PoiNodeRef) getRelatedObject();
    }

    public void setPoi(Poi poi) {
        setRelatedObject(poi);
    }

    public void setNote(Note note) {
        setRelatedObject(note);
    }

    public Note getNote() {
        return (Note) getRelatedObject();
    }

    public boolean isPoi() {
        return type.equals(MarkerType.POI);
    }

    public boolean isNodeRef() {
        return type.equals(MarkerType.NODE_REF);
    }

    public boolean isNote() {
        return type.equals(MarkerType.NOTE);
    }

    @Override
    public void setPoint(LatLng point) {
        super.setPoint(point);
        switch (type) {
            case NODE_REF:
                getNodeRef().setLatitude(point.getLatitude());
                getNodeRef().setLongitude(point.getLongitude());
                break;
            case POI:
                getPoi().setLatitude(point.getLatitude());
                getPoi().setLongitude(point.getLongitude());
                break;
        }
    }

    @Override
    public int hashCode() {
        Object related = getRelatedObject();
        return related != null ? related.hashCode() : super.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof LocationMarker)) {
            return false;
        }

        LocationMarker marker = (LocationMarker) o;
        Object ourRelated = getRelatedObject();
        Object itsRelated = marker.getRelatedObject();

        return ourRelated == null ? itsRelated == null : ourRelated.equals(itsRelated);
    }
}
