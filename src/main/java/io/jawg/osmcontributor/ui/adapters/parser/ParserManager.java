/**
 * Copyright (C) 2019 Takima
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
package io.jawg.osmcontributor.ui.adapters.parser;


import android.content.Context;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import io.jawg.osmcontributor.ui.adapters.item.shelter.TagItem;
import io.jawg.osmcontributor.ui.utils.Translator;

/**
 * Contains the list of all parser.
 */
public class ParserManager {

    public static final int PRIORITY_LOW = 100;

    public static final int PRIORITY_NOT_IMPORTANT = 75;

    public static final int PRIORITY_NORMAL = 50;

    public static final int PRIORITY_IMPORTANT = 25;

    public static final int PRIORITY_HIGH = 0;

    /**
     * Elements are sorted by priority.
     */
    public static Map<Integer, TagParser> tagParsers = new TreeMap<>();

    /**
     * List of value parsers.
     */
    public static Map<TagItem.Type, ValueParser> valueParsers = new HashMap<>();

    static {
        NumberTagParserImpl numberParser = new NumberTagParserImpl();
        AutoCompleteTagParserImpl autoCompleteParser = new AutoCompleteTagParserImpl();
        TextTagParserImpl textParser = new TextTagParserImpl();
        OpeningTimeTagParserImpl openingTimeParserImpl = new OpeningTimeTagParserImpl();
        SingleChoiceTagParserImpl singleChoiceParser = new SingleChoiceTagParserImpl();
        BusLineTagParserImpl busLineTagParser = new BusLineTagParserImpl();

        tagParsers.put(numberParser.getPriority(), numberParser);
        tagParsers.put(autoCompleteParser.getPriority(), autoCompleteParser);
        tagParsers.put(textParser.getPriority(), textParser);
        tagParsers.put(openingTimeParserImpl.getPriority(), openingTimeParserImpl);
        tagParsers.put(singleChoiceParser.getPriority(), singleChoiceParser);
        tagParsers.put(busLineTagParser.getPriority(), busLineTagParser);

        valueParsers.put(singleChoiceParser.getType(), new SingleChoiceValueParserImpl());
        valueParsers.put(numberParser.getType(), new NumberValueParserImpl());
        valueParsers.put(busLineTagParser.getType(), new BusLineValueParserImpl());
    }

    /**
     * Get tag name formated.
     *
     * @param tagName tag name
     * @return tag name correctly formated
     */
    public static String parseTagName(String tagName, Context context) {
        String translation = Translator.getTranslation(tagName, context);
        if (translation == null) {
            return Character.toUpperCase(tagName.charAt(0)) + tagName.substring(1).replace("_", " ");
        } else {
            return translation;
        }
    }

    /**
     * Get tag name formated.
     *
     * @param tagName tag name
     * @return tag name correctly formated
     */
    public static String deparseTagName(String tagName) {
        return Character.toLowerCase(tagName.charAt(0)) + tagName.substring(1).replace(" ", "_");
    }

    @SuppressWarnings("unchecked")
    public static boolean supportValue(String value, TagItem.Type tagType) {
        ValueParser valueParser = valueParsers.get(tagType);
        return valueParser == null || tagParsers.get(valueParser.getPriority()).supports(value);
    }
}