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

package io.jawg.osmcontributor.model.entities.relation;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.Collection;

public class FullOSMRelation {

    private Long id;

    private String backendId;

    private String version = "1";

    private String name;

    private DateTime updateDate;

    private String changeset;

    private Collection<RelationMember> members = new ArrayList<>();

    private Collection<RelationTag> tags = new ArrayList<>();

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

