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

import io.mapsquare.osmcontributor.ui.adapters.item.TagItem;

public class PoiTypeTagDto {

    @SerializedName("key")
    private String key;

    @SerializedName("value")
    private String value;

    @SerializedName("mandatory")
    private boolean mandatory;

    @SerializedName("implied")
    private boolean implied;

    @SerializedName("values")
    private JsonElement possibleValues;

    @SerializedName("type")
    private TagItem.Type type;

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

    public boolean isMandatory() {
        return mandatory;
    }

    public void setMandatory(boolean mandatory) {
        this.mandatory = mandatory;
    }

    public boolean isImplied() {
        return implied;
    }

    public void setImplied(boolean implied) {
        this.implied = implied;
    }

    public JsonElement getPossibleValues() {
        return possibleValues;
    }

    public void setPossibleValues(JsonElement possibleValues) {
        this.possibleValues = possibleValues;
    }

    public TagItem.Type getType() {
        return type;
    }

    public void setType(TagItem.Type type) {
        this.type = type;
    }
}
