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
package io.mapsquare.osmcontributor.modules;

import android.app.Application;

import com.google.gson.Gson;

import org.greenrobot.eventbus.EventBus;

import javax.inject.Singleton;

import dagger.Component;
import io.mapsquare.osmcontributor.database.PoiAssetLoader;
import io.mapsquare.osmcontributor.database.helper.DatabaseHelper;
import io.mapsquare.osmcontributor.database.helper.DatabaseModule;
import io.mapsquare.osmcontributor.database.helper.OsmSqliteOpenHelper;
import io.mapsquare.osmcontributor.flickr.oauth.FlickrOAuth;
import io.mapsquare.osmcontributor.login.LoginModule;
import io.mapsquare.osmcontributor.rest.CommonSyncModule;
import io.mapsquare.osmcontributor.rest.security.GoogleOAuthManager;
import io.mapsquare.osmcontributor.rest.managers.H2GeoPresetsManager;
import io.mapsquare.osmcontributor.rest.managers.SyncManager;
import io.mapsquare.osmcontributor.service.CancelButtonReceiver;
import io.mapsquare.osmcontributor.service.OfflineRegionDownloadService;
import io.mapsquare.osmcontributor.service.OfflineRegionModule;
import io.mapsquare.osmcontributor.sync.SyncModule;
import io.mapsquare.osmcontributor.ui.activities.EditPoiActivity;
import io.mapsquare.osmcontributor.ui.activities.LoadProfileActivity;
import io.mapsquare.osmcontributor.ui.activities.MapActivity;
import io.mapsquare.osmcontributor.ui.activities.MyPreferencesActivity;
import io.mapsquare.osmcontributor.ui.activities.NoteActivity;
import io.mapsquare.osmcontributor.ui.activities.OfflineRegionsActivity;
import io.mapsquare.osmcontributor.ui.activities.PhotoActivity;
import io.mapsquare.osmcontributor.ui.activities.PickValueActivity;
import io.mapsquare.osmcontributor.ui.activities.SplashScreenActivity;
import io.mapsquare.osmcontributor.ui.activities.TypeListActivity;
import io.mapsquare.osmcontributor.ui.activities.UploadActivity;
import io.mapsquare.osmcontributor.ui.adapters.OpeningHoursLinearLayoutAdapter;
import io.mapsquare.osmcontributor.ui.adapters.OpeningMonthAdapter;
import io.mapsquare.osmcontributor.ui.adapters.ProfileAdapter;
import io.mapsquare.osmcontributor.ui.adapters.TagsAdapter;
import io.mapsquare.osmcontributor.ui.adapters.parser.OpeningMonthValueParser;
import io.mapsquare.osmcontributor.ui.adapters.parser.OpeningTimeValueParser;
import io.mapsquare.osmcontributor.ui.dialogs.AddValueDialogFragment;
import io.mapsquare.osmcontributor.ui.dialogs.EditPoiTagDialogFragment;
import io.mapsquare.osmcontributor.ui.dialogs.EditPoiTypeDialogFragment;
import io.mapsquare.osmcontributor.ui.dialogs.EditPoiTypeNameDialogFragment;
import io.mapsquare.osmcontributor.ui.dialogs.LoginDialogFragment;
import io.mapsquare.osmcontributor.ui.dialogs.NoteCommentDialogFragment;
import io.mapsquare.osmcontributor.ui.fragments.EditPoiFragment;
import io.mapsquare.osmcontributor.ui.fragments.MapFragment;
import io.mapsquare.osmcontributor.ui.fragments.MyPreferenceFragment;
import io.mapsquare.osmcontributor.ui.fragments.NoteDetailFragment;
import io.mapsquare.osmcontributor.ui.fragments.PoiDetailFragment;
import io.mapsquare.osmcontributor.ui.managers.EditPoiManager;
import io.mapsquare.osmcontributor.ui.managers.LoginManager;
import io.mapsquare.osmcontributor.ui.managers.NoteManager;
import io.mapsquare.osmcontributor.ui.managers.PoiManager;
import io.mapsquare.osmcontributor.ui.managers.TypeManager;
import io.mapsquare.osmcontributor.ui.managers.WaysManager;
import io.mapsquare.osmcontributor.ui.presenters.MapFragmentPresenter;
import io.mapsquare.osmcontributor.ui.presenters.TypeListActivityPresenter;
import io.mapsquare.osmcontributor.utils.core.ArpiInitializer;
import io.mapsquare.osmcontributor.utils.core.CoreModule;
import io.mapsquare.osmcontributor.utils.ways.Geocoder;

@Singleton
@Component(modules = {
        OsmTemplateModule.class,
        CoreModule.class,
        DatabaseModule.class,
        SyncModule.class,
        PresetsModule.class,
        CommonSyncModule.class,
        LoginModule.class,
        TypeModule.class,
        OfflineRegionModule.class
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

    void inject(LoadProfileActivity loadProfileActivity);

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

    void inject(EditPoiTypeNameDialogFragment editPoiTypeNameDialogFragment);

    void inject(EditPoiTagDialogFragment editPoiTagDialogFragment);

    void inject(OpeningMonthAdapter openingMonthAdapter);

    void inject(OpeningHoursLinearLayoutAdapter openingHoursLinearLayoutAdapter);

    void inject(TagsAdapter tagsAdapter);

    void inject(PhotoActivity photoActivity);

    void inject(FlickrOAuth flickrOAuth);

    void inject(LoginDialogFragment loginDialogFragment);

    void inject(ProfileAdapter profileAdapter);

    void inject(OfflineRegionDownloadService offlineRegionDownloadService);

    void inject(CancelButtonReceiver cancelButtonReceiver);

    void inject(OfflineRegionsActivity offlineRegionsActivity);

    // PROVIDING

    // Core
    EventBus getEventBus();

    WaysManager getEditVectorialWayManager();

    Geocoder getGeocoder();

    ArpiInitializer getArpiInitializer();

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

    // Presets

    H2GeoPresetsManager getPresetsManager();

    // Poi type
    TypeManager getTypeManager();

    OpeningTimeValueParser getOpeningTimeParser();

    OpeningMonthValueParser getOpeningMonthParser();

    GoogleOAuthManager getGoogleOAuthManager();
}
