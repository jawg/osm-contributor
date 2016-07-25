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
package io.mapsquare.osmcontributor.flickr.model;

import java.util.Map;

/**
 * This class contains all information about the connected user.
 */
public class FlickrUser {

    private String fullname;

    private String oAuthToken;

    private String oAuthTokenSecret;

    private String userNsid;

    private String username;

    public FlickrUser(Map<String, String> userInfos) {
        this.fullname = userInfos.get("fullname");
        this.oAuthToken = userInfos.get("oauth_token");
        this.oAuthTokenSecret = userInfos.get("oauth_token");
        this.userNsid = userInfos.get("user_nsid");
        this.username = userInfos.get("username");
    }

    public String getFullname() {
        return fullname;
    }

    public String getoAuthToken() {
        return oAuthToken;
    }

    public String getoAuthTokenSecret() {
        return oAuthTokenSecret;
    }

    public String getUserNsid() {
        return userNsid;
    }

    public String getUsername() {
        return username;
    }
}
