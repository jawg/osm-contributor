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
import java.util.Map;

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

    public static TagItem.Type getTagType(String key, List<String> values, Map<Integer, Parser> parsers) {
        TagItem.Type type = TagItem.Type.TEXT;
        for (Parser parser : parsers.values()) {
            if (parser.isCandidate(key, values)) {
                type = parser.getType();
                break;
            }
        }
        return type;
    }
}