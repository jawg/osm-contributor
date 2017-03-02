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
package io.jawg.osmcontributor.ui.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.yarolegovich.lovelydialog.LovelyStandardDialog;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.jawg.osmcontributor.OsmTemplateApplication;
import io.jawg.osmcontributor.R;
import io.jawg.osmcontributor.model.entities.H2GeoPresetsItem;
import io.jawg.osmcontributor.model.events.DatabaseResetFinishedEvent;
import io.jawg.osmcontributor.model.events.PleaseLoadPoiTypes;
import io.jawg.osmcontributor.model.events.ResetTypeDatabaseEvent;
import io.jawg.osmcontributor.rest.dtos.dma.H2GeoDto;
import io.jawg.osmcontributor.rest.events.PresetDownloadedEvent;
import io.jawg.osmcontributor.rest.events.PresetListDownloadedEvent;
import io.jawg.osmcontributor.rest.events.error.PresetDownloadErrorEvent;
import io.jawg.osmcontributor.rest.events.error.PresetListDownloadErrorEvent;
import io.jawg.osmcontributor.rest.mappers.H2GeoPresetsItemMapper;
import io.jawg.osmcontributor.service.OfflineRegionDownloadService;
import io.jawg.osmcontributor.ui.adapters.ProfileAdapter;
import io.jawg.osmcontributor.ui.events.presets.PleaseDownloadPresetEvent;
import io.jawg.osmcontributor.ui.events.presets.PleaseDownloadPresetListEvent;

public class LoadProfileActivity extends AppCompatActivity
        implements ProfileAdapter.ProfileSelectedListener {
    private static final String TAG = "LoadProfileActivity";
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

    @Inject
    H2GeoPresetsItemMapper mapper;

    private ProfileAdapter profileAdapter;
    private boolean databaseReseted, regionDownloadStart;

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

        showProgressBar();
        profileAdapter = new ProfileAdapter(this, this);
        ((OsmTemplateApplication) getApplication()).getOsmTemplateComponent().inject(profileAdapter);
        listView.setAdapter(profileAdapter);

        eventBus.register(this);
        startLoadingPresetList();
    }

    private void startOfflineDownloadService(List<List<Double>> offlineAreas) {
        Intent intent = new Intent(this, OfflineRegionDownloadService.class);
        int c = 0;
        for (List<Double> d : offlineAreas) {
            intent.putStringArrayListExtra(OfflineRegionDownloadService.LIST_PARAM + c, convertDoubleList(d));
            c++;
        }
        intent.putExtra(OfflineRegionDownloadService.SIZE_PARAM, offlineAreas.size());
        startService(intent);
    }

    private ArrayList<String> convertDoubleList(List<Double> ds) {
        ArrayList<String> s = new ArrayList<>();
        for (Double d : ds) {
            s.add(d.toString());
        }
        return s;
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
        profileAdapter.addAll(event.getPresets());
        hideProgressBar();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onPresetDownloadedEvent(final PresetDownloadedEvent event) {
        Log.i(TAG, "onPresetDownloadedEvent: " + event.getH2GeoDto().getOfflineArea());
        if (event.getH2GeoDto().getOfflineArea() != null && !event.getH2GeoDto().getOfflineArea().isEmpty()) {
            askUserForDownloading(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    startOfflineDownloadService(event.getH2GeoDto().getOfflineArea());
                    regionDownloadStart = true;
                    checkFinishActivity();
                }
            });
        } else {
            regionDownloadStart = true;
            checkFinishActivity();
        }
    }

    private void askUserForDownloading(View.OnClickListener onClickListener) {
        new LovelyStandardDialog(this)
                .setTopColorRes(R.color.colorPrimary)
                .setButtonsColorRes(R.color.colorPrimaryDark)
                .setIcon(R.drawable.ic_file_download_white)
                .setTitle(R.string.download_offline_area_title)
                .setMessage(R.string.download_offline_area_message)
                .setPositiveButton(R.string.ok, onClickListener)
                .setNegativeButton(R.string.download_offline_area_more_later, new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        regionDownloadStart = true;
                        checkFinishActivity();
                    }
                })
                .show();
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
        eventBus.post(new PleaseLoadPoiTypes());
        databaseReseted = true;
        checkFinishActivity();
    }

    private void checkFinishActivity() {
        if (databaseReseted && regionDownloadStart) {
            finish();
            databaseReseted = false;
            regionDownloadStart = false;
        }
    }

    // *********************************
    // ************ private ************
    // *********************************

    private void startLoadingPresetList() {
        showProgressBar();
        eventBus.post(new PleaseDownloadPresetListEvent());
    }

    private void startLoadingPresetList(String filename) {
        showProgressBar();
        eventBus.post(new PleaseDownloadPresetEvent(filename));
    }

    private void handleDownloadError() {
        Toast.makeText(this, R.string.profile_refresh_error, Toast.LENGTH_LONG).show();
        hideProgressBar();
    }

    private void showProgressBar() {
        listView.setVisibility(View.INVISIBLE);
        progressBar.setVisibility(View.VISIBLE);
    }

    private void hideProgressBar() {
        listView.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.INVISIBLE);
    }

    @Override
    public void profileClicked(H2GeoPresetsItem h2GeoPresetsItem) {
        showProgressBar();
        if (h2GeoPresetsItem == null) {
            eventBus.post(new ResetTypeDatabaseEvent());
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean(getString(R.string.shared_prefs_preset_default), true);
            editor.putString(getString(R.string.shared_prefs_preset_selected), mapper.getCurrentLanguage(H2GeoDto.getDefaultPreset(this).getName()));
            editor.apply();
            profileAdapter.notifyDataSetChanged();
            regionDownloadStart = true;
        } else {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean(getString(R.string.shared_prefs_preset_default), false);
            editor.putString(getString(R.string.shared_prefs_preset_selected), h2GeoPresetsItem.getName());
            editor.apply();
            profileAdapter.notifyDataSetChanged();
            eventBus.post(new PleaseDownloadPresetEvent(h2GeoPresetsItem.getFile()));
        }
    }
}
