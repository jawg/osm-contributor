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

import io.jawg.osmcontributor.ui.adapters.item.shelter.TagItem;

public class PoiTypeTagDto {

    @SerializedName("key")
    private String key;

    @SerializedName("value")
    private String value;

    @SerializedName("type")
    private TagItem.Type type;

    @SerializedName("required")
    private boolean required;

    @SerializedName("editable")
    private Boolean editable;

    @SerializedName("show")
    private Boolean show;

    @SerializedName("values")
    private List<Map<String, Map<String, String>>> values;


    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public TagItem.Type getType() {
        return type;
    }

    public void setType(String type) {
        this.type = TagItem.Type.valueOf(type);
    }

    public Boolean getRequired() {
        return required;
    }

    public void setRequired(Boolean required) {
        this.required = required;
    }

    public Boolean getEditable() {
        return editable;
    }

    public void setEditable(Boolean editable) {
        this.editable = editable;
    }

    public Boolean getShow() {
        return show;
    }

    public void setShow(Boolean show) {
        this.show = show;
    }

    public List<Map<String, Map<String, String>>> getValues() {
        return values;
    }

    public void setValues(List<Map<String, Map<String, String>>> values) {
        this.values = values;
    }
}
