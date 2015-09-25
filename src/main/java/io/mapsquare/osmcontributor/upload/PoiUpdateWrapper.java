/**
 * Copyright (C) 2015 eBusiness Information
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
package io.mapsquare.osmcontributor.upload;

import io.mapsquare.osmcontributor.core.model.Poi;
import io.mapsquare.osmcontributor.core.model.PoiNodeRef;

public class PoiUpdateWrapper {

    public enum PoiAction {
        CREATE,
        UPDATE,
        DELETED
    }

    private Poi poi;
    private PoiNodeRef nodeRef;
    private PoiAction action;
    private final Boolean isPoi;

    public PoiUpdateWrapper(boolean isPoi, Poi poi, PoiNodeRef nodeRef, PoiAction action) {
        this.poi = poi;
        this.nodeRef = nodeRef;
        this.isPoi = isPoi;
        this.action = action;
    }

    public String getName() {
        String name;
        if (isPoi) {
            name = poi.getName();
            if (name == null || name.isEmpty()) {
                name = "POI" + (poi.getBackendId() == null ? "" : poi.getBackendId());
            }
        } else {
            name = "POI " + nodeRef.getNodeBackendId();
        }
        return name;
    }

    public Poi getPoi() {
        return poi;
    }

    public void setPoi(Poi poi) {
        this.poi = poi;
    }

    public PoiAction getAction() {
        return action;
    }

    public void setAction(PoiAction action) {
        this.action = action;
    }

    public PoiNodeRef getNodeRef() {
        return nodeRef;
    }

    public Boolean getIsPoi() {
        return isPoi;
    }
}