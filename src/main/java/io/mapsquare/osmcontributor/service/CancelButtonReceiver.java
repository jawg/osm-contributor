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
package io.mapsquare.osmcontributor.service;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.mapbox.mapboxsdk.offline.OfflineManager;

import org.greenrobot.eventbus.EventBus;

import javax.inject.Inject;

import io.mapsquare.osmcontributor.OsmTemplateApplication;
import io.mapsquare.osmcontributor.service.event.CancelOfflineAreaEvent;

/**
 * @author Tommy Buonomo on 08/08/16.
 */
public class CancelButtonReceiver extends BroadcastReceiver {
    static final String MAP_TAG_PARAM = "MAP_TAG";

    @Inject
    OfflineManager offlineManager;

    @Inject
    NotificationManager notificationManager;

    @Inject
    EventBus eventBus;

    @Override
    public void onReceive(Context context, Intent intent) {
        ((OsmTemplateApplication) context.getApplicationContext()).getOsmTemplateComponent().inject(this);
        String mapTag = intent.getStringExtra(MAP_TAG_PARAM);
        eventBus.post(new CancelOfflineAreaEvent(mapTag));
        notificationManager.cancel(mapTag.hashCode());
    }
}
