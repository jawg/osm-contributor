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
package io.mapsquare.osmcontributor.sync;

import com.squareup.okhttp.OkHttpClient;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import org.greenrobot.eventbus.EventBus;
import io.mapsquare.osmcontributor.core.ConfigManager;
import io.mapsquare.osmcontributor.sync.converter.PoiStorageConverter;
import io.mapsquare.osmcontributor.sync.rest.AuthenticationRequestInterceptor;
import io.mapsquare.osmcontributor.sync.rest.InterceptorChain;
import io.mapsquare.osmcontributor.sync.rest.PoiStorageClient;
import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import retrofit.android.AndroidLog;
import retrofit.client.OkClient;

@Module
@Singleton
public class SyncModule {

    @Provides
    SyncWayManager getSyncWayManager() {
        return new PoiStorageSyncWayManager();
    }

    @Provides
    Backend getBackend(PoiStorageClient poiStorageClient, PoiStorageConverter poiStorageConverter, EventBus bus) {
        return new PoiStorageBackend(poiStorageClient, poiStorageConverter, bus);
    }

    @Provides
    RestAdapter getRestAdapter(OkHttpClient okHttpClient, ConfigManager configManager, AuthenticationRequestInterceptor authenticationRequestInterceptor) {
        return new RestAdapter.Builder()
                .setEndpoint(configManager.getBasePoiApiUrl())
                .setClient(new OkClient(okHttpClient))
                .setRequestInterceptor(
                        new InterceptorChain(authenticationRequestInterceptor,
                                new RequestInterceptor() {
                                    @Override
                                    public void intercept(RequestFacade request) {
                                        request.addHeader("Accept", "application/json");
                                    }
                                }))
                .setLogLevel(RestAdapter.LogLevel.FULL).setLog(new AndroidLog("-------------------->"))
                .build();
    }

    @Provides
    SyncNoteManager getSyncNoteManager() {
        return new PoiStorageSyncNoteManager();
    }

    @Provides
    PoiStorageClient getPoiStorageService(RestAdapter restAdapter) {
        return restAdapter.create(PoiStorageClient.class);
    }
}
