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
package io.mapsquare.osmcontributor.map.events;

import com.mapbox.mapboxsdk.geometry.LatLng;

import io.mapsquare.osmcontributor.core.model.PoiType;

public class PleaseCreateNoTagPoiEvent {
    private final PoiType poiType;
    private final LatLng latLng;
    private final Double level;

    public PleaseCreateNoTagPoiEvent(PoiType poiType, LatLng latLng, Double level) {
        this.poiType = poiType;
        this.latLng = latLng;
        this.level = level;
    }

    public PoiType getPoiType() {
        return poiType;
    }

    public LatLng getLatLng() {
        return latLng;
    }

    public Double getLevel() {
        return level;
    }
}
