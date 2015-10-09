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

package com.google.android.gms.analytics;

import java.util.HashMap;
import java.util.Map;

public class HitBuilders {

    public static class EventBuilder{
        public EventBuilder() {}

        public HitBuilders.EventBuilder setCategory(String category) {
            return this;
        }

        public HitBuilders.EventBuilder setAction(String action) {
            return this;
        }

        public Map<String, String> build() {
            return new HashMap<>();
        }
    }


    public static class ScreenViewBuilder{
        public ScreenViewBuilder() {}

        public Map<String, String> build() {
            return new HashMap<>();
        }
    }
}
