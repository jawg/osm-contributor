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
import com.j256.ormlite.stmt.SelectArg;

import java.util.List;
import java.util.concurrent.Callable;

import javax.inject.Inject;

import io.mapsquare.osmcontributor.core.database.DatabaseHelper;
import io.mapsquare.osmcontributor.core.model.PoiType;

/**
 * Dao for {@link io.mapsquare.osmcontributor.core.model.PoiType} objects.
 */
public class PoiTypeDao extends RuntimeExceptionDao<PoiType, Long> {

    @Inject
    public PoiTypeDao(Dao<PoiType, Long> dao) {
        super(dao);
    }

    /**
     * Query for the PoiType with the given backend id.
     *
     * @param backendId the backend id.
     * @return The PoiType.
     */
    public PoiType findByBackendId(final String backendId) {
        return DatabaseHelper.wrapException(new Callable<PoiType>() {
            @Override
            public PoiType call() throws Exception {
                return queryBuilder()
                        .where().eq(PoiType.BACKEND_ID, new SelectArg(backendId))
                        .queryForFirst();
            }
        });
    }

    /**
     * Query for all the PoiTypes alphabetically sorted.
     *
     * @return The List of PoiTypes alphabetically sorted.
     */
    public List<PoiType> queryAllSortedByName() {
        return DatabaseHelper.wrapException(new Callable<List<PoiType>>() {
            @Override
            public List<PoiType> call() throws Exception {
                return queryBuilder()
                        .orderByRaw(PoiType.NAME + " COLLATE NOCASE")
                        .query();
            }
        });
    }

    /**
     * Query for all the PoiTypes last use sorted.
     *
     * @return The List of PoiTypes alphabetically sorted.
     */
    public List<PoiType> queryAllSortedByLastUse() {
        return DatabaseHelper.wrapException(new Callable<List<PoiType>>() {
            @Override
            public List<PoiType> call() throws Exception {
                return queryBuilder()
                        .orderByRaw(PoiType.LAST_USE + " DESC, " + PoiType.NAME + " COLLATE NOCASE ASC")
                        .query();
            }
        });
    }

    /**
     * Delete all the PoiTypes in the database.
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
