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

import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.mapsquare.osmcontributor.ui.adapters.item.TagItem;

public class SingleChoiceTagParserImpl implements TagParser {

    private static final String TAG = "SingleChoice";

    /**
     * Limit to consider the use of a single choice widget. If size of possible values
     * are higher that 7, we use another widget (AUTOCOMPLETE).
     */
    private static final int LIMIT = 7;

    /**
     * Possible values.
     */
    private List<String> possibleValues = new ArrayList<>(0);

    /**
     * Supported value.
     */
    private List<String> supportedValues = Arrays.asList("y", "n", "0", "1", "true", "false", "oui", "non", "si", "yes", "no", "undefined", "");

    @Override
    public TagItem.Type getType() {
        return TagItem.Type.SINGLE_CHOICE;
    }

    @Override
    public boolean isCandidate(String key, List<String> values) {
        this.possibleValues = values;
        // If size is < 7 and values must be choose in a list with yes/no.
        Log.i(TAG, "isCandidate: " + key + " " + values);
        return values.size() < LIMIT && values.contains("yes") || values.contains("no");
    }

    @Override
    public boolean support(String value) {
        // If value is not in possible values, user have to format the value.
        return possibleValues.contains(value) || supportedValues.contains(value);
    }

    @Override
    public int getPriority() {
        return ParserManager.PRIORITY_IMPORTANT;
    }
}