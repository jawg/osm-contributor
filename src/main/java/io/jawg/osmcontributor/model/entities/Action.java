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

@DatabaseTable(tableName = Action.TABLE_NAME)
public class Action {

    public static final String TABLE_NAME = "POI_TYPE_CONSTRAINT_ACTION";
    public static final String ID = "ID";
    public static final String TYPE = "TYPE";
    public static final String KEY = "KEY";
    public static final String VALUE = "VALUE";

    public enum ActionValue {
        SET_TAG_VALUE
    }

    @DatabaseField(columnName = ID, generatedId = true, canBeNull = false)
    private Long id;

    @DatabaseField(columnName = TYPE, canBeNull = false)
    private ActionValue type;

    @DatabaseField(columnName = KEY, canBeNull = false)
    private String key;

    @DatabaseField(columnName = VALUE, canBeNull = false)
    private String value;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ActionValue getType() {
        return type;
    }

    public void setType(ActionValue type) {
        this.type = type;
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

    @Override
    public String toString() {
        return "Action{" +
                "id=" + id +
                ", type=" + type +
                ", key='" + key + '\'' +
                ", value='" + value + '\'' +
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

        Action action = (Action) o;

        if (id != null ? !id.equals(action.id) : action.id != null) {
            return false;
        }
        if (type != action.type) {
            return false;
        }
        if (key != null ? !key.equals(action.key) : action.key != null) {
            return false;
        }
        return value != null ? value.equals(action.value) : action.value == null;

    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (type != null ? type.hashCode() : 0);
        result = 31 * result + (key != null ? key.hashCode() : 0);
        result = 31 * result + (value != null ? value.hashCode() : 0);
        return result;
    }
}
