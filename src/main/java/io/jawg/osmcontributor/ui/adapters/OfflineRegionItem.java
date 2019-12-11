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
package io.jawg.osmcontributor.ui.adapters;

import com.mapbox.mapboxsdk.offline.OfflineRegion;
import com.mapbox.mapboxsdk.offline.OfflineRegionStatus;
import com.mapbox.mapboxsdk.offline.OfflineTilePyramidRegionDefinition;

/**
 * @author Tommy Buonomo on 10/08/16.
 */
public class OfflineRegionItem {
    private OfflineRegion offlineRegion;

    private OfflineRegionStatus status;

    private boolean selected;

    public OfflineRegionItem(OfflineRegion offlineRegion, OfflineRegionStatus status) {
        this.offlineRegion = offlineRegion;
        this.status = status;
    }

    public OfflineRegion getOfflineRegion() {
        return offlineRegion;
    }

    public void setOfflineRegion(OfflineRegion offlineRegion) {
        this.offlineRegion = offlineRegion;
    }

    public OfflineRegionStatus getStatus() {
        return status;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof OfflineRegionItem)) {
            return false;
        }

        OfflineRegionItem that = (OfflineRegionItem) o;

        if (that.offlineRegion == null || offlineRegion == null) {
            return false;
        }

        OfflineTilePyramidRegionDefinition thatDefinition =
                (OfflineTilePyramidRegionDefinition) that.offlineRegion.getDefinition();

        OfflineTilePyramidRegionDefinition definition =
                (OfflineTilePyramidRegionDefinition) offlineRegion.getDefinition();

        return thatDefinition.getBounds().equals(definition.getBounds()) ||
                (offlineRegion != null ? offlineRegion.equals(that.offlineRegion) : that.offlineRegion == null);
    }

    @Override
    public int hashCode() {
        return offlineRegion != null ? offlineRegion.hashCode() : 0;
    }
}
