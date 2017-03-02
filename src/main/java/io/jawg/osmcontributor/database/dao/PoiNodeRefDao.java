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
package io.jawg.osmcontributor.database.dao;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.RawRowMapper;
import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.j256.ormlite.stmt.DeleteBuilder;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import javax.inject.Inject;

import io.jawg.osmcontributor.database.helper.DatabaseHelper;
import io.jawg.osmcontributor.model.entities.Poi;
import io.jawg.osmcontributor.model.entities.PoiNodeRef;
import io.jawg.osmcontributor.model.entities.PoiTag;

/**
 * Dao for {@link io.jawg.osmcontributor.model.entities.PoiNodeRef} objects.
 */
public class PoiNodeRefDao extends RuntimeExceptionDao<PoiNodeRef, Long> {

    @Inject
    public PoiNodeRefDao(Dao<PoiNodeRef, Long> dao) {
        super(dao);
    }

    /**
     * Query for all the PoiNodeRefs of a given Poi.
     *
     * @param poiId The id of the Poi.
     * @return The list of PoiNodeRef.
     */
    public List<PoiNodeRef> queryByPoiId(Long poiId) {
        if (poiId == null) {
            return new ArrayList<>();
        }
        return queryForEq(PoiTag.POI_ID, poiId);
    }

    /**
     * Query for all PoiNodeRefs to update.
     *
     * @return The list of PoiNodeRefs.
     */
    public List<PoiNodeRef> queryAllToUpdate() {
        return DatabaseHelper.wrapException(new Callable<List<PoiNodeRef>>() {
            @Override
            public List<PoiNodeRef> call() throws Exception {
                return queryBuilder()
                        .where().eq(PoiNodeRef.UPDATED, true)
                        .query();
            }
        });
    }

    /**
     * Query for all PoiNodeRefs by Ids .
     *
     * @return The list of PoiNodeRefs.
     */
    public List<PoiNodeRef> queryByPoiNodeRefIds(final List<Long> poiIds) {
        return DatabaseHelper.wrapException(new Callable<List<PoiNodeRef>>() {
            @Override
            public List<PoiNodeRef> call() throws Exception {
                return queryBuilder()
                        .where().in(PoiNodeRef.ID, poiIds)
                        .query();
            }
        });
    }

    /**
     * Count for all PoiNodeRefs to update.
     *
     * @return The count of PoiNodeRefs to update.
     */
    public Long countAllToUpdate() {
        return DatabaseHelper.wrapException(new Callable<Long>() {
            @Override
            public Long call() throws Exception {
                return queryBuilder()
                        .where().eq(PoiNodeRef.UPDATED, true)
                        .countOf();
            }
        });
    }

    /**
     * Query for the backend id of all PoiNodeRef to update.
     *
     * @return The list of backend ids.
     */
    public List<Long> queryAllUpdated() {
        return DatabaseHelper.wrapException(new Callable<List<Long>>() {
            @Override
            public List<Long> call() throws Exception {
                String statement = queryBuilder()
                        .selectColumns(PoiNodeRef.NODE_BACKEND_ID).distinct()
                        .where().eq(PoiNodeRef.UPDATED, true)
                        .prepare().getStatement();
                return queryRaw(statement, new RawRowMapper<Long>() {
                    @Override
                    public Long mapRow(String[] columnNames, String[] resultColumns) throws SQLException {
                        return Long.parseLong(resultColumns[0]);
                    }
                }).getResults();
            }
        });
    }

    /**
     * Query for all the PoiNodeRefs contained in a box centered on the lat lng position.
     * The box has a 0.00004° width.
     *
     * @param lat Latitude of the center of the box.
     * @param lng Longitude of the center of the box.
     * @return The PoiNodeRefs contained in the box.
     */
    public List<PoiNodeRef> queryAllInRect(final double lat, final double lng) {
        return DatabaseHelper.wrapException(new Callable<List<PoiNodeRef>>() {
            @Override
            public List<PoiNodeRef> call() throws Exception {
                return queryBuilder()
                        .where().gt(Poi.LATITUDE, lat - 0.00002)
                        .and().lt(Poi.LATITUDE, lat + 0.00002)
                        .and().gt(Poi.LONGITUDE, lng - 0.00002)
                        .and().lt(Poi.LONGITUDE, lng + 0.00002)
                        .query();
            }
        });
    }

    /**
     * Delete all POI node refs with the given POI ids.
     *
     * @param poiIds A list of POI ids
     * @return The number of POI node refs deleted
     */
    public Integer deleteByPoiIds(final List<Long> poiIds) {
        return DatabaseHelper.wrapException(new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                DeleteBuilder<PoiNodeRef, Long> builder = deleteBuilder();
                builder.where().in(PoiNodeRef.POI_ID, poiIds);
                return builder.delete();
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

    /**
     * Count for PoiNodeRefs with the same backend Id.
     *
     * @param backendId The backend id.
     * @return The count of poiNodeRefs.
     */
    public Long countForBackendId(final String backendId) {
        return DatabaseHelper.wrapException(new Callable<Long>() {
            @Override
            public Long call() throws Exception {
                return queryBuilder()
                        .where().eq(PoiNodeRef.NODE_BACKEND_ID, backendId)
                        .countOf();
            }
        });
    }
}
