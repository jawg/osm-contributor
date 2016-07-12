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

import android.app.Application;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.mapsquare.osmcontributor.ui.adapters.item.TagItem;

/**
 * Parser type is used when we want to create a POI. This parser getTagInfoFromH2Geo the possible values
 * for each tag vie the h2geo.json file present in assets folder and indicates what widget
 * we have to show to the user.
 *
 * Work in progress, can be change. First proposition.
 *
 * @version 0.0.1
 */
@Singleton
public class TagParser {
    private static final String TAG = "TagParser";

    @Inject
    public TagParser(Application application) { }

    public String parseTagName(String tagName) {
        return Character.toUpperCase(tagName.charAt(0)) + tagName.substring(1).replace("_", " ");
    }

    public static TagItem.TagType getTagType(String key, List<String> possibleValues, List<String> autoCompleteValues) {
        if (key.equals("opening_hours")) {
            return TagItem.TagType.OPENING_HOURS;
        } else if (key.equals("name")) {
            return TagItem.TagType.AUTOCOMPLETE;
        } else if (key.contains("phone")) {
            return TagItem.TagType.PHONE;
        } else if (key.contains("height")) {
            return TagItem.TagType.NUMBER;
        }

        TagItem.TagType tagType = TagItem.TagType.TEXT;
        // Check possible values from h2geo.json to identify tag type. First step.
        int sizePossibleValues = 0;
        if (possibleValues != null && !possibleValues.isEmpty()) {
            sizePossibleValues = possibleValues.size();
            if (possibleValues.contains("yes") || possibleValues.contains("no")) {
                if (sizePossibleValues < 7) {
                    tagType = TagItem.TagType.SINGLE_CHOICE_SHORT;
                } else if (sizePossibleValues >= 7) {
                    tagType = TagItem.TagType.AUTOCOMPLETE;
                }
            } else if (autoCompleteValues.size() > 1) {
                tagType = TagItem.TagType.AUTOCOMPLETE;
            }
        }

        int autoCompleteValuesSize = countAutoCompleteValues(possibleValues, autoCompleteValues);

        // Check auto complete values to provide more choice to the user. Next step.
        int globalSize = sizePossibleValues;
        if (autoCompleteValues != null && !autoCompleteValues.isEmpty()) {
            globalSize += autoCompleteValuesSize;
            if (autoCompleteValues.contains("yes") || autoCompleteValues.contains("no")) {
                if (globalSize < 7) {
                    tagType = TagItem.TagType.SINGLE_CHOICE_SHORT;
                } else if (globalSize >= 7 && globalSize < 20) {
                    tagType = TagItem.TagType.AUTOCOMPLETE;
                }
            } else if (autoCompleteValues.size() > 1) {
                tagType = TagItem.TagType.AUTOCOMPLETE;
            }
        }
        return tagType;
    }

    /**
     * Count values that are not in possible values.
     * @param possibleValues List of possible values
     * @param autoCompleteValues List of propositions
     * @return Size of autocomplete values that are not in possible values list
     */
    private static int countAutoCompleteValues(List<String> possibleValues, List<String> autoCompleteValues) {
        int autoCompleteValuesSize = 0;
        if (possibleValues != null && autoCompleteValues != null) {
            autoCompleteValuesSize = autoCompleteValues.size();
            for (String possibleValue : possibleValues) {
                if (autoCompleteValues.contains(possibleValue)) {
                    autoCompleteValuesSize -= 1;
                }
            }
        }
        return autoCompleteValuesSize;
    }
}
