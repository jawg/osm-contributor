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

import javax.inject.Inject;

import io.mapsquare.osmcontributor.OsmTemplateApplication;
import io.mapsquare.osmcontributor.sync.SyncManager;
import io.mapsquare.osmcontributor.utils.StringUtils;

/**
 * Service that upload the {@link io.mapsquare.osmcontributor.core.model.Poi} to the backend.
 */
public class SyncUploadService extends IntentService {

    public static final String CHANGESET_COMMENT = "CHANGESET_COMMENT";

    @Inject
    SyncManager syncManager;

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
        String changesetComment = intent.getStringExtra(CHANGESET_COMMENT);

        if (!StringUtils.isEmpty(changesetComment)) {
            syncManager.remoteAddOrUpdateOrDeletePois(changesetComment);
        }
    }

    /**
     * Start the SyncUploadService with a comment for the changeset.
     *
     * @param context          The context that start the service.
     * @param changesetComment The comment of the changeset that will contain all the Pois modifications to send.
     */
    public static void startService(Context context, String changesetComment) {
        Intent syncIntent = new Intent(context, SyncUploadService.class);
        syncIntent.putExtra(CHANGESET_COMMENT, changesetComment);
        context.startService(syncIntent);
    }
}
