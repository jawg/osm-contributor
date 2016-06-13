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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.inject.Inject;

import io.mapsquare.osmcontributor.model.entities.PoiTag;
import io.mapsquare.osmcontributor.rest.dtos.osm.TagDto;

public class PoiTagMapper {

    @Inject
    public PoiTagMapper() {
    }

    public List<TagDto> convertFromPoiTag(Collection<PoiTag> poiTags) {
        List<TagDto> result = new ArrayList<>();
        for (PoiTag tag : poiTags) {
            TagDto tagDto = new TagDto();

            tagDto.setKey(tag.getKey());
            tagDto.setValue(tag.getValue());

            result.add(tagDto);
        }
        return result;
    }
}