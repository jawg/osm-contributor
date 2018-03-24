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

import com.github.scribejava.core.model.Verb;

import org.greenrobot.eventbus.EventBus;

import java.util.Map;

import javax.inject.Inject;

import io.jawg.osmcontributor.flickr.event.FlickrUserConnectedEvent;
import io.jawg.osmcontributor.flickr.event.PleaseAuthorizeEvent;
import io.jawg.osmcontributor.flickr.rest.FlickrOauthClient;
import io.jawg.osmcontributor.flickr.util.OAuthParams;
import io.jawg.osmcontributor.flickr.util.ResponseConverter;
import io.jawg.osmcontributor.rest.utils.MapParams;
import io.jawg.osmcontributor.utils.ConfigManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.scalars.ScalarsConverterFactory;
import timber.log.Timber;

/**
 * Utils methods for request signing.
 */
public class FlickrOAuth {

  private static boolean initialized = false;

  /*=========================================*/
    /*-------------CONSTANTS-------------------*/
    /*=========================================*/
  private static final String TAG = "FlickrOAuth";

  private static final String BASE_URL = "http://www.flickr.com";

  private static final String OAUTH_URL = BASE_URL + "/services/oauth";

  private static final String REQUEST_TOKEN_URL = OAUTH_URL + "/request_token";

  private static final String AUTHORIZE_URL = OAUTH_URL + "/authorize";

  private static final String ACCESS_TOKEN_URL = OAUTH_URL + "/access_token";

  private static final String PERMS = "perms";

  private static final String WRITE_PERM = "write";

  /*=========================================*/
    /*------------INJECTIONS-------------------*/
    /*=========================================*/
  @Inject ConfigManager configManager;

  @Inject EventBus eventBus;

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
    Retrofit adapter = new Retrofit.Builder().addConverterFactory(ScalarsConverterFactory.create()).baseUrl(BASE_URL).build();
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
    flickrOauthClient.requestToken(oAuthRequest.getParams()).enqueue(new Callback<String>() {
      @Override public void onResponse(Call<String> call, Response<String> response) {
        // If request succeed.
        if (response.isSuccessful()) {
          // Response is not JSON. Parse it and get oauth_token and oauth_token_secret.
          Map<String, String> responseConverted = ResponseConverter.convertOAuthResponse(response.body());
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
      }

      @Override public void onFailure(Call<String> call, Throwable t) {
        Timber.e(t, t.getMessage());
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
   *
   * @param oAuthToken token obtained with authorize method
   * @param oAuthVerfier verifier token obtained with authorize method
   */
  public void flickrAccessToken(String oAuthToken, String oAuthVerfier) {
    oAuthRequest.setRequestUrl(ACCESS_TOKEN_URL);
    oAuthRequest.setOAuthToken(oAuthToken);
    // Init request params.
    oAuthRequest.initParam(
        OAuthParams.getOAuthParams().put(OAuthParams.OAUTH_TOKEN, oAuthToken).put(OAuthParams.OAUTH_VERIFIER, oAuthVerfier).toMap());
    oAuthRequest.signRequest(Verb.GET);

    // Send request.
    flickrOauthClient.accessToken(oAuthRequest.getParams()).enqueue(new Callback<String>() {
      @Override public void onResponse(Call<String> call, Response<String> response) {
        if (response.isSuccessful()) {
          // Get user informations.
          eventBus.post(new FlickrUserConnectedEvent(ResponseConverter.convertOAuthResponse(response.body())));
        }
      }

      @Override public void onFailure(Call<String> call, Throwable t) {
        Timber.e(t, t.getMessage());
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