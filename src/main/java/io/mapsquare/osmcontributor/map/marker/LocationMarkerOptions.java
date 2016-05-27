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


import android.os.Parcel;
import android.os.Parcelable;

import com.mapbox.mapboxsdk.annotations.BaseMarkerOptions;

import io.mapsquare.osmcontributor.core.model.Note;
import io.mapsquare.osmcontributor.core.model.Poi;
import io.mapsquare.osmcontributor.core.model.PoiNodeRef;

/**
 * Contains options for LocationMarker.
 * @param <T> Parameter of related object
 */
public class LocationMarkerOptions<T> extends BaseMarkerOptions<LocationMarker<T>, LocationMarkerOptions<T>> {

    // Must be declared because BaseMarkerOptions implements Parceable. DO NOT REMOVE !
    private Parcelable.Creator CREATOR;

    private T relatedObject;

    private LocationMarker.MarkerType markerType;

    private LocationMarker<T> marker;


    public LocationMarkerOptions<T> relatedObject(T relatedObject) {
        this.relatedObject = relatedObject;
        setMarkerType();
        return this;
    }

    @Override
    public LocationMarkerOptions<T> getThis() {
        return this;
    }

    @Override
    public LocationMarker<T> getMarker() {
        if (marker == null) {
            marker = new LocationMarker<>(this);
            marker.setPosition(position);
            marker.setRelatedObject(relatedObject);
            marker.setType(markerType);
        }
        return marker;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {

    }

    /**
     * Set marker type depending on object type.
     */
    private void setMarkerType() {
        if (relatedObject instanceof Poi) {
            markerType = LocationMarker.MarkerType.POI;
        } else if (relatedObject instanceof Note) {
            markerType = LocationMarker.MarkerType.NOTE;
        } else if (relatedObject instanceof PoiNodeRef) {
            markerType = LocationMarker.MarkerType.NODE_REF;
        } else {
            markerType = LocationMarker.MarkerType.NONE;
        }
    }
}

