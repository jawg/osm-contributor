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
package io.jawg.osmcontributor;


import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.preference.PreferenceManager;
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
import com.mapbox.mapboxsdk.MapboxAccountManager;

import org.greenrobot.eventbus.EventBus;

import java.io.File;

import io.fabric.sdk.android.Fabric;
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

    /*=========================================*/
    /*---------------OVERRIDE------------------*/
    /*=========================================*/
    @Override
    public void onCreate() {
        super.onCreate();
        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
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

        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(this).edit();
        if (!PreferenceManager.getDefaultSharedPreferences(this).getBoolean(getString(R.string.shared_prefs_preset_default), false)) {
            editor.putBoolean(getString(R.string.shared_prefs_preset_default), true);
        }
        editor.apply();

        MapboxAccountManager.start(this, BuildConfig.MAPBOX_TOKEN);
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

    /**
     * Get Flickr Helper for API request.
     * @return flickr object with API key set
     */
    public Flickr getFlickr() {
        return flickr;
    }
}
