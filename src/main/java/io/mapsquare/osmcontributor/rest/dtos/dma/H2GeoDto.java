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
    @SerializedName("version")
    private String version;

    @SerializedName("lastUpdate")
    private String lastUpdate;

    @SerializedName("data")
    private List<PoiTypeDto> data;

    @SerializedName("description")
    private String description;

    @SerializedName("name")
    private String name;

    @SerializedName("offlineArea")
    private List<List<Double>> offlineArea;

    @SerializedName("image")
    private String image;

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(String lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    public List<PoiTypeDto> getData() {
        return data;
    }

    public void setData(List<PoiTypeDto> data) {
        this.data = data;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<List<Double>> getOfflineArea() {
        return offlineArea;
    }

    public void setOfflineArea(List<List<Double>> offlineArea) {
        this.offlineArea = offlineArea;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }
}
