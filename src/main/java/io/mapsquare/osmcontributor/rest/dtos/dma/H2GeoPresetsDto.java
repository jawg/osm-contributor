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
package io.mapsquare.osmcontributor.rest.dtos.dma;

import com.google.gson.annotations.SerializedName;

import org.joda.time.DateTime;

import java.util.Map;

public class H2GeoPresetsDto {

    @SerializedName("revision")
    private int revision;

    @SerializedName("lastUpdate")
    private DateTime lastUpdate;

    @SerializedName("presets")
    private Map<String, H2GeoPresetsItemDto> presets;

    public int getRevision() {
        return revision;
    }

    public void setRevision(int revision) {
        this.revision = revision;
    }

    public DateTime getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(DateTime lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    public Map<String, H2GeoPresetsItemDto> getPresets() {
        return presets;
    }

    public void setPresets(Map<String, H2GeoPresetsItemDto> presets) {
        this.presets = presets;
    }
}
