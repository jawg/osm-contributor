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
package io.mapsquare.osmcontributor.ui.activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.mapsquare.osmcontributor.OsmTemplateApplication;
import io.mapsquare.osmcontributor.R;
import io.mapsquare.osmcontributor.model.entities.H2GeoPresetsItem;
import io.mapsquare.osmcontributor.model.events.DatabaseResetFinishedEvent;
import io.mapsquare.osmcontributor.model.events.PleaseLoadPoiTypes;
import io.mapsquare.osmcontributor.model.events.RefreshPresetFilters;
import io.mapsquare.osmcontributor.model.events.ResetTypeDatabaseEvent;
import io.mapsquare.osmcontributor.rest.events.PresetDownloadedEvent;
import io.mapsquare.osmcontributor.rest.events.PresetListDownloadedEvent;
import io.mapsquare.osmcontributor.rest.events.error.PresetDownloadErrorEvent;
import io.mapsquare.osmcontributor.rest.events.error.PresetListDownloadErrorEvent;
import io.mapsquare.osmcontributor.ui.adapters.ProfileAdapter;
import io.mapsquare.osmcontributor.ui.events.presets.PleaseDownloadPresetEvent;
import io.mapsquare.osmcontributor.ui.events.presets.PleaseDownloadPresetListEvent;

public class LoadProfileActivity extends AppCompatActivity
    implements ProfileAdapter.ProfileSelectedListener {

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.progressbar)
    ProgressBar progressBar;

    @BindView(R.id.list_view)
    ListView listView;

    @Inject
    EventBus eventBus;

    @Inject
    SharedPreferences sharedPreferences;

    private ProfileAdapter profileAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_load_profile);

        ButterKnife.bind(this);
        ((OsmTemplateApplication) getApplication()).getOsmTemplateComponent().inject(this);

        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        showLoadingSeekBar();
        profileAdapter = new ProfileAdapter(this, this);
        listView.setAdapter(profileAdapter);

        eventBus.register(this);
        startLoadingPresetList();
    }

    @Override
    protected void onStop() {
        eventBus.unregister(this);
        super.onStop();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    // ********************************
    // ************ Events ************
    // ********************************

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onPresetListDownloadedEvent(PresetListDownloadedEvent event) {
        // TODO refresh preset list
        /* TODO In order to refresh a particular preset, call the following:
         * eventBus.post(new PleaseDownloadPresetEvent("filename.json"));
         */
        profileAdapter.addAll(event.getPresets());
        hideLoadingSeekBar();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onPresetDownloadedEvent(PresetDownloadedEvent event) {
        // TODO cache/display preset data
        hideLoadingSeekBar();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onPresetListDownloadErrorEvent(PresetListDownloadErrorEvent event) {
        handleDownloadError();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onPresetDownloadErrorEvent(PresetDownloadErrorEvent event) {
        handleDownloadError();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onDatabaseResetFinishedEvent(DatabaseResetFinishedEvent event) {
        PleaseLoadPoiTypes pleaseLoadPoiTypes = new PleaseLoadPoiTypes();
        pleaseLoadPoiTypes.setPreset(true);
        eventBus.post(pleaseLoadPoiTypes);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onRefreshPresetFilter(RefreshPresetFilters event) {

        finish();
    }

    // *********************************
    // ************ private ************
    // *********************************

    private void startLoadingPresetList() {
        showLoadingSeekBar();
        eventBus.post(new PleaseDownloadPresetListEvent());
    }

    private void startLoadingPresetList(String filename) {
        showLoadingSeekBar();
        eventBus.post(new PleaseDownloadPresetEvent(filename));
    }

    private void handleDownloadError() {
        Toast.makeText(this, R.string.profile_refresh_error, Toast.LENGTH_LONG).show();
        hideLoadingSeekBar();
    }

    private void showLoadingSeekBar() {
        listView.setVisibility(View.INVISIBLE);
        progressBar.setVisibility(View.VISIBLE);
    }

    private void hideLoadingSeekBar() {
        listView.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.INVISIBLE);
    }

    @Override
    public void profileClicked(H2GeoPresetsItem h2GeoPresetsItem) {
        showLoadingSeekBar();
        if (h2GeoPresetsItem == null) {
            eventBus.post(new ResetTypeDatabaseEvent(null));
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean(getString(R.string.shared_prefs_preset_default), false);
            editor.apply();
        } else {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean(getString(R.string.shared_prefs_preset_default), true);
            editor.apply();
            eventBus.post(new PleaseDownloadPresetEvent(h2GeoPresetsItem.getFile()));
        }
    }
}
