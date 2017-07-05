package io.jawg.osmcontributor.ui.activities;


import android.util.Base64;

import com.github.scribejava.core.model.Verb;
import com.squareup.okhttp.Interceptor;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;


import java.io.IOException;
import java.util.Map;

import javax.inject.Inject;

import io.jawg.osmcontributor.database.preferences.LoginPreferences;
import io.jawg.osmcontributor.rest.security.OAuthParams;
import io.jawg.osmcontributor.rest.security.OAuthRequest;
import timber.log.Timber;


/**
 * Created by ebiz on 03/07/17.
 */

public class AuthorisationInterceptor implements Interceptor {

    public static final String CONSUMER_PARAM = "oauth_consumer_key";
    public static final String TOKEN_PARAM = "oauth_token";
    public static final String TOKEN_SECRET_PARAM = "oauth_token_secret";
    public static final String CONSUMER_SECRET_PARAM = "oauth_consumer_secret_key";

    LoginPreferences loginPreferences;

    @Inject
    public AuthorisationInterceptor(LoginPreferences loginPreferences) {
        this.loginPreferences = loginPreferences;
    }
    @Override
    public Response intercept(Chain chain) throws IOException {
        Map<String, String> oAuthParams = loginPreferences.retrieveOAuthParams();

        if (oAuthParams != null) {
            Request originalRequest = chain.request();
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
                    .addHeader("Authorization", oAuthRequest.getOAuthHeader())
                    .method(originalRequest.method(), originalRequest.body());
            return chain.proceed(finalRequest.build());
        } else {
            // create Base64 encoded string
            String authorization = "Basic " + Base64.encodeToString((loginPreferences.retrieveLogin() + ":" + loginPreferences.retrievePassword()).getBytes(), Base64.NO_WRAP);
            Timber.i("AUTHORIZATION TEST");
            return chain.proceed(chain
                    .request()
                    .newBuilder()
                    .header("Authorization", authorization)
                    .header("Accept", "text/xml")
                    .build());
        }
    }
}
