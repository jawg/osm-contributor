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

import io.mapsquare.osmcontributor.model.entities.H2GeoPreset;
import io.mapsquare.osmcontributor.rest.dtos.dma.H2GeoPresetDto;
import javax.inject.Inject;

public class H2GeoPresetMapper {

  H2GeoPresetPoiMapper h2GeoPresetPoiMapper;

  @Inject public H2GeoPresetMapper(H2GeoPresetPoiMapper h2GeoPresetPoiMapper) {
    this.h2GeoPresetPoiMapper = h2GeoPresetPoiMapper;
  }

  public H2GeoPreset convertToH2GeoPreset(H2GeoPresetDto h2GeoPresetDto) {
    if (h2GeoPresetDto == null) {
      return null;
    }
    H2GeoPreset h2GeoPreset = new H2GeoPreset();
    h2GeoPreset.setData(h2GeoPresetPoiMapper.convertToH2GeoPresetPois(h2GeoPresetDto.getData()));
    h2GeoPreset.setDescription(h2GeoPresetDto.getDescription());
    h2GeoPreset.setName(h2GeoPresetDto.getName());
    return h2GeoPreset;
  }
}