/**
 * Copyright (C) 2019 Takima
 * <p>
 * This file is part of OSM Contributor.
 * <p>
 * OSM Contributor is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * OSM Contributor is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with OSM Contributor.  If not, see <http://www.gnu.org/licenses/>.
 */
package io.jawg.osmcontributor;


import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.multidex.MultiDex;

import com.crashlytics.android.Crashlytics;
import com.facebook.cache.disk.DiskCacheConfig;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.imagepipeline.cache.MemoryCacheParams;
import com.facebook.imagepipeline.core.ImagePipelineConfig;
import com.facebook.stetho.Stetho;
import com.flickr4java.flickr.Flickr;
import com.flickr4java.flickr.REST;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.firebase.FirebaseApp;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.services.android.telemetry.MapboxTelemetry;
import io.fabric.sdk.android.Fabric;
import org.greenrobot.eventbus.EventBus;

import java.io.File;

import io.jawg.osmcontributor.modules.DaggerOsmTemplateComponent;
import io.jawg.osmcontributor.modules.OsmTemplateComponent;
import io.jawg.osmcontributor.modules.OsmTemplateModule;
import io.jawg.osmcontributor.utils.core.StoreConfigManager;
import timber.log.Timber;

public class OsmTemplateApplication extends Application {

    /*=========================================*/
    /*--------------ATTRIBUTES-----------------*/
    /*=========================================*/
    private OsmTemplateComponent osmTemplateComponent;

    private Flickr flickr;

    private static ConnectivityManager conMgr;

    /*=========================================*/
    /*---------------OVERRIDE------------------*/
    /*=========================================*/
    @Override
    public void onCreate() {
        super.onCreate();
        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());

            // Init Stetho for debug purpose (database)
            Stetho.initializeWithDefaults(this);
        }
        configureCrashReporting();

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
                .setBitmapMemoryCacheParamsSupplier(() -> new MemoryCacheParams(10485760, 100, 100, 100, 100))
                .setMainDiskCacheConfig(diskCacheConfig)
                .build();

        // Init Fresco
        Fresco.initialize(this, imagePipelineConfig);

        // Init event bus
        EventBus bus = osmTemplateComponent.getEventBus();
        bus.register(getOsmTemplateComponent().getLoginManager());
        bus.register(getOsmTemplateComponent().getEditPoiManager());
        bus.register(getOsmTemplateComponent().getPoiManager());
        bus.register(getOsmTemplateComponent().getRelationManager());
        bus.register(getOsmTemplateComponent().getNoteManager());
        bus.register(getOsmTemplateComponent().getSyncManager());
        bus.register(getOsmTemplateComponent().getTypeManager());
        bus.register(getOsmTemplateComponent().getPresetsManager());
        bus.register(getOsmTemplateComponent().getGeocoder());
        bus.register(getOsmTemplateComponent().getEditVectorialWayManager());

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        if (!sharedPreferences.getBoolean(getString(R.string.shared_prefs_preset_default), false)) {
            editor.putBoolean(getString(R.string.shared_prefs_preset_default), true);
        }
        if (!sharedPreferences.contains(getString(R.string.shared_prefs_auto_commit))) {
            editor.putBoolean(getString(R.string.shared_prefs_auto_commit), true);
        }
        editor.apply();

        Mapbox.getInstance(this, BuildConfig.MAPBOX_TOKEN);
        MapboxTelemetry.getInstance().setTelemetryEnabled(false);

        conMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
    }

    private void configureCrashReporting() {
        Fabric.with(this, new Crashlytics());
        FirebaseApp.initializeApp(this);
        GoogleAnalytics.getInstance(this);
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        try {
            super.attachBaseContext(newBase);
            MultiDex.install(newBase);
        } catch (RuntimeException ignored) {
            // Multidex support doesn't play well with Robolectric yet
        }
    }

    /*=========================================*/
    /*----------------GETTER-------------------*/
    /*=========================================*/

    /**
     * Use for Dagger Injection.
     *
     * @return an object to inject a class
     */
    public OsmTemplateComponent getOsmTemplateComponent() {
        return osmTemplateComponent;
    }

    /**
     * Get Flickr Helper for API request.
     *
     * @return flickr object with API key set
     */
    public Flickr getFlickr() {
        return flickr;
    }

    public static Boolean hasNetwork() {
        if (conMgr != null) {
            if (conMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE) != null) {
                return conMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED
                        || conMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED;
            } else { //check only wifi for tablets
                return conMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED;
            }
        } else {
            return null;
        }
    }
}
