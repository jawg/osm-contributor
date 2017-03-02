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
package io.jawg.osmcontributor.ui.events.type;

public class PleaseSavePoiTag {

    private final Long id;
    private final String key;
    private final String value;
    private final boolean mandatory;

    private PleaseSavePoiTag(Long id, String key, String value, boolean mandatory) {
        this.id = id;
        this.key = key;
        this.value = value;
        this.mandatory = mandatory;
    }

    public Long getId() {
        return id;
    }

    public String getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }

    public boolean isMandatory() {
        return mandatory;
    }

    public static PleaseSavePoiTag newTag(String key, String value, boolean mandatory) {
        return new PleaseSavePoiTag(null, key, value, mandatory);
    }

    public static PleaseSavePoiTag editTag(Long id, String key, String value, boolean mandatory) {
        return new PleaseSavePoiTag(id, key, value, mandatory);
    }
}
