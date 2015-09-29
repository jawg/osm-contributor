/**
 * Copyright (C) 2015 eBusiness Information
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
package io.mapsquare.osmcontributor.map.vectorial;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Singleton;

import de.greenrobot.event.EventBus;
import io.mapsquare.osmcontributor.map.events.AddressFoundEvent;
import io.mapsquare.osmcontributor.map.events.PleaseFindAddressEvent;
import io.mapsquare.osmcontributor.utils.EventCountDownTimer;
import timber.log.Timber;

@Singleton
public class Geocoder {

    EventBus eventBus;

    public static final String URL = "http://nominatim.openstreetmap.org/reverse?format=json&lat=%s&lon=%s&zoom=18&addressdetails=1";

    private OkHttpClient client = new OkHttpClient();

    private EventCountDownTimer timer;

    @Inject
    public Geocoder(EventBus eventBus) {
        this.eventBus = eventBus;
        this.timer = new EventCountDownTimer(2000, 2000, eventBus);
    }

    public void delayedReverseGeocoding(double lat, double lng) {
        timer.cancel();
        timer.setEvent(new PleaseFindAddressEvent(lat, lng));
        timer.start();
    }

    public void onEventBackgroundThread(PleaseFindAddressEvent event) {
        String address = reverseGeocoding(event.getLat(), event.getLng());
        if (!address.isEmpty()) {
            eventBus.post(new AddressFoundEvent(address));
        }
    }

    public String reverseGeocoding(double lat, double lng) {
        String url = String.format(URL, lat, lng);

        Request request = new Request.Builder()
                .url(url)
                .build();

        try {
            Response response = client.newCall(request).execute();
            String responseBody = response.body().string();

            JSONObject reader = new JSONObject(responseBody);
            return reader.optString("display_name");
        } catch (IOException | JSONException e) {
            Timber.e(e, "Failed to parse response");
        }
        return "";
    }
}
