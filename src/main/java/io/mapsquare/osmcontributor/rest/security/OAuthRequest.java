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
package io.mapsquare.osmcontributor.rest.security;

import com.github.scribejava.core.model.Verb;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;
import java.util.TreeMap;

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
        this.params.clear();
        this.params.put("oauth_consumer_key", apiKey);
        for (Map.Entry<String, String> param : params.entrySet()) {
            this.params.put(param.getKey(), param.getValue());
        }
    }

    /**
     * Sign request.
     */
    public void signRequest(Verb verb) {
        String convertedUrl = SecurityUtils.convertUrl(requestUrl, verb, params);
        if (convertedUrl != null) {
            params.put("oauth_signature", SecurityUtils.getSignatureFromRequest(convertedUrl,
                    apiKeySecret + SEPARATOR + (oAuthTokenSecret == null ? "" : oAuthTokenSecret)));
        }
    }

    /**
     * Get request as URL.
     * @return request url
     */
    public String toUrl() {
        StringBuilder url = new StringBuilder(requestUrl).append("?");
        for (Map.Entry<String, String> param : params.entrySet()) {
            url.append(param.getKey()).append("=").append(param.getValue()).append("&");
        }
        url.deleteCharAt(url.lastIndexOf("&"));
        return url.toString();
    }

    /**
     * Encode the request parameters in http format
     */
    public void encodeParams() {
        for (Map.Entry<String, String> e : params.entrySet()) {
            try {
                params.put(e.getKey(), URLEncoder.encode(e.getValue(), "UTF-8"));
            } catch (UnsupportedEncodingException e1) {
                e1.printStackTrace();
            }
        }
    }

    public String getOAuthHeader() {
        StringBuilder builder = new StringBuilder("OAuth ");

        for (Map.Entry<String, String> e : params.entrySet()) {
            builder.append(e.getKey())
                    .append("=")
                    .append("\"")
                    .append(e.getValue())
                    .append("\"")
                    .append(",");
        }
        builder.deleteCharAt(builder.length() - 1);
        return builder.toString();
    }

    /*=========================================*/
    /*--------------GETTERS--------------------*/
    /*=========================================*/
    public Map<String, String> getParams() {
        return params;
    }

    public String getoAuthToken() {
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