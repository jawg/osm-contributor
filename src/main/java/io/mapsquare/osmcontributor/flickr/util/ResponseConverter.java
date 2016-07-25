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
package io.mapsquare.osmcontributor.flickr.util;


import java.util.HashMap;
import java.util.Map;

/**
 * Convert response from Flickr into a map.
 */
public class ResponseConverter {

    /**
     * Flickr returns response like :
     * oauth_callback_confirmed=true&oauth_token=72157670587824772-23f323e2bbf56f24&oauth_token_secret=b783aaac0d6b54c2
     * It's not JSON so we split it to get a map with key/value.
     * @param responseOauthRequest response from an OAuth request
     * @return a map with key/value for each element present in response
     */
    public static Map<String, String> convertOAuthResponse(String responseOauthRequest) {
        Map<String, String> responseElements = new HashMap<>();
        String[] elements = responseOauthRequest.split("&");
        for (String element : elements) {
            String[] keyValue = element.split("=");
            responseElements.put(keyValue[0], keyValue[1]);
        }
       return responseElements;
    }

    /**
     * Used when trying to obtain oauth_token and oauth_verifier. Parsing is not the same as
     * previous method.
     * @param urlToConvert url to convert
     * @return a map containing oauth_token and oauth_verifier
     */
    public static Map<String, String> convertOAuthUrl(String urlToConvert) {
        Map<String, String> responseElements = new HashMap<>();
        String[] elements = urlToConvert.split("\\?");
        elements = elements[1].split("&");
        for (String element : elements) {
            String[] keyValue = element.split("=");
            responseElements.put(keyValue[0], keyValue[1]);
        }
        return responseElements;
    }
}
