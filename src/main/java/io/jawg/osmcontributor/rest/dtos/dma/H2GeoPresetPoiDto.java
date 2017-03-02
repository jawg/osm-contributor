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
package io.jawg.osmcontributor.rest.dtos.dma;

import com.google.gson.annotations.SerializedName;

import java.util.List;
import java.util.Map;

public class H2GeoPresetPoiDto {

    @SerializedName("name")
    private String name;

    @SerializedName("url")
    private String url;

    @SerializedName("label")
    private Map<String, String> label;

    @SerializedName("description")
    private Map<String, String> description;

    @SerializedName("keywords")
    private Map<String, List<String>> keywords;

    @SerializedName("tags")
    private List<H2GeoPresetPoiTagDto> tags;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Map<String, String> getLabel() {
        return label;
    }

    public void setLabel(Map<String, String> label) {
        this.label = label;
    }

    public Map<String, String> getDescription() {
        return description;
    }

    public void setDescription(Map<String, String> description) {
        this.description = description;
    }

    public Map<String, List<String>> getKeywords() {
        return keywords;
    }

    public void setKeywords(Map<String, List<String>> keywords) {
        this.keywords = keywords;
    }

    public List<H2GeoPresetPoiTagDto> getTags() {
        return tags;
    }

    public void setTags(List<H2GeoPresetPoiTagDto> tags) {
        this.tags = tags;
    }
}
