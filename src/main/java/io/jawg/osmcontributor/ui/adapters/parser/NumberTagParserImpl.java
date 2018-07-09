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
package io.jawg.osmcontributor.ui.adapters.parser;

import java.util.Arrays;
import java.util.List;

import io.jawg.osmcontributor.ui.adapters.item.shelter.TagItem;

public class NumberTagParserImpl implements TagParser {

    private static final List<String> TAG_KEY_POSSIBLE = Arrays.asList("phone", "height", "floors", "level", "layer", "visitors");

    private static final String NUMBER_PATTERN = "[0-9]*(.[0-9]*)?";

    @Override
    public TagItem.Type getType() {
        return TagItem.Type.NUMBER;
    }

    @Override
    public boolean isCandidate(String key, List<String> values) {
        for (String possibleKey : TAG_KEY_POSSIBLE) {
            if (key.contains(possibleKey)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean supports(String value) {
        return value == null || value.matches(NUMBER_PATTERN);
    }

    @Override
    public int getPriority() {
        return ParserManager.PRIORITY_NOT_IMPORTANT;
    }

    @Override
    public int hashCode() {
        return getPriority();
    }
}