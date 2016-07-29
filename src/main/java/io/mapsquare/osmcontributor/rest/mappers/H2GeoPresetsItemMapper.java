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

import io.mapsquare.osmcontributor.model.entities.H2GeoPresetsItem;
import io.mapsquare.osmcontributor.rest.dtos.dma.H2GeoPresetsItemDto;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import javax.inject.Inject;

public class H2GeoPresetsItemMapper {

    String language = "";

    @Inject public H2GeoPresetsItemMapper() {
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
            h2GeoPresetsItem.setName(getName(h2GeoPresetsItemDto.getName()));
            h2GeoPresetsItem.setDescription(h2GeoPresetsItemDto.getDescription());
            h2GeoPresetsItem.setFile(h2GeoPresetsItemDto.getFile());
            h2GeoPresetsItems.put(entry.getKey(), h2GeoPresetsItem);
        }

        return h2GeoPresetsItems;
    }

    private String getName(Map<String, String> mapName) {
        if (mapName != null && mapName.containsKey(language)) {
            return mapName.get(language);
        }
        if (mapName != null && mapName.containsKey("en")) {
            return mapName.get("en");
        }
        return "No name provided";
    }
}