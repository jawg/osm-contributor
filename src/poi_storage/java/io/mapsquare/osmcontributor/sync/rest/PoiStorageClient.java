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

    /**
     * Get a Poi from the backend with the requested id.
     *
     * @param backendId Id of the Poi in the backend?
     * @return <dl>
     * <dt>404-NOT FOUND</dt><dd>the poi does not exist</dd>
     * <dt>200-OK</dt><dd>The PoiDto.</dd>
     * </dl>
     */
    @GET("/poi/{id}")
    PoiDto getPoi(@Path("id") String backendId);

    /**
     * Delete a Poi from the backend with the requested id.
     * <br/>
     * Authentication needed.
     *
     * @param backendId Id of the Poi to delete.
     * @return <dl>
     * <dt>404-NOT FOUND</dt><dd>the poi does not exist</dd>
     * <dt>403-FORBIDDEN</dt><dd>if you're not logged</dd>
     * <dt>200-OK</dt><dd>The backend response.</dd>
     * </dl>
     */
    @DELETE("/poi/{id}")
    Response deletePoi(@Path("id") String backendId);

    /**
     * Create a Poi in the backend.
     * <br/>
     * Authentication needed.
     *
     * @param poi The Poi to create.
     * @return The result of the creation.
     */
    @POST("/poi")
    CreationOrUpdateResult createPoi(@Body PoiDto poi);

    /**
     * Update a Poi in the backend.
     * <br/>
     * Authentication needed.
     *
     * @param backendId The backend id of the Poi to update.
     * @param poi       The Poi to update.
     * @return <dl>
     * <dt>404-NOT FOUND</dt><dd>the poi does not exist</dd>
     * <dt>403-FORBIDDEN</dt><dd>if you're not logged</dd>
     * <dt>400-BAD REQUEST</dt><dd>if there is a problem with the PoiDto</dd>
     * <dt>200-OK</dt><dd>The result of the update.</dd>
     * </dl>
     */
    @POST("/poi/{id}")
    CreationOrUpdateResult updatePoi(@Path("id") String backendId, @Body PoiDto poi);

    /**
     * Get all the Pois who where added or modified after the given date and who are in the bounds.
     *
     * @param since The date after which we want Pois, can be null.
     * @param north The north bound, can be null.
     * @param west  The west bound, can be null.
     * @param south The south bound, can be null.
     * @param east  The east bound, can be null.
     * @return The List of Pois.
     */
    @GET("/poi/")
    List<PoiDto> getPois(@Query("after") String since, @Query("lat1") Double north, @Query("lng1") Double west, @Query("lat2") Double south, @Query("lng2") Double east);

    /**
     * Get a PoiType from the backend with the requested id.
     *
     * @param backendId Id of the PoiType in the backend?
     * @return <dl>
     * <dt>404-NOT FOUND</dt><dd>the type does not exist</dd>
     * <dt>200-OK</dt><dd>The TypeDto.</dd>
     * </dl>
     */
    @GET("/type/{id}")
    TypeDto getType(@Path("id") String backendId);

    /**
     * Delete a PoiType from the backend with the requested id.
     * <br/>
     * Authentication needed.
     *
     * @param backendId Id of the PoiType to delete.
     * @return <dl>
     * <dt>404-NOT FOUND</dt><dd>the type does not exist</dd>
     * <dt>403-FORBIDDEN</dt><dd>if you're not logged</dd>
     * <dt>200-OK</dt><dd>The backend response.</dd>
     * </dl>
     */
    @DELETE("/type/{id}")
    Response deleteType(@Path("id") String backendId);

    /**
     * Create a PoiType in the backend with the given name. Return a template of Type with the backend id.
     * <br/>
     * To set the others parameters, complete the template and send it back with {@link #updateType(TypeDto)}.
     * <br/>
     * Authentication needed.
     *
     * @param name The name of the PoiType.
     * @return <dl>
     * <dt>403-FORBIDDEN</dt><dd>if you're not logged</dd>
     * <dt>200-OK</dt><dd>The created Type.</dd>
     * </dl>
     */
    @POST("/type/{name}")
    TypeDto createType(@Path("name") String name);

    /**
     * Update a PoiType in the backend.
     * <br/>
     * Authentication needed.
     *
     * @param typeDto The updated PoiType.
     * @return <dl>
     * <dt>403-FORBIDDEN</dt><dd>if you're not logged</dd>
     * <dt>400-BAD REQUEST</dt><dd>if there is a problem with the typeDto</dd>
     * <dt>200-OK</dt><dd>The backend response.</dd>
     * </dl>
     */
    @POST("/type")
    Response updateType(@Body TypeDto typeDto);

    /**
     * Get all the PoiTypes of the backend.
     *
     * @return The TypeDtos.
     */
    @GET("/type")
    List<TypeDto> getTypes();

    /**
     * Log the user in the backend.
     * <br/>
     * Set a Session-token header in the response
     *
     * @param userDto The credentials.
     * @return <dl>
     * <dt>401-UNAUTHORIZED</dt><dd>if user credentials are wrong</dd>
     * <dt>200-OK</dt><dd>the current user</dd>
     * </dl>
     */
    @POST("/user/login")
    Response login(@Body UserDto userDto);

}
