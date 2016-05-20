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
package io.mapsquare.osmcontributor.core.database;

import android.database.sqlite.SQLiteDatabase;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Set;
import java.util.TreeSet;

import io.mapsquare.osmcontributor.SysoTree;
import io.mapsquare.osmcontributor.TestApplication;
import timber.log.Timber;

import static org.fest.assertions.api.Assertions.assertThat;

/**
 * This test generates two sqlite database files : one updated all the way from v1 and one brand new. These files are then checked for differences
 */
@RunWith(RobolectricTestRunner.class)
@Config(manifest = "src/main/AndroidManifest.xml", emulateSdk = 18, application = TestApplication.class)
public class DatabaseUpgradeTest {


    private File newDbFile;
    private File updatedDbFile;

    @Before
    public void setup() throws IOException {
        File baseDir = new File("build/tmp/sqlupdate");
        baseDir.mkdirs();
        FileUtils.cleanDirectory(baseDir);

        File baseOldBd = new File("src/test/resources/db-v1.sqlite");
        newDbFile = new File(baseDir, "new.sqlite");
        updatedDbFile = new File(baseDir, "updated.sqlite");
        FileUtils.copyFile(baseOldBd, updatedDbFile);
        Timber.plant(new SysoTree());

    }

    @Test
    public void upgrade() throws Exception {
        System.out.println(new File(".").getAbsolutePath());
        OsmSqliteOpenHelper openHelper = new OsmSqliteOpenHelper(Robolectric.application);

        SQLiteDatabase newDb = SQLiteDatabase.openOrCreateDatabase(newDbFile, null);
        openHelper.onCreate(newDb);

        SQLiteDatabase upgradedDb = SQLiteDatabase.openDatabase(updatedDbFile.getAbsolutePath(), null, SQLiteDatabase.OPEN_READWRITE);
        openHelper.onUpgrade(upgradedDb, 1, OsmSqliteOpenHelper.CURRENT_VERSION);

        openHelper.close();

        Class.forName("org.sqlite.JDBC");
        String baseJdbcUrl = "jdbc:sqlite:";

        Set<String> newContents = extractContent(baseJdbcUrl + newDbFile.getAbsolutePath());
        Set<String> updatedContents = extractContent(baseJdbcUrl + updatedDbFile.getAbsolutePath());

        assertThat(updatedContents).containsAll(newContents);
        assertThat(updatedContents).isEqualTo(newContents);

    }

    private Set<String> extractContent(String newUrl) throws SQLException {
        Connection newConnection = DriverManager.getConnection(newUrl);

        Set<String> contents = new TreeSet<String>();
        ResultSet newTables = null;
        ResultSet newColumns = null;
        try {
            newTables = newConnection.getMetaData().getTables(null, null, null, null);

            while (newTables.next()) {
                contents.add(newTables.getString("TABLE_TYPE") + " " + newTables.getString("TABLE_NAME"));

                newColumns = newConnection.getMetaData().getColumns(null, null, newTables.getString("TABLE_NAME"), null);
                while (newColumns.next()) {
                    contents.add("TABLE " + newColumns.getString("TABLE_NAME") + " COLUMN " + newColumns.getString("COLUMN_NAME") + " " + newColumns.getString("TYPE_NAME") + " NULLABLE=" + newColumns.getString("IS_NULLABLE"));
                    ResultSet indexes = newConnection.getMetaData().getIndexInfo(null, null, newColumns.getString("TABLE_NAME"), true, false);
                    while (indexes.next()) {
                        contents.add("TABLE " + indexes.getString("TABLE_NAME") + " INDEX " + indexes.getString("INDEX_NAME") + " " + indexes.getString("ORDINAL_POSITION") + " " + indexes.getString("COLUMN_NAME") + " NON_UNIQUE=" + indexes.getString("NON_UNIQUE"));
                    }
                }
            }
            return contents;
        } finally {
            if (newTables != null) {
                try {
                    newTables.close();
                } catch (SQLException ignored) {
                }
            }
            if (newColumns != null) {
                try {
                    newColumns.close();
                } catch (SQLException ignored) {
                }
            }
        }
    }
}
