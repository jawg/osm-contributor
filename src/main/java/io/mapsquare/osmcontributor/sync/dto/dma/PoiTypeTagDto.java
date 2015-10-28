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
package io.mapsquare.osmcontributor.sync.dto.dma;


import com.google.gson.JsonElement;
import com.google.gson.annotations.SerializedName;

public class PoiTypeTagDto {

    @SerializedName("key")
    private String key;

    @SerializedName("value")
    private String value;

    @SerializedName("mandatory")
    private boolean mandatory;

    @SerializedName("implied")
    private boolean implied;

    @SerializedName("possibleValues")
    private JsonElement possibleValues;

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
}
