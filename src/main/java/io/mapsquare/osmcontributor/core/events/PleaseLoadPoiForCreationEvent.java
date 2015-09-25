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
package io.mapsquare.osmcontributor.core.events;

public class PleaseLoadPoiForCreationEvent {
    private final double lat;
    private final double lng;
    private final long poiType;
    private final double level;

    public PleaseLoadPoiForCreationEvent(double lat, double lng, long poiType, double level) {
        this.lat = lat;
        this.lng = lng;
        this.poiType = poiType;
        this.level = level;
    }

    public double getLat() {
        return lat;
    }

    public double getLng() {
        return lng;
    }

    public long getPoiType() {
        return poiType;
    }

    public double getLevel() {
        return level;
    }
}
