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

import java.util.List;

import io.jawg.osmcontributor.ui.adapters.item.TagItem;


public class OpeningTimeTagParserImpl implements TagParser {

    public static final String PATTERN_MONTH = "(\\bJan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec\\b)";

    public static final String PATTERN_DAY = "(\\bMo|Tu|We|Th|Fr|Sa|Su\\b)";

    public static final String PATTERN_HOUR = "\\d{2}:\\d{2}-\\d{2}:\\d{2}";

    public static final String PATTERN =
            "(((" + PATTERN_MONTH + "(-" + PATTERN_MONTH + ")?" + "(,\\s?" + PATTERN_MONTH +
                    "(-" + PATTERN_MONTH + ")?)*:\\s?)?" +
                    "(" + PATTERN_DAY + "(-" + PATTERN_DAY + ")?" +
                    "((,\\s?" + PATTERN_DAY + "(-" + PATTERN_DAY + ")?)?)*\\s" +
                    PATTERN_HOUR + ")(,\\s?(" + PATTERN_DAY +
                    "(-" + PATTERN_DAY + ")?" +
                    "((,\\s?" + PATTERN_DAY +
                    "(-" + PATTERN_DAY + ")?)?)*\\s" +
                    PATTERN_HOUR + "))*)" +
                    "|" + "(" + PATTERN_MONTH + "(-" + PATTERN_MONTH + ")?" + "(,\\s?" + PATTERN_MONTH +
                    "(-" + PATTERN_MONTH + ")?)*))";

    public static final String PATTERN_FINAL = PATTERN + "(;\\s?" + PATTERN + ")*";

    @Override
    public TagItem.Type getType() {
        return TagItem.Type.OPENING_HOURS;
    }

    @Override
    public boolean isCandidate(String key, List<String> values) {
        return key.contains("hours");
    }

    @Override
    public boolean support(String value) {
        return value == null || value.equals("24/7") || value.matches(PATTERN_FINAL);
    }

    @Override
    public int getPriority() {
        return ParserManager.PRIORITY_HIGH;
    }

    @Override
    public int hashCode() {
        return getPriority();
    }
}