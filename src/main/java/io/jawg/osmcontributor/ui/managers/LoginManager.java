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
package io.jawg.osmcontributor.ui.managers;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import io.jawg.osmcontributor.database.preferences.LoginPreferences;
import io.jawg.osmcontributor.model.events.InitCredentialsEvent;
import io.jawg.osmcontributor.ui.events.login.AttemptLoginEvent;
import io.jawg.osmcontributor.ui.events.login.ErrorLoginEvent;
import io.jawg.osmcontributor.ui.events.login.CheckFirstConnectionEvent;
import io.jawg.osmcontributor.ui.events.login.LoginInitializedEvent;
import io.jawg.osmcontributor.ui.events.login.PleaseOpenLoginDialogEvent;
import io.jawg.osmcontributor.ui.events.login.UpdateFirstConnectionEvent;
import io.jawg.osmcontributor.ui.events.login.UpdateGoogleCredentialsEvent;
import io.jawg.osmcontributor.ui.events.login.ValidLoginEvent;

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

    @Subscribe(threadMode = ThreadMode.ASYNC)
    @SuppressWarnings("unused")
    public void onAttemptLoginEvent(final AttemptLoginEvent event) {
        if (isValidLogin(event.getLogin(), event.getPassword())) {
            bus.post(new ValidLoginEvent());
            loginPreferences.updateCredentials(event.getLogin(), event.getPassword());
        } else {
            bus.post(new ErrorLoginEvent());
        }
    }

    @Subscribe(threadMode = ThreadMode.ASYNC)
    public void onInitCredentialsEvent(InitCredentialsEvent event) {
        initializeCredentials();
    }

    @Subscribe(threadMode = ThreadMode.ASYNC)
    public void onCheckFirstConnectionEvent(CheckFirstConnectionEvent event) {
        bus.post(new PleaseOpenLoginDialogEvent());
//        if (checkFirstConnection()) {
//            bus.post(new PleaseOpenLoginDialogEvent());
//        } else {
//            bus.postSticky(new LoginInitializedEvent());
//        }
    }

    /**
     * Check if the credentials in the SharedPreferences are valid.
     *
     * @return Whether the credentials are valid.
     */
    public boolean checkCredentials() {
        return isValidLogin(loginPreferences.retrieveLogin(), loginPreferences.retrievePassword());
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onUpdateGoogleCredentialsEvent(UpdateGoogleCredentialsEvent event) {
        loginPreferences.updateGoogleCredentials(event.getConsumer(), event.getConsumerSecret(), event.getToken(), event.getTokenSecret());
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onUpdateFisrtConnectionEvent(UpdateFirstConnectionEvent event) {
        loginPreferences.updateFirstConnection(false);
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

    public abstract boolean checkFirstConnection();
}
