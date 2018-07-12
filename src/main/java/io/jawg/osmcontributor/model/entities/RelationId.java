package io.jawg.osmcontributor.model.entities;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = RelationId.TABLE_NAME)
public class RelationId {

    public static final String TABLE_NAME = "RELATION_ID";
    public static final String ID = "ID";
    public static final String POI_ID = "POI_ID";
    private static final String RELATION_ID = "RELATION_ID";


    @DatabaseField(columnName = ID, generatedId = true, canBeNull = false)
    private Long id;

    @DatabaseField(columnName = POI_ID, foreign = true)
    private Poi poi;

    @DatabaseField(columnName = RELATION_ID, canBeNull = false)
    private String relationId;

    public RelationId() {
    }

    public RelationId(String relationId, Poi poi) {
        this.relationId = relationId;
        this.poi = poi;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getBackendRelationId() {
        return relationId;
    }

    public void setRelationId(String relationId) {
        this.relationId = relationId;
    }

    public Poi getPoi() {
        return poi;
    }

    public void setPoi(Poi poi) {
        this.poi = poi;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        RelationId that = (RelationId) o;

        return relationId != null && that.getBackendRelationId() != null && relationId.equals(that.getBackendRelationId());

    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}
