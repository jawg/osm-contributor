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
package io.jawg.osmcontributor.rest.security;

import android.app.Activity;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;

import org.greenrobot.eventbus.EventBus;

import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.jawg.osmcontributor.R;
import io.jawg.osmcontributor.rest.events.GoogleAuthenticatedEvent;
import io.jawg.osmcontributor.rest.utils.MapParams;
import io.jawg.osmcontributor.ui.dialogs.WebViewDialogFragment;

/**
 * @author Tommy Buonomo on 25/07/16.
 */
@Singleton
public class GoogleOAuthManager {
    public static final String LOGIN_HINT_PARAM = "login_hint";
    public static final String OSM_GOOGLE_AUTH_URL = "https://www.openstreetmap.org/auth/google";
    public static final String OSM_MAP_URL = "https://www.openstreetmap.org/#map";
    public static final String OSM_NEW_USER_URL = "https://www.openstreetmap.org/user/new";
    public static final String OSM_TERMS_URL = "https://www.openstreetmap.org/user/terms#";
    public static final String GOOGLE_SERVICE_LOGIN_URL = "https://accounts.google.com/ServiceLogin";
    public static final String OSM_WELCOME_URL = "https://www.openstreetmap.org/welcome";

    private EventBus eventBus;

    private boolean finish;

    @Inject
    public GoogleOAuthManager(EventBus eventBus) {
        this.eventBus = eventBus;
    }

    public void authenticate(final Activity activity, final String email) {
        finish = false;
        final WebViewDialogFragment dialog = WebViewDialogFragment.newInstance(OSM_GOOGLE_AUTH_URL,
                new MapParams<String, String>().put(LOGIN_HINT_PARAM, email).toMap());

        dialog.show(activity.getFragmentManager(), WebViewDialogFragment.class.getSimpleName());
        dialog.setOnPageFinishedListener(new WebViewDialogFragment.OnPageFinishedListener() {
            @Override
            public void onPageFinished(WebView webView, String url, boolean isRedirect) {
                webView.addJavascriptInterface(new JsInterface(activity, webView, dialog), "Android");
                if (!finish && url.contains(OSM_MAP_URL) && !isRedirect) {
                    if (dialog.isProgressing()) {
                        dialog.stopProgressBar();
                    }
                    // Get the tokens in hidden input in the html page
                    webView.loadUrl("javascript:Android.showToken(" +
                            "OSM.oauth_token " +
                            "+ ' ' " +
                            "+ OSM.oauth_token_secret" +
                            "+ ' ' " +
                            "+ OSM.oauth_consumer_key" +
                            "+ ' ' " +
                            "+ OSM.oauth_consumer_secret)");
                }

                if (url.contains(OSM_NEW_USER_URL) && !url.equals(OSM_NEW_USER_URL)) {
                    dialog.startProgressBar();
                    // Skip the inscription page
                    webView.loadUrl("javascript:" +
                            "document.getElementsByName('user[display_name]')[0].value = '"
                            + email.substring(0, email.indexOf('@')) + UUID.randomUUID().toString().substring(0, 5)
                            + "';" +
                            "document.getElementsByName('commit')[0].click();");
                }

                if (url.equals(OSM_NEW_USER_URL)) {
                    dialog.showErrorText(activity.getString(R.string.login_google_error));

                }

                if (url.contains(OSM_TERMS_URL) && !isRedirect) {
                    dialog.stopProgressBar();
                }

                if (url.contains(GOOGLE_SERVICE_LOGIN_URL) && !isRedirect) {
                    // Skip the google email next page
                    webView.loadUrl("javascript:" +
                            "if (!document.getElementById('password-shown').hasChildNodes()) {" +
                            "   document.getElementById('next').click()" +
                            "} else {" +
                            "   Android.stopProgressBar();" +
                            "}");
                }

                if (url.contains(OSM_WELCOME_URL) && !isRedirect) {
                    webView.loadUrl(OSM_MAP_URL);
                }

                if (url.contains(OSM_WELCOME_URL) && isRedirect) {
                    dialog.startProgressBar();
                }
            }
        });
    }

    private class JsInterface {
        private WebView webView;
        private Activity activity;
        private WebViewDialogFragment dialog;

        public JsInterface(Activity activity, WebView webView, WebViewDialogFragment dialog) {
            this.webView = webView;
            this.activity = activity;
            this.dialog = dialog;
        }

        @JavascriptInterface
        @SuppressWarnings("unused")
        public synchronized void showToken(String attr) {
            finish = true;
            String[] tokens = attr.split(" ");
            final GoogleAuthenticatedEvent event = new GoogleAuthenticatedEvent(tokens[0], tokens[1], tokens[2], tokens[3]);
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    webView.stopLoading();
                    dialog.dismiss();
                    eventBus.post(event);
                }
            });
        }

        @JavascriptInterface
        @SuppressWarnings("unused")
        public void stopProgressBar() {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    dialog.stopProgressBar();
                }
            });
        }
    }
}
