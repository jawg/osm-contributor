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
package io.jawg.osmcontributor.ui.fragments;

import android.accounts.AccountManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import com.google.android.gms.common.AccountPicker;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import javax.inject.Inject;

import io.jawg.osmcontributor.OsmTemplateApplication;
import io.jawg.osmcontributor.R;
import io.jawg.osmcontributor.model.events.DatabaseResetFinishedEvent;
import io.jawg.osmcontributor.model.events.ResetDatabaseEvent;
import io.jawg.osmcontributor.model.events.ResetTypeDatabaseEvent;
import io.jawg.osmcontributor.rest.events.GoogleAuthenticatedEvent;
import io.jawg.osmcontributor.rest.security.GoogleOAuthManager;
import io.jawg.osmcontributor.ui.events.login.AttemptLoginEvent;
import io.jawg.osmcontributor.ui.events.login.ErrorLoginEvent;
import io.jawg.osmcontributor.ui.events.login.UpdateGoogleCredentialsEvent;
import io.jawg.osmcontributor.ui.events.login.ValidLoginEvent;
import io.jawg.osmcontributor.utils.ConfigManager;
import io.jawg.osmcontributor.utils.StringUtils;

public class MyPreferenceFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {
    private static final int PICK_ACCOUNT_CODE = 1;
    public static final int RC_SIGN_IN = 2;
    private String loginKey;
    private String passwordKey;
    private Preference loginPref;
    private Preference passwordPref;
    private Preference resetTypePref;
    private Preference googleConnectPref;

    @Inject
    GoogleOAuthManager googleOAuthManager;

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


        googleConnectPref = findPreference(getString(R.string.shared_prefs_google_connection_key));
        googleConnectPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Intent intent = AccountPicker.newChooseAccountIntent(
                        null, null,
                        new String[]{"com.google"},
                        false, null, null, null, null);
                startActivityForResult(intent, PICK_ACCOUNT_CODE);
                return false;
            }
        });

        if (!sharedPreferences.getBoolean(getString(R.string.shared_prefs_expert_mode), false)) {
            getPreferenceScreen().removePreference(resetTypePref);
        }
    }

    private static final String TAG = "MyPreferenceFragment";

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_ACCOUNT_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                String email = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                googleOAuthManager.authenticate(getActivity(), email);
            }
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
            // Login changed
            updateLoginSummary(sharedPreferences);
            attemptLoginIfValidFields(sharedPreferences);
        } else if (passwordKey.equals(key)) {
            // Password changed
            updatePasswordSummary(sharedPreferences);
            attemptLoginIfValidFields(sharedPreferences);
        }
    }

    private String getLogin(SharedPreferences sharedPreferences) {
        return sharedPreferences.getString(loginKey, null);
    }

    private String getPassword(SharedPreferences sharedPreferences) {
        return sharedPreferences.getString(passwordKey, null);
    }

    @Subscribe
    public void onGoogleAuthenticatedEvent(GoogleAuthenticatedEvent event) {
        if (event.isSuccessful()) {
            Snackbar.make(getView(), R.string.valid_login, Snackbar.LENGTH_SHORT).show();
            bus.post(new UpdateGoogleCredentialsEvent(event.getToken(), event.getTokenSecret(), event.getConsumer(), event.getConsumerSecret()));
        } else {
            Snackbar.make(getView(), R.string.error_login, Snackbar.LENGTH_SHORT).show();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onValidLoginEvent(ValidLoginEvent event) {
        Snackbar.make(getView(), R.string.valid_login, Snackbar.LENGTH_SHORT).show();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onErrorLoginEvent(ErrorLoginEvent event) {
        Snackbar.make(getView(), R.string.error_login, Snackbar.LENGTH_LONG).show();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onDatabaseResetFinishedEvent(DatabaseResetFinishedEvent event) {
        Snackbar.make(getView(), event.isSuccess() ? R.string.reset_success : R.string.reset_failure, Snackbar.LENGTH_SHORT).show();
    }
}
