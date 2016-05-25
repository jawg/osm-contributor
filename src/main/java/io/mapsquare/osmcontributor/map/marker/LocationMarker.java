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
package io.mapsquare.osmcontributor.map.marker;

import com.mapbox.mapboxsdk.annotations.BaseMarkerOptions;
import com.mapbox.mapboxsdk.annotations.Marker;


public class LocationMarker<T> extends Marker {

    private T relatedObject;

    private MarkerType type;

    LocationMarker(BaseMarkerOptions markerOptions) {
        super(markerOptions);
    }

    @Override
    public int hashCode() {
        return relatedObject != null ? relatedObject.hashCode() : super.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }

        if (this == o) {
            return true;
        }

        if (!(o instanceof LocationMarker)) {
            return false;
        }

        LocationMarker marker = (LocationMarker) o;
        Object itsRelated = marker.getRelatedObject();

        return relatedObject == null ? itsRelated == null : relatedObject.equals(itsRelated);
    }

    public MarkerType getType() {
        return type;
    }

    public void setType(MarkerType type) {
        this.type = type;
    }

    public T getRelatedObject() {
        return relatedObject;
    }

    public void setRelatedObject(T relatedObject) {
        this.relatedObject = relatedObject;
    }

    /**
     * Indicate marker type.
     */
    public enum MarkerType {
        POI,
        NODE_REF,
        NOTE,
        NONE
    }
}
