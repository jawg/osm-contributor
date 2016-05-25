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
public class LocationMarkerOptions<T> extends BaseMarkerOptions {

    // Must be declared because BaseMarkerOptions implements Parceable. DO NOT REMOVE !
    private Parcelable.Creator CREATOR;

    private T relatedObject;

    private LocationMarker.MarkerType markerType;


    public BaseMarkerOptions relatedObject(T relatedObject) {
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
        LocationMarker<T> marker = new LocationMarker<>(this);
        marker.setPosition(position);
        marker.setRelatedObject(relatedObject);
        marker.setType(markerType);
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

