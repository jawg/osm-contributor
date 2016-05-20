/**
 * Copyright (C) 2016 eBusiness Information
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
import com.j256.ormlite.table.DatabaseTable;

import org.joda.time.DateTime;

@DatabaseTable(tableName = Comment.TABLE_NAME)
public class Comment implements Comparable<Comment> {
    public static final String TABLE_NAME = "COMMENT";

    public static final String ID = "ID";
    public static final String ACTION = "ACTION";
    public static final String TEXT = "TEXT";
    public static final String BACKEND_ID = "BACKEND_ID";
    public static final String CREATED_DATE = "CREATED_DATE";
    public static final String UPDATED = "UPDATED";
    public static final String NOTE_ID = "NOTE_ID";

    public static final String ACTION_CLOSE = "closed";
    public static final String ACTION_OPEN = "opened";
    public static final String ACTION_REOPEN = "reopened";
    public static final String ACTION_COMMENT = "commented";

    @DatabaseField(generatedId = true, columnName = ID)
    private Long id;

    @DatabaseField(foreign = true, columnName = NOTE_ID, canBeNull = false)
    private Note note;

    @DatabaseField(columnName = TEXT)
    private String text;

    @DatabaseField(columnName = ACTION)
    private String action;

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

    public Note getNote() {
        return note;
    }

    public void setNote(Note note) {
        this.note = note;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
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

    @Override
    public int compareTo(Comment comment) {
        if (comment.getCreatedDate() == null) {
            return 1;
        }
        if (createdDate == null) {
            return -1;
        }
        return createdDate.compareTo(comment.getCreatedDate());
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}
