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
package io.mapsquare.osmcontributor.core.database.dao;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.RawRowMapper;
import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.j256.ormlite.stmt.DeleteBuilder;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import javax.inject.Inject;

import io.mapsquare.osmcontributor.core.database.DatabaseHelper;
import io.mapsquare.osmcontributor.core.model.PoiTypeTag;

/**
 * Dao for {@link io.mapsquare.osmcontributor.core.model.PoiTypeTag} objects.
 */
public class PoiTypeTagDao extends RuntimeExceptionDao<PoiTypeTag, Long> {

    @Inject
    public PoiTypeTagDao(Dao<PoiTypeTag, Long> dao) {
        super(dao);
    }

    /**
     * Query for all the PoiTypeTag of a given PoiType.
     *
     * @param poiTypeId The id of the Poi.
     * @return The list of PoiTypeTag.
     */
    public List<PoiTypeTag> queryByPoiTypeId(Long poiTypeId) {
        if (poiTypeId == null) {
            return new ArrayList<>();
        }
        return queryForEq(PoiTypeTag.POI_TYPE_ID, poiTypeId);
    }

    /**
     * Delete all POI type tags with the given POI type id.
     *
     * @param poiTypeId The POI type tag id
     * @return The number of POI type tags deleted
     */
    public Integer deleteByPoiTypeId(final Long poiTypeId) {
        return DatabaseHelper.wrapException(new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                DeleteBuilder<PoiTypeTag, Long> builder = deleteBuilder();
                builder.where().eq(PoiTypeTag.POI_TYPE_ID, poiTypeId);
                return builder.delete();
            }
        });
    }

    public List<String> queryForTagKeysWithDefaultValues() {
        return DatabaseHelper.wrapException(new Callable<List<String>>() {
            @Override
            public List<String> call() throws Exception {
                String statement = queryBuilder()
                        .selectColumns(PoiTypeTag.KEY).distinct()
                        .where().isNotNull(PoiTypeTag.VALUE)
                        .prepare().getStatement();
                return queryRaw(statement, new RawRowMapper<String>() {
                    @Override
                    public String mapRow(String[] columnNames, String[] resultColumns) throws SQLException {
                        return resultColumns[0];
                    }
                }).getResults();
            }
        });
    }

    /**
     * Delete all the PoiTypeTags in the database.
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
