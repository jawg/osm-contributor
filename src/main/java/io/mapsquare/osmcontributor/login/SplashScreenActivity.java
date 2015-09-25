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

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.widget.TextView;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import de.greenrobot.event.EventBus;
import io.mapsquare.osmcontributor.OsmTemplateApplication;
import io.mapsquare.osmcontributor.R;
import io.mapsquare.osmcontributor.core.events.InitCredentialsEvent;
import io.mapsquare.osmcontributor.core.events.InitSyncAlarmEvent;
import io.mapsquare.osmcontributor.core.events.SyncAlarmInitializedEvent;
import io.mapsquare.osmcontributor.login.events.SplashScreenTimerFinishedEvent;
import io.mapsquare.osmcontributor.map.MapActivity;
import io.mapsquare.osmcontributor.map.events.GeoJSONInitializedEvent;
import io.mapsquare.osmcontributor.map.events.InitGeoJSONEvent;
import io.mapsquare.osmcontributor.sync.assets.events.DbInitializedEvent;
import io.mapsquare.osmcontributor.sync.assets.events.InitDbEvent;
import io.mapsquare.osmcontributor.utils.EventCountDownTimer;
import timber.log.Timber;

public class SplashScreenActivity extends AppCompatActivity {

    private EventCountDownTimer timer;

    @Inject
    EventBus bus;

    @InjectView(R.id.edited_by)
    TextView editBy;

    @InjectView(R.id.powered_by)
    TextView poweredBy;

    @InjectView(R.id.mapsquare)
    TextView mapsquare;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        ((OsmTemplateApplication) getApplication()).getOsmTemplateComponent().inject(this);
        ButterKnife.inject(this);

        mapsquare.setText(Html.fromHtml(getString(R.string.mapsquare)));
        editBy.setText(Html.fromHtml(getString(R.string.splash_screen_edited_by)));
        poweredBy.setText(Html.fromHtml(getString(R.string.splash_screen_powered_by)));

        timer = new EventCountDownTimer(3000, 3000);
        timer.setStickyEvent(new SplashScreenTimerFinishedEvent());
        timer.start();

        bus.post(new InitCredentialsEvent());
        bus.post(new InitDbEvent());
        bus.post(new InitGeoJSONEvent());
        bus.post(new InitSyncAlarmEvent());
    }

    @Override
    protected void onResume() {
        super.onResume();
        bus.registerSticky(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        bus.unregister(this);
    }

    /**
     * Remove all the initialization finished sticky events and start the MapActivity.
     */
    private void startMapActivity() {
        bus.removeStickyEvent(DbInitializedEvent.class);
        bus.removeStickyEvent(GeoJSONInitializedEvent.class);
        bus.removeStickyEvent(SyncAlarmInitializedEvent.class);
        bus.removeStickyEvent(SplashScreenTimerFinishedEvent.class);
        Intent intent = new Intent(this, MapActivity.class);
        startActivity(intent);
    }

    /**
     * Check whether all the initialization finished response events are there and we should start MapActivity.
     *
     * @return Whether we should start MapActivity.
     */
    private boolean shouldStartMapActivity() {
        return bus.getStickyEvent(DbInitializedEvent.class) != null &&
                bus.getStickyEvent(SyncAlarmInitializedEvent.class) != null &&
                bus.getStickyEvent(GeoJSONInitializedEvent.class) != null &&
                bus.getStickyEvent(SplashScreenTimerFinishedEvent.class) != null;
    }

    public void onEventBackgroundThread(DbInitializedEvent event) {
        Timber.d("Database initialized");
        startMapActivityIfNeeded();
    }

    public void onEventBackgroundThread(SyncAlarmInitializedEvent event) {
        Timber.d("Sync Alarm initialized");
        startMapActivityIfNeeded();
    }

    public void onEventBackgroundThread(GeoJSONInitializedEvent event) {
        Timber.d("GeoJson initialized");
        startMapActivityIfNeeded();
    }

    public void onEventBackgroundThread(SplashScreenTimerFinishedEvent event) {
        Timber.d("Timer finished");
        startMapActivityIfNeeded();
    }

    private void startMapActivityIfNeeded() {
        if (shouldStartMapActivity()) {
            startMapActivity();
        }
    }
}
