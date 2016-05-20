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
package io.mapsquare.osmcontributor.preferences;

import android.app.AlertDialog;
import android.content.DialogInterface;
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

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import io.mapsquare.osmcontributor.OsmTemplateApplication;
import io.mapsquare.osmcontributor.R;
import io.mapsquare.osmcontributor.core.ConfigManager;
import io.mapsquare.osmcontributor.core.events.DatabaseResetFinishedEvent;
import io.mapsquare.osmcontributor.core.events.ResetDatabaseEvent;
import io.mapsquare.osmcontributor.core.events.ResetTypeDatabaseEvent;
import io.mapsquare.osmcontributor.login.events.AttemptLoginEvent;
import io.mapsquare.osmcontributor.login.events.ErrorLoginEvent;
import io.mapsquare.osmcontributor.login.events.ValidLoginEvent;
import io.mapsquare.osmcontributor.utils.StringUtils;

public class MyPreferenceFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {

    String loginKey;
    String passwordKey;
    Preference loginPref;
    Preference passwordPref;
    Preference resetTypePref;

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

        loginPref = findPreference(loginKey);
        passwordPref = findPreference(passwordKey);

        Preference preference = findPreference(getString(R.string.shared_prefs_server_key));
        preference.setSummary(configManager.getBasePoiApiUrl());

        updateLoginSummary(getPreferenceScreen().getSharedPreferences());
        updatePasswordSummary(getPreferenceScreen().getSharedPreferences());

        Preference h2geoPreference = findPreference(getString(R.string.shared_prefs_h2geo_version));
        h2geoPreference.setTitle(sharedPreferences.getString(getString(R.string.shared_prefs_h2geo_version), ""));
        h2geoPreference.setSummary(sharedPreferences.getString(getString(R.string.shared_prefs_h2geo_date), ""));

        Preference resetPreference = findPreference(getString(R.string.shared_prefs_reset));
        resetPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                new AlertDialog.Builder(getActivity())
                        .setMessage(R.string.reset_dialog_message)
                        .setPositiveButton(R.string.reset_dialog_positive_button, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                bus.post(new ResetDatabaseEvent());
                                dialog.dismiss();
                            }
                        })
                        .setNegativeButton(R.string.reset_dialog_negative_button, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        }).show();
                return false;
            }
        });

        resetTypePref = findPreference(getString(R.string.shared_prefs_reset_type));
        resetTypePref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                new AlertDialog.Builder(getActivity())
                        .setMessage(R.string.reset_dialog_message)
                        .setPositiveButton(R.string.reset_dialog_positive_button, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                bus.post(new ResetTypeDatabaseEvent());
                                dialog.dismiss();
                            }
                        })
                        .setNegativeButton(R.string.reset_dialog_negative_button, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        }).show();
                return false;
            }
        });

        findPreference(getString(R.string.shared_prefs_expert_mode)).setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                if (sharedPreferences.getBoolean(getString(R.string.shared_prefs_expert_mode), false)) {
                    new AlertDialog.Builder(getActivity())
                            .setCancelable(false)
                            .setMessage(getString(R.string.expert_mode_dialog))
                            .setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            }).show();
                }
                return false;
            }
        });
        if (!sharedPreferences.getBoolean(getString(R.string.shared_prefs_expert_mode), false)) {
            getPreferenceScreen().removePreference(resetTypePref);
        }
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
        // Show or hide the reset type preference depending on the value of the expert mode preference.
        if (getString(R.string.shared_prefs_expert_mode).equals(key)) {
            if (sharedPreferences.getBoolean(key, false)) {
                getPreferenceScreen().addPreference(resetTypePref);
            } else {
                getPreferenceScreen().removePreference(resetTypePref);
            }
        }
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
        }
    }

    private String getLogin(SharedPreferences sharedPreferences) {
        return sharedPreferences.getString(loginKey, null);
    }

    private String getPassword(SharedPreferences sharedPreferences) {
        return sharedPreferences.getString(passwordKey, null);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onValidLoginEvent(ValidLoginEvent event) {
        Toast.makeText(getActivity(), R.string.valid_login, Toast.LENGTH_SHORT).show();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onErrorLoginEvent(ErrorLoginEvent event) {
        Toast.makeText(getActivity(), R.string.error_login, Toast.LENGTH_LONG).show();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onDatabaseResetFinishedEvent(DatabaseResetFinishedEvent event) {
        Toast.makeText(getActivity(), event.isSuccess() ? R.string.reset_success : R.string.reset_failure, Toast.LENGTH_SHORT).show();
    }
}
