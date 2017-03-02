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

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;

import io.jawg.osmcontributor.model.entities.Group;
import io.jawg.osmcontributor.utils.CloseableUtils;
import timber.log.Timber;

public class H2GeoDto {

    private static H2GeoDto h2GeoDto;

    @SerializedName("version")
    private String version;

    @SerializedName("lastUpdate")
    private String lastUpdate;

    @SerializedName("groups")
    private List<Group<PoiTypeDto>> groups;

    @SerializedName("description")
    private Map<String, String> description;

    @SerializedName("name")
    private Map<String, String> name;

    @SerializedName("offlineArea")
    private List<List<Double>> offlineArea;

    @SerializedName("image")
    private String image;

    @SerializedName("author")
    private String author;

    @SerializedName("link")
    private String link;

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

    public List<Group<PoiTypeDto>> getGroups() {
        return groups;
    }

    public void setGroups(List<Group<PoiTypeDto>> groups) {
        this.groups = groups;
    }

    public Map<String, String> getDescription() {
        return description;
    }

    public void setDescription(Map<String, String> description) {
        this.description = description;
    }

    public Map<String, String> getName() {
        return name;
    }

    public void setName(Map<String, String> name) {
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

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public static H2GeoDto getDefaultPreset(Context context) {
        if (h2GeoDto == null) {
            InputStreamReader reader = null;
            try {
                reader = new InputStreamReader(context.getAssets().open("h2geo.json"));
                return new Gson().fromJson(reader, H2GeoDto.class);
            } catch (Exception e) {
                Timber.e(e, "Error while loading POI Types from assets");
                e.printStackTrace();
                throw new RuntimeException(e);
            } finally {
                CloseableUtils.closeQuietly(reader);
            }
        }
        return h2GeoDto;
    }
}
