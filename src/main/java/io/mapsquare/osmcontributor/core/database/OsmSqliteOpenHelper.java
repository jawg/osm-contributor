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

import io.mapsquare.osmcontributor.core.model.Comment;
import io.mapsquare.osmcontributor.core.model.Note;
import io.mapsquare.osmcontributor.core.model.Poi;
import io.mapsquare.osmcontributor.core.model.PoiNodeRef;
import io.mapsquare.osmcontributor.core.model.PoiTag;
import io.mapsquare.osmcontributor.core.model.PoiType;
import io.mapsquare.osmcontributor.core.model.PoiTypeTag;
import timber.log.Timber;

public class OsmSqliteOpenHelper extends OrmLiteSqliteOpenHelper {

    public static final String DATABASE_NAME = "osm-db.sqlite";
    public static final int CURRENT_VERSION = 6;

    private Context context;

    public OsmSqliteOpenHelper(Context context) {
        super(context, DATABASE_NAME, null, CURRENT_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase database, ConnectionSource connectionSource) {
        try {
            TableUtils.createTable(connectionSource, PoiType.class);
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

        for (int version = oldVersion; version < newVersion; version++) {
            String sqlScriptFilename = "updateSql-" + version + "-" + (version + 1) + ".sql";
            try {
                executeSqlScript(context, connectionSource, database, sqlScriptFilename);
            } catch (Exception e) {
                Timber.e(e, "Error while upgrading database");
            }
        }
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
}
