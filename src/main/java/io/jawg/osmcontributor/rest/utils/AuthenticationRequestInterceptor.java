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
package io.jawg.osmcontributor.rest.utils;

import android.util.Base64;

import com.github.scribejava.core.model.Verb;
import com.squareup.okhttp.Authenticator;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.net.Proxy;
import java.util.Map;

import javax.inject.Inject;

import io.jawg.osmcontributor.database.preferences.LoginPreferences;
import io.jawg.osmcontributor.rest.security.OAuthParams;
import io.jawg.osmcontributor.rest.security.OAuthRequest;

/**
 * {@link retrofit.RequestInterceptor} adding Base64 encoded credentials to the requests.
 */
public class AuthenticationRequestInterceptor implements Authenticator {
    public static final String CONSUMER_PARAM = "oauth_consumer_key";
    public static final String TOKEN_PARAM = "oauth_token";
    public static final String TOKEN_SECRET_PARAM = "oauth_token_secret";
    public static final String CONSUMER_SECRET_PARAM = "oauth_consumer_secret_key";

    LoginPreferences loginPreferences;

    @Inject
    public AuthenticationRequestInterceptor(LoginPreferences loginPreferences) {
        this.loginPreferences = loginPreferences;
    }

    private static final String TAG = "RequestInterceptor";


    @Override
    public Request authenticate(Proxy proxy, Response response) throws IOException {
        Map<String, String> oAuthParams = loginPreferences.retrieveOAuthParams();

        if (oAuthParams != null) {
            Request originalRequest = response.request();
            String requestUrl = originalRequest.urlString();

            OAuthRequest oAuthRequest = new OAuthRequest(oAuthParams.get(CONSUMER_PARAM), oAuthParams.get(CONSUMER_SECRET_PARAM));
            oAuthRequest.initParam(OAuthParams.getOAuthParams().put(TOKEN_PARAM, oAuthParams.get(TOKEN_PARAM)).toMap());
            oAuthRequest.setOAuthToken(oAuthParams.get(TOKEN_PARAM));
            oAuthRequest.setOAuthTokenSecret(oAuthParams.get(TOKEN_SECRET_PARAM));
            oAuthRequest.setRequestUrl(requestUrl);
            oAuthRequest.signRequest(Verb.valueOf(originalRequest.method()));
            oAuthRequest.encodeParams();

            Request.Builder finalRequest = originalRequest
                    .newBuilder()
                    .header("Accept", "text/xml")
                    .header("Authorization", oAuthRequest.getOAuthHeader())
                    .method(originalRequest.method(), originalRequest.body());
            return finalRequest.build();
        } else {
            // create Base64 encoded string
            String authorization = "Basic " + Base64.encodeToString((loginPreferences.retrieveLogin() + ":" + loginPreferences.retrievePassword()).getBytes(), Base64.NO_WRAP);
            return response
                    .request()
                    .newBuilder()
                    .header("Authorization", authorization)
                    .header("Accept", "text/xml")
                    .build();
        }
    }

    @Override
    public Request authenticateProxy(Proxy proxy, Response response) throws IOException {
        return null;
    }
}
