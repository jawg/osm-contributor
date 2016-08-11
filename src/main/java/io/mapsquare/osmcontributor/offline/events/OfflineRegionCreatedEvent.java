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
package io.mapsquare.osmcontributor.offline.events;

import com.mapbox.mapboxsdk.offline.OfflineRegion;
import com.mapbox.mapboxsdk.offline.OfflineTilePyramidRegionDefinition;

/**
 * @author Tommy Buonomo on 11/08/16.
 */
public class OfflineRegionCreatedEvent {
    private final OfflineRegion offlineRegion;
    private final OfflineTilePyramidRegionDefinition definition;
    private final String regionName;

    public OfflineRegionCreatedEvent(OfflineRegion offlineRegion, String regionName, OfflineTilePyramidRegionDefinition definition) {
        this.offlineRegion = offlineRegion;
        this.regionName = regionName;
        this.definition = definition;
    }

    public OfflineRegion getOfflineRegion() {
        return offlineRegion;
    }

    public String getRegionName() {
        return regionName;
    }

    public OfflineTilePyramidRegionDefinition getDefinition() {
        return definition;
    }
}
