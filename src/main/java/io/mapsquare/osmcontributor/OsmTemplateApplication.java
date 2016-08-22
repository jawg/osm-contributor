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
import android.content.Context;
import android.os.Environment;
import android.support.multidex.MultiDex;

import com.crashlytics.android.Crashlytics;
import com.facebook.cache.disk.DiskCacheConfig;
import com.facebook.common.internal.Supplier;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.imagepipeline.cache.MemoryCacheParams;
import com.facebook.imagepipeline.core.ImagePipelineConfig;
import com.facebook.stetho.Stetho;
import com.flickr4java.flickr.Flickr;
import com.flickr4java.flickr.REST;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;
import com.mapbox.mapboxsdk.MapboxAccountManager;
import com.squareup.leakcanary.LeakCanary;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.util.HashMap;

import io.fabric.sdk.android.Fabric;
import io.mapsquare.osmcontributor.modules.DaggerOsmTemplateComponent;
import io.mapsquare.osmcontributor.modules.OsmTemplateComponent;
import io.mapsquare.osmcontributor.modules.OsmTemplateModule;
import io.mapsquare.osmcontributor.utils.core.StoreConfigManager;
import io.mapsquare.osmcontributor.utils.crashlytics.CrashlyticsTree;
import timber.log.Timber;

public class OsmTemplateApplication extends Application {

    /*=========================================*/
    /*---------------CONSTANTS-----------------*/
    /*=========================================*/
    private static final String ANALYTICS_PROPERTY_ID = "UA-63422911-1";

    public enum TrackerName {
        APP_TRACKER, // Tracker used only in this app.
    }

    /*=========================================*/
    /*--------------ATTRIBUTES-----------------*/
    /*=========================================*/
    private HashMap<TrackerName, Tracker> trackers = new HashMap<>();

    private OsmTemplateComponent osmTemplateComponent;

    private Flickr flickr;

    /*=========================================*/
    /*---------------OVERRIDE------------------*/
    /*=========================================*/
    @Override
    public void onCreate() {
        super.onCreate();
        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        } else {
            Timber.plant(new CrashlyticsTree());
        }
        Fabric.with(this, new Crashlytics());

        // Init Stetho for debug purpose (database)
        Stetho.initializeWithDefaults(this);

        // Init Dagger
        osmTemplateComponent = DaggerOsmTemplateComponent.builder().osmTemplateModule(new OsmTemplateModule(this)).build();
        osmTemplateComponent.inject(this);

        // Init Flickr object
        StoreConfigManager configManager = new StoreConfigManager();
        flickr = new Flickr(configManager.getFlickrApiKey(), configManager.getFlickrApiKeySecret(), new REST());

        // Cache Disk for Fresco
        DiskCacheConfig diskCacheConfig = DiskCacheConfig.newBuilder(this)
                .setBaseDirectoryPath(new File(Environment.getExternalStorageDirectory().getAbsoluteFile(), getPackageName()))
                .setBaseDirectoryName("images")
                .build();
        // Cache Memory for Fresco
        ImagePipelineConfig imagePipelineConfig = ImagePipelineConfig.newBuilder(this)
                .setBitmapMemoryCacheParamsSupplier(new Supplier<MemoryCacheParams>() {
                    @Override
                    public MemoryCacheParams get() {
                        return new MemoryCacheParams(10485760, 100, 100, 100, 100);
                    }
                })
                .setMainDiskCacheConfig(diskCacheConfig)
                .build();

        // Init Fresco
        Fresco.initialize(this, imagePipelineConfig);

        // Init event bus
        EventBus bus = osmTemplateComponent.getEventBus();
        bus.register(getOsmTemplateComponent().getLoginManager());
        bus.register(getOsmTemplateComponent().getEditPoiManager());
        bus.register(getOsmTemplateComponent().getPoiManager());
        bus.register(getOsmTemplateComponent().getNoteManager());
        bus.register(getOsmTemplateComponent().getSyncManager());
        bus.register(getOsmTemplateComponent().getTypeManager());
        bus.register(getOsmTemplateComponent().getPresetsManager());
        bus.register(getOsmTemplateComponent().getGeocoder());
        bus.register(getOsmTemplateComponent().getArpiInitializer());
        bus.register(getOsmTemplateComponent().getEditVectorialWayManager());

        MapboxAccountManager.start(this, BuildConfig.MAPBOX_TOKEN);
        LeakCanary.install(this);
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(newBase);
        MultiDex.install(this);
    }

    /*=========================================*/
    /*----------------GETTER-------------------*/
    /*=========================================*/

    /**
     * Use for Dagger Injection.
     * @return an object to inject a class
     */
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

    /**
     * Get Flickr Helper for API request.
     * @return flickr object with API key set
     */
    public Flickr getFlickr() {
        return flickr;
    }
}
