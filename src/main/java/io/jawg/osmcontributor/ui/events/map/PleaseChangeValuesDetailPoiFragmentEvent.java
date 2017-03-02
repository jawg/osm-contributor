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
package io.jawg.osmcontributor.ui.events.map;

import io.jawg.osmcontributor.model.entities.Poi;

public class PleaseChangeValuesDetailPoiFragmentEvent {
    private final String poiType;
    private final String poiName;
    private final boolean isWay;
    private Poi poi;

    public PleaseChangeValuesDetailPoiFragmentEvent(Poi poi) {
        this.poiType = poi.getType().getName();
        this.poiName = poi.getName();
        this.isWay = poi.getWay();
        this.poi = poi;
    }

    public String getPoiType() {
        return poiType;
    }

    public String getPoiName() {
        return poiName;
    }

    public boolean isWay() {
        return isWay;
    }

    public Poi getPoi() {
        return poi;
    }
}
