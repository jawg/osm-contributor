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
package io.jawg.osmcontributor.ui.managers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.inject.Inject;

import io.jawg.osmcontributor.database.dao.RelationDisplayDao;
import io.jawg.osmcontributor.database.dao.RelationDisplayTagDao;
import io.jawg.osmcontributor.database.dao.RelationEditionDao;
import io.jawg.osmcontributor.model.entities.Poi;
import io.jawg.osmcontributor.model.entities.RelationId;
import io.jawg.osmcontributor.model.entities.relation_display.RelationDisplay;
import io.jawg.osmcontributor.model.entities.relation_save.RelationEdition;

/**
 * Manager class for Relations.
 * Provides a number of methods to manipulate the Relations in the database.
 */
public class RelationManager {

    private RelationDisplayDao relationDisplayDao;
    private RelationDisplayTagDao relationDisplayTagDao;
    private RelationEditionDao relationEditionDao;


    @Inject
    public RelationManager(RelationDisplayDao relationDisplayDao, RelationDisplayTagDao relationDisplayTagDao, RelationEditionDao relationEditionDao) {
        this.relationDisplayDao = relationDisplayDao;
        this.relationEditionDao = relationEditionDao;
        this.relationDisplayTagDao = relationDisplayTagDao;
    }


    public List<RelationDisplay> getRelationDisplaysFromRelationsIDs(Collection<RelationId> relationIds) {
        return relationDisplayDao.queryByBackendRelationIds(getBackendIdsFromRelation(relationIds));
    }

    private List<Long> getBackendIdsFromRelation(Collection<RelationId> relationIds) {
        List<Long> ids = new ArrayList<>();
        for (RelationId re : relationIds) {
            if (re.getBackendRelationId() != null && !re.getBackendRelationId().isEmpty()) {
                ids.add(Long.valueOf(re.getBackendRelationId()));
            }
        }
        return ids;
    }

    /**
     * Save the changes made to the relations associated to a poi
     *
     * @param relationEditions the list of changes
     * @param poi              the poi
     */
    public void saveRelationEditions(List<RelationEdition> relationEditions, Poi poi) {
        for (RelationEdition relationEdition : relationEditions) {
            relationEdition.setPoi(poi);
            relationEditionDao.create(relationEdition);
        }
    }

    /**
     * get values for autocompletion of bus field
     *
     * @return
     */
    public List<RelationDisplay> getValuesForBusLinesAutocompletion(String search) {
        return relationDisplayDao.queryByDatabaseIds(
                relationDisplayTagDao.queryForRelationIDsByTag(search)
        );
    }

    /**
     * delete the relation edition objects
     * @param relationEditions list of editions
     */
    public void deleteFinishedEditions(RelationEdition relationEditions) {
        relationEditionDao.delete(relationEditions);
    }
}
