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
package io.jawg.osmcontributor.rest.clients;

import io.jawg.osmcontributor.rest.dtos.dma.H2GeoDto;
import io.jawg.osmcontributor.rest.dtos.dma.H2GeoPresetsDto;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface H2GeoPresetsRestClient {

  /**
   * Load the list of available profiles.
   *
   * @return A wrapper of H2Geo presets descriptor DTO.
   */
  @GET("presets.json") Call<H2GeoPresetsDto> loadProfiles();

  /**
   * Load a specified profile.
   *
   * @param filename Name of the profile file to load.
   */
  @GET("{profile}") Call<H2GeoDto> loadProfile(@Path("profile") String filename);
}
