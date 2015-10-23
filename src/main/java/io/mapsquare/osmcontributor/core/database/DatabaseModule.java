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
package io.mapsquare.osmcontributor.core.database;


import android.app.Application;

import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.Dao;

import java.sql.SQLException;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import io.mapsquare.osmcontributor.core.model.Comment;
import io.mapsquare.osmcontributor.core.model.KeyWord;
import io.mapsquare.osmcontributor.core.model.Note;
import io.mapsquare.osmcontributor.core.model.Poi;
import io.mapsquare.osmcontributor.core.model.PoiNodeRef;
import io.mapsquare.osmcontributor.core.model.PoiTag;
import io.mapsquare.osmcontributor.core.model.PoiType;
import io.mapsquare.osmcontributor.core.model.PoiTypeTag;
import timber.log.Timber;

/**
 * Module providing all the Daos and the OsmSqliteOpenHelper.
 */
@Module
public class DatabaseModule {

    @Provides
    @Singleton
    OsmSqliteOpenHelper getOsmSqliteOpenHelper(Application application) {
        return OpenHelperManager.getHelper(application, OsmSqliteOpenHelper.class);
    }

    @Provides
    Dao<Note, Long> getNoteDao(OsmSqliteOpenHelper helper) {
        return createDao(helper, Note.class);
    }

    @Provides
    Dao<KeyWord, Long> getKeyWordDao(OsmSqliteOpenHelper helper) {
        return createDao(helper, KeyWord.class);
    }

    @Provides
    Dao<Comment, Long> getCommentDao(OsmSqliteOpenHelper helper) {
        return createDao(helper, Comment.class);
    }

    @Provides
    Dao<Poi, Long> getPoiDao(OsmSqliteOpenHelper helper) {
        return createDao(helper, Poi.class);
    }

    @Provides
    Dao<PoiTag, Long> getPoiTagDao(OsmSqliteOpenHelper helper) {
        return createDao(helper, PoiTag.class);
    }

    @Provides
    Dao<PoiNodeRef, Long> getPoiNodeRefDao(OsmSqliteOpenHelper helper) {
        return createDao(helper, PoiNodeRef.class);
    }

    @Provides
    Dao<PoiType, Long> getPoiTypeDao(OsmSqliteOpenHelper helper) {
        return createDao(helper, PoiType.class);
    }

    @Provides
    Dao<PoiTypeTag, Long> getPoiTypeTagDao(OsmSqliteOpenHelper helper) {
        return createDao(helper, PoiTypeTag.class);
    }


    /**
     * Create a Dao of a given model class.
     *
     * @param helper The helper used to create the Dao.
     * @param clazz  The class that the code will be operating on.
     * @param <T>    The class that the code will be operating on.
     * @return A Dao operating on the given class.
     */
    private <T> Dao<T, Long> createDao(OsmSqliteOpenHelper helper, Class<T> clazz) {
        try {
            return helper.getDao(clazz);
        } catch (SQLException e) {
            Timber.e(e, "Error while creating %s dao", clazz.getSimpleName());
            throw new RuntimeException(e);
        }
    }


}
