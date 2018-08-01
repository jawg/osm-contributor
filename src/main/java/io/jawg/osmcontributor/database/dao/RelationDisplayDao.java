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
import com.j256.ormlite.dao.GenericRawResults;
import com.j256.ormlite.dao.RuntimeExceptionDao;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;

import io.jawg.osmcontributor.database.helper.DatabaseHelper;
import io.jawg.osmcontributor.model.entities.relation_display.RelationDisplay;
import timber.log.Timber;

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
    public List<Long> queryForRelationIdsOrderedByDistance(Long poiId) {
        return DatabaseHelper.wrapException(() -> {
            final String    RELATION_TAG_REF    = "ref";
            final int       LIMIT_NB_NEARBY_POI = 20;

            final String rawSqlFetchPoisRelationsIdsOrderedByDistance =
                    " SELECT DISTINCT rdt.relation_display_id "
                            + " FROM "
                            + " ( "
                            + "     SELECT DISTINCT p_nearby.id AS id, ABS(p_nearby.latitude - p_origin.latitude) + ABS(p_nearby.longitude - p_origin.longitude) AS distance "
                            + "     FROM poi p_origin "
                            + "     INNER JOIN poi p_nearby "
                            + "     ON p_nearby.id <> p_origin.id "
                            + "     WHERE p_origin.id = %d "
                            + "     ORDER BY distance "
                            + "     LIMIT %d "
                            + " ) "
                            + " AS nearby_pois "
                            + " JOIN relation_id ri ON ri.poi_id = nearby_pois.id "
                            + " JOIN relation_display rd ON rd.backend_id = ri.relation_id "
                            + " JOIN relation_display_tag rdt ON rdt.relation_display_id = rd.id AND rdt.key = '%s' "
                            + " WHERE rdt.value NOT IN "
                            + " ( "
                            + "     SELECT DISTINCT rdt.value AS value "
                            + "     FROM relation_id ri "
                            + "     JOIN relation_display rd ON rd.backend_id = ri.relation_id "
                            + "     JOIN relation_display_tag rdt ON rdt.relation_display_id = rd.id AND rdt.key = '%s' "
                            + "     WHERE poi_id = %d "
                            + "     ORDER BY relation_id "
                            + " ) "
                            + " ORDER BY nearby_pois.distance "
                    ;

            GenericRawResults<String[]> rawResults = queryRaw(String.format(
                    Locale.FRENCH,
                    rawSqlFetchPoisRelationsIdsOrderedByDistance,
                    poiId, LIMIT_NB_NEARBY_POI, RELATION_TAG_REF, RELATION_TAG_REF, poiId
            ));

            List<Long> relationsIds = new ArrayList<>();

            for (String[] result : rawResults.getResults()) {
                relationsIds.add(Long.valueOf(result[0]));
            }

            return relationsIds;
        });
    }

}
