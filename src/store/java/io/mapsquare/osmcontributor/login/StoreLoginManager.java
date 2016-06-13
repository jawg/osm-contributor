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
package io.mapsquare.osmcontributor.login;

import org.greenrobot.eventbus.EventBus;

import io.mapsquare.osmcontributor.database.preferences.LoginPreferences;
import io.mapsquare.osmcontributor.rest.dtos.osm.OsmDto;
import io.mapsquare.osmcontributor.rest.dtos.osm.PermissionDto;
import io.mapsquare.osmcontributor.rest.clients.OsmRestClient;
import io.mapsquare.osmcontributor.ui.managers.LoginManager;
import retrofit.RetrofitError;
import timber.log.Timber;


public class StoreLoginManager extends LoginManager {

    OsmRestClient osmRestClient;

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
     * <p/>
     * Do nothing for the store flavor.
     */
    @Override
    public void initializeCredentials() {
    }
}
