/**
 * Copyright (C) 2017 eBusiness Information
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
package io.jawg.osmcontributor.database.dao;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.j256.ormlite.stmt.DeleteBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import javax.inject.Inject;

import io.jawg.osmcontributor.database.helper.DatabaseHelper;
import io.jawg.osmcontributor.model.entities.Constraint;
import io.jawg.osmcontributor.model.entities.PoiTypeTag;

/**
 * Dao for {@link io.jawg.osmcontributor.model.entities.Constraint} objects.
 */
public class ConstraintDao extends RuntimeExceptionDao<Constraint, Long> {

    @Inject
    public ConstraintDao(Dao<Constraint, Long> dao) {
        super(dao);
    }

    /**
     * Query for all the Constraint of a given PoiType.
     *
     * @param poiTypeId The id of the Poi.
     * @return The list of Constraints.
     */
    public List<Constraint> queryByPoiTypeId(Long poiTypeId) {
        if (poiTypeId == null) {
            return new ArrayList<>();
        }
        return queryForEq(Constraint.POI_TYPE_ID, poiTypeId);
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
                DeleteBuilder<Constraint, Long> builder = deleteBuilder();
                builder.where().eq(PoiTypeTag.POI_TYPE_ID, poiTypeId);
                return builder.delete();
            }
        });
    }

    /**
     * Delete all the Constraints in the database.
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
