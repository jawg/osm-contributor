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
package io.mapsquare.osmcontributor.preferences;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import javax.inject.Inject;

import de.greenrobot.event.EventBus;
import io.mapsquare.osmcontributor.OsmTemplateApplication;
import io.mapsquare.osmcontributor.R;
import io.mapsquare.osmcontributor.core.ConfigManager;
import io.mapsquare.osmcontributor.login.events.AttemptLoginEvent;
import io.mapsquare.osmcontributor.login.events.ErrorLoginEvent;
import io.mapsquare.osmcontributor.login.events.ValidLoginEvent;
import io.mapsquare.osmcontributor.sync.upload.SyncUploadService;
import io.mapsquare.osmcontributor.utils.StringUtils;

public class MyPreferenceFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {

    String loginKey;
    String passwordKey;
    String syncKey;
    Preference loginPref;
    Preference passwordPref;
    Preference syncPref;

    private Tracker tracker;

    @Inject
    SharedPreferences sharedPreferences;

    @Inject
    EventBus bus;

    @Inject
    ConfigManager configManager;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((OsmTemplateApplication) getActivity().getApplication()).getOsmTemplateComponent().inject(this);
        addPreferencesFromResource(R.xml.preferences);
        setHasOptionsMenu(true);

        AppCompatActivity appCompatActivity = (AppCompatActivity) getActivity();
        if (appCompatActivity != null) {
            ActionBar actionBar = appCompatActivity.getSupportActionBar();
            if (actionBar != null) {
                actionBar.setDisplayHomeAsUpEnabled(true);
            }
        }

        tracker = ((OsmTemplateApplication) this.getActivity().getApplication()).getTracker(OsmTemplateApplication.TrackerName.APP_TRACKER);
        tracker.setScreenName("CreationView");
        tracker.send(new HitBuilders.ScreenViewBuilder().build());

        loginKey = getString(R.string.shared_prefs_login);
        passwordKey = getString(R.string.shared_prefs_password);
        syncKey = getString(R.string.shared_prefs_sync_key);

        loginPref = findPreference(loginKey);
        passwordPref = findPreference(passwordKey);
        syncPref = findPreference(syncKey);

        boolean isManual = sharedPreferences.getBoolean(SyncUploadService.IS_MANUAL_SYNC, false);
        syncPref.setDefaultValue(isManual);

        Preference preference = findPreference(getString(R.string.shared_prefs_server_key));
        preference.setSummary(configManager.getBasePoiApiUrl());

        updateLoginSummary(getPreferenceScreen().getSharedPreferences());
        updatePasswordSummary(getPreferenceScreen().getSharedPreferences());
    }

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
        bus.register(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
        bus.unregister(this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            getActivity().finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        updatePrefsSummary(sharedPreferences, key);
    }

    private void updateLoginSummary(SharedPreferences sharedPreferences) {
        String login = getLogin(sharedPreferences);
        if (!StringUtils.isEmpty(login)) {
            loginPref.setSummary(login);
        } else {
            loginPref.setSummary(getString(R.string.summary_login));
        }
    }

    private void updatePasswordSummary(SharedPreferences sharedPreferences) {
        String password = getPassword(sharedPreferences);
        if (!StringUtils.isEmpty(password)) {
            passwordPref.setSummary(password.replaceAll("(?s).", "*"));
        } else {
            passwordPref.setSummary(getString(R.string.summary_password));
        }
    }

    private void attemptLoginIfValidFields(SharedPreferences sharedPreferences) {
        String login = getLogin(sharedPreferences);
        String password = getPassword(sharedPreferences);
        if (!StringUtils.isEmpty(login) && !StringUtils.isEmpty(password)) {
            bus.post(new AttemptLoginEvent(login, password));
        }
    }

    private void updatePrefsSummary(SharedPreferences sharedPreferences, String key) {
        if (loginKey.equals(key)) {
            updateLoginSummary(sharedPreferences);
            attemptLoginIfValidFields(sharedPreferences);

            tracker.send(new HitBuilders.EventBuilder()
                    .setCategory("Edit preference")
                    .setAction("Login edited")
                    .build());

        } else if (passwordKey.equals(key)) {
            updatePasswordSummary(sharedPreferences);
            attemptLoginIfValidFields(sharedPreferences);

            tracker.send(new HitBuilders.EventBuilder()
                    .setCategory("Edit preference")
                    .setAction("Password edited")
                    .build());

        } else if (syncKey.equals(key)) {
            boolean s = sharedPreferences.getBoolean(syncKey, false);
            sharedPreferences.edit().putBoolean(SyncUploadService.IS_MANUAL_SYNC, s).apply();

            tracker.send(new HitBuilders.EventBuilder()
                    .setCategory("Edit preference")
                    .setAction("Switch to manual sync mode = " + s)
                    .build());
        }
    }

    private String getLogin(SharedPreferences sharedPreferences) {
        return sharedPreferences.getString(loginKey, null);
    }

    private String getPassword(SharedPreferences sharedPreferences) {
        return sharedPreferences.getString(passwordKey, null);
    }

    public void onEventMainThread(ValidLoginEvent event) {
        Toast.makeText(getActivity(), R.string.valid_login, Toast.LENGTH_SHORT).show();
        // once log update all changes to OSM
        SyncUploadService.startService(getActivity());
    }

    public void onEventMainThread(ErrorLoginEvent event) {
        Toast.makeText(getActivity(), R.string.error_login, Toast.LENGTH_LONG).show();
    }
}
