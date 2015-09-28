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
package io.mapsquare.osmcontributor;

import android.app.Application;

import com.google.gson.Gson;

import javax.inject.Singleton;

import dagger.Component;
import de.greenrobot.event.EventBus;
import io.mapsquare.osmcontributor.core.CoreModule;
import io.mapsquare.osmcontributor.core.PoiManager;
import io.mapsquare.osmcontributor.core.database.DatabaseHelper;
import io.mapsquare.osmcontributor.core.database.DatabaseModule;
import io.mapsquare.osmcontributor.core.database.OsmSqliteOpenHelper;
import io.mapsquare.osmcontributor.edition.AddValueDialogFragment;
import io.mapsquare.osmcontributor.edition.EditPoiActivity;
import io.mapsquare.osmcontributor.edition.EditPoiFragment;
import io.mapsquare.osmcontributor.edition.EditPoiManager;
import io.mapsquare.osmcontributor.edition.PickValueActivity;
import io.mapsquare.osmcontributor.login.LoginManager;
import io.mapsquare.osmcontributor.login.LoginModule;
import io.mapsquare.osmcontributor.login.SplashScreenActivity;
import io.mapsquare.osmcontributor.map.MapActivity;
import io.mapsquare.osmcontributor.map.MapFragment;
import io.mapsquare.osmcontributor.map.MapFragmentPresenter;
import io.mapsquare.osmcontributor.map.NoteDetailFragment;
import io.mapsquare.osmcontributor.map.PoiDetailFragment;
import io.mapsquare.osmcontributor.map.vectorial.EditVectorialWayManager;
import io.mapsquare.osmcontributor.map.vectorial.GeoJSONFileManager;
import io.mapsquare.osmcontributor.map.vectorial.Geocoder;
import io.mapsquare.osmcontributor.note.NoteActivity;
import io.mapsquare.osmcontributor.note.NoteCommentDialogFragment;
import io.mapsquare.osmcontributor.note.NoteManager;
import io.mapsquare.osmcontributor.preferences.MyPreferenceFragment;
import io.mapsquare.osmcontributor.preferences.MyPreferencesActivity;
import io.mapsquare.osmcontributor.sync.CommonSyncModule;
import io.mapsquare.osmcontributor.sync.SyncManager;
import io.mapsquare.osmcontributor.sync.SyncModule;
import io.mapsquare.osmcontributor.sync.assets.PoiAssetLoader;
import io.mapsquare.osmcontributor.sync.download.SyncDownloadService;
import io.mapsquare.osmcontributor.sync.upload.SyncUploadService;
import io.mapsquare.osmcontributor.type.EditPoiTagDialogFragment;
import io.mapsquare.osmcontributor.type.EditPoiTypeDialogFragment;
import io.mapsquare.osmcontributor.type.TypeListActivity;
import io.mapsquare.osmcontributor.type.TypeListActivityPresenter;
import io.mapsquare.osmcontributor.type.TypeManager;
import io.mapsquare.osmcontributor.upload.UploadActivity;

@Singleton
@Component(modules = {
        OsmTemplateModule.class,
        CoreModule.class,
        DatabaseModule.class,
        SyncModule.class,
        CommonSyncModule.class,
        LoginModule.class
})
public interface OsmTemplateComponent {
    // INJECTING

    void inject(Application osmTemplateApplication);

    // Activities
    void inject(SplashScreenActivity splashScreenActivity);

    void inject(EditPoiActivity editPoiActivity);

    void inject(MapActivity mapActivity);

    void inject(MyPreferencesActivity myPreferencesActivity);

    void inject(PickValueActivity pickValueActivity);

    void inject(NoteActivity noteActivity);

    void inject(UploadActivity uploadActivity);

    void inject(TypeListActivity typeListActivity);

    void inject(TypeListActivityPresenter typeListActivityPresenter);

    // Fragments
    void inject(MyPreferenceFragment myPreferenceFragment);

    void inject(EditPoiFragment editPoiFragment);

    void inject(AddValueDialogFragment addValueDialogFragment);

    void inject(MapFragment mapFragment);

    void inject(MapFragmentPresenter mapFragmentPresenter);

    void inject(PoiDetailFragment poiDetailFragment);

    void inject(NoteDetailFragment noteDetailFragment);

    void inject(NoteCommentDialogFragment noteCommentDialogFragment);

    void inject(EditPoiTypeDialogFragment editPoiTypeDialogFragment);

    void inject(EditPoiTagDialogFragment editPoiTagDialogFragment);

    // Services

    void inject(SyncDownloadService syncDownloadService);

    void inject(SyncUploadService syncUploadService);


    // PROVIDING

    // Core
    EventBus getEventBus();

    GeoJSONFileManager getGeoJSONFileManager();

    EditVectorialWayManager getEditVectorialWayManager();

    Geocoder getGeocoder();

    // Database

    DatabaseHelper getDatabaseHelper();

    OsmSqliteOpenHelper getDatabaseOpenHelper();

    PoiAssetLoader getPoiAssetLoader();

    PoiManager getPoiManager();

    NoteManager getNoteManager();

    // Login

    LoginManager getLoginManager();

    // Sync

    Gson getGson();

    EditPoiManager getEditPoiManager();

    SyncManager getSyncManager();

    // Poi type

    TypeManager getTypeManager();
}
