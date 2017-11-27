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
package io.jawg.osmcontributor.modules;


import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import io.jawg.osmcontributor.rest.clients.OsmTagInfoRestClient;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;

@Module
@Singleton
public class TypeModule {

    @Provides
    OsmTagInfoRestClient getOsmTaginfoRestClient(OkHttpClient okHttpClient) {
        return new Retrofit.Builder()
                .baseUrl("http://taginfo.openstreetmap.org/api/4/")
                .client(okHttpClient)
                .build()
                .create(OsmTagInfoRestClient.class);
    }
}
