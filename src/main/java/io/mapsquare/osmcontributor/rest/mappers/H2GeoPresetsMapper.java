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

import javax.inject.Inject;

import io.mapsquare.osmcontributor.model.entities.H2GeoPresets;
import io.mapsquare.osmcontributor.rest.dtos.dma.H2GeoPresetsDto;

public class H2GeoPresetsMapper {
  H2GeoPresetsItemMapper h2GeoPresetsItemMapper;

  @Inject
  public H2GeoPresetsMapper(H2GeoPresetsItemMapper h2GeoPresetsItemMapper) {
    this.h2GeoPresetsItemMapper = h2GeoPresetsItemMapper;
  }

  public H2GeoPresets convertToH2GeoPresets(H2GeoPresetsDto h2GeoPresetsDto) {
    if (h2GeoPresetsDto == null) {
      return null;
    }
    H2GeoPresets h2GeoPresets = new H2GeoPresets();
    h2GeoPresets.setLastUpdate(h2GeoPresetsDto.getLastUpdate());
    h2GeoPresets.setPresets(
        h2GeoPresetsItemMapper.convertToH2GeoPresetsItem(h2GeoPresetsDto.getPresets()));
    h2GeoPresets.setRevision(h2GeoPresetsDto.getRevision());
    return h2GeoPresets;
  }
}