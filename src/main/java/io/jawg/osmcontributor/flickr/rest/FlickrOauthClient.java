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
package io.jawg.osmcontributor.flickr.rest;

import java.util.Map;

import retrofit.Callback;
import retrofit.http.GET;
import retrofit.http.QueryMap;

public interface FlickrOauthClient {

    /**
     * First step when trying to authenticate an user. Get an oauth_token to begin the process.
     * @param params request params
     * @param response response
     */
    @GET("/request_token")
    void requestToken(@QueryMap Map<String, String> params, Callback<String> response);

    /**
     * Last step when trying to authenticate an user. Get final token to use Flickr API.
     * @param params request params
     * @param response response
     */
    @GET("/access_token")
    void accessToken(@QueryMap Map<String, String> params, Callback<String> response);
}
