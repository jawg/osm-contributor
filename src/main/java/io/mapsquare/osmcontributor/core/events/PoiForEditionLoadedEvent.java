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

import java.util.List;
import java.util.Map;

import io.mapsquare.osmcontributor.core.model.Poi;

public class PoiForEditionLoadedEvent {

    private final Poi poi;
    private final Map<String, List<String>> valuesMap;

    public PoiForEditionLoadedEvent(Poi poi, Map<String, List<String>> valuesMap) {
        this.poi = poi;
        this.valuesMap = valuesMap;
    }

    public Poi getPoi() {
        return poi;
    }

    public Map<String, List<String>> getValuesMap() {
        return valuesMap;
    }
}
