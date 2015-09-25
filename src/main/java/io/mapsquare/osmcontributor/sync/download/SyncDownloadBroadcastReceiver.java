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
package io.mapsquare.osmcontributor.sync.download;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * {@link android.content.BroadcastReceiver} starting a {@link io.mapsquare.osmcontributor.sync.download.SyncDownloadService}
 * whenever a broadcast is received.
 */
public class SyncDownloadBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent syncIntent = new Intent(context, SyncDownloadService.class);
        context.startService(syncIntent);
    }
}
