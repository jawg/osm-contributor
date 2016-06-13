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
package io.mapsquare.osmcontributor.utils.edition;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

public class CardModel implements Parcelable {
    private String key;
    private String value;
    private boolean mandatory;
    private List<String> autocompleteValues = new ArrayList<>();
    private boolean open;
    private CardType type;

    public enum CardType {
        HEADER_OPTIONAL,
        HEADER_REQUIRED,
        TAG_FEW_VALUES,
        TAG_MANY_VALUES,
        TAG_IMPOSED
    }

    public boolean isTag() {
        return type == CardType.TAG_FEW_VALUES || type == CardType.TAG_MANY_VALUES || type == CardType.TAG_IMPOSED;
    }

    public CardModel(String key, String value, boolean mandatory, List<String> autocompleteValues, boolean open, CardType separator) {
        this.key = key;
        this.value = value;
        this.autocompleteValues = autocompleteValues;
        this.mandatory = mandatory;
        this.open = open;
        this.type = separator;
    }

    public CardModel(Parcel in) {
        this.key = in.readString();
        this.value = in.readString();
        this.mandatory = in.readByte() != 0;
        this.open = in.readByte() != 0;
        try {
            this.type = CardType.valueOf(in.readString());
        } catch (IllegalArgumentException x) {
            this.type = null;
        }

        final int size = in.readInt();

        for (int i = 0; i < size; i++) {
            final String value = in.readString();
            this.autocompleteValues.add(value);
        }
    }

    public List<String> getAutocompleteValues() {
        return autocompleteValues;
    }

    public void setAutocompleteValues(List<String> autocompleteValues) {
        this.autocompleteValues = autocompleteValues;
    }

    public boolean isOpen() {
        return open;
    }

    public void setOpen(boolean open) {
        this.open = open;
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

    public CardType getType() {
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
        dest.writeByte((byte) (open ? 1 : 0));
        dest.writeString((type == null) ? "" : type.toString());

        if (autocompleteValues != null) {
            dest.writeInt(autocompleteValues.size());
            for (String autocomplete : autocompleteValues) {
                dest.writeString(autocomplete);
            }
        } else {
            dest.writeInt(0);
        }
    }


    public static final Parcelable.Creator<CardModel> CREATOR = new Parcelable.Creator<CardModel>() {
        @Override
        public CardModel createFromParcel(Parcel source) {
            return new CardModel(source);
        }

        @Override
        public CardModel[] newArray(int size) {
            return new CardModel[size];
        }
    };

}
