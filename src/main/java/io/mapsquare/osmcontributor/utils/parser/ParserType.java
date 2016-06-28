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
package io.mapsquare.osmcontributor.utils.parser;

import android.content.Context;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import io.mapsquare.osmcontributor.model.entities.PoiType;
import io.mapsquare.osmcontributor.model.entities.PoiTypeTag;

/**
 * Parser type is used when we want to create a POI. This parser getTagInfoFromH2Geo the possible values
 * for each tag vie the h2geo.json file present in assets folder and indicates what widget
 * we have to show to the user.
 *
 * Work in progress, can be change. First propsition.
 *
 * @version 0.0.1
 */
public class ParserType {

    private static final String TAG = "ParserType";

    /**
     * Use the best UI widget based on the tag name and possible values.
     */
    public enum Widget {
        OPENING_HOURS,      // Use when tag value is opening_hours
        DATE,               // Use when tag value is a date
        MULTI_CHOICE,       // Use when a tag can contain multiple values
        BOOLEAN_CHOICE,     // Use when tag value can be yes, no or undefined
        LIST,               // Use when tag value must be choose in a list of element
        TEXT                // Use by default
    }

    private static JSONArray h2geoJson;

    /**
     * The main idea of this method is to get the tag of poi type and return, for each tag,
     * the widget to use.
     *
     * This is a first propsition.
     *
     * @param poiType poi type to process
     * @return map of tag name with the widget corresponding
     */
    public static Map<String, Widget> getWidgetForType(PoiType poiType, Context context) {
        instantiateH2geoJson(context);

        // Map to return
        Map<String, Widget> widgets = new HashMap<>();

        // Loop over tag of the poi type
        for (PoiTypeTag tag : poiType.getTags()) {
            // Print all tag key / value just for info
            Log.i(TAG, tag.getKey() + "=" + tag.getValue());

            // Get tag info about the current poi type from h2geo file
            JSONArray tagsInfoFromH2geo = getTagsInfoFromH2Geo(tag.getKey() + "=" + tag.getValue());
            if (tagsInfoFromH2geo != null) {
                for (int i = 0; i < tagsInfoFromH2geo.length(); i++) {
                    if (tagsInfoFromH2geo.getJSONObject(i).getString("key").equals("opening_hours")) {
                        widgets.put("opening_hours", Widget.OPENING_HOURS);
                    } else if (tagsInfoFromH2geo.getJSONObject(i).has("possibleValues")) {
                        // Get possible values. If exists, possible values can be yes / no, a name,
                        // a number, 24 / 7, a word.
                    } else {

                    }
                }
            }
        }
        return widgets;
    }


    /**
     * Get the info tag from h2geo.
     *
     * @param name Name of poi type (ex : amenity=pharmacy or shop=e-cig)
     * @return a Json Array with all tags for the poi type
     */
    private static JSONArray getTagsInfoFromH2Geo(String name) {
        try {
            for (int i = 0; i < h2geoJson.length(); i++) {
                if (h2geoJson.getJSONObject(i).getString("name").equals(name)) {
                    Log.i(TAG, h2geoJson.getJSONObject(i).getJSONArray("tags").toString());
                    return h2geoJson.getJSONObject(i).getJSONArray("tags");
                }
            }
        } catch (JSONException exception) {
            throw new RuntimeException(exception);
        }
        return null;
    }


    /**
     * Parse h2geo.json in a JSONArray object.
     *
     * @param context context to access assets
     */
    private static void instantiateH2geoJson(Context context) {
        if (h2geoJson == null) {
            try {
                h2geoJson = new JSONObject(loadJsonFromAsset(context)).getJSONArray("data");
            } catch (JSONException exception) {
                throw new RuntimeException(exception);
            }
        }
    }

    private static String loadJsonFromAsset(Context context) {
        String json;
        try {
            InputStream is = context.getAssets().open("h2geo.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
        return json;
    }
}
