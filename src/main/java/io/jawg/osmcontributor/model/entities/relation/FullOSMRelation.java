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
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.Collection;

@DatabaseTable(tableName = FullOSMRelation.TABLE_NAME)
public class FullOSMRelation {

    public static final String TABLE_NAME = "RELATION";

    public static final String ID = "ID";
    public static final String CHANGESET = "CHANGESET";
    public static final String BACKEND_ID = "BACKEND_ID";
    public static final String NAME = "NAME";
    public static final String VERSION = "VERSION";
    public static final String UPDATE_DATE = "UPDATE_DATE";
    public static final String UPDATED = "UPDATED";
    public static final String RELATION_CENTER = "RELATION_CENTER";
    public static final String RELATION_MEMBERS = "RELATION_MEMBERS";
    public static final String RELATION_TAGS = "RELATION_TAGS";


    /**
     * Common attributes of a relation
     */

    @DatabaseField(generatedId = true, columnName = ID)
    private Long id;

    @DatabaseField(columnName = BACKEND_ID)
    private String backendId;

    @DatabaseField(columnName = VERSION)
    private String version = "1";

    @DatabaseField(columnName = NAME)
    private String name;

    @DatabaseField(columnName = UPDATE_DATE)
    private DateTime updateDate;

    @DatabaseField(columnName = CHANGESET)
    private String changeset;

    @ForeignCollectionField(columnName = RELATION_MEMBERS)
    private Collection<RelationMember> members = new ArrayList<>();

    @ForeignCollectionField(columnName = RELATION_TAGS)
    private Collection<RelationTag> tags = new ArrayList<>();

    @DatabaseField(columnName = UPDATED, canBeNull = false)
    private Boolean updated;

    @Override
    public String toString() {
        return "FullOSMRelation{" +
                "id=" + id + " ...}";
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public DateTime getUpdateDate() {
        return updateDate;
    }

    public void setUpdateDate(DateTime updateDate) {
        this.updateDate = updateDate;
    }

    public String getChangeset() {
        return changeset;
    }

    public void setChangeset(String changeset) {
        this.changeset = changeset;
    }


    public Collection<RelationMember> getMembers() {
        return members;
    }

    public void setMembers(Collection<RelationMember> members) {
        this.members = members;
    }

    public Collection<RelationTag> getTags() {
        return tags;
    }

    public void setTags(Collection<RelationTag> tags) {
        this.tags = tags;
    }

    public String getBackendId() {
        return backendId;
    }

    public void setBackendId(String backendId) {
        this.backendId = backendId;
    }

    public Boolean getUpdated() {
        return updated;
    }

    public void setUpdated(Boolean updated) {
        this.updated = updated;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


}

