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
package io.jawg.osmcontributor.utils.edition;


import android.os.Parcel;
import android.os.Parcelable;

import java.util.HashMap;
import java.util.Map;

public class PoiChanges implements Parcelable {
    Map<String, String> tagsMap;
    private Long id;

    public PoiChanges(Long id) {
        this.tagsMap = new HashMap<>();
        this.id = id;
    }

    public PoiChanges(Parcel in) {
        this.id = in.readLong();
        final int size = in.readInt();

        for (int i = 0; i < size; i++) {
            final String key = in.readString();
            final String value = in.readString();
            this.tagsMap.put(key, value);
        }
    }

    public Map<String, String> getTagsMap() {
        return tagsMap;
    }

    public void setTagsMap(Map<String, String> tagsMap) {
        this.tagsMap = tagsMap;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeInt(tagsMap.size());

        for (Map.Entry<String, String> entry : tagsMap.entrySet()) {
            dest.writeString(entry.getKey());
            dest.writeString(entry.getValue());
        }
    }


    public static final Parcelable.Creator<PoiChanges> CREATOR = new Parcelable.Creator<PoiChanges>() {
        @Override
        public PoiChanges createFromParcel(Parcel source) {
            return new PoiChanges(source);
        }

        @Override
        public PoiChanges[] newArray(int size) {
            return new PoiChanges[size];
        }
    };
}
