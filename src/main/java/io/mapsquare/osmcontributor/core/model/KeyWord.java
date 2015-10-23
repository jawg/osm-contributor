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
package io.mapsquare.osmcontributor.core.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = KeyWord.TABLE_NAME)
public class KeyWord {

    public static final String TABLE_NAME = "KEYWORD";
    public static final String POI_TYPE_ID = "POI_TYPE_ID";
    public static final String VALUE = "VALUE";
    public static final String ID = "ID";

    @DatabaseField(columnName = ID, generatedId = true, canBeNull = false)
    private Long id;

    @DatabaseField(columnName = VALUE, canBeNull = false)
    private String value;

    @DatabaseField(foreign = true, columnName = POI_TYPE_ID, canBeNull = false)
    private PoiType poiType;

    public KeyWord() {
    }

    public KeyWord(String value, PoiType poiType) {
        this.value = value;
        this.poiType = poiType;
    }

    @Override
    public String toString() {
        return "KeyWord{" +
                "id=" + id +
                ", value='" + value +
                '}';
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public PoiType getPoiType() {
        return poiType;
    }

    public void setPoiType(PoiType poiType) {
        this.poiType = poiType;
    }
}
