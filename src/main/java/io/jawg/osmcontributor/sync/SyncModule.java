/**
 * Copyright (C) 2016 eBusiness Information
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
package io.jawg.osmcontributor.sync;

import org.greenrobot.eventbus.EventBus;
import org.simpleframework.xml.core.Persister;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import io.jawg.osmcontributor.database.PoiAssetLoader;
import io.jawg.osmcontributor.database.dao.PoiNodeRefDao;
import io.jawg.osmcontributor.database.preferences.LoginPreferences;
import io.jawg.osmcontributor.rest.Backend;
import io.jawg.osmcontributor.rest.OSMProxy;
import io.jawg.osmcontributor.rest.OsmBackend;
import io.jawg.osmcontributor.rest.clients.OsmRestClient;
import io.jawg.osmcontributor.rest.clients.OverpassRestClient;
import io.jawg.osmcontributor.rest.managers.OSMSyncNoteManager;
import io.jawg.osmcontributor.rest.managers.OSMSyncWayManager;
import io.jawg.osmcontributor.rest.managers.SyncNoteManager;
import io.jawg.osmcontributor.rest.managers.SyncWayManager;
import io.jawg.osmcontributor.rest.mappers.NoteMapper;
import io.jawg.osmcontributor.rest.mappers.PoiMapper;
import io.jawg.osmcontributor.ui.activities.AuthorisationInterceptor;
import io.jawg.osmcontributor.ui.managers.PoiManager;
import io.jawg.osmcontributor.utils.ConfigManager;
import retrofit2.Retrofit;
import retrofit2.converter.simplexml.SimpleXmlConverterFactory;


@Module
@Singleton
public class SyncModule {

    @Provides
    Backend getBackend(LoginPreferences loginPreferences, EventBus bus, OSMProxy osmProxy, OverpassRestClient overpassRestClient, OsmRestClient osmRestClient, PoiMapper poiMapper, PoiManager poiManager, PoiAssetLoader poiAssetLoader) {
        return new OsmBackend(loginPreferences, bus, osmProxy, overpassRestClient, osmRestClient, poiMapper, poiManager, poiAssetLoader);
    }

    @Singleton
    @Provides
    Retrofit getRestAdapter(Persister persister, okhttp3.OkHttpClient okHttpClient, ConfigManager configManager) {
        return new Retrofit.Builder()
                .baseUrl(configManager.getBasePoiApiUrl())
                .client(okHttpClient)
                .addConverterFactory(SimpleXmlConverterFactory.create(persister))
                .build();
    }

    @Provides
    SyncWayManager getSyncWayManager(OSMProxy osmProxy, OverpassRestClient overpassRestClient, PoiMapper poiMapper, PoiManager poiManager, EventBus bus, PoiNodeRefDao poiNodeRefDao, OsmRestClient osmRestClient) {
        return new OSMSyncWayManager(osmProxy, overpassRestClient, poiMapper, poiManager, bus, poiNodeRefDao, osmRestClient);
    }

    @Provides
    SyncNoteManager getSyncNoteManager(OSMProxy osmProxy, OsmRestClient osmRestClient, EventBus bus, NoteMapper noteMapper) {
        return new OSMSyncNoteManager(osmProxy, osmRestClient, bus, noteMapper);
    }

    @Provides
    OsmRestClient getOsmService(Retrofit restAdapter) {
        return restAdapter.create(OsmRestClient.class);
    }

    @Provides
    OverpassRestClient getOverpassRestClient(Persister persister, AuthorisationInterceptor interceptor, ConfigManager configManager) {
        return new Retrofit.Builder()
                .baseUrl(configManager.getBaseOverpassApiUrl())
                .addConverterFactory(SimpleXmlConverterFactory.create(persister))
                .build()
                .create(OverpassRestClient.class);
    }
}
