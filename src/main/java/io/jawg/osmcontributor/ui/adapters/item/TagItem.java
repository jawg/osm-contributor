/**
 * Copyright (C) 2016 eBusiness Information
 * <p>
 * This file is part of OSM Contributor.
 * <p>
 * OSM Contributor is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * OSM Contributor is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with OSM Contributor.  If not, see <http://www.gnu.org/licenses/>.
 */
package io.jawg.osmcontributor.ui.adapters.item;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public abstract class TagItem {

    protected String key;
    protected String value;
    protected String oldValue;
    protected boolean mandatory;
    protected Map<String, String> values = new LinkedHashMap<>();
    protected Type type;
    protected boolean isConform;
    protected boolean show;

    /**
     * Use the best UI widget based on the tag name and possible values.
     */
    public enum Type {
        OPENING_HOURS,          // Use when tag value is opening_hours
        SINGLE_CHOICE,    // Use when tag value can be choose in a short list (< 7)
        CONSTANT,           // Use when tag value can't be modified (ex: type amenity)
        NUMBER,                 // Use when tag value is a number (ex: height, floors)
        TEXT,                    // Use by default
        TIME,
        SHELTER
    }

    public Map<String, String> getValues() {
        return values;
    }

    public void setValues(Map<String, String> values) {
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

    public Type getTagType() {
        return type;
    }

    public boolean isMandatory() {
        return mandatory;
    }

    public void setMandatory(boolean mandatory) {
        this.mandatory = mandatory;
    }

    public boolean isConform() {
        return isConform;
    }

    public void setConform(boolean conform) {
        isConform = conform;
    }

    public boolean isShow() {
        return show;
    }

    public void setShow(boolean show) {
        this.show = show;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public String getOldValue() {
        return oldValue;
    }

    public void setOldValue(String oldValue) {
        this.oldValue = oldValue;
    }

    public boolean hasChanged() {
        if (value == null && oldValue == null) {
            return true;
        }
        if (value == null) {
            return false;
        }
        return value.compareTo(oldValue) != 0;
    }


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

    protected TagItem(TagItemBuilder builder) {
        this.key = builder.key;
        this.value = builder.value;
        this.values = builder.values;
        this.mandatory = builder.mandatory;
        this.type = builder.type;
        this.isConform = builder.isConform;
        this.show = builder.show;
        this.oldValue = builder.oldValue;
    }


    public abstract static class TagItemBuilder<T extends TagItem> {
        private String key;
        private String value;
        private String oldValue;
        private boolean mandatory;
        private Map<String, String> values = new LinkedHashMap<>();
        private Map<String, String> osmValues = new HashMap<>();
        private Type type;
        private boolean isConform;
        private boolean show;

        public TagItemBuilder(String key, String value) {
            this.key = key;
            this.value = value;
            this.oldValue = value;
        }

        public TagItemBuilder value(String value) {
            this.value = value;
            return this;
        }

        public TagItemBuilder oldValue(String oldValue) {
            this.oldValue = oldValue;
            return this;
        }

        public TagItemBuilder mandatory(boolean mandatory) {
            this.mandatory = mandatory;
            return this;
        }

        public TagItemBuilder values(Map<String, String> values) {
            this.values = values;
            return this;
        }

        public TagItemBuilder type(Type type) {
            this.type = type;
            return this;
        }

        public TagItemBuilder isConform(boolean isConform) {
            this.isConform = isConform;
            return this;
        }

        public TagItemBuilder show(boolean show) {
            this.show = show;
            return this;
        }


        public TagItemBuilder addOsmKeyValue(String key, String value) {
            osmValues.put(key, value);
            return this;
        }

        public abstract T build();
    }


    public abstract Map<String, String> getOsmValues();
}
