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


import android.support.annotation.NonNull;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = PoiTypeTag.TABLE_NAME)
public class PoiTypeTag implements Comparable<PoiTypeTag> {
    public static final String TABLE_NAME = "POI_TYPE_TAG";

    public static final String ID = "ID";
    public static final String KEY = "KEY";
    public static final String VALUE = "VALUE";
    public static final String POI_TYPE_ID = "POI_TYPE_ID";
    public static final String MANDATORY = "MANDATORY";
    public static final String ORDINAL = "ORDINAL";

    @DatabaseField(columnName = ID, generatedId = true, canBeNull = false)
    private Long id;

    @DatabaseField(columnName = KEY, canBeNull = false)
    private String key;

    @DatabaseField(columnName = VALUE)
    private String value;

    @DatabaseField(columnName = ORDINAL, canBeNull = false)
    private Integer ordinal;

    @DatabaseField(columnName = MANDATORY, canBeNull = false)
    private Boolean mandatory;

    @DatabaseField(foreign = true, columnName = POI_TYPE_ID, canBeNull = false)
    private PoiType poiType;

    @DatabaseField(columnName = "POSSIBLE_VALUES")
    private String possibleValues;

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

    public Integer getOrdinal() {
        return ordinal;
    }

    public void setOrdinal(Integer ordinal) {
        this.ordinal = ordinal;
    }

    public Boolean getMandatory() {
        return mandatory;
    }

    public void setMandatory(Boolean mandatory) {
        this.mandatory = mandatory;
    }

    public PoiType getPoiType() {
        return poiType;
    }

    public void setPoiType(PoiType poiType) {
        this.poiType = poiType;
    }

    public String getPossibleValues() {
        return possibleValues;
    }

    public void setPossibleValues(String possibleValues) {
        this.possibleValues = possibleValues;
    }

    @Override
    public String toString() {
        return "PoiTypeTag{" +
                "id=" + id +
                ", key='" + key + '\'' +
                ", value='" + value + '\'' +
                ", ordinal=" + ordinal +
                ", mandatory=" + mandatory +
                ", poiType=" + (poiType == null ? null : poiType.getId()) +
                ", possibleValues=" + possibleValues +
                '}';
    }

    @Override
    public int compareTo(@NonNull PoiTypeTag another) {
        return ordinal - another.ordinal;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        PoiTypeTag that = (PoiTypeTag) o;

        return !(id != null ? !id.equals(that.id) : that.id != null);

    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }


    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final PoiTypeTag poiTypeTag;

        public Builder() {
            this.poiTypeTag = new PoiTypeTag();
        }

        public Builder id(Long id) {
            this.poiTypeTag.id = id;
            return this;
        }

        public Builder key(String key) {
            this.poiTypeTag.key = key;
            return this;
        }

        public Builder value(String value) {
            this.poiTypeTag.value = value;
            return this;
        }

        public Builder ordinal(Integer ordinal) {
            this.poiTypeTag.ordinal = ordinal;
            return this;
        }

        public Builder mandatory(Boolean mandatory) {
            this.poiTypeTag.mandatory = mandatory;
            return this;
        }

        public Builder poiType(PoiType poiType) {
            this.poiTypeTag.poiType = poiType;
            return this;
        }

        public PoiTypeTag build() {
            return this.poiTypeTag;
        }
    }
}
