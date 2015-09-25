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
package io.mapsquare.osmcontributor.sync.rest;

import java.util.List;

import io.mapsquare.osmcontributor.sync.dto.poistorage.CreationOrUpdateResult;
import io.mapsquare.osmcontributor.sync.dto.poistorage.PoiDto;
import io.mapsquare.osmcontributor.sync.dto.poistorage.TypeDto;
import io.mapsquare.osmcontributor.sync.dto.poistorage.UserDto;
import retrofit.client.Response;
import retrofit.http.Body;
import retrofit.http.DELETE;
import retrofit.http.GET;
import retrofit.http.POST;
import retrofit.http.Path;
import retrofit.http.Query;

public interface PoiStorageClient {

    @GET("/poi/{id}")
    PoiDto getPoi(@Path("id") String backendId);

    @DELETE("/poi/{id}")
    Response deletePoi(@Path("id") String backendId);

    @POST("/poi")
    CreationOrUpdateResult createPoi(@Body PoiDto poi);

    @POST("/poi/{id}")
    CreationOrUpdateResult updatePoi(@Path("id") String backendId, @Body PoiDto poi);

    @GET("/poi/")
    List<PoiDto> getPois(@Query("after") String since, @Query("bounding_box") String boundingBox, @Query("bounding_circle") String boundingCircle);

    @GET("/type/{id}")
    TypeDto getType(@Path("id") String backendId);

    @DELETE("/type/{id}")
    Response deleteType(@Path("id") String backendId);

    @POST("/type")
    CreationOrUpdateResult createOrUpdateType(@Body TypeDto poi);

    @GET("/type")
    List<TypeDto> getTypes();

    @POST("/user/login")
    Response login(@Body UserDto userDto);

}
