/**
 * Copyright (C) 2016 eBusiness Information
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

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.misc.TransactionManager;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.SQLException;
import java.util.concurrent.Callable;

import io.jawg.osmcontributor.BuildConfig;
import io.jawg.osmcontributor.model.entities.Action;
import io.jawg.osmcontributor.model.entities.Comment;
import io.jawg.osmcontributor.model.entities.Condition;
import io.jawg.osmcontributor.model.entities.Constraint;
import io.jawg.osmcontributor.model.entities.Note;
import io.jawg.osmcontributor.model.entities.Poi;
import io.jawg.osmcontributor.model.entities.PoiNodeRef;
import io.jawg.osmcontributor.model.entities.PoiTag;
import io.jawg.osmcontributor.model.entities.PoiType;
import io.jawg.osmcontributor.model.entities.PoiTypeTag;
import io.jawg.osmcontributor.model.entities.Source;
import timber.log.Timber;

public class OsmSqliteOpenHelper extends OrmLiteSqliteOpenHelper {

    public static final String DATABASE_NAME = "osm-db" + BuildConfig.APPLICATION_ID + ".sqlite";
    public static final int CURRENT_VERSION = 12;

    private Context context;

    public OsmSqliteOpenHelper(Context context) {
        super(context, DATABASE_NAME, null, CURRENT_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase database, ConnectionSource connectionSource) {
        try {
            TableUtils.createTable(connectionSource, PoiType.class);
            TableUtils.createTable(connectionSource, Constraint.class);
            TableUtils.createTable(connectionSource, Source.class);
            TableUtils.createTable(connectionSource, Condition.class);
            TableUtils.createTable(connectionSource, Action.class);
            TableUtils.createTable(connectionSource, PoiTypeTag.class);
            TableUtils.createTable(connectionSource, Poi.class);
            TableUtils.createTable(connectionSource, PoiTag.class);
            TableUtils.createTable(connectionSource, PoiNodeRef.class);
            TableUtils.createTable(connectionSource, Note.class);
            TableUtils.createTable(connectionSource, Comment.class);
        } catch (SQLException e) {
            Timber.e(e, "Error while creating tables");
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase database, ConnectionSource connectionSource, int oldVersion, int newVersion) {
        Timber.d("Upgrading schema from version " + oldVersion + " to " + newVersion);
        // Drop all tables
        dropTables();
        // Create All over
        onCreate(database, connectionSource);
    }

    /**
     * Execute a script in a SQLiteDatabase.
     *
     * @param context           Context of the application.
     * @param connectionSource  ConnectionSource.
     * @param database          SQLiteDatabase where the script must be executed.
     * @param sqlScriptFilename Name of the script to execute.
     * @throws IOException
     * @throws SQLException
     */
    private static void executeSqlScript(final Context context, final ConnectionSource connectionSource, final SQLiteDatabase database, final String sqlScriptFilename) throws IOException, SQLException {
        final BufferedReader reader = new BufferedReader(new InputStreamReader(context.getAssets().open(sqlScriptFilename)));
        TransactionManager.callInTransaction(connectionSource, (new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                String line;
                while ((line = reader.readLine()) != null) {
                    Timber.i("executing sql : %s", line);
                    database.execSQL(line);
                }
                return null;
            }
        }));
        reader.close();
    }

    private void dropTables() {
        try {
            TableUtils.dropTable(connectionSource, PoiType.class, true);
            TableUtils.dropTable(connectionSource, PoiTypeTag.class, true);
            TableUtils.dropTable(connectionSource, Poi.class, true);
            TableUtils.dropTable(connectionSource, PoiTag.class, true);
            TableUtils.dropTable(connectionSource, PoiNodeRef.class, true);
            TableUtils.dropTable(connectionSource, Note.class, true);
            TableUtils.dropTable(connectionSource, Comment.class, true);
        } catch (SQLException e) {
            Timber.e(e, "Error while creating tables");
        }
    }
}
