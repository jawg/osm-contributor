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
package io.mapsquare.osmcontributor.utils.upload;

import io.mapsquare.osmcontributor.utils.HtmlFontHelper;

public class PoiDiffWrapper {
    private String key;
    private String oldValue;
    private String newValue;

    public PoiDiffWrapper(String key, String oldValue, String newValue) {
        this.key = key;
        this.oldValue = oldValue;
        this.newValue = newValue;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getOldValue() {
        return oldValue;
    }

    public void setOldValue(String oldValue) {
        this.oldValue = oldValue;
    }

    public String getNewValue() {
        return newValue;
    }

    public void setNewValue(String newValue) {
        this.newValue = newValue;
    }

    public String getColoredDetail(boolean isNew) {
        if ((!isNew && oldValue == null) || (isNew && newValue == null)) {
            return "";
        }

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(HtmlFontHelper.getBold(getKey() + " : "));

        if (newValue == null) {
            //the tag has been deleted
            stringBuilder.append(HtmlFontHelper.addColor(getOldValue(), HtmlFontHelper.RED));
        } else if (oldValue == null) {
            //the tag has been added
            stringBuilder.append(HtmlFontHelper.addColor(getNewValue(), HtmlFontHelper.GREEN));
        } else if (oldValue.compareTo(newValue) != 0) {
            //The value has been updated
            stringBuilder.append(HtmlFontHelper.addColor(isNew ? getNewValue() : getOldValue(), HtmlFontHelper.ORANGE));
        } else {
            stringBuilder.append(isNew ? getNewValue() : getOldValue());
        }

        return stringBuilder.toString();
    }
}
