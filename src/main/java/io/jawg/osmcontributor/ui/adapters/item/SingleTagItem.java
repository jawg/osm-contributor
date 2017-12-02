/**
 * Copyright (C) 2016 eBusiness Information
 * <p>
 * This file is part of OSM Contributor.
 * <p>
 * OSM Contributor is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * OSM Contributor is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with OSM Contributor.  If not, see <http://www.gnu.org/licenses/>.
 */
package io.jawg.osmcontributor.ui.adapters.item;

import java.util.HashMap;
import java.util.Map;

public class SingleTagItem extends TagItem {

    private SingleTagItem(SingleTagItemBuilder builder) {
        super(builder);
    }

    public static class SingleTagItemBuilder extends TagItemBuilder {

        public SingleTagItemBuilder(String key, String value) {
            super(key, value);
        }

        @Override
        public SingleTagItem build() {
            return new SingleTagItem(this);
        }
    }

    @Override
    public Map<String, String> getOsmValues() {
        HashMap<String, String> stringStringHashMap = new HashMap<>();
        stringStringHashMap.put(key, value);
        return stringStringHashMap;
    }
}
