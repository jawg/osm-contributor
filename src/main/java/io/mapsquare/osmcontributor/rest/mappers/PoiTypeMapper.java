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
package io.mapsquare.osmcontributor.rest.mappers;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;

import org.joda.time.DateTime;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.inject.Inject;

import io.mapsquare.osmcontributor.model.entities.PoiType;
import io.mapsquare.osmcontributor.model.entities.PoiTypeTag;
import io.mapsquare.osmcontributor.rest.dtos.dma.PoiTypeDto;
import io.mapsquare.osmcontributor.rest.dtos.dma.PoiTypeTagDto;

public class PoiTypeMapper {
    private static final String TAG = "PoiTypeMapper";
    private String language;
    private DateTime dateTime;
    private Gson gson;

    @Inject
    public PoiTypeMapper() {
        language = Locale.getDefault().getLanguage();
        if (language.isEmpty()) {
            language = "en";
        }
        dateTime = new DateTime(0);
        gson = new Gson();
    }

    private PoiType convert(PoiTypeDto dto) {
        PoiType type = new PoiType();
        type.setName(getTranslationFormJson(dto.getLabels(), getName(dto.getName())));
        type.setTechnicalName(dto.getName());
        type.setDescription(getTranslationFormJson(dto.getDescription(), ""));
        type.setKeyWords(getKeywordsFormJson(dto.getKeyWords()));
        type.setIcon(getName(dto.getName()));
        type.setUsageCount(dto.getUsageCount());
        // When creating a new PoiType from file, put the same date of last use to all new PoiTypes : 1970-01-01T00:00:00Z
        type.setLastUse(dateTime);

        int ordinal = 0;
        if (dto.getTags() != null) {
            ArrayList<PoiTypeTag> tags = new ArrayList<>(dto.getTags().size());
            type.setTags(tags);
            for (PoiTypeTagDto tagDto : dto.getTags()) {
                // If the tag is implied, do not keep it
                if (!tagDto.isImplied()) {
                    PoiTypeTag poiTypeTag = new PoiTypeTag();
                    poiTypeTag.setPoiType(type);
                    poiTypeTag.setKey(tagDto.getKey());
                    poiTypeTag.setValue(tagDto.getValue());
                    poiTypeTag.setMandatory(tagDto.isMandatory());
                    poiTypeTag.setOrdinal(ordinal++);
                    poiTypeTag.setPossibleValues(getPossibleValues(tagDto.getPossibleValues()));
                    poiTypeTag.setTagType(tagDto.getType());
                    tags.add(poiTypeTag);
                    Log.i(TAG, "convert: " + poiTypeTag);
                }
            }
        }
        return type;
    }

    public List<PoiType> convert(List<PoiTypeDto> dtos) {
        if (dtos == null) {
            return null;
        }
        ArrayList<PoiType> result = new ArrayList<>(dtos.size());
        for (PoiTypeDto dto : dtos) {
            result.add(convert(dto));
        }
        return result;
    }

    /**
     * A name looks like "amenity=restaurant". This method return the part after the "=" symbol.
     * If there was no "=" symbol or more than one, return an empty String.
     *
     * @param str The String to split.
     * @return The part after the "=" symbol.
     */
    private String getName(String str) {
        if (str != null) {
            String[] splits = str.split("=");
            if (splits.length == 2) {
                return splits[1];
            }
        }
        return "";
    }

    /**
     * Get the value of the JsonElement in the language of the system. If the language is not found, search for English.
     * If the language is still not found, put the default name.
     *
     * @param jsonElement The json element containing a value in many languages.
     * @param defaultName The default value if no translation was found for the system language or in English.
     * @return The value of the element for the system language or in English.
     */
    private String getTranslationFormJson(JsonElement jsonElement, String defaultName) {
        if (jsonElement != null) {
            Type mapType = (new TypeToken<Map<String, String>>() {
            }).getType();
            Map<String, String> map = gson.fromJson(jsonElement, mapType);

            // if there is a translation for the user language
            if (map.containsKey(language)) {
                return map.get(language);
            }
            // else we look for the english translation
            if (map.containsKey("en")) {
                return map.get("en");
            }
        }

        // if we don't have the translation we use the name from the JSON file
        return defaultName;
    }

    /**
     * Get the keywords from a JsonElement in the language of the system.
     *
     * @param jsonElement The json element containing the keywords in many languages.
     * @return The list of keywords.
     */
    private String getKeywordsFormJson(JsonElement jsonElement) {
        StringBuilder stringBuilder = new StringBuilder();
        Type mapType = (new TypeToken<Map<String, List<String>>>() {
        }).getType();
        Map<String, List<String>> map = gson.fromJson(jsonElement, mapType);

        // if there is a translation for the user language
        if (map != null && map.containsKey(language)) {
            List<String> strList = map.get(language);
            for (String keyword : strList) {
                stringBuilder.append(keyword);
                stringBuilder.append(" ");
            }
        }

        return stringBuilder.toString();
    }

    /**
     * Get the possible values from a JsonElement as a String with each values separated by a Group Separator character (ASCII character 29).
     *
     * @param jsonElement The jsonElement containing the possible values.
     * @return The possible values.
     */
    private String getPossibleValues(JsonElement jsonElement) {
        StringBuilder possibleValues = new StringBuilder();

        if (jsonElement != null) {
            List<String> values = gson.fromJson(jsonElement, (new TypeToken<List<String>>() {
            }).getType());
            Iterator<String> it = values.iterator();
            if (it.hasNext()) {
                possibleValues.append(it.next());
                while (it.hasNext()) {
                    possibleValues.append((char) 29).append(it.next());
                }
            }
        }
        return possibleValues.toString();
    }
}
