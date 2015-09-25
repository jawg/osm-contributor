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
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;

import java.util.Collection;

@DatabaseTable(tableName = PoiType.POI_TYPE)
public class PoiType implements Comparable<PoiType> {
    public static final String POI_TYPE = "POI_TYPE";

    public static final String ID = "ID";
    public static final String NAME = "NAME";
    public static final String ICON = "ICON";
    public static final String BACKEND_ID = "BACKEND_ID";

    @DatabaseField(columnName = ID, generatedId = true, canBeNull = false)
    private Long id;

    @DatabaseField(columnName = NAME)
    private String name;

    @DatabaseField(columnName = ICON)
    private String icon;

    @DatabaseField(columnName = BACKEND_ID)
    private String backendId;

    @ForeignCollectionField(orderColumnName = PoiTypeTag.ORDINAL)
    private Collection<PoiTypeTag> tags;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    @Override
    public String toString() {
        return "PoiType{" +
                "backendId='" + backendId + '\'' +
                ", id=" + id +
                ", name='" + name + '\'' +
                ", icon='" + icon + '\'' +
                ", tags=" + tags +
                '}';
    }

    @Override
    public int compareTo(@NonNull PoiType o) {
        if (backendId != null && o.backendId != null) {
            return backendId.compareTo(o.backendId);
        }
        return name.compareTo(o.getName());
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
}
