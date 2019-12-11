/**
 * Copyright (C) 2019 Takima
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
package io.jawg.osmcontributor.database.helper;


import android.app.Application;

import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.Dao;

import java.sql.SQLException;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import io.jawg.osmcontributor.model.entities.Action;
import io.jawg.osmcontributor.model.entities.Comment;
import io.jawg.osmcontributor.model.entities.Condition;
import io.jawg.osmcontributor.model.entities.Constraint;
import io.jawg.osmcontributor.model.entities.MapArea;
import io.jawg.osmcontributor.model.entities.Note;
import io.jawg.osmcontributor.model.entities.Poi;
import io.jawg.osmcontributor.model.entities.PoiNodeRef;
import io.jawg.osmcontributor.model.entities.PoiTag;
import io.jawg.osmcontributor.model.entities.PoiType;
import io.jawg.osmcontributor.model.entities.PoiTypeTag;
import io.jawg.osmcontributor.model.entities.RelationId;
import io.jawg.osmcontributor.model.entities.Source;
import io.jawg.osmcontributor.model.entities.relation.FullOSMRelation;
import io.jawg.osmcontributor.model.entities.relation.RelationMember;
import io.jawg.osmcontributor.model.entities.relation.RelationTag;
import io.jawg.osmcontributor.model.entities.relation_display.RelationDisplay;
import io.jawg.osmcontributor.model.entities.relation_display.RelationDisplayTag;
import io.jawg.osmcontributor.model.entities.relation_save.RelationEdition;
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

    @Provides
    Dao<Constraint, Long> getConstraintDao(OsmSqliteOpenHelper helper) {
        return createDao(helper, Constraint.class);
    }

    @Provides
    Dao<Source, Long> getSourceDao(OsmSqliteOpenHelper helper) {
        return createDao(helper, Source.class);
    }

    @Provides
    Dao<Condition, Long> getConditionDao(OsmSqliteOpenHelper helper) {
        return createDao(helper, Condition.class);
    }

    @Provides
    Dao<Action, Long> getActionDao(OsmSqliteOpenHelper helper) {
        return createDao(helper, Action.class);
    }

    @Provides
    Dao<RelationId, Long> getRelationIdDao(OsmSqliteOpenHelper helper) {
        return createDao(helper, RelationId.class);
    }

    @Provides
    Dao<RelationDisplay, Long> getRelationDisplayDao(OsmSqliteOpenHelper helper) {
        return createDao(helper, RelationDisplay.class);
    }

    @Provides
    Dao<RelationDisplayTag, Long> getRelationDisplayTagDao(OsmSqliteOpenHelper helper) {
        return createDao(helper, RelationDisplayTag.class);
    }

    @Provides
    Dao<RelationEdition, Long> getRelationSaveDao(OsmSqliteOpenHelper helper) {
        return createDao(helper, RelationEdition.class);
    }

    @Provides
    Dao<MapArea, String> getMapAreaDao(OsmSqliteOpenHelper helper) {
        try {
            return helper.getDao(MapArea.class);
        } catch (SQLException e) {
            Timber.e(e, "Error while creating %s dao", MapArea.class.getSimpleName());
            throw new RuntimeException(e);
        }
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
