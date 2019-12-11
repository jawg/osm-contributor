/**
 * Copyright (C) 2019 Takima
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
package io.jawg.osmcontributor.database.dao;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.j256.ormlite.stmt.SelectArg;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;

import javax.inject.Inject;

import io.jawg.osmcontributor.database.helper.DatabaseHelper;
import io.jawg.osmcontributor.model.entities.Note;
import io.jawg.osmcontributor.utils.Box;

/**
 * Dao for {@link io.jawg.osmcontributor.model.entities.Note} objects.
 */
public class NoteDao extends RuntimeExceptionDao<Note, Long> {

    @Inject
    public NoteDao(Dao<Note, Long> dao) {
        super(dao);
    }

    /**
     * Query for all the Notes contained in the bounds defined by the box.
     *
     * @param box Bounds of the search in latitude and longitude coordinates.
     * @return The notes contained in the box.
     */
    public List<Note> queryForAllInRect(final Box box) {
        return DatabaseHelper.wrapException(new Callable<List<Note>>() {
            @Override
            public List<Note> call() throws Exception {
                return queryBuilder()
                        .where().gt(Note.LATITUDE, new SelectArg(box.getSouth()))
                        .and().lt(Note.LATITUDE, new SelectArg(box.getNorth()))
                        .and().gt(Note.LONGITUDE, new SelectArg(box.getWest()))
                        .and().lt(Note.LONGITUDE, new SelectArg(box.getEast()))
                        .query();
            }
        });
    }

    /**
     * Query a Note with the given backend id.
     *
     * @param backendId The backend id.
     * @return The note.
     */
    public Note queryByBackendId(final String backendId) {
        List<Note> notes = DatabaseHelper.wrapException(new Callable<List<Note>>() {
            @Override
            public List<Note> call() throws Exception {
                return queryBuilder()
                        .where().eq(Note.BACKEND_ID, backendId)
                        .query();
            }
        });
        return notes.size() > 0 ? notes.get(0) : null;
    }

    /**
     * Query a List of Notes with one of the given backend ids.
     *
     * @param backendIds The Collection of backend ids.
     * @return The List of Notes.
     */
    public List<Note> queryByBackendIds(final Collection<String> backendIds) {
        return DatabaseHelper.wrapException(new Callable<List<Note>>() {
            @Override
            public List<Note> call() throws Exception {
                return queryBuilder()
                        .where().in(Note.BACKEND_ID, backendIds)
                        .query();
            }
        });
    }

    /**
     * Delete all the PoiNodeRefs in the database.
     */
    public void deleteAll() {
        DatabaseHelper.wrapException(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                deleteBuilder().delete();
                return null;
            }
        });
    }
}
