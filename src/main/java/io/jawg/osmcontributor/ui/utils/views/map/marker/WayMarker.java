/**
 * Copyright (C) 2019 Takima
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

import com.mapbox.mapboxsdk.annotations.BaseMarkerOptions;
import com.mapbox.mapboxsdk.annotations.Marker;

import io.jawg.osmcontributor.model.entities.PoiNodeRef;

/**
 * @author Tommy Buonomo on 20/06/16.
 */
public class WayMarker extends Marker {
    private PoiNodeRef poiNodeRef;

    WayMarker(BaseMarkerOptions markerOptions) {
        super(markerOptions);
    }

    @Override
    public int hashCode() {
        return poiNodeRef != null ? poiNodeRef.hashCode() : super.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }

        if (this == o) {
            return true;
        }

        if (!(o instanceof LocationMarkerView)) {
            return false;
        }

        LocationMarkerView marker = (LocationMarkerView) o;
        Object itsRelated = marker.getRelatedObject();

        return poiNodeRef == null ? itsRelated == null : poiNodeRef.equals(itsRelated);
    }

    public PoiNodeRef getPoiNodeRef() {
        return poiNodeRef;
    }

    public void setPoiNodeRef(PoiNodeRef poiNodeRef) {
        this.poiNodeRef = poiNodeRef;
    }
}
