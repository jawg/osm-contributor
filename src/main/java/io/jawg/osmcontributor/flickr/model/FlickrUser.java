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
package io.jawg.osmcontributor.flickr.model;

import java.util.Map;

import io.jawg.osmcontributor.flickr.util.OAuthParams;

/**
 * This class contains all information about the connected user.
 */
public class FlickrUser {

    private String oAuthToken;

    private String oAuthTokenSecret;

    public FlickrUser(Map<String, String> userInfos) {
        this.oAuthToken = userInfos.get(OAuthParams.OAUTH_TOKEN);
        this.oAuthTokenSecret = userInfos.get(OAuthParams.OAUTH_TOKEN_SECRET);
    }

    public String getoAuthToken() {
        return oAuthToken;
    }

    public String getoAuthTokenSecret() {
        return oAuthTokenSecret;
    }
}
