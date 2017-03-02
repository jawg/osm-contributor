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
package io.jawg.osmcontributor.flickr.oauth;


import android.util.Log;

import com.github.scribejava.core.model.Verb;
import com.squareup.okhttp.OkHttpClient;

import org.greenrobot.eventbus.EventBus;

import java.util.Map;

import javax.inject.Inject;

import io.jawg.osmcontributor.flickr.event.FlickrUserConnectedEvent;
import io.jawg.osmcontributor.flickr.event.PleaseAuthorizeEvent;
import io.jawg.osmcontributor.flickr.rest.FlickrOauthClient;
import io.jawg.osmcontributor.flickr.util.OAuthParams;
import io.jawg.osmcontributor.flickr.util.ResponseConverter;
import io.jawg.osmcontributor.rest.utils.MapParams;
import io.jawg.osmcontributor.rest.utils.StringConverter;
import io.jawg.osmcontributor.utils.ConfigManager;
import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.OkClient;
import retrofit.client.Response;

/**
 * Utils methods for request signing.
 */
public class FlickrOAuth {

    /*=========================================*/
    /*-------------CONSTANTS-------------------*/
    /*=========================================*/
    private static final String TAG = "FlickrOAuth";

    private static final String OAUTH_URL = "http://www.flickr.com/services/oauth";

    private static final String REQUEST_TOKEN_URL = OAUTH_URL + "/request_token";

    private static final String AUTHORIZE_URL = OAUTH_URL + "/authorize";

    private static final String ACCESS_TOKEN_URL = OAUTH_URL + "/access_token";

    private static final String PERMS = "perms";

    private static final String WRITE_PERM = "write";

    /*=========================================*/
    /*------------INJECTIONS-------------------*/
    /*=========================================*/
    @Inject
    ConfigManager configManager;

    @Inject
    EventBus eventBus;

    /*=========================================*/
    /*------------ATTRIBUTES-------------------*/
    /*=========================================*/
    private FlickrOauthClient flickrOauthClient;

    private OAuthRequest oAuthRequest;

    /*=========================================*/
    /*------------CONSTRUCTORS-----------------*/
    /*=========================================*/
    public FlickrOAuth() {
        // Init Flickr retrofit client
        RestAdapter adapter = new RestAdapter.Builder()
                .setConverter(new StringConverter())
                .setEndpoint(OAUTH_URL)
                .setClient(new OkClient(new OkHttpClient()))
                .setLogLevel(RestAdapter.LogLevel.FULL)
                .build();
        flickrOauthClient = adapter.create(FlickrOauthClient.class);
    }

    /*=========================================*/
    /*-------------FLICKR API------------------*/
    /*=========================================*/
    /**
     * OAuth with Flickr API.
     * First step: Request a token.
     */
    public void flickrRequestToken() {
        // Creation of the request with API_KEY and API_KEY_SECRET.
        oAuthRequest = new OAuthRequest(configManager.getFlickrApiKey(), configManager.getFlickrApiKeySecret());
        oAuthRequest.setRequestUrl(REQUEST_TOKEN_URL);

        // Creation of request parameters. Callback is set to one by default (no need for callback)
        oAuthRequest.initParam(OAuthParams.getOAuthParams().put(OAuthParams.OAUTH_CALLBACK, "1").toMap());
        // Sign request with HMAC-SHA1 algorithm.
        oAuthRequest.signRequest(Verb.GET);

        // Make the request.
        flickrOauthClient.requestToken(oAuthRequest.getParams(), new Callback<String>() {
            @Override
            public void success(String oauthResponse, Response response) {
                // If request succeed.
                if (response.getStatus() == 200) {
                    // Response is not JSON. Parse it and get oauth_token and oauth_token_secret.
                    Map<String, String> responseConverted = ResponseConverter.convertOAuthResponse(oauthResponse);
                    String oauthToken = responseConverted.get(OAuthParams.OAUTH_TOKEN);
                    String oauthTokenSecret = responseConverted.get(OAuthParams.OAUTH_TOKEN_SECRET);
                    // Set the request object with values.
                    if (oauthToken != null) {
                        oAuthRequest.setOAuthToken(oauthToken);
                        oAuthRequest.setOAuthTokenSecret(oauthTokenSecret);
                        flickrAuthorize();
                        return;
                    }
                }
                Log.e(TAG, response.getStatus() + " " + response.getReason());
            }

            @Override
            public void failure(RetrofitError error) {
                error.printStackTrace();
            }
        });
    }

    /**
     * OAuth with Flickr API.
     * Second step: Authorize app with previous token.
     */
    public void flickrAuthorize() {
        // Request creation. Second step : Ask for user authorization.
        oAuthRequest.setRequestUrl(AUTHORIZE_URL);
        // Init request param.
        oAuthRequest.initParam(new MapParams<String, String>().put(OAuthParams.OAUTH_TOKEN, oAuthRequest.getOAuthToken()).put(PERMS, WRITE_PERM).toMap());
        oAuthRequest.signRequest(Verb.GET);
        // Launch the webview.
        eventBus.post(new PleaseAuthorizeEvent(oAuthRequest));
    }

    /**
     * Final step in Flickr OAuth. Get final token.
     * @param oAuthToken token obtained with authorize method
     * @param oAuthVerfier verifier token obtained with authorize method
     */
    public void flickrAccessToken(String oAuthToken, String oAuthVerfier) {
        oAuthRequest.setRequestUrl(ACCESS_TOKEN_URL);
        oAuthRequest.setOAuthToken(oAuthToken);
        // Init request params.
        oAuthRequest.initParam(OAuthParams.getOAuthParams().put(OAuthParams.OAUTH_TOKEN, oAuthToken).put(OAuthParams.OAUTH_VERIFIER, oAuthVerfier).toMap());
        oAuthRequest.signRequest(Verb.GET);

        // Send request.
        flickrOauthClient.accessToken(oAuthRequest.getParams(), new Callback<String>() {
            @Override
            public void success(String oauthResponse, Response response) {
                if (response.getStatus() == 200) {
                    // Get user informations.
                    eventBus.post(new FlickrUserConnectedEvent(ResponseConverter.convertOAuthResponse(oauthResponse)));
                } else {
                    Log.e(TAG, response.getStatus() + " " + response.getReason());
                }
            }

            @Override
            public void failure(RetrofitError error) {
                error.printStackTrace();
            }
        });
    }

    /*=========================================*/
    /*------------GETTERS/SETTERS--------------*/
    /*=========================================*/
    public OAuthRequest getOAuthRequest() {
        return oAuthRequest;
    }

    public void setOAuthRequest(OAuthRequest oAuthRequest) {
        this.oAuthRequest = oAuthRequest;
    }
}