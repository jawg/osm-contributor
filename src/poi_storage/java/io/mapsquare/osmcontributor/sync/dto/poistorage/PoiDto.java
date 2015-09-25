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

import java.util.Map;

public class PoiDto {
    @SerializedName("name")
    private String name;

    @SerializedName("fields")
    private Map<String, String> fields;

    @SerializedName("zone")
    private String zoneBackendId;

    @SerializedName("latLng")
    private LatLng latLng;

    @SerializedName("level")
    private Float level;

    @SerializedName("typeId")
    private String typeBackendId;

    @SerializedName("id")
    private String backendId;

    @SerializedName("revision")
    private Integer revision;

    @SerializedName("lastUpdate")
    private String lastUpdate;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBackendId() {
        return backendId;
    }

    public void setBackendId(String backendId) {
        this.backendId = backendId;
    }

    public Map<String, String> getFields() {
        return fields;
    }

    public void setFields(Map<String, String> fields) {
        this.fields = fields;
    }

    public String getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(String lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    public LatLng getLatLng() {
        return latLng;
    }

    public void setLatLng(LatLng latLng) {
        this.latLng = latLng;
    }

    public Float getLevel() {
        return level;
    }

    public void setLevel(Float level) {
        this.level = level;
    }

    public Integer getRevision() {
        return revision;
    }

    public void setRevision(Integer revision) {
        this.revision = revision;
    }

    public String getTypeBackendId() {
        return typeBackendId;
    }

    public void setTypeBackendId(String typeBackendId) {
        this.typeBackendId = typeBackendId;
    }

    public String getZoneBackendId() {
        return zoneBackendId;
    }

    public void setZoneBackendId(String zoneBackendId) {
        this.zoneBackendId = zoneBackendId;
    }
}
