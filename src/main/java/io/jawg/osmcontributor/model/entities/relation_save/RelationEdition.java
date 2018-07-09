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
package io.jawg.osmcontributor.model.entities.relation_save;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import io.jawg.osmcontributor.model.entities.Poi;

/**
 * The model is used to save changes made to the relations containing a poi
 */
@DatabaseTable(tableName = RelationEdition.TABLE_NAME)
public class RelationEdition {

    public enum RelationModificationType {
        ADD_MEMBER, REMOVE_MEMBER
    }

    public static final String TABLE_NAME = "RELATION-SAVE";

    public static final String ID = "ID";
    public static final String BACKEND_ID = "BACKEND_ID";
    public static final String POI_ID = "POI_ID";
    public static final String MODIFICATION = "MODIFICATION";

    public RelationEdition(){
        //empty
    }

    /**
     * This class represents a change for a poi
     * This contains the id of the relation to be changed
     * And the type of change
     *
     * @param relationId the id of the relation
     * @param poi        the poi associated with the change
     * @param change     the change to be made
     */
    public RelationEdition(String relationId, Poi poi, RelationModificationType change) {
        this.backendId = relationId;
        this.poi = poi;
        this.change = change;
    }

    @DatabaseField(generatedId = true, columnName = ID)
    private Long id;

    @DatabaseField(columnName = BACKEND_ID)
    private String backendId;

    @DatabaseField(foreign = true, columnName = POI_ID, canBeNull = false)
    private Poi poi;

    @DatabaseField(columnName = MODIFICATION)
    private RelationModificationType change;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getBackendId() {
        return backendId;
    }

    public void setBackendId(String backendId) {
        this.backendId = backendId;
    }

    public Poi getPoi() {
        return poi;
    }

    public void setPoi(Poi poi) {
        this.poi = poi;
    }

    public RelationModificationType getChange() {
        return change;
    }

    public void setChange(RelationModificationType change) {
        this.change = change;
    }
}

