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
package io.mapsquare.osmcontributor.ui.dialogs;

import android.app.DialogFragment;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.mapsquare.osmcontributor.R;

/**
 * @author Tommy Buonomo on 21/07/16.
 */
public class WebViewDialogFragment extends DialogFragment {
    @BindView(R.id.dialog_web_view)
    WebView webView;

    private String url;
    private Map<String, String> params;
    private OnPageFinishedListener onPageFinishedListener;

    public static WebViewDialogFragment newInstance(String url, Map<String, String> params) {
        WebViewDialogFragment dialogFragment = new WebViewDialogFragment();
        dialogFragment.setUrl(url);
        dialogFragment.setParams(params);
        return dialogFragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.dialog_google_connection, container, false);
        ButterKnife.bind(this, rootView);

        webView.getSettings().setJavaScriptEnabled(true);
        webView.clearCache(true);
        webView.clearHistory();
        clearCookies(getActivity());

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                if (onPageFinishedListener != null) {
                    onPageFinishedListener.onPageFinished(webView, url);
                }
            }
        });

        if (url != null) {
            webView.loadUrl(url);
        }
        return rootView;
    }

    public void setUrl(String url) {
        if (url != null) {
            this.url = url;
            if (params != null) {
                refreshParams();
            }
        }
    }

    public void setParams(Map<String, String> params) {
        if (params != null) {
            this.params = params;
            if (url != null) {
                refreshParams();
            }
        }
    }

    private void refreshParams() {
        Uri.Builder builder = Uri.parse(url)
                .buildUpon();
        for (Map.Entry<String, String> e : params.entrySet()) {
            builder.appendQueryParameter(e.getKey(), e.getValue());
        }
        url = builder.build().toString();
    }

    public void setOnPageFinishedListener(OnPageFinishedListener onPageFinishedListener) {
        this.onPageFinishedListener = onPageFinishedListener;
    }

    public interface OnPageFinishedListener {
        void onPageFinished(WebView webView, String url);
    }

    @SuppressWarnings("deprecation")
    public static void clearCookies(Context context) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            CookieManager.getInstance().removeAllCookies(null);
            CookieManager.getInstance().flush();
        } else {
            CookieSyncManager cookieSyncMngr = CookieSyncManager.createInstance(context);
            cookieSyncMngr.startSync();
            CookieManager cookieManager = CookieManager.getInstance();
            cookieManager.removeAllCookie();
            cookieManager.removeSessionCookie();
            cookieSyncMngr.stopSync();
            cookieSyncMngr.sync();
        }
    }
}
