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
import io.mapsquare.osmcontributor.core.events.InitCredentialsEvent;
import io.mapsquare.osmcontributor.login.events.AttemptLoginEvent;
import io.mapsquare.osmcontributor.login.events.ErrorLoginEvent;
import io.mapsquare.osmcontributor.login.events.ValidLoginEvent;

/**
 * Manage the credentials of the backend.
 */
public abstract class LoginManager {

    protected EventBus bus;
    protected LoginPreferences loginPreferences;

    public LoginManager(EventBus bus, LoginPreferences loginPreferences) {
        this.bus = bus;
        this.loginPreferences = loginPreferences;
    }

    public void onEventAsync(final AttemptLoginEvent event) {
        if (isValidLogin(event.getLogin(), event.getPassword())) {
            bus.post(new ValidLoginEvent());
        } else {
            bus.post(new ErrorLoginEvent());
        }
    }

    public void onEventAsync(InitCredentialsEvent event) {
        initializeCredentials();
    }

    /**
     * Check if the credentials in the SharedPreferences are valid.
     *
     * @return Whether the credentials are valid.
     */
    public boolean checkCredentials() {
        return isValidLogin(loginPreferences.retrieveLogin(), loginPreferences.retrievePassword());
    }

    /**
     * Check whether the credentials are valid.
     *
     * @param login    User's login.
     * @param password User's password.
     * @return Whether the credentials are valid.
     */
    public abstract boolean isValidLogin(final String login, final String password);

    /**
     * Initialize the value of the credentials in the preferences.
     */
    public abstract void initializeCredentials();
}
