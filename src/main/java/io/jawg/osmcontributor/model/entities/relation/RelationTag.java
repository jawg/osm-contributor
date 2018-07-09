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


package io.jawg.osmcontributor.model.entities.relation;


import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = RelationTag.TABLE_NAME)
public class RelationTag {
    public static final String TABLE_NAME = "RELATION_TAG";

    public static final String ID = "ID";
    public static final String KEY = "KEY";
    public static final String VALUE = "VALUE";
    public static final String RELATION_ID = "RELATION_ID";

    @DatabaseField(columnName = ID, generatedId = true, canBeNull = false)
    private Long id;

    @DatabaseField(columnName = KEY, canBeNull = false)
    private String key;

    @DatabaseField(columnName = VALUE)
    private String value;

    @DatabaseField(foreign = true, columnName = RELATION_ID, canBeNull = false)
    private FullOSMRelation fullOSMRelation;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public FullOSMRelation getRelationDisplay() {
        return fullOSMRelation;
    }

    public void setRelationDisplay(FullOSMRelation fullOSMRelation) {
        this.fullOSMRelation = fullOSMRelation;
    }

}


