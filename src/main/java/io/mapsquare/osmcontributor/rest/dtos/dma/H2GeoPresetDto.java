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

import java.util.List;
import java.util.Map;

public class H2GeoPresetDto {

    @SerializedName("name")
    private Map<String, String> name;

    @SerializedName("description")
    private Map<String, String> description;

    @SerializedName("data")
    private List<H2GeoPresetPoiDto> data;

    public Map<String, String> getName() {
        return name;
    }

    public void setName(Map<String, String> name) {
        this.name = name;
    }

    public Map<String, String> getDescription() {
        return description;
    }

    public void setDescription(Map<String, String> description) {
        this.description = description;
    }

    public List<H2GeoPresetPoiDto> getData() {
        return data;
    }

    public void setData(List<H2GeoPresetPoiDto> data) {
        this.data = data;
    }
}
