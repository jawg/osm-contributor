/**
 * Copyright (C) 2019 Takima
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
package io.jawg.osmcontributor.rest.clients;

import io.jawg.osmcontributor.rest.dtos.osm.OsmBlockDto;
import io.jawg.osmcontributor.rest.dtos.osm.OsmDto;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface OverpassRestClient {

    @POST("0.6")
    Call<OsmDto> sendRequest(@Body String data);

    @POST("0.6")
    Call<OsmBlockDto> sendRequestBlock(@Body String data);
}
