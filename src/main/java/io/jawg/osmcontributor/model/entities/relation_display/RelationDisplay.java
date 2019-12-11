/**
 * Copyright (C) 2019 Takima
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
package io.jawg.osmcontributor.model.entities.relation_display;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;

import java.util.ArrayList;
import java.util.Collection;

@DatabaseTable(tableName = RelationDisplay.TABLE_NAME)
public class RelationDisplay {

    public static final String TABLE_NAME = "RELATION_DISPLAY";

    public static final String ID = "ID";
    public static final String BACKEND_ID = "BACKEND_ID";
    public static final String VERSION = "VERSION";
    public static final String RELATION_DISPLAY_TAGS = "RELATION_DISPLAY_TAGS";

    @DatabaseField(generatedId = true, columnName = ID)
    private Long id;

    @DatabaseField(columnName = BACKEND_ID)
    private String backendId;

    @ForeignCollectionField(columnName = RELATION_DISPLAY_TAGS)
    private Collection<RelationDisplayTag> tags = new ArrayList<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Collection<RelationDisplayTag> getTags() {
        return tags;
    }

    public void setTags(Collection<RelationDisplayTag> tags) {
        this.tags = tags;
    }

    public String getBackendId() {
        return backendId;
    }

    public void setBackendId(String backendId) {
        this.backendId = backendId;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        return backendId.equals(((RelationDisplay) o).getBackendId());
    }

    @Override
    public int hashCode() {
        return backendId != null ? backendId.hashCode() : super.hashCode();
    }


    @Override
    public String toString() {
        return "RelationDisplay{" +
                "id=" + id +
                ", backendId='" + backendId + '\'' +
                '}';
    }
}

