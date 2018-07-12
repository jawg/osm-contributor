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

package io.jawg.osmcontributor.database.dao;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.j256.ormlite.stmt.DeleteBuilder;

import java.util.List;

import javax.inject.Inject;

import io.jawg.osmcontributor.database.helper.DatabaseHelper;
import io.jawg.osmcontributor.model.entities.Poi;
import io.jawg.osmcontributor.model.entities.RelationId;
import io.jawg.osmcontributor.model.entities.relation_save.RelationEdition;

/**
 * Dao for {@link RelationEdition} objects.
 */
public class RelationEditionDao extends RuntimeExceptionDao<RelationEdition, Long> {
    @Inject
    public RelationEditionDao(Dao<RelationEdition, Long> dao) {
        super(dao);
    }

    /**
     * Query for all RelationsDisplays by Backend Ids .
     *
     * @return The list of RelationDisplay.
     */
    public List<RelationEdition> queryByPoisDescendingOrder(final List<Poi> pois) {
        return DatabaseHelper.wrapException(() -> queryBuilder()
                .orderBy(RelationEdition.ID,false)
                .where().in(RelationEdition.POI_ID, pois)
                .query());
    }

    /**
     * Delete all relation editions associated with the given POI id.
     *
     * @param poiId of POI id
     * @return The number of relation edition deleted
     */
    public Integer deleteByPoiIds(final Long poiId) {
        return DatabaseHelper.wrapException(() -> {
            DeleteBuilder<RelationEdition, Long> builder = deleteBuilder();
            builder.where().in(RelationId.POI_ID, poiId);
            return builder.delete();
        });
    }

}
