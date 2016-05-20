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

import android.app.Application;
import android.content.SharedPreferences;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.mapsquare.osmcontributor.R;

/**
 * Class managing the credentials preferences of the application.
 */
@Singleton
public class LoginPreferences {

    Application application;
    SharedPreferences sharedPreferences;

    @Inject
    public LoginPreferences(Application application, SharedPreferences sharedPreferences) {
        this.application = application;
        this.sharedPreferences = sharedPreferences;
    }

    /**
     * Get the login from the preferences.
     *
     * @return The login.
     */
    public String retrieveLogin() {
        return sharedPreferences.getString(application.getString(R.string.shared_prefs_login), "");
    }

    /**
     * Get the password from the preferences.
     *
     * @return The password.
     */
    public String retrievePassword() {
        return sharedPreferences.getString(application.getString(R.string.shared_prefs_password), "");
    }

    /**
     * Update the login and the password in the preferences.
     *
     * @param login    The new login.
     * @param password The new password.
     */
    public void updateCredentials(String login, String password) {
        sharedPreferences.edit().putString(application.getString(R.string.shared_prefs_login), login).putString(application.getString(R.string.shared_prefs_password), password).apply();
    }
}
