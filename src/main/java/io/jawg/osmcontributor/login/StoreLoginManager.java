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
package io.jawg.osmcontributor.login;

import android.util.Base64;

import com.github.scribejava.core.model.Verb;

import org.greenrobot.eventbus.EventBus;

import java.util.Map;

import io.jawg.osmcontributor.database.preferences.LoginPreferences;
import io.jawg.osmcontributor.rest.clients.OsmRestClient;
import io.jawg.osmcontributor.rest.dtos.osm.OsmDto;
import io.jawg.osmcontributor.rest.dtos.osm.PermissionDto;
import io.jawg.osmcontributor.rest.security.OAuthParams;
import io.jawg.osmcontributor.rest.security.OAuthRequest;
import io.jawg.osmcontributor.ui.managers.LoginManager;
import retrofit.RetrofitError;
import timber.log.Timber;


public class StoreLoginManager extends LoginManager {
    public static final String CONSUMER_PARAM = "oauth_consumer_key";
    public static final String TOKEN_PARAM = "oauth_token";
    public static final String TOKEN_SECRET_PARAM = "oauth_token_secret";
    public static final String CONSUMER_SECRET_PARAM = "oauth_consumer_secret_key";

    private OsmRestClient osmRestClient;

    public StoreLoginManager(EventBus bus, LoginPreferences loginPreferences, OsmRestClient osmRestClient) {
        super(bus, loginPreferences);
        this.osmRestClient = osmRestClient;
    }

    /**
     * Calls the permissions web service to determine if the user provided has editing capabilities
     *
     * @return editing capabilities or not (false also if an retrofit error occurred)
     */
    @Override
    public boolean isValidLogin(final String login, final String password) {
        try {
            OsmDto permissions = null;
            Map<String, String> oAuthParams = loginPreferences.retrieveOAuthParams();

            // OAuth connection
            if (oAuthParams != null) {
                String requestUrl = "http://www.openstreetmap.org/api/0.6/permissions";

                OAuthRequest oAuthRequest = new OAuthRequest(oAuthParams.get(CONSUMER_PARAM), oAuthParams.get(CONSUMER_SECRET_PARAM));
                oAuthRequest.initParam(OAuthParams.getOAuthParams().put(TOKEN_PARAM, oAuthParams.get(TOKEN_PARAM)).toMap());
                oAuthRequest.setOAuthToken(oAuthParams.get(TOKEN_PARAM));
                oAuthRequest.setOAuthTokenSecret(oAuthParams.get(TOKEN_SECRET_PARAM));
                oAuthRequest.setRequestUrl(requestUrl);
                oAuthRequest.signRequest(Verb.GET);
                permissions = osmRestClient.getPermissions(oAuthRequest.getParams());
            } else {
                // Basic Auth connection
                String authorization = "Basic " + Base64.encodeToString((login + ":" + password).getBytes(), Base64.NO_WRAP);
                permissions = osmRestClient.getPermissions(authorization);
            }

            if (permissions.getPermissionsDto() != null && permissions.getPermissionsDto().getPermissionDtoList() != null) {
                for (PermissionDto permissionDto : permissions.getPermissionsDto().getPermissionDtoList()) {
                    if ("allow_write_api".equals(permissionDto.getName())) {
                        return true;
                    }
                }
            }
        } catch (RetrofitError e) {
            Timber.e("Couldn't request permissions " + e);
        }
        return false;
    }

    /**
     * {@inheritDoc}
     * <p/>
     * Do nothing for the store flavor.
     */
    @Override
    public void initializeCredentials() {
    }

    @Override
    public boolean checkFirstConnection() {
        return loginPreferences.retrieveFirstConnection();
    }
}
