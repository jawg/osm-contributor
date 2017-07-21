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

@DatabaseTable(tableName = Constraint.TABLE_NAME)
public class Constraint {
    public static final String TABLE_NAME = "POI_TYPE_CONSTRAINT";
    public static final String ID = "ID";
    public static final String SOURCE = "SOURCE";
    public static final String CONDITION = "CONDITION";
    public static final String ACTION = "ACTION";
    public static final String POI_TYPE_ID = "POI_TYPE_ID";
    public static final String ORDINAL = "ORDINAL";

    @DatabaseField(columnName = ID, generatedId = true, canBeNull = false)
    private Long id;

    @DatabaseField(columnName = SOURCE, canBeNull = false, foreign = true, foreignAutoRefresh = true)
    private Source source;

    @DatabaseField(columnName = CONDITION, canBeNull = false, foreign = true, foreignAutoRefresh = true)
    private Condition condition;

    @DatabaseField(columnName = ACTION, canBeNull = false, foreign = true, foreignAutoRefresh = true)
    private Action action;

    @DatabaseField(columnName = POI_TYPE_ID, canBeNull = false, foreign = true)
    private PoiType poiType;

    @DatabaseField(columnName = ORDINAL, canBeNull = false)
    private Integer ordinal;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Source getSource() {
        return source;
    }

    public void setSource(Source source) {
        this.source = source;
    }

    public Condition getCondition() {
        return condition;
    }

    public void setCondition(Condition condition) {
        this.condition = condition;
    }

    public Action getAction() {
        return action;
    }

    public void setAction(Action action) {
        this.action = action;
    }

    public PoiType getPoiType() {
        return poiType;
    }

    public void setPoiType(PoiType poiType) {
        this.poiType = poiType;
    }

    public Integer getOrdinal() {
        return ordinal;
    }

    public void setOrdinal(Integer ordinal) {
        this.ordinal = ordinal;
    }

    @Override
    public String toString() {
        return "Constraint{" +
                "id=" + id +
                ", source=" + source +
                ", condition=" + condition +
                ", action=" + action +
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

        Constraint that = (Constraint) o;

        if (id != null ? !id.equals(that.id) : that.id != null) {
            return false;
        }
        if (source != null ? !source.equals(that.source) : that.source != null) {
            return false;
        }
        if (condition != null ? !condition.equals(that.condition) : that.condition != null) {
            return false;
        }
        return action != null ? action.equals(that.action) : that.action == null;

    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (source != null ? source.hashCode() : 0);
        result = 31 * result + (condition != null ? condition.hashCode() : 0);
        result = 31 * result + (action != null ? action.hashCode() : 0);
        return result;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final Constraint constraint = new Constraint();

        public Builder setId(Long id) {
            this.constraint.setId(id);
            return this;
        }

        public Builder setSource(Source source) {
            this.constraint.setSource(source);
            return this;
        }

        public Builder setCondition(Condition condition) {
            this.constraint.setCondition(condition);
            return this;
        }

        public Builder setAction(Action action) {
            this.constraint.setAction(action);
            return this;
        }

        public Builder setPoiType(PoiType poiType) {
            this.constraint.setPoiType(poiType);
            return this;
        }

        public Builder setOrdinal(Integer ordinal) {
            this.constraint.setOrdinal(ordinal);
            return this;
        }

        public Constraint build() {
            return this.constraint;
        }
    }
}
