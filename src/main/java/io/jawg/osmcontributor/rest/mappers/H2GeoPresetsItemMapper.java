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
package io.jawg.osmcontributor.rest.mappers;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.inject.Inject;

import io.jawg.osmcontributor.model.entities.H2GeoPresetsItem;
import io.jawg.osmcontributor.rest.dtos.dma.H2GeoPresetsItemDto;

public class H2GeoPresetsItemMapper {

    String language = "";

    @Inject
    public H2GeoPresetsItemMapper() {
        language = Locale.getDefault().getLanguage();
        if (language.isEmpty()) {
            language = "en";
        }
    }

    public Map<String, H2GeoPresetsItem> convertToH2GeoPresetsItem(
        Map<String, H2GeoPresetsItemDto> h2GeoPresetsItemDtos) {
        Map<String, H2GeoPresetsItem> h2GeoPresetsItems = new HashMap<>();
        if (h2GeoPresetsItemDtos == null || h2GeoPresetsItemDtos.isEmpty()) {
            return h2GeoPresetsItems;
        }

        for (Map.Entry<String, H2GeoPresetsItemDto> entry : h2GeoPresetsItemDtos.entrySet()) {
            H2GeoPresetsItemDto h2GeoPresetsItemDto = entry.getValue();
            H2GeoPresetsItem h2GeoPresetsItem = new H2GeoPresetsItem();
            h2GeoPresetsItem.setName(getCurrentLanguage(h2GeoPresetsItemDto.getName()));
            h2GeoPresetsItem.setDescription(getCurrentLanguage((h2GeoPresetsItemDto.getDescription())));
            h2GeoPresetsItem.setFile(h2GeoPresetsItemDto.getFile());
            h2GeoPresetsItem.setImage(h2GeoPresetsItemDto.getImage());
            h2GeoPresetsItems.put(entry.getKey(), h2GeoPresetsItem);
        }

        return h2GeoPresetsItems;
    }

    public String getCurrentLanguage(Map<String, String> map) {
        if (map != null && map.containsKey(language)) {
            return map.get(language);
        }
        if (map != null && map.containsKey("en")) {
            return map.get("en");
        }
        return "No name provided";
    }
}