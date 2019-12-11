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
package io.jawg.osmcontributor.flickr.util;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

import javax.net.ssl.SSLSocketFactory;

import io.jawg.osmcontributor.flickr.oauth.NoSSLv3SocketFactory;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class FlickrUploadUtils {

    public static Retrofit adapter;

    public static Retrofit getRestAdapter(final Map<String, String> oAuthParams) {
        if (adapter == null) {
            try {
                SSLSocketFactory NoSSLv3Factory = new NoSSLv3SocketFactory(new URL("https://up.flickr.com/services"));
                adapter = new Retrofit.Builder()
                        .addConverterFactory(ScalarsConverterFactory.create())
                        .baseUrl("https://up.flickr.com/services/")
                        .client(new okhttp3.OkHttpClient().newBuilder().sslSocketFactory(NoSSLv3Factory).addInterceptor(new Interceptor() {
                            @Override
                            public Response intercept(Chain chain) throws IOException {
                                Request request = chain.request();
                                Request newRequest = request.newBuilder()
                                        .addHeader("Authorization", FlickrSecurityUtils.getAuthorizationHeader(oAuthParams))
                                        .build();
                                return chain.proceed(newRequest);
                            }
                        }).build())
                        .build();
            } catch (MalformedURLException e) {

            } catch (IOException e) {

            }
        }
        return adapter;
    }
}