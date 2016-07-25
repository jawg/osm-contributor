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
package io.mapsquare.osmcontributor.flickr.util;

import java.util.Map;
import java.util.TreeMap;

public class MapParams<K, V> {

    private Map<K, V> params;

    public MapParams() {
        params = new TreeMap<>();
    }

    public MapParams<K, V> put(K key, V value) {
        params.put(key, value);
        return this;
    }

    public MapParams<K, V> put(Map<K, V> map) {
        params.putAll(map);
        return this;
    }

    public Map<K, V> toMap() {
        return params;
    }
}
