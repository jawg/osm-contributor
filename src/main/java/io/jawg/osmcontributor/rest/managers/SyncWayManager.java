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

import io.jawg.osmcontributor.model.entities.Poi;
import io.jawg.osmcontributor.utils.Box;

/**
 * Manage the synchronization of ways between the backend and the application.
 */
public interface SyncWayManager {

    /**
     * Download from the backend the ways contained in the box.
     * Update the database with the result.
     *
     * @param box The box to synchronize.
     */
    void syncDownloadWay(final Box box);

    /**
     * Download from the backend, the POIs corresponding to the PoiNodeRefs to update filtered by ids in params.
     * <p/>
     * Apply the new latitude and longitude to those POIs, set them as Updated=true and save them in the database.
     * Set the PoiNodeRefs as Updated=false and save them in the database.
     *
     * @param ids ids of PoiNodeRef to take in account for the modification.
     * @return The corresponding modified Pois.
     */
    List<Poi> downloadPoiForWayEdition(List<Long> ids);
}
