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
package io.mapsquare.osmcontributor.flickr.oauth;


import android.util.Log;

import com.squareup.okhttp.OkHttpClient;

import org.greenrobot.eventbus.EventBus;

import java.util.Map;

import javax.inject.Inject;

import io.mapsquare.osmcontributor.flickr.event.FlickrUserConnectedEvent;
import io.mapsquare.osmcontributor.flickr.event.PleaseAuthorizeEvent;
import io.mapsquare.osmcontributor.flickr.rest.FlickrOauthClient;
import io.mapsquare.osmcontributor.flickr.util.MapParams;
import io.mapsquare.osmcontributor.flickr.util.OAuthParams;
import io.mapsquare.osmcontributor.flickr.util.ResponseConverter;
import io.mapsquare.osmcontributor.flickr.util.StringConverter;
import io.mapsquare.osmcontributor.utils.ConfigManager;
import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.OkClient;
import retrofit.client.Response;

/**
 * Utils methods for request signing.
 */
public class FlickrOAuth {

    private static final String TAG = "FlickrOAuth";

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

    /**
     * Request used during the process of authentication.
     */
    private OAuthRequest oAuthRequest;

    /*=========================================*/
    /*------------CONSTRUCTORS-----------------*/
    /*=========================================*/
    public FlickrOAuth() {
        // Init Flickr retrofit client
        RestAdapter adapter = new RestAdapter.Builder()
                .setConverter(new StringConverter())
                .setEndpoint("https://www.flickr.com/services/oauth")
                .setClient(new OkClient(new OkHttpClient())).build();
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
        // Request creation. First step : Ask for a oauth_token.
        final String oauthRequestUrl = "https://www.flickr.com/services/oauth/request_token";

        // Creation of the request with API_KEY and API_KEY_SECRET.
        oAuthRequest = new OAuthRequest(configManager.getFlickrApiKey(), configManager.getFlickrApiKeySecret());
        oAuthRequest.setRequestUrl(oauthRequestUrl);

        // Creation of request parameters
        oAuthRequest.initParam(OAuthParams.getOAuthParams().put("oauth_callback", "1").toMap());
        // Sign request with HMAC-SHA1 algorithm.
        oAuthRequest.signRequest();

        // Make the request.
        flickrOauthClient.requestToken(oAuthRequest.getParams(), new Callback<String>() {
            @Override
            public void success(String oauthResponse, Response response) {
                // If request succeed.
                if (response.getStatus() == 200) {
                    // Response is not JSON. Parse it and get oauth_token and oauth_token_secret.
                    Map<String, String> responseConverted = ResponseConverter.convertOAuthResponse(oauthResponse);
                    String oauthToken = responseConverted.get("oauth_token");
                    String oauthTokenSecret = responseConverted.get("oauth_token_secret");
                    // Set the request object with values.
                    if (oauthToken != null) {
                        oAuthRequest.setoAuthToken(oauthToken);
                        oAuthRequest.setoAuthTokenSecret(oauthTokenSecret);
                        flickrAuthorize();
                        return;
                    }
                }
                Log.e(TAG, "Request 1 failed with status : " + response.getStatus() + " " + response.getReason());
            }

            @Override
            public void failure(RetrofitError error) {
                Log.e(TAG, "Request 1 failed. RetrofitError is : " + error.getBody());
            }
        });
    }

    /**
     * OAuth with Flickr API.
     * Second step: Authorize app with previous token.
     */
    public void flickrAuthorize() {
        // Request creation. Second step : Ask for user authorization.
        final String authorizeUrl = "https://www.flickr.com/services/oauth/authorize";
        oAuthRequest.setRequestUrl(authorizeUrl);
        // Init request param.
        oAuthRequest.initParam(new MapParams<String, String>().put("oauth_token", oAuthRequest.getoAuthToken()).put("perms", "write").toMap());
        oAuthRequest.signRequest();
        // Launch the webview.
        eventBus.post(new PleaseAuthorizeEvent(oAuthRequest));
    }

    /**
     * Final step in Flickr OAuth. Get final token.
     * @param oAuthToken token obtained with authorize method
     * @param oAuthVerfier verifier token obtained with authorize method
     */
    public void flickrAccessToken(String oAuthToken, String oAuthVerfier) {
        // Request creation. Final step: Get token.
        final String accessTokenUrl = "https://www.flickr.com/services/oauth/access_token";
        oAuthRequest.setRequestUrl(accessTokenUrl);
        oAuthRequest.setoAuthToken(oAuthToken);
        // Init request params.
        oAuthRequest.initParam(OAuthParams.getOAuthParams().put("oauth_token", oAuthToken).put("oauth_verifier", oAuthVerfier).toMap());
        oAuthRequest.signRequest();

        // Send request.
        flickrOauthClient.accessToken(oAuthRequest.getParams(), new Callback<String>() {
            @Override
            public void success(String oauthResponse, Response response) {
                if (response.getStatus() == 200) {
                    // Get user informations.
                    eventBus.post(new FlickrUserConnectedEvent(ResponseConverter.convertOAuthResponse(oauthResponse)));
                    return;
                }
                Log.e(TAG, "Request 2 failed with status : " + response.getStatus() + " " + response.getReason());
            }

            @Override
            public void failure(RetrofitError error) {
                Log.e(TAG, "Request 2 failed. RetrofitError is : " + error.getBody());
            }
        });
    }
}