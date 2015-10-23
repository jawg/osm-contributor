/**
 * Copyright (C) 2015 eBusiness Information
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
package io.mapsquare.osmcontributor.sync.converter;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.inject.Inject;

import io.mapsquare.osmcontributor.core.model.KeyWord;
import io.mapsquare.osmcontributor.core.model.PoiType;
import io.mapsquare.osmcontributor.core.model.PoiTypeTag;
import io.mapsquare.osmcontributor.sync.dto.dma.PoiTypeDto;
import io.mapsquare.osmcontributor.sync.dto.dma.PoiTypeTagDto;

public class PoiTypeConverter {

    String language;

    @Inject
    public PoiTypeConverter() {
        language = Locale.getDefault().getLanguage();
        if (language.isEmpty()) {
            language = "en";
        }
    }

    private PoiType convert(PoiTypeDto dto) {
        PoiType type = new PoiType();
        type.setName(getTranslationFormJson(dto.getLabels(), type, dto.getName()));
        type.setDescription(getTranslationFormJson(dto.getDescription(), type, ""));
        type.setKeyWords(getKeywordsFormJson(dto.getKeyWords(), type));
        type.setIcon(getName(dto.getName()));
        type.setUsageCount(dto.getUsageCount());

        int ordinal = 0;
        if (dto.getTags() != null) {
            ArrayList<PoiTypeTag> tags = new ArrayList<>(dto.getTags().size());
            type.setTags(tags);
            for (PoiTypeTagDto tagDto : dto.getTags()) {
                PoiTypeTag poiTypeTag = new PoiTypeTag();
                poiTypeTag.setPoiType(type);
                poiTypeTag.setKey(tagDto.getKey());
                poiTypeTag.setValue(tagDto.getValue());
                poiTypeTag.setMandatory(tagDto.isMandatory());
                poiTypeTag.setOrdinal(ordinal++);
                tags.add(poiTypeTag);
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

    private String getName(String str) {
        if (str != null) {
            String[] splits = str.split("=");
            if (splits.length == 2) {
                return splits[1];
            }
        }
        return "";
    }

    private String getTranslationFormJson(JsonElement jsonElement, PoiType type, String defaultName) {
        Type mapType = new TypeToken<Map<String, String>>() { } .getType();
        Gson gson = new Gson();
        Map<String, String> map = gson.fromJson(jsonElement, mapType);

        // if there is a translation for the user language
        if (map.containsKey(language)) {
            return map.get(language);
        }
        // else we look for the english translation
        if (map.containsKey("en")) {
            return map.get("en");
        }

        // if we don't have the translation we use the name from the JSON file
        return defaultName;
    }

    private List<KeyWord> getKeywordsFormJson(JsonElement jsonElement, PoiType poiType) {
        List<KeyWord> keyWords = new ArrayList<>();
        Type mapType = new TypeToken<Map<String, List<String>>>() { } .getType();
        Gson gson = new Gson();
        Map<String, List<String>> map = gson.fromJson(jsonElement, mapType);

        // if there is a translation for the user language
        if (map.containsKey(language)) {
            List<String> strList = map.get(language);
            for (String k : strList) {
                keyWords.add(new KeyWord(k, poiType));
            }
        }

        return keyWords;
    }
}
