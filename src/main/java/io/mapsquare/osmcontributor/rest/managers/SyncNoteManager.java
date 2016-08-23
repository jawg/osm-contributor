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
package io.mapsquare.osmcontributor.rest.managers;

import java.util.List;

import io.mapsquare.osmcontributor.model.entities.Comment;
import io.mapsquare.osmcontributor.model.entities.Note;
import io.mapsquare.osmcontributor.utils.Box;

/**
 * Manage the synchronization of Notes between the backend and the application.
 */
public interface SyncNoteManager {

    /**
     * Download from backend the list of Notes contained in the box.
     * Update the database with the obtained list.
     *
     * @param box The Box to synchronize with the database.
     */
    List<Note> syncDownloadNotesInBox(final Box box);

    /**
     * Send a new Comment to the backend.
     *
     * @param comment The comment to send to the backend.
     * @return The resulting Note, and null if there was an error.
     */
    Note remoteAddComment(final Comment comment);
}
