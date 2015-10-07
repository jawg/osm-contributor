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

import android.app.IntentService;
import android.content.Intent;

import javax.inject.Inject;

import io.mapsquare.osmcontributor.BuildConfig;
import de.greenrobot.event.EventBus;
import io.mapsquare.osmcontributor.OsmTemplateApplication;
import io.mapsquare.osmcontributor.core.ConfigManager;
import io.mapsquare.osmcontributor.core.events.NotesLoadedEvent;
import io.mapsquare.osmcontributor.note.NoteManager;
import io.mapsquare.osmcontributor.sync.SyncManager;
import io.mapsquare.osmcontributor.sync.SyncNoteManager;
import io.mapsquare.osmcontributor.utils.FlavorUtils;
import timber.log.Timber;
import io.mapsquare.osmcontributor.utils.Box;

/**
 * {@link android.app.IntentService} synchronising parts of the database and the backend whenever an intent is received.
 */
public class SyncDownloadService extends IntentService {

    @Inject
    SyncManager syncManager;

    @Inject
    SyncNoteManager syncNoteManager;

    @Inject
    ConfigManager configManager;

    @Inject
    EventBus bus;

    @Inject
    NoteManager noteManager;

    @Override
    public void onCreate() {
        super.onCreate();
        ((OsmTemplateApplication) getApplication()).getOsmTemplateComponent().inject(this);
    }

    public SyncDownloadService() {
        super(SyncDownloadService.class.getSimpleName());
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        //time to time look for all the changes newer than the last sync
        Timber.d("synchronizing!");
        switch (BuildConfig.FLAVOR) {
            case FlavorUtils.POI_STORAGE:
                syncManager.syncDownloadPoiTypes();
                break;
            case FlavorUtils.TEMPLATE:
                Box box = configManager.getPreloadedBox();
                syncManager.syncDownloadPoiBox(box);
                syncNoteManager.syncDownloadNotesInBox(box);
                bus.post(new NotesLoadedEvent(box, noteManager.queryForAllInRect(box)));
                break;
        }
    }
}
