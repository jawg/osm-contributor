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
package io.mapsquare.osmcontributor.rest.managers;

import android.app.Activity;
import android.os.AsyncTask;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;

import org.greenrobot.eventbus.EventBus;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.mapsquare.osmcontributor.rest.events.GoogleAuthenticatedEvent;
import io.mapsquare.osmcontributor.rest.utils.MapParams;
import io.mapsquare.osmcontributor.ui.dialogs.WebViewDialogFragment;
import okhttp3.OkHttpClient;

/**
 * @author Tommy Buonomo on 25/07/16.
 */
@Singleton
public class GoogleOAuthManager {
    public static final String LOGIN_HINT_PARAM = "login_hint";
    public static final String OSM_GOOGLE_AUTH_URL = "https://www.openstreetmap.org/auth/google";

    private EventBus eventBus;
    private OkHttpClient okHttpClient;

    private boolean finish;

    @Inject
    public GoogleOAuthManager(EventBus eventBus) {
        this.eventBus = eventBus;
        okHttpClient = new OkHttpClient();
    }

    public void authenticate(final Activity activity, String email) {
        final WebViewDialogFragment dialog = WebViewDialogFragment.newInstance(OSM_GOOGLE_AUTH_URL, new MapParams<String, String>().put(LOGIN_HINT_PARAM, email).toMap());
        dialog.show(activity.getFragmentManager(), WebViewDialogFragment.class.getSimpleName());
        finish = false;
        dialog.setOnPageFinishedListener(new WebViewDialogFragment.OnPageFinishedListener() {
            @Override
            public void onPageFinished(WebView webView, String url) {
                webView.addJavascriptInterface(new JsInterface(activity, webView), "Android");
                if (!finish && url.contains("https://www.openstreetmap.org/#map")) {
                    webView.loadUrl("javascript:Android.showToken(" +
                            "OSM.oauth_token " +
                            "+ ' ' " +
                            "+ OSM.oauth_token_secret" +
                            "+ ' ' " +
                            "+ OSM.oauth_consumer_key" +
                            "+ ' ' " +
                            "+ OSM.oauth_consumer_secret)");
                    webView.stopLoading();
                    dialog.dismiss();
                    finish = true;
                }

                if (url.contains("https://www.openstreetmap.org/user/new")) {
                    webView.loadUrl("javascript:Android.showUserParams(" +
                            "document.getElementsByName('authenticity_token')[0].value" +
                            "+ ' ' " +
                            "+ document.getElementsByName('user[email]')[0].value" +
                            "+ ' ' " +
                            "+ document.getElementsByName('user[auth_uid]')[0].value)");
                }
            }
        });
    }

    private static final String UTF8 = "utf8";
    private static final String AUTHENTICITY_TOKEN = "authenticity_token";
    private static final String USER_EMAIL = "user[email]";
    private static final String USER_EMAIL_CONFIRMATION = "user[email_confirmation]";
    private static final String USER_DISPLAY_NAME = "user[display_name]";
    private static final String USER_AUTH_PROVIDER = "user[auth_provider]";
    private static final String USER_AUTH_UID = "user[auth_uid]";
    private static final String USER_PASS_CRYPT = "user[pass_crypt]";
    private static final String USER_PASS_CRYPT_CONFIRMATION = "user[pass_crypt_confirmation]";
    private static final String COMMIT = "commit";
    private static final String SEPARATOR = "&";
    private static final String EQUALS = "=";
    private static final String TAG = "GoogleOAuthManager";
    private static final String NEW_USER_URL = "https://www.openstreetmap.org/user/new";

    private void skipInscriptionStep(Activity activity, final WebView webView, String authenticityToken, String email, String authUid) {
        MapParams<String, String> mapParams = new MapParams<String, String>()
                .put(UTF8, "✓")
                .put(AUTHENTICITY_TOKEN, authenticityToken)
                .put(USER_EMAIL, email)
                .put(USER_EMAIL_CONFIRMATION, email)
                .put(USER_DISPLAY_NAME, email.substring(0, email.indexOf("@")))
                .put(USER_AUTH_PROVIDER, "google")
                .put(USER_AUTH_UID, authUid)
                .put(USER_PASS_CRYPT, "")
                .put(USER_PASS_CRYPT_CONFIRMATION, "")
                .put(COMMIT, "S’inscrire");



        StringBuilder builder = new StringBuilder();
        for (Map.Entry<String, String> e : mapParams.toMap().entrySet()) {
            builder.append(e.getKey())
                    .append(EQUALS)
                    .append(e.getValue())
                    .append(SEPARATOR);
        }
        builder.deleteCharAt(builder.length() - 1);
        String data = "";
        try {
            data = URLEncoder.encode(builder.toString(), "UTF-8");
            data = URLEncoder.encode(data, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        final String finalData = data;
        Log.i(TAG, "skipInscriptionStep: " + builder);
        Log.i(TAG, "skipInscriptionStep: " + data);
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                webView.postUrl(NEW_USER_URL, finalData.getBytes());
            }
        });
    }

    private class NewUserTask extends AsyncTask<Void, Void, String> {
        Map<String, String> params;

        public NewUserTask(Map<String, String> params) {
            this.params = params;
        }

        @Override
        protected String doInBackground(Void... voids) {
//            RequestBody requestBody = new FormBody.Builder()
//                    .addEncoded()
//
//// Do whatever you want with the content
//
//// Show the web page
//            webView.loadDataWithBaseURL(url, content, "text/html", "UTF-8", null);
            return null;
        }
    }

    private class JsInterface {
        private WebView webView;
        private Activity activity;

        public JsInterface(Activity activity, WebView webView) {
            this.webView = webView;
            this.activity = activity;
        }

        @JavascriptInterface
        public void showToken(String attr) {
            String[] tokens = attr.split(" ");
            eventBus.post(new GoogleAuthenticatedEvent(tokens[0], tokens[1], tokens[2], tokens[3]));
        }

        @JavascriptInterface
        public void showUserParams(String value) {
            String[] params = value.split(" ");
            String authenticityToken = params[0];
            String email = params[1];
            String authUid = params[2];
            Log.i(GoogleOAuthManager.class.getSimpleName(), "showUserParams: " + value);
            skipInscriptionStep(activity, webView, authenticityToken, email, authUid);
        }
    }
}
