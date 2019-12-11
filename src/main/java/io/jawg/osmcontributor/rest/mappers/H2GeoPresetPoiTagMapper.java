/**
 * Copyright (C) 2019 Takima
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

import io.jawg.osmcontributor.model.entities.H2GeoPresetPoiTag;
import io.jawg.osmcontributor.rest.dtos.dma.H2GeoPresetPoiTagDto;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;

public class H2GeoPresetPoiTagMapper {

  @Inject public H2GeoPresetPoiTagMapper() {
  }

  public List<H2GeoPresetPoiTag> convertToH2GeoPresetPoiTag(
      List<H2GeoPresetPoiTagDto> h2GeoPresetPoiTagDtos) {
    List<H2GeoPresetPoiTag> h2GeoPresetPoiTags = new ArrayList<>();
    if (h2GeoPresetPoiTagDtos == null || h2GeoPresetPoiTagDtos.isEmpty()) {
      return h2GeoPresetPoiTags;
    }
    for (H2GeoPresetPoiTagDto h2GeoPresetPoiTagDto : h2GeoPresetPoiTagDtos) {
      H2GeoPresetPoiTag h2GeoPresetPoiTag = new H2GeoPresetPoiTag();
      h2GeoPresetPoiTag.setKey(h2GeoPresetPoiTagDto.getKey());
      h2GeoPresetPoiTag.setType(h2GeoPresetPoiTagDto.getType());
      h2GeoPresetPoiTag.setValues(h2GeoPresetPoiTagDto.getValues());
      h2GeoPresetPoiTags.add(h2GeoPresetPoiTag);
    }
    return h2GeoPresetPoiTags;
  }
}