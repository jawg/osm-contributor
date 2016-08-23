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

import com.google.gson.JsonElement;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class PoiTypeDto {
    @SerializedName("name")
    private String name;

    @SerializedName("label")
    private JsonElement labels;

    @SerializedName("description")
    private JsonElement description;

    @SerializedName("tags")
    private List<PoiTypeTagDto> tags;

    @SerializedName("usageCount")
    private int usageCount;

    @SerializedName("keyWords")
    private JsonElement keyWords;

    public JsonElement getLabels() {
        return labels;
    }

    public void setLabels(JsonElement labels) {
        this.labels = labels;
    }

    public String getName() {
        return name;
    }

    public int getUsageCount() {
        return usageCount;
    }

    public void setUsageCount(int usageCount) {
        this.usageCount = usageCount;
    }

    public void setName(String name) {
        this.name = name;
    }

    public JsonElement getDescription() {
        return description;
    }

    public void setDescription(JsonElement description) {
        this.description = description;
    }

    public List<PoiTypeTagDto> getTags() {
        return tags;
    }

    public void setTags(List<PoiTypeTagDto> tags) {
        this.tags = tags;
    }

    public JsonElement getKeyWords() {
        return keyWords;
    }

    public void setKeyWords(JsonElement keyWords) {
        this.keyWords = keyWords;
    }
}
