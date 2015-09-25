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
package io.mapsquare.osmcontributor.sync.upload;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import javax.inject.Inject;

import io.mapsquare.osmcontributor.OsmTemplateApplication;
import io.mapsquare.osmcontributor.sync.SyncManager;
import io.mapsquare.osmcontributor.sync.SyncNoteManager;
import timber.log.Timber;

public class SyncUploadService extends IntentService {

    public static final String IS_MANUAL_SYNC = "IS_MANUAL_SYNC";
    public static final String COMMENT = "COMMENT";

    @Inject
    SyncManager syncManager;

    @Inject
    SyncNoteManager syncNoteManager;

    @Inject
    SharedPreferences sharedPreferences;

    @Override
    public void onCreate() {
        super.onCreate();
        ((OsmTemplateApplication) getApplication()).getOsmTemplateComponent().inject(this);
    }

    public SyncUploadService() {
        super(SyncUploadService.class.getSimpleName());
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        boolean isManual = sharedPreferences.getBoolean(IS_MANUAL_SYNC, false);

        String comment = intent.getStringExtra(COMMENT);

        if (!isManual) {
            Timber.d("uploading our POIs");
            syncManager.remoteAddOrUpdateOrDeletePois();
        } else if (comment != null && !comment.isEmpty()) {
            syncManager.remoteAddOrUpdateOrDeletePois(comment);
        }

        //note are sent without changesets or comments
        syncNoteManager.remoteAddComments();
    }

    public static void startService(Context context) {
        Intent syncIntent = new Intent(context, SyncUploadService.class);
        context.startService(syncIntent);
    }

    public static void startService(Context context, String comment) {
        Intent syncIntent = new Intent(context, SyncUploadService.class);
        syncIntent.putExtra(COMMENT, comment);
        context.startService(syncIntent);
    }
}
