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
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = RelationMember.TABLE_NAME)
public class RelationMember {
    public static final String TABLE_NAME = "RELATION_MEMBER";

    public static final String ID = "ID";
    public static final String TYPE = "TYPE";
    public static final String REF = "REF";
    public static final String ROLE = "ROLE";
    public static final String RELATION_ID = "RELATION_ID";

    @DatabaseField(columnName = ID, generatedId = true, canBeNull = false)
    private Long id;

    @DatabaseField(columnName = TYPE, canBeNull = false)
    private String type;

    @DatabaseField(columnName = REF, canBeNull = false)
    private Long ref;

    @DatabaseField(columnName = ROLE)
    private String role;

    @DatabaseField(foreign = true, columnName = RELATION_ID, canBeNull = false)
    private FullOSMRelation fullOSMRelation;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Long getRef() {
        return ref;
    }

    public void setRef(Long ref) {
        this.ref = ref;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public FullOSMRelation getFullOSMRelation() {
        return fullOSMRelation;
    }

    public void setFullOSMRelation(FullOSMRelation fullOSMRelation) {
        this.fullOSMRelation = fullOSMRelation;
    }

    public static RelationMember createBusStop(Long ref) {
        RelationMember add = new RelationMember();
        add.setRef(ref);
        add.setRole("platform");
        add.setType("node");
        return add;
    }

}

