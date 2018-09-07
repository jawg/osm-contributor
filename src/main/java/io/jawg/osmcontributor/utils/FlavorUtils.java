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
package io.jawg.osmcontributor.utils;

import java.util.Collection;

import io.jawg.osmcontributor.BuildConfig;
import io.jawg.osmcontributor.model.entities.PoiType;
import io.jawg.osmcontributor.model.entities.PoiTypeTag;

public class FlavorUtils {
    public static final String TEMPLATE = "template";
    public static final String POI_STORAGE = "poi_storage";
    public static final String STORE = "store";
    public static final String BUS = "bus";
    public static final String TAG_HIGHWAY_KEY = "highway";
    public static final String TAG_HIGHWAY_VAL = "bus_stop";
    public static final String TAG_PUBLIC_TRANSPORT_KEY = "public_transport";
    public static final String TAG_PUBLIC_TRANSPORT_VAL = "platform";


    public static boolean isTemplate() {
        return TEMPLATE.equals(BuildConfig.FLAVOR);
    }

    public static boolean isPoiStorage() {
        return POI_STORAGE.equals(BuildConfig.FLAVOR);
    }

    public static boolean isStore() {
        return STORE.equals(BuildConfig.FLAVOR);
    }

    public static boolean isBus() {
        return BUS.endsWith(BuildConfig.FLAVOR);
    }

    public static boolean hasFilter() {
        return BuildConfig.WITH_FILTER;
    }

    public static boolean isBus(PoiType poiTypeDto) {
        if (poiTypeDto == null) {
            return false;
        }
        Collection<PoiTypeTag> tags = poiTypeDto.getTags();
        if (tags == null || tags.isEmpty()) {
            return false;
        }
        boolean highway = false;
        boolean transport = false;
        for (PoiTypeTag t : tags) {
            if (TAG_HIGHWAY_KEY.equals(t.getKey()) && TAG_HIGHWAY_VAL.equals(t.getValue())) {
                highway = true;
            }
            if (TAG_PUBLIC_TRANSPORT_KEY.equals(t.getKey()) && TAG_PUBLIC_TRANSPORT_VAL.equals(t.getValue())) {
                transport = true;
            }
        }
        return highway && transport;
    }
}
