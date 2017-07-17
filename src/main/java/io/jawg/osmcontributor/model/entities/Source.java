/**
 * Copyright (C) 2017 eBusiness Information
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
package io.jawg.osmcontributor.model.entities;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = Source.TABLE_NAME)
public class Source {
    public static final String TABLE_NAME = "POI_TYPE_CONSTRAINT_SOURCE";
    public static final String ID = "ID";
    public static final String TYPE = "TYPE";
    public static final String KEY = "KEY";

    public enum SourceValue {
        TAG
    }

    @DatabaseField(columnName = ID, generatedId = true, canBeNull = false)
    private Long id;

    @DatabaseField(columnName = TYPE, canBeNull = false)
    private SourceValue type;

    @DatabaseField(columnName = KEY, canBeNull = false)
    private String key;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public SourceValue getType() {
        return type;
    }

    public void setType(SourceValue type) {
        this.type = type;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    @Override
    public String toString() {
        return "Source{" +
                "id=" + id +
                ", type=" + type +
                ", key='" + key + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Source source = (Source) o;

        if (id != null ? !id.equals(source.id) : source.id != null) {
            return false;
        }
        if (type != source.type) {
            return false;
        }
        return key != null ? key.equals(source.key) : source.key == null;

    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (type != null ? type.hashCode() : 0);
        result = 31 * result + (key != null ? key.hashCode() : 0);
        return result;
    }
}
