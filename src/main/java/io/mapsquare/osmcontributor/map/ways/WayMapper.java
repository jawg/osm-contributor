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
package io.mapsquare.osmcontributor.map.ways;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import io.mapsquare.osmcontributor.core.model.Poi;
import io.mapsquare.osmcontributor.core.model.PoiNodeRef;

public class WayMapper {

    static Set<Way> poisToWays(List<Poi> pois) {
        Set<Way> ways = new HashSet<>();
        if (pois != null && !pois.isEmpty()) {
            for (Poi poi : pois) {
                Way way = new Way(poi);
                for (PoiNodeRef nodeRef : poi.getNodeRefs()) {
                    // Properties of a node in way edition
                    way.add(nodeRef);
                }
                ways.add(way);
            }
        }
        return ways;
    }
}
