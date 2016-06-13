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
package io.mapsquare.osmcontributor.sync;

import java.util.List;

import io.mapsquare.osmcontributor.model.entities.Poi;
import io.mapsquare.osmcontributor.utils.Box;

/**
 * To be implemented
 * <p/>
 * Implementation of a {@link io.mapsquare.osmcontributor.sync.SyncWayManager} using a PoiStorage backend.
 */
public class PoiStorageSyncWayManager implements SyncWayManager {

    /**
     * To be implemented.
     * <p/>
     * {@inheritDoc}
     */
    @Override
    public void syncDownloadWay(Box box) {
        //TODO Implement method
    }

    /**
     * To be implemented.
     * <p/>
     * {@inheritDoc}
     */
    @Override
    public List<Poi> downloadPoiForWayEdition(List<Long> ids) {
        //TODO Implement method
        return null;
    }
}
