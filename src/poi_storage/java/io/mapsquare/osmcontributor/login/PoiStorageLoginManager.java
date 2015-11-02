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

import de.greenrobot.event.EventBus;
import io.mapsquare.osmcontributor.sync.dto.poistorage.UserDto;
import io.mapsquare.osmcontributor.sync.rest.PoiStorageClient;
import retrofit.RetrofitError;
import retrofit.client.Response;
import timber.log.Timber;

/**
 * Implementation of {@link io.mapsquare.osmcontributor.login.LoginManager} for an OpenStreetMap database.
 */
public class PoiStorageLoginManager extends LoginManager {

    PoiStorageClient poiStorageClient;

    public PoiStorageLoginManager(EventBus bus, LoginPreferences loginPreferences, PoiStorageClient poiStorageClient) {
        super(bus, loginPreferences);
        this.poiStorageClient = poiStorageClient;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isValidLogin(String login, String password) {
        try {
            Response response = poiStorageClient.login(new UserDto(login, password));
            if (response != null && response.getStatus() == 200) {
                return true;
            }
        } catch (RetrofitError e) {
            Timber.e("Couldn't request permissions " + e);
        }
        return false;
    }

    /**
     * {@inheritDoc}
     * <p/>
     * Do nothing for the poiStorage flavor.
     */
    @Override
    public void initializeCredentials() {
    }
}
