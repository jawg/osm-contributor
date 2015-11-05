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
package io.mapsquare.osmcontributor.upload.events;

public class PleaseConfirmRevertEvent {
    private final Long idToRevert;
    private final boolean poi;

    /**
     * @param idToRevert The Id of the object to revert
     * @param poi        True if it's a POI and False for a PoiNodeRef
     */
    public PleaseConfirmRevertEvent(Long idToRevert, boolean poi) {
        this.idToRevert = idToRevert;
        this.poi = poi;
    }

    public Long getIdToRevert() {
        return idToRevert;
    }

    /**
     * Used to know if it's a POI or PoiNodeRef
     *
     * @return True if it's a POI and False for a PoiNodeRef
     */
    public boolean isPoi() {
        return poi;
    }
}
