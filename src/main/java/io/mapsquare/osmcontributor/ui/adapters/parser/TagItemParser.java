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
import android.content.Context;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.mapsquare.osmcontributor.model.entities.PoiType;
import io.mapsquare.osmcontributor.model.entities.PoiTypeTag;
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
public class TagItemParser {
    private static final String TAG = "TagItemParser";

    private JSONArray h2geoJson;

    @Inject
    public TagItemParser(Application application) {
        instantiateH2geoJson(application.getApplicationContext());
    }

    /**
     * The main idea of this method is to get the tag of poi type and return, for each tag,
     * the widget to use.
     *
     * This is a first proposition.
     *
     * @param poiType poi type to process
     * @return map of tag name with the widget corresponding
     */
    public Map<String, TagItem.TagType> getTagTypeMap(PoiType poiType) {
        // Map to return
        Map<String, TagItem.TagType> tagTypes = new HashMap<>();

        // Loop over tag of the poi type
        for (PoiTypeTag tag : poiType.getTags()) {
            // Print all tag key / value just for info
            //Log.i(TAG, tag.getKey() + "=" + tag.getValue());

            // Get tag info about the current poi type from h2geo file
            JSONArray tagsInfoFromH2geo = getTagsInfoFromH2Geo(tag.getKey() + "=" + tag.getValue());

            try {
                if (tagsInfoFromH2geo != null) {
                    for (int i = 0; i < tagsInfoFromH2geo.length(); i++) {
                        String key = tagsInfoFromH2geo.getJSONObject(i).getString("key");
                        if (key.equals("opening_hours")) {
                            tagTypes.put(key, TagItem.TagType.OPENING_HOURS);
                        } else if (tagsInfoFromH2geo.getJSONObject(i).has("possibleValues")) {
                            // Get possible values. If exists, possible values can be yes / no, a name,
                            // a number, 24 / 7, a word.
                            tagTypes.put(key, TagItem.TagType.MULTI_CHOICE);
                        } else {
                            tagTypes.put(key, TagItem.TagType.TEXT);
                        }
                    }
                }
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        }
        return tagTypes;
    }

    /**
     * The main idea of this method is to get the tag item type of the tag key
     *
     * This is a first proposition.
     *
     * @param poiType poi type to process
     * @return the tag item type enum
     */
    public TagItem.TagType getTagTypeForKey(PoiType poiType) {
        // Loop over tag of the poi type
        for (PoiTypeTag tag : poiType.getTags()) {
            // Get tag info about the current poi type from h2geo file
            JSONArray tagsInfoFromH2geo = getTagsInfoFromH2Geo(tag.getKey() + "=" + tag.getValue());

            try {
                if (tagsInfoFromH2geo != null) {
                    for (int i = 0; i < tagsInfoFromH2geo.length(); i++) {
                        if (tagsInfoFromH2geo.getJSONObject(i).getString("key").equals("opening_hours")) {
                            return TagItem.TagType.OPENING_HOURS;
                        } else if (tagsInfoFromH2geo.getJSONObject(i).has("possibleValues")) {
                            // Get possible values. If exists, possible values can be yes / no, a name,
                            // a number, 24 / 7, a word.
                            return TagItem.TagType.MULTI_CHOICE;
                        } else {
                            return TagItem.TagType.TEXT;
                        }
                    }
                }
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        }
        return null;
    }




    /**
     * Get the info tag from h2geo.
     *
     * @param name Name of poi type (ex : amenity=pharmacy or shop=e-cig)
     * @return a Json Array with all tags for the poi type
     */
    private JSONArray getTagsInfoFromH2Geo(String name) {
        try {
            for (int i = 0; i < h2geoJson.length(); i++) {
                if (h2geoJson.getJSONObject(i).getString("name").equals(name)) {
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
    private void instantiateH2geoJson(Context context) {
        if (h2geoJson == null) {
            try {
                h2geoJson = new JSONObject(loadJsonFromAsset(context)).getJSONArray("data");
            } catch (JSONException exception) {
                throw new RuntimeException(exception);
            }
        }
    }

    private String loadJsonFromAsset(Context context) {
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

    public String parseTagName(String tagName) {
        return Character.toUpperCase(tagName.charAt(0)) + tagName.substring(1).replace("_", " ");
    }

    public void parseOpeningHoursToValue() {

    }
}
