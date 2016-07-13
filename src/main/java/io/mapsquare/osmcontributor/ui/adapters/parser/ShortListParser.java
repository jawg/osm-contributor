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
package io.mapsquare.osmcontributor.ui.adapters.parser;

import java.util.List;

import io.mapsquare.osmcontributor.ui.adapters.item.TagItem;

/**
 * Parse value from OSM database.
 */
public class ShortListParser {

    public static String getFormatedValue(TagItem.TagType tagType, String value, List<String> possibleValues) {
        if (value == null || value.isEmpty()) {
            return null;
        }

        if (tagType == TagItem.TagType.SINGLE_CHOICE) {
            if (possibleValues.size() == 2 && (possibleValues.contains("yes") || possibleValues.contains("no"))) {
                // Case yes/no/undefined
                if (value.startsWith("y") || value.startsWith("s") || value.startsWith("o") || value.startsWith("1") || value.equals("true")) {
                    value = "yes";
                } else if (value.startsWith("n") || value.startsWith("0") || value.equals("false")) {
                    value = "no";
                } else {
                    value = "undefined";
                }
            } else if (possibleValues.size() == 3 && (possibleValues.contains("yes") || possibleValues.contains("no"))) {
                // Case yes/no/undefined/only
                if (value.startsWith("y") || value.startsWith("s") || value.startsWith("ou") || value.startsWith("1") || value.equals("true")) {
                    value = "yes";
                } else if (value.startsWith("n") || value.startsWith("0") || value.equals("false")) {
                    value = "no";
                } else if (value.startsWith("on")) {
                    value = "only";
                } else {
                    value = "undefined";
                }
            }
        }
        return value;
    }
}
