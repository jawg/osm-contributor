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

public class H2GeoDto {

    @SerializedName("h2GeoVersion")
    private String h2GeoVersion;

    @SerializedName("generationDate")
    private String generationDate;

    @SerializedName("data")
    private List<PoiTypeDto> data;

    public String getH2GeoVersion() {
        return h2GeoVersion;
    }

    public void setH2GeoVersion(String h2GeoVersion) {
        this.h2GeoVersion = h2GeoVersion;
    }

    public String getGenerationDate() {
        return generationDate;
    }

    public void setGenerationDate(String generationDate) {
        this.generationDate = generationDate;
    }

    public List<PoiTypeDto> getData() {
        return data;
    }

    public void setData(List<PoiTypeDto> data) {
        this.data = data;
    }
}
