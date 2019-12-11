/**
 * Copyright (C) 2019 Takima
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
package io.jawg.osmcontributor.ui.managers;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import io.jawg.osmcontributor.database.preferences.LoginPreferences;
import io.jawg.osmcontributor.ui.events.login.UpdateGoogleCredentialsEvent;

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

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onUpdateGoogleCredentialsEvent(UpdateGoogleCredentialsEvent event) {
        loginPreferences.updateGoogleCredentials(event.getConsumer(), event.getConsumerSecret(), event.getToken(), event.getTokenSecret());
    }

    /**
     * Check if the user is logged.
     *
     * @return Whether the user is logged.
     */
    public boolean isUserLogged() {
        return loginPreferences.isLogged() || updateCredentialsIfValid(loginPreferences.retrieveLogin(), loginPreferences.retrievePassword());
    }

    /**
     * Update the credentials after verifying them using the {@link LoginManager#isValidLogin(String, String)}.
     *
     * @param login    User's login.
     * @param password User's password.
     * @return Whether the credentials are valid.
     */
    public boolean updateCredentialsIfValid(final String login, final String password) {
        boolean validLogin = isValidLogin(login, password);

        if (validLogin) {
            loginPreferences.updateCredentials(login, password);
        }
        loginPreferences.setLogged(validLogin);
        return validLogin;
    }

    /**
     * Check whether the credentials are valid.
     *
     * @param login    User's login.
     * @param password User's password.
     * @return Whether the credentials are valid.
     */
    public abstract boolean isValidLogin(final String login, final String password);
}
