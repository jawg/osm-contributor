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

import io.mapsquare.osmcontributor.model.entities.H2GeoPresetPoi;
import io.mapsquare.osmcontributor.rest.dtos.dma.H2GeoPresetPoiDto;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;

public class H2GeoPresetPoiMapper {

  private H2GeoPresetPoiTagMapper h2GeoPresetPoiTagMapper;

  @Inject public H2GeoPresetPoiMapper(H2GeoPresetPoiTagMapper h2GeoPresetPoiTagMapper) {
    this.h2GeoPresetPoiTagMapper = h2GeoPresetPoiTagMapper;
  }

  public List<H2GeoPresetPoi> convertToH2GeoPresetPois(List<H2GeoPresetPoiDto> h2GeoPresetPoiDtos) {
    List<H2GeoPresetPoi> h2GeoPresetPois = new ArrayList<>();
    if (h2GeoPresetPoiDtos == null || h2GeoPresetPoiDtos.isEmpty()) {
      return h2GeoPresetPois;
    }

    for (H2GeoPresetPoiDto h2GeoPresetPoiDto : h2GeoPresetPoiDtos) {
      H2GeoPresetPoi h2GeoPresetPoi = new H2GeoPresetPoi();
      h2GeoPresetPoi.setDescription(h2GeoPresetPoiDto.getDescription());
      h2GeoPresetPoi.setKeywords(h2GeoPresetPoiDto.getKeywords());
      h2GeoPresetPoi.setLabel(h2GeoPresetPoiDto.getLabel());
      h2GeoPresetPoi.setName(h2GeoPresetPoiDto.getName());
      h2GeoPresetPoi.setTags(
          h2GeoPresetPoiTagMapper.convertToH2GeoPresetPoiTag(h2GeoPresetPoiDto.getTags()));
      h2GeoPresetPoi.setUrl(h2GeoPresetPoiDto.getUrl());
      h2GeoPresetPois.add(h2GeoPresetPoi);
    }

    return h2GeoPresetPois;
  }
}