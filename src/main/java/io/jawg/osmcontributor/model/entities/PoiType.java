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
package io.jawg.osmcontributor.model.entities;

import android.support.annotation.NonNull;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;

import org.joda.time.DateTime;

import java.util.Collection;

@DatabaseTable(tableName = PoiType.POI_TYPE)
public class PoiType implements Comparable<PoiType> {
    public static final String POI_TYPE = "POI_TYPE";

    public static final String ID = "ID";
    public static final String ICON = "ICON";
    public static final String BACKEND_ID = "BACKEND_ID";
    public static final String EN = "en";
    public static final String NAME = "NAME";
    public static final String TECHNICAL_NAME = "TECHNICAL_NAME";
    public static final String DESCRIPTION = "DESCRIPTION";
    public static final String USAGE_COUNT = "USAGE_COUNT";
    public static final String LAST_USE = "LAST_USE";
    public static final String KEYWORDS = "KEYWORDS";
    public static final String QUERY = "QUERY";

    @DatabaseField(columnName = ID, generatedId = true, canBeNull = false)
    private Long id;

    @DatabaseField(columnName = ICON)
    private String icon;

    @DatabaseField(columnName = USAGE_COUNT)
    private int usageCount;

    @DatabaseField(columnName = BACKEND_ID)
    private String backendId;

    @ForeignCollectionField(orderColumnName = PoiTypeTag.ORDINAL, eager = true)
    private Collection<PoiTypeTag> tags;

    @DatabaseField(columnName = DESCRIPTION)
    private String description;

    @DatabaseField(columnName = NAME, canBeNull = false)
    private String name;

    @DatabaseField(columnName = TECHNICAL_NAME, canBeNull = false)
    private String technicalName;

    @DatabaseField(columnName = LAST_USE, canBeNull = false)
    private DateTime lastUse;

    @DatabaseField(columnName = KEYWORDS)
    private String keyWords;

    @DatabaseField(columnName = QUERY)
    private String query;

    @ForeignCollectionField(orderColumnName = Constraint.ORDINAL)
    private Collection<Constraint> constraints;

    public int getUsageCount() {
        return usageCount;
    }

    public void setUsageCount(int usageCount) {
        this.usageCount = usageCount;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getBackendId() {
        return backendId;
    }

    public void setBackendId(String backendId) {
        this.backendId = backendId;
    }

    public Collection<PoiTypeTag> getTags() {
        return tags;
    }

    public void setTags(Collection<PoiTypeTag> tags) {
        this.tags = tags;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public String getTechnicalName() {
        return technicalName;
    }

    public void setTechnicalName(String technicalName) {
        this.technicalName = technicalName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setKeyWords(String keyWords) {
        this.keyWords = keyWords;
    }

    public String getKeyWords() {
        return keyWords;
    }

    public DateTime getLastUse() {
        return lastUse;
    }

    public void setLastUse(DateTime lastUse) {
        this.lastUse = lastUse;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public Collection<Constraint> getConstraints() {
        return constraints;
    }

    public void setConstraints(Collection<Constraint> constraints) {
        this.constraints = constraints;
    }

    @Override
    public String toString() {
        return "PoiType{" +
                "id=" + id +
                ", icon='" + icon + '\'' +
                ", usageCount=" + usageCount +
                ", backendId='" + backendId + '\'' +
                ", tags=" + tags +
                ", description='" + description + '\'' +
                ", name='" + name + '\'' +
                ", technicalName='" + technicalName + '\'' +
                ", lastUse=" + lastUse +
                ", keyWords=" + keyWords +
                ", constraints=" + constraints +
                '}';
    }

    @Override
    public int compareTo(@NonNull PoiType o) {
        int result = 0;
        if (name != null && o.name != null) {
            result = name.compareToIgnoreCase(o.name);
        }
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        PoiType poiType = (PoiType) o;

        if (id != null ? !id.equals(poiType.id) : poiType.id != null) {
            return false;
        }
        return !(backendId != null ? !backendId.equals(poiType.backendId) : poiType.backendId != null);

    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (backendId != null ? backendId.hashCode() : 0);
        return result;
    }

    /**
     * Return true if the PoiType has at least one mandatory tag.
     *
     * @return true if the PoiType has at least one mandatory tag.
     */
    public boolean hasMandatoryTags() {
        if (tags != null) {
            for (PoiTypeTag poiTypeTag : tags) {
                if (poiTypeTag.getMandatory()) {
                    return true;
                }
            }
        }
        return false;
    }
}
