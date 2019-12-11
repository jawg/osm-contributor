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
package io.jawg.osmcontributor.flickr.oauth;


import com.github.scribejava.core.model.Verb;

import java.util.Map;
import java.util.TreeMap;

import io.jawg.osmcontributor.flickr.util.FlickrSecurityUtils;

public class OAuthRequest {

    private static final String SEPARATOR = "&";

    /*=========================================*/
    /*------------ATTRIBUTES-------------------*/
    /*=========================================*/
    private Map<String, String> params;

    private String apiKey;

    private String apiKeySecret;

    private String requestUrl;

    private String oAuthToken;

    private String oAuthTokenSecret;

    /*=========================================*/
    /*------------CONSTRUCTORS-----------------*/
    /*=========================================*/
    public OAuthRequest(String apiKey, String apiKeySecret) {
        this.apiKey = apiKey;
        this.apiKeySecret = apiKeySecret;
        this.params = new TreeMap<>();
    }

    /*=========================================*/
    /*----------------CODE---------------------*/
    /*=========================================*/
    public void initParam(Map<String, String> params) {
        this.params = new TreeMap<>();
        this.params.put("oauth_consumer_key", apiKey);
        for (Map.Entry<String, String> param : params.entrySet()) {
            this.params.put(param.getKey(), param.getValue());
        }
    }

    /**
     * Sign request.
     */
    public void signRequest(Verb verb) {
        String convertedUrl = FlickrSecurityUtils.convertUrl(requestUrl, verb, params);
        if (convertedUrl != null) {
            params.put("oauth_signature", FlickrSecurityUtils.getSignatureFromRequest(convertedUrl,
                    apiKeySecret + SEPARATOR + (oAuthTokenSecret == null ? "" : oAuthTokenSecret)));
        }
    }

    /**
     * Get request as URL.
     * @return request url
     */
    public String toUrl() {
        StringBuilder url = new StringBuilder(requestUrl).append("?");
        for (Map.Entry<String, String> param: params.entrySet()) {
            url.append(param.getKey()).append("=").append(param.getValue()).append("&");
        }

        int lastIndexEsp = url.lastIndexOf("&");
        if (lastIndexEsp >= 0) {
            url.deleteCharAt(lastIndexEsp);
        }
        return url.toString();
    }

    /*=========================================*/
    /*--------------GETTERS--------------------*/
    /*=========================================*/
    public Map<String, String> getParams() {
        return params;
    }

    public String getOAuthToken() {
        return oAuthToken;
    }

    /*=========================================*/
    /*---------------SETTERS-------------------*/
    /*=========================================*/
    public void setOAuthToken(String oAuthToken) {
        this.oAuthToken = oAuthToken;
    }

    public void setOAuthTokenSecret(String oAuthTokenSecret) {
        this.oAuthTokenSecret = oAuthTokenSecret;
    }

    public void setRequestUrl(String requestUrl) {
        this.requestUrl = requestUrl;
    }
}