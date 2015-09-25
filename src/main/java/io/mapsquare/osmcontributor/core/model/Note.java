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


import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.Collection;

@DatabaseTable(tableName = Note.TABLE_NAME)
public class Note {
    public static final String TABLE_NAME = "NOTE";

    public static final String ID = "ID";
    public static final String LONGITUDE = "LONGITUDE";
    public static final String LATITUDE = "LATITUDE";
    public static final String STATUS = "STATUS";
    public static final String TEXT = "TEXT";
    public static final String BACKEND_ID = "BACKEND_ID";
    public static final String CREATED_DATE = "CREATED_DATE";
    public static final String UPDATED = "UPDATED";

    //Status
    public static final String STATUS_OPEN = "open";
    public static final String STATUS_CLOSE = "closed";
    public static final String STATUS_SYNC = "Synchronizing";

    public enum State {
        OPEN, CLOSED, SELECTED, MOVING, SYNC
    }

    public static State computeState(Note note, boolean selected, boolean moving) {
        if (moving) {
            return State.MOVING;
        }

        if (selected) {
            return State.SELECTED;
        }

        if (note != null) {
            switch (note.getStatus()) {
                case Note.STATUS_OPEN:
                    return State.OPEN;

                case Note.STATUS_CLOSE:
                    return State.CLOSED;

                case Note.STATUS_SYNC:
                    return State.SYNC;
            }
        }
        return State.SYNC;
    }

    @DatabaseField(generatedId = true, columnName = ID)
    private Long id;

    @DatabaseField(columnName = BACKEND_ID)
    private String backendId;

    @DatabaseField(columnName = LONGITUDE, canBeNull = false)
    private Double longitude;

    @DatabaseField(columnName = LATITUDE, canBeNull = false)
    private Double latitude;

    @DatabaseField(columnName = TEXT)
    private String text;

    @DatabaseField(columnName = STATUS)
    private String status;

    @ForeignCollectionField
    private Collection<Comment> comments = new ArrayList<>();

    @DatabaseField(columnName = CREATED_DATE)
    private DateTime createdDate;

    @DatabaseField(columnName = UPDATED, canBeNull = false)
    private Boolean updated;

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

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Collection<Comment> getComments() {
        return comments;
    }

    public void setComments(Collection<Comment> comments) {
        this.comments = comments;
    }

    public DateTime getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(DateTime createdDate) {
        this.createdDate = createdDate;
    }

    public Boolean getUpdated() {
        return updated;
    }

    public void setUpdated(Boolean updated) {
        this.updated = updated;
    }
}

