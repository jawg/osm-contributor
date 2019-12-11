/**
 * Copyright (C) 2019 Takima
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
package io.jawg.osmcontributor.flickr.util;


import java.sql.Timestamp;
import java.util.Date;
import java.util.UUID;

import io.jawg.osmcontributor.rest.utils.MapParams;

public class OAuthParams {

    public static final String OAUTH_TOKEN = "oauth_token";

    public static final String OAUTH_TOKEN_SECRET = "oauth_token_secret";

    public static final String OAUTH_VERIFIER = "oauth_verifier";

    public static final String OAUTH_CALLBACK = "oauth_callback";

    public static final String OAUTH_CONSUMER_KEY = "oauth_consumer_key";

    public static final String OAUTH_CONSUMER_SECRET = "oauth_consumer_secret";

    public static final String OAUTH_TIMESTAMP = "oauth_timestamp";

    public static final String OAUTH_NONCE = "oauth_nonce";

    public static final String OAUTH_VERSION = "oauth_version";

    public static final String OAUTH_SIGNATURE_METHOD = "oauth_signature_method";

    /**
     * Flickr use OAuth 1.0.
     */
    public static String VERSION = "1.0";

    /**
     * Flick use HMAC-SHA1 to sign request.
     */
    public static String SIGNATURE_METHOD =  "HMAC-SHA1";

    /**
     * Get OAuth param for all request.
     * @return map with params. Call to map to get a Map.
     */
    public static MapParams<String, String> getOAuthParams() {
        return new MapParams<String, String>()
                .put(OAUTH_TIMESTAMP, String.valueOf(new Timestamp(new Date().getTime()).getTime()).substring(0, 10))
                .put(OAUTH_NONCE, UUID.randomUUID().toString().substring(0, 6))
                .put(OAUTH_VERSION, VERSION)
                .put(OAUTH_SIGNATURE_METHOD, SIGNATURE_METHOD);
    }
}