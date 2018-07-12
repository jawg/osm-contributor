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

public class RelationMember {

    private Long id;

    private String type;

    private Long ref;

    private String role;

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


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        return ref.equals(((RelationMember) o).getRef());
    }

}

