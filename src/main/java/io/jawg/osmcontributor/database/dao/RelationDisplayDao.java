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

package io.jawg.osmcontributor.database.dao;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.GenericRawResults;
import com.j256.ormlite.dao.RuntimeExceptionDao;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;

import io.jawg.osmcontributor.database.helper.DatabaseHelper;
import io.jawg.osmcontributor.model.entities.Poi;
import io.jawg.osmcontributor.model.entities.relation_display.RelationDisplay;

/**
 * Dao for {@link RelationDisplay} objects.
 */
public class RelationDisplayDao extends RuntimeExceptionDao<RelationDisplay, Long> {

    @Inject
    public RelationDisplayDao(Dao<RelationDisplay, Long> dao) {
        super(dao);
    }

    /**
     * Query for all RelationsDisplays by Backend Ids .
     *
     * @return The list of RelationDisplay.
     */
    public List<RelationDisplay> queryByBackendRelationIds(final List<Long> relationsIds) {
        return DatabaseHelper.wrapException(() -> queryBuilder()
                .where().in(RelationDisplay.BACKEND_ID, relationsIds)
                .query());
    }

    /**
     * Query for a RelationsDisplay by Backend Id .
     *
     * @return The RelationDisplay.
     */
    public RelationDisplay queryByBackendRelationId(final String relationId) {
        return DatabaseHelper.wrapException(() -> queryBuilder()
                .where().eq(RelationDisplay.BACKEND_ID, relationId)
                .queryForFirst());
    }

    /**
     * @param data
     * @return
     */
    @Override
    public RelationDisplay createIfNotExists(RelationDisplay data) {
        if (queryByBackendRelationId(data.getBackendId()) == null) {
            create(data);
        }
        return queryByBackendRelationId(data.getBackendId());
    }

    /**
     * Query for all RelationsDisplays by their database Ids .
     *
     * @return The list of RelationDisplay.
     */
    public List<RelationDisplay> queryByDatabaseIds(final List<Long> dbIds) {
        return DatabaseHelper.wrapException(() -> queryBuilder()
                .where().in(RelationDisplay.ID, dbIds)
                .query());
    }

    /**
     * Query for all RelationsDisplays ordered by their distance from the given POI.
     *
     * @return A list of RelationDisplay.
     */
    public List<Long> queryForRelationIdsOrderedByDistance(Poi poi) {
        return DatabaseHelper.wrapException(() -> {
            final String    RELATION_TAG_REF    = "ref";
            final int       LIMIT_NB_NEARBY_POI = 20;

            final String query =
                    " SELECT DISTINCT rdt.relation_display_id "
                            + " FROM "
                            + " ( "
                            + "     SELECT DISTINCT id, ABS(latitude - " + poi.getLatitude() + ") + ABS(longitude - " + poi.getLongitude() + ") AS distance "
                            + "     FROM poi "
                            + (poi.getId() == null ? "" : "     WHERE id != " + poi.getId() + " ")
                            + "     ORDER BY distance "
                            + "     LIMIT " + LIMIT_NB_NEARBY_POI + " "
                            + " ) "
                            + " AS nearby_pois "
                            + " JOIN relation_id ri ON ri.poi_id = nearby_pois.id "
                            + " JOIN relation_display rd ON rd.backend_id = ri.relation_id "
                            + " JOIN relation_display_tag rdt ON rdt.relation_display_id = rd.id AND rdt.key = '" + RELATION_TAG_REF + "' "
                            + " ORDER BY nearby_pois.distance "
                    ;

            GenericRawResults<String[]> rawResults = queryRaw(query);

            List<Long> relationsIds = new ArrayList<>();

            for (String[] result : rawResults.getResults()) {
                relationsIds.add(Long.valueOf(result[0]));
            }

            return relationsIds;
        });
    }

}
