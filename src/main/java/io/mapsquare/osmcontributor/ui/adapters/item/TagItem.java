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
package io.mapsquare.osmcontributor.ui.adapters.item;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

public class TagItem implements Parcelable {
    private String key;
    private String value;
    private boolean mandatory;
    private List<String> values = new ArrayList<>();
    private TagType type;

    /**
     * Use the best UI widget based on the tag name and possible values.
     */
    public enum TagType {
        OPENING_HOURS,          // Use when tag value is opening_hours
        SINGLE_CHOICE,    // Use when tag value can be choose in a short list (< 7)
        CONSTANT,           // Use when tag value can't be modified (ex: type amenity)
        PHONE,                  // Use when tag value is a phone number
        NUMBER,                 // Use when tag value is a number (ex: height, floors)
        TEXT                    // Use by default
    }

    public TagItem(String key, String value, boolean mandatory, List<String> values, TagType separator) {
        this.key = key;
        this.value = value;
        this.values = values;
        this.mandatory = mandatory;
        this.type = separator;
    }

    public TagItem(Parcel in) {
        this.key = in.readString();
        this.value = in.readString();
        this.mandatory = in.readByte() != 0;
        try {
            this.type = TagType.valueOf(in.readString());
        } catch (IllegalArgumentException x) {
            this.type = null;
        }

        final int size = in.readInt();

        for (int i = 0; i < size; i++) {
            final String value = in.readString();
            this.values.add(value);
        }
    }

    public List<String> getValues() {
        return values;
    }

    public void setValues(List<String> values) {
        this.values = values;
    }

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

    public TagType getTagType() {
        return type;
    }

    public boolean isMandatory() {
        return mandatory;
    }

    public void setMandatory(boolean mandatory) {
        this.mandatory = mandatory;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(key);
        dest.writeString(value);
        dest.writeByte((byte) (mandatory ? 1 : 0));
        dest.writeString((type == null) ? "" : type.toString());

        if (values != null) {
            dest.writeInt(values.size());
            for (String autocomplete : values) {
                dest.writeString(autocomplete);
            }
        } else {
            dest.writeInt(0);
        }
    }


    public static final Parcelable.Creator<TagItem> CREATOR = new Parcelable.Creator<TagItem>() {
        @Override
        public TagItem createFromParcel(Parcel source) {
            return new TagItem(source);
        }

        @Override
        public TagItem[] newArray(int size) {
            return new TagItem[size];
        }
    };

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        TagItem tagItem = (TagItem) o;

        return key != null ? key.equals(tagItem.key) : tagItem.key == null;

    }

    @Override
    public int hashCode() {
        return key != null ? key.hashCode() : 0;
    }
}
