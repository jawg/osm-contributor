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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import io.mapsquare.osmcontributor.core.model.PoiType;
import io.mapsquare.osmcontributor.core.model.PoiTypeTag;
import io.mapsquare.osmcontributor.sync.dto.dma.PoiTypeDto;
import io.mapsquare.osmcontributor.sync.dto.dma.PoiTypeTagDto;

public class PoiTypeConverter {

    @Inject
    public PoiTypeConverter() {
    }

    private List<String> supportedTagTypes = Arrays.asList("text", "number", "on/off");

    private PoiType convert(PoiTypeDto dto) {
        PoiType type = new PoiType();
        type.setName(dto.getName());
        type.setIcon(dto.getName());

        int ordinal = 0;
        if (dto.getTags() != null) {
            ArrayList<PoiTypeTag> tags = new ArrayList<>(dto.getTags().size());
            type.setTags(tags);
            for (PoiTypeTagDto tagDto : dto.getTags()) {
                if (supportedTagTypes.contains(tagDto.getType())) {
                    PoiTypeTag poiTypeTag = new PoiTypeTag();
                    poiTypeTag.setPoiType(type);
                    poiTypeTag.setKey(tagDto.getKey());
                    poiTypeTag.setValue(tagDto.getValue());
                    poiTypeTag.setMandatory(tagDto.isMandatory());
                    poiTypeTag.setOrdinal(ordinal++);
                    tags.add(poiTypeTag);
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
}
