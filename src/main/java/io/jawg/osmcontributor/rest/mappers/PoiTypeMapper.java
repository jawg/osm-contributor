/**
 * Copyright (C) 2016 eBusiness Information
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
package io.jawg.osmcontributor.rest.mappers;

import com.google.gson.Gson;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.inject.Inject;

import io.jawg.osmcontributor.model.entities.Action;
import io.jawg.osmcontributor.model.entities.Condition;
import io.jawg.osmcontributor.model.entities.Constraint;
import io.jawg.osmcontributor.model.entities.PoiType;
import io.jawg.osmcontributor.model.entities.PoiTypeTag;
import io.jawg.osmcontributor.model.entities.Source;
import io.jawg.osmcontributor.rest.dtos.dma.ConstraintDto;
import io.jawg.osmcontributor.rest.dtos.dma.PoiTypeDto;
import io.jawg.osmcontributor.rest.dtos.dma.PoiTypeTagDto;

public class PoiTypeMapper {

    public static final String ITEM_SEPARATOR = String.valueOf((char) 29);
    public static final String VALUE_SEPARATOR = ";;";
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
        type.setName(getTranslationFormJson(dto.getLabel(), getName(dto.getName())));
        type.setTechnicalName(dto.getName());
        type.setDescription(getTranslationFormJson(dto.getDescription(), ""));
        type.setKeyWords(getKeywordsFormJson(dto.getKeywords()));
        type.setIcon(getName(dto.getName()));
        // When creating a new PoiType from file, put the same date of last use to all new PoiTypes : 1970-01-01T00:00:00Z
        type.setLastUse(dateTime);
        int ordinal = 0;
        if (dto.getTags() != null) {
            ArrayList<PoiTypeTag> tags = new ArrayList<>(dto.getTags().size());
            type.setTags(tags);
            for (PoiTypeTagDto tagDto : dto.getTags()) {
                // If the tag is implied, do not keep it
                PoiTypeTag poiTypeTag = new PoiTypeTag();
                poiTypeTag.setPoiType(type);
                poiTypeTag.setKey(tagDto.getKey());
                poiTypeTag.setValue(tagDto.getValue());
                poiTypeTag.setMandatory(tagDto.getRequired());
                poiTypeTag.setOrdinal(ordinal++);
                poiTypeTag.setShow(tagDto.getShow());
                if (tagDto.getValues() != null && !tagDto.getValues().isEmpty()) {
                    List<String> possibleValues = new ArrayList<>();
                    for (Map<String, Map<String, String>> valuesMap : tagDto.getValues()) {
                        for (Map.Entry<String, Map<String, String>> value : valuesMap.entrySet()) {
                            possibleValues.add(value.getKey() + VALUE_SEPARATOR + getTranslationFormJson(value.getValue(), ""));
                        }
                    }
                    poiTypeTag.setPossibleValues(getPossibleValues(possibleValues));
                }
                poiTypeTag.setTagType(tagDto.getType());
                tags.add(poiTypeTag);

            }
        }

        ordinal = 0;
        if (dto.getConstraints() != null) {
            ArrayList<Constraint> constraints = new ArrayList<>();
            type.setConstraints(constraints);
            for (ConstraintDto constraintDto : dto.getConstraints()) {
                // Constraint's Source
                Source source = Source.builder()
                        .setType(constraintDto.getSource().getType())
                        .setKey(constraintDto.getSource().getKey())
                        .build();
                // Constraint's Condition
                Condition condition = Condition.builder()
                        .setType(constraintDto.getCondition().getType())
                        .setValue(constraintDto.getCondition().getValue())
                        .build();
                // Constraint's Action
                Action action = Action.builder()
                        .setType(constraintDto.getAction().getType())
                        .setKey(constraintDto.getAction().getKey())
                        .setValue(constraintDto.getAction().getValue())
                        .build();
                // Complete constraint
                Constraint constraint = Constraint.builder()
                        .setPoiType(type)
                        .setSource(source)
                        .setCondition(condition)
                        .setAction(action)
                        .setOrdinal(ordinal++)
                        .build();
                // Add constraint to the list
                constraints.add(constraint);
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
     * @param translation The json element containing a value in many languages.
     * @param defaultName The default value if no translation was found for the system language or in English.
     * @return The value of the element for the system language or in English.
     */
    private String getTranslationFormJson(Map<String, String> translation, String defaultName) {
        if (translation != null) {
            // if there is a translation for the user language
            if (translation.containsKey(language)) {
                return translation.get(language);
            }
            if (translation.containsKey("default")) {
                return translation.get("default");
            }
        }

        // if we don't have the translation we use the name from the JSON file
        return defaultName;
    }

    /**
     * Get the keywords from a JsonElement in the language of the system.
     *
     * @param keywords The json element containing the keywords in many languages.
     * @return The list of keywords.
     */
    private String getKeywordsFormJson(Map<String, List<String>> keywords) {
        StringBuilder stringBuilder = new StringBuilder();
        // if there is a translation for the user language
        if (keywords != null && keywords.containsKey(language)) {
            List<String> strList = keywords.get(language);
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
     * @param values The jsonElement containing the possible values.
     * @return The possible values.
     */
    private String getPossibleValues(List<String> values) {
        StringBuilder possibleValues = new StringBuilder();

        if (values != null) {
            Iterator<String> it = values.iterator();
            if (it.hasNext()) {
                possibleValues.append(it.next());
                while (it.hasNext()) {
                    possibleValues.append(ITEM_SEPARATOR).append(it.next());
                }
            }
        }
        return possibleValues.toString();
    }
}
