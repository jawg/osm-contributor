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
}
