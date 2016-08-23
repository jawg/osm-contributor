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
package io.mapsquare.osmcontributor.sync;

import java.util.List;

import io.mapsquare.osmcontributor.model.entities.Comment;
import io.mapsquare.osmcontributor.model.entities.Note;
import io.mapsquare.osmcontributor.utils.Box;

/**
 * Empty implementation of {@link io.mapsquare.osmcontributor.sync.SyncNoteManager} for a PoiStorage.
 * <p/>
 * The PoiStorage backend does not support Notes.
 */
public class PoiStorageSyncNoteManager implements SyncNoteManager {

    /**
     * Empty implementation.
     * <p/>
     * {@inheritDoc}
     */
    @Override
    public List<Note> syncDownloadNotesInBox(Box box) {
        return null;
    }

    /**
     * Empty implementation.
     * <p/>
     * {@inheritDoc}
     */
    @Override
    public Note remoteAddComment(Comment comment) {
        return null;
    }
}
