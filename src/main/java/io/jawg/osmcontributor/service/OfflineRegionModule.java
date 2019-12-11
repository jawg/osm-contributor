/**
 * Copyright (C) 2019 Takima
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
package io.jawg.osmcontributor.service;

import android.app.Activity;
import android.app.Application;
import android.app.NotificationManager;

import com.mapbox.mapboxsdk.offline.OfflineManager;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import io.jawg.osmcontributor.offline.OfflineRegionManager;

/**
 * @author Tommy Buonomo on 08/08/16.
 */

@Module
@Singleton
public class OfflineRegionModule {

    @Provides
    public NotificationManager providesNotificationManager(Application application) {
        return (NotificationManager) application.getSystemService(Activity.NOTIFICATION_SERVICE);
    }

    @Provides
    public OfflineManager provideOfflineManager(Application application) {
        return OfflineManager.getInstance(application);
    }

    @Provides
    public OfflineRegionManager provideOfflineRegionManager(OfflineManager offlineManager) {
        return new OfflineRegionManager(offlineManager);
    }
}
