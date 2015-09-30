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
package io.mapsquare.osmcontributor.type.dto;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Wiki {

    String lang;
    String title;
    String description;
    @SerializedName("tags_combination")
    List<String> tagsCombination;

    public String getLang() {
        return lang;
    }

    public void setLang(String lang) {
        this.lang = lang;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<String> getTagsCombination() {
        return tagsCombination;
    }

    public void setTagsCombination(List<String> tagsCombination) {
        this.tagsCombination = tagsCombination;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Wiki{");
        sb.append("lang='").append(lang).append('\'');
        sb.append(", title='").append(title).append('\'');
        sb.append(", description='").append(description).append('\'');
        sb.append(", tagsCombination=").append(tagsCombination);
        sb.append('}');
        return sb.toString();
    }
}
