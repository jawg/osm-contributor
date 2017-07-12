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
package io.jawg.osmcontributor.rest.security;

import java.sql.Timestamp;
import java.util.Date;
import java.util.UUID;


import io.jawg.osmcontributor.rest.utils.MapParams;

public class OAuthParams {
    /**
     * Flickr use OAuth 1.0.
     */
    public static final String OAUTH_VERSION = "1.0";

    /**
     * Flick use HMAC-SHA1 to sign request.
     */
    public static final String SIGNATURE_METHOD = "HMAC-SHA1";

    public static MapParams<String, String> getOAuthParams() {

        return new MapParams<String, String>()
                .put("oauth_timestamp", String.valueOf(new Timestamp(new Date().getTime()).getTime()).substring(0, 10))
                .put("oauth_nonce", UUID.randomUUID().toString().substring(0, 6))
                .put("oauth_version", OAUTH_VERSION)
                .put("oauth_signature_method", SIGNATURE_METHOD);
    }
}
