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

import org.simpleframework.xml.core.Persister;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import org.greenrobot.eventbus.EventBus;
import io.mapsquare.osmcontributor.core.ConfigManager;
import io.mapsquare.osmcontributor.core.PoiManager;
import io.mapsquare.osmcontributor.core.database.dao.PoiNodeRefDao;
import io.mapsquare.osmcontributor.sync.assets.PoiAssetLoader;
import io.mapsquare.osmcontributor.sync.converter.NoteConverter;
import io.mapsquare.osmcontributor.sync.converter.PoiConverter;
import io.mapsquare.osmcontributor.sync.rest.AuthenticationRequestInterceptor;
import io.mapsquare.osmcontributor.sync.rest.InterceptorChain;
import io.mapsquare.osmcontributor.sync.rest.OsmRestClient;
import io.mapsquare.osmcontributor.sync.rest.OverpassRestClient;
import io.mapsquare.osmcontributor.sync.rest.XMLConverter;
import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import retrofit.android.AndroidLog;
import retrofit.client.OkClient;

@Module
@Singleton
public class SyncModule {


    @Provides
    Backend getBackend(EventBus bus, OSMProxy osmProxy, OverpassRestClient overpassRestClient, OsmRestClient osmRestClient, PoiConverter poiConverter, PoiManager poiManager, PoiAssetLoader poiAssetLoader) {
        return new OsmBackend(bus, osmProxy, overpassRestClient, osmRestClient, poiConverter, poiManager, poiAssetLoader);
    }

    @Provides
    RestAdapter getRestAdapter(Persister persister, AuthenticationRequestInterceptor authenticationRequestInterceptor, OkHttpClient okHttpClient, ConfigManager configManager) {
        return new RestAdapter.Builder()
                .setEndpoint(configManager.getBasePoiApiUrl())
                .setConverter(getXMLConverterWithDateTime(persister))
                .setClient(new OkClient(okHttpClient))
                .setLogLevel(RestAdapter.LogLevel.FULL).setLog(new AndroidLog("-------------------->"))
                .setRequestInterceptor(
                        new InterceptorChain(authenticationRequestInterceptor,
                                new RequestInterceptor() {
                                    @Override
                                    public void intercept(RequestFacade request) {
                                        request.addHeader("Accept", "text/xml");
                                    }
                                }))
                .build();
    }

    @Provides
    SyncWayManager getSyncWayManager(OSMProxy osmProxy, OverpassRestClient overpassRestClient, PoiConverter poiConverter, PoiManager poiManager, EventBus bus, PoiNodeRefDao poiNodeRefDao, OsmRestClient osmRestClient) {
        return new OSMSyncWayManager(osmProxy, overpassRestClient, poiConverter, poiManager, bus, poiNodeRefDao, osmRestClient);
    }

    @Provides
    SyncNoteManager getSyncNoteManager(OSMProxy osmProxy, OsmRestClient osmRestClient, EventBus bus, NoteConverter noteConverter) {
        return new OSMSyncNoteManager(osmProxy, osmRestClient, bus, noteConverter);
    }

    @Provides
    OsmRestClient getOsmService(RestAdapter restAdapter) {
        return restAdapter.create(OsmRestClient.class);
    }

    @Provides
    OverpassRestClient getOverpassRestClient(Persister persister, ConfigManager configManager) {
        return new RestAdapter.Builder()
                .setEndpoint(configManager.getBaseOverpassApiUrl())
                .setConverter(getXMLConverterWithDateTime(persister))
                .setLogLevel(RestAdapter.LogLevel.HEADERS).setLog(new AndroidLog("-------------------->"))
                .build()
                .create(OverpassRestClient.class);
    }

    private XMLConverter getXMLConverterWithDateTime(Persister persister) {
        return new XMLConverter(persister);
    }
}
