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
package io.jawg.osmcontributor.ui.utils.views.map.marker;


import android.os.Parcel;
import android.os.Parcelable;

import com.mapbox.mapboxsdk.annotations.BaseMarkerOptions;

import io.jawg.osmcontributor.model.entities.PoiNodeRef;

/**
 * @author Tommy Buonomo on 20/06/16.
 */
public class WayMarkerOptions extends BaseMarkerOptions<WayMarker, WayMarkerOptions> {
    // Must be declared because BaseMarkerOptions implements Parceable. DO NOT REMOVE !
    private Parcelable.Creator CREATOR;

    private PoiNodeRef poiNodeRef;

    private WayMarker marker;


    public WayMarkerOptions poiNodeRef(PoiNodeRef poiNodeRef) {
        if (marker == null) {
            this.poiNodeRef = poiNodeRef;
        } else {
            marker.setPoiNodeRef(poiNodeRef);
        }
        return this;
    }

    @Override
    public WayMarkerOptions getThis() {
        return this;
    }

    @Override
    public WayMarker getMarker() {
        if (marker == null) {
            marker = new WayMarker(this);
            marker.setPoiNodeRef(poiNodeRef);
            marker.setPosition(position);
            marker.setSnippet(snippet);
            marker.setTitle(title);
            marker.setIcon(icon);
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
}
