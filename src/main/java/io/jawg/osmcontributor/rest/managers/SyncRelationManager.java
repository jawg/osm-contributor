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
package io.jawg.osmcontributor.rest.managers;

import java.util.List;

import io.jawg.osmcontributor.model.entities.relation.FullOSMRelation;
import io.jawg.osmcontributor.model.entities.relation_save.RelationEdition;

/**
 * Manage the synchronization of relations between the backend and the application.
 */
public interface SyncRelationManager {
    /**
     * Download from the backend, the complete Relations objects.
     *
     * @param ids ids of relations to download.
     * @return The corresponding Relations.
     */
    List<FullOSMRelation> downloadRelationsForEdition(List<Long> ids);

    List<FullOSMRelation> applyChangesToRelations(List<FullOSMRelation> fullOSMRelations, List<RelationEdition> relationEditions);
}
