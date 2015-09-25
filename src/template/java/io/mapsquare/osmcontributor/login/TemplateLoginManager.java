/**
 * Copyright (C) 2015 eBusiness Information
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
package io.mapsquare.osmcontributor.login;

import android.app.Application;

import de.greenrobot.event.EventBus;
import io.mapsquare.osmcontributor.R;
import io.mapsquare.osmcontributor.sync.dto.osm.OsmDto;
import io.mapsquare.osmcontributor.sync.dto.osm.PermissionDto;
import io.mapsquare.osmcontributor.sync.rest.OsmRestClient;
import io.mapsquare.osmcontributor.utils.StringUtils;
import retrofit.RetrofitError;
import timber.log.Timber;

/**
 * Implementation of {@link io.mapsquare.osmcontributor.login.LoginManager} for an OpenStreetMap database.
 */
public class TemplateLoginManager extends LoginManager {

    Application application;
    OsmRestClient osmRestClient;
    LoginPreferences loginPreferences;

    public TemplateLoginManager(EventBus bus, Application application, OsmRestClient osmRestClient, LoginPreferences loginPreferences) {
        super(bus);
        this.application = application;
        this.osmRestClient = osmRestClient;
        this.loginPreferences = loginPreferences;
    }

    /**
     * Calls the permissions web service to determine if the user provided has editing capabilities
     *
     * @return editing capabilities or not (false also if an retrofit error occurred)
     */
    @Override
    public boolean isValidLogin(final String login, final String password) {
        try {
            OsmDto permissions = osmRestClient.getPermissions();
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
     */
    @Override
    public void initializeCredentials() {
        if (StringUtils.isEmpty(loginPreferences.retrieveLogin()) && StringUtils.isEmpty(loginPreferences.retrievePassword())) {
            loginPreferences.updateCredentials(application.getString(R.string.login), application.getString(R.string.password));
        }
    }
}
