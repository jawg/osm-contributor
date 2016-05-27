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
package io.mapsquare.osmcontributor;


import android.app.Application;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;
import com.squareup.leakcanary.LeakCanary;

import org.greenrobot.eventbus.EventBus;

import java.util.HashMap;

import io.fabric.sdk.android.Fabric;
import io.mapsquare.osmcontributor.crashlytics.CrashContextWrapper;
import io.mapsquare.osmcontributor.crashlytics.CrashlyticsTree;
import timber.log.Timber;

public class OsmTemplateApplication extends Application {
    private static final String ANALYTICS_PROPERTY_ID = "UA-63422911-1";

    public enum TrackerName {
        APP_TRACKER, // Tracker used only in this app.
    }

    HashMap<TrackerName, Tracker> trackers = new HashMap<>();

    OsmTemplateComponent osmTemplateComponent;

    @Override
    public void onCreate() {
        super.onCreate();
        LeakCanary.install(this);

        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        } else {
            Fabric.with(new CrashContextWrapper(this, getPackageName()), new Crashlytics());
            Timber.plant(new CrashlyticsTree());
        }

        osmTemplateComponent = DaggerOsmTemplateComponent.builder()
                .osmTemplateModule(new OsmTemplateModule(this)).build();
        osmTemplateComponent.inject(this);

        EventBus bus = osmTemplateComponent.getEventBus();

        bus.register(getOsmTemplateComponent().getLoginManager());
        bus.register(getOsmTemplateComponent().getEditPoiManager());
        bus.register(getOsmTemplateComponent().getPoiManager());
        bus.register(getOsmTemplateComponent().getNoteManager());
        bus.register(getOsmTemplateComponent().getSyncManager());
        bus.register(getOsmTemplateComponent().getTypeManager());
        bus.register(getOsmTemplateComponent().getGeocoder());
        bus.register(getOsmTemplateComponent().getArpiInitializer());
    }

    public OsmTemplateComponent getOsmTemplateComponent() {
        return osmTemplateComponent;
    }

    public synchronized Tracker getTracker(TrackerName trackerId) {
        if (!trackers.containsKey(trackerId)) {
            GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
            analytics.setDryRun(BuildConfig.DEBUG);
            analytics.setLocalDispatchPeriod(1);
            Tracker t = analytics.newTracker(ANALYTICS_PROPERTY_ID);
            t.enableAutoActivityTracking(true);
            t.enableAdvertisingIdCollection(true);

            trackers.put(trackerId, t);
        }
        return trackers.get(trackerId);
    }
}
