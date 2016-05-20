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
package io.mapsquare.osmcontributor.map;

import android.support.annotation.NonNull;

public class PoiTypeFilter implements Comparable<PoiTypeFilter> {
    private String poiTypeName;
    private Long poiTypeId;
    private String poiTypeIconName;
    private boolean active;

    public PoiTypeFilter(String poiTypeName, Long poiTypeId, String poiTypeIconName, boolean active) {
        this.poiTypeName = poiTypeName;
        this.poiTypeId = poiTypeId;
        this.poiTypeIconName = poiTypeIconName;
        this.active = active;
    }

    public Long getPoiTypeId() {
        return poiTypeId;
    }

    public void setPoiTypeId(Long poiTypeId) {
        this.poiTypeId = poiTypeId;
    }

    public String getPoiTypeName() {
        return poiTypeName;
    }

    public void setPoiTypeName(String poiTypeName) {
        this.poiTypeName = poiTypeName;
    }

    public String getPoiTypeIconName() {
        return poiTypeIconName;
    }

    public void setPoiTypeIconName(String poiTypeIconName) {
        this.poiTypeIconName = poiTypeIconName;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        PoiTypeFilter that = (PoiTypeFilter) o;


        if (poiTypeId != null ? !poiTypeId.equals(that.poiTypeId) : that.poiTypeId != null) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        return poiTypeId != null ? poiTypeId.hashCode() : 0;
    }

    @Override
    public int compareTo(@NonNull PoiTypeFilter o) {
        int result;
        if (poiTypeName != null && o.poiTypeName != null) {
            result = poiTypeName.toLowerCase().compareTo(o.poiTypeName.toLowerCase());
        } else {
            result = 0;
        }
        if (result == 0 && poiTypeId != null && o.poiTypeId != null) {
            result = poiTypeId.compareTo(o.poiTypeId);
        }
        return result;
    }
}