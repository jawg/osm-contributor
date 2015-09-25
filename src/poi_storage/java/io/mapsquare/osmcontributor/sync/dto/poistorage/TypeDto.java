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
package io.mapsquare.osmcontributor.sync.dto.poistorage;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class TypeDto {
    @SerializedName("id")
    private String id;

    @SerializedName("name")
    private String name;

    @SerializedName("icon")
    private String iconUrl;

    @SerializedName("anchorX")
    private Long anchorX;

    @SerializedName("anchorY")
    private Long anchorY;

    @SerializedName("fields")
    private List<String> keys;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<String> getKeys() {
        return keys;
    }

    public void setKeys(List<String> keys) {
        this.keys = keys;
    }

    public String getIconUrl() {
        return iconUrl;
    }

    public void setIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getAnchorX() {
        return anchorX;
    }

    public void setAnchorX(Long anchorX) {
        this.anchorX = anchorX;
    }

    public Long getAnchorY() {
        return anchorY;
    }

    public void setAnchorY(Long anchorY) {
        this.anchorY = anchorY;
    }
}
