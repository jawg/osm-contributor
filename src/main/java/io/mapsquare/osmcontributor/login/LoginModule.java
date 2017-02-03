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

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import org.greenrobot.eventbus.EventBus;

import io.mapsquare.osmcontributor.database.preferences.LoginPreferences;
import io.mapsquare.osmcontributor.rest.clients.OsmRestClient;
import io.mapsquare.osmcontributor.ui.managers.LoginManager;

@Module
@Singleton
public class LoginModule {

    @Provides
    LoginManager getLoginManager(EventBus bus, LoginPreferences loginPreferences, OsmRestClient osmRestClient) {
        return new StoreLoginManager(bus, loginPreferences, osmRestClient);
    }
}
