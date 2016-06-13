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

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.widget.TextView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.mapsquare.osmcontributor.OsmTemplateApplication;
import io.mapsquare.osmcontributor.R;
import io.mapsquare.osmcontributor.model.events.InitCredentialsEvent;
import io.mapsquare.osmcontributor.ui.events.login.SplashScreenTimerFinishedEvent;
import io.mapsquare.osmcontributor.ui.events.map.ArpiBitmapsPrecomputedEvent;
import io.mapsquare.osmcontributor.ui.events.map.PrecomputeArpiBitmapsEvent;
import io.mapsquare.osmcontributor.database.events.DbInitializedEvent;
import io.mapsquare.osmcontributor.database.events.InitDbEvent;
import io.mapsquare.osmcontributor.ui.utils.views.EventCountDownTimer;
import timber.log.Timber;

public class SplashScreenActivity extends AppCompatActivity {
    @Inject
    EventBus bus;

    @BindView(R.id.edited_by)
    TextView editBy;

    @BindView(R.id.powered_by)
    TextView poweredBy;

    @BindView(R.id.mapsquare)
    TextView mapsquare;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        ButterKnife.setDebug(true);

        ButterKnife.bind(this);
        ((OsmTemplateApplication) getApplication()).getOsmTemplateComponent().inject(this);

        mapsquare.setText(Html.fromHtml(getString(R.string.mapsquare)));
        editBy.setText(Html.fromHtml(getString(R.string.splash_screen_edited_by)));
        poweredBy.setText(Html.fromHtml(getString(R.string.splash_screen_powered_by)));

        EventCountDownTimer timer = new EventCountDownTimer(3000, 3000, bus);
        timer.setStickyEvent(new SplashScreenTimerFinishedEvent());
        timer.start();

        bus.post(new InitCredentialsEvent());
        bus.post(new InitDbEvent());
        bus.post(new PrecomputeArpiBitmapsEvent());

    }

    @Override
    protected void onResume() {
        super.onResume();
        bus.register(this);
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
        bus.removeStickyEvent(SplashScreenTimerFinishedEvent.class);
        bus.removeStickyEvent(ArpiBitmapsPrecomputedEvent.class);
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
                bus.getStickyEvent(SplashScreenTimerFinishedEvent.class) != null &&
                bus.getStickyEvent(ArpiBitmapsPrecomputedEvent.class) != null;
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.ASYNC)
    public void onDbInitializedEvent(DbInitializedEvent event) {
        Timber.d("Database initialized");
        startMapActivityIfNeeded();
    }

    @Subscribe(threadMode = ThreadMode.ASYNC)
    public void onSplashScreenTimerFinishedEvent(SplashScreenTimerFinishedEvent event) {
        Timber.d("Timer finished");
        startMapActivityIfNeeded();
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.ASYNC)
    public void onArpiBitmapsPrecomputedEvent(ArpiBitmapsPrecomputedEvent event) {
        Timber.d("Arpi bitmaps precomputed");
        startMapActivityIfNeeded();
    }

    private void startMapActivityIfNeeded() {
        if (shouldStartMapActivity()) {
            startMapActivity();
        }
    }
}
