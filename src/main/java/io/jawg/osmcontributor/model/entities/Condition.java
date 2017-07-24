/**
 * Copyright (C) 2017 eBusiness Information
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
package io.jawg.osmcontributor.model.entities;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = Condition.TABLE_NAME)
public class Condition {

    public static final String TABLE_NAME = "POI_TYPE_CONSTRAINT_CONDITION";
    public static final String ID = "ID";
    public static final String TYPE = "TYPE";
    public static final String VALUE = "VALUE";

    public enum ConditionValue {
        EXISTS, EQUALS
    }

    public enum ExistsValues {
        TRUE, FALSE
    }

    @DatabaseField(columnName = ID, generatedId = true, canBeNull = false)
    private Long id;

    @DatabaseField(columnName = TYPE, canBeNull = false)
    private ConditionValue type;

    @DatabaseField(columnName = VALUE, canBeNull = false)
    private String value;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ConditionValue getType() {
        return type;
    }

    public void setType(ConditionValue type) {
        this.type = type;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "Condition{" +
                "id=" + id +
                ", type=" + type +
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

        Condition condition = (Condition) o;

        return id != null ? id.equals(condition.id) : condition.id == null;

    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (type != null ? type.hashCode() : 0);
        result = 31 * result + (value != null ? value.hashCode() : 0);
        return result;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final Condition condition = new Condition();

        public Builder setId(Long id) {
            this.condition.setId(id);
            return this;
        }

        public Builder setType(ConditionValue conditionValue) {
            this.condition.setType(conditionValue);
            return this;
        }

        public Builder setValue(String value) {
            this.condition.setValue(value);
            return this;
        }

        public Condition build() {
            return this.condition;
        }
    }
}
