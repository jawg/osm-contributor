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
package io.mapsquare.osmcontributor.core.database.dao;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.j256.ormlite.stmt.DeleteBuilder;

import java.util.List;
import java.util.concurrent.Callable;

import javax.inject.Inject;

import io.mapsquare.osmcontributor.core.database.DatabaseHelper;
import io.mapsquare.osmcontributor.core.model.Comment;

/**
 * Dao for {@link io.mapsquare.osmcontributor.core.model.Comment} objects.
 */
public class CommentDao extends RuntimeExceptionDao<Comment, Long> {

    @Inject
    public CommentDao(Dao<Comment, Long> dao) {
        super(dao);
    }

    /**
     * Delete comments associated to a Note and with the given updated value.
     *
     * @param noteId  Id of the Note.
     * @param updated Delete the comment with status updated or not.
     * @return The number of comments deleted
     */
    public Integer deleteByNoteIdAndUpdated(final Long noteId, final boolean updated) {
        if (noteId == null) {
            return 0;
        }
        return DatabaseHelper.wrapException(new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                DeleteBuilder<Comment, Long> db = deleteBuilder();
                db.where().eq(Comment.NOTE_ID, noteId)
                        .and().eq(Comment.UPDATED, updated);
                return db.delete();
            }
        });
    }

    /**
     * Query for all new Comments.
     *
     * @return List of new comments found.
     */
    public List<Comment> queryForAllNew() {
        return queryForEq(Comment.UPDATED, true);
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
