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
package io.jawg.osmcontributor.model.events;

import java.util.Collection;

import io.jawg.osmcontributor.model.entities.RelationId;

public class PleaseLoadBusLinesForPoiEvent {
    private final Collection<RelationId> relationIds;

    public PleaseLoadBusLinesForPoiEvent(Collection<RelationId> relationIds) {
        this.relationIds = relationIds;
    }

    public Collection<RelationId> getRelationIds() {
        return relationIds;
    }
}
