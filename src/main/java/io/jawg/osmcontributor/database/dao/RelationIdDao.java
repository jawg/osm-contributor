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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import javax.inject.Inject;

import io.jawg.osmcontributor.database.helper.DatabaseHelper;
import io.jawg.osmcontributor.model.entities.PoiTag;
import io.jawg.osmcontributor.model.entities.RelationId;

/**
 * Dao for {@link RelationId} objects.
 */
public class RelationIdDao extends RuntimeExceptionDao<RelationId, Long> {

    @Inject
    public RelationIdDao(Dao<RelationId, Long> dao) {
        super(dao);
    }

    /**
     * Query for all the RelationsIds of a given Poi.
     *
     * @param poiId The id of the Poi.
     * @return The list of PoiNodeRef.
     */
    public List<RelationId> queryByPoiId(Long poiId) {
        if (poiId == null) {
            return new ArrayList<>();
        }
        return queryForEq(PoiTag.POI_ID, poiId);
    }

    /**
     * Delete all the RelationsIds in the database.
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
