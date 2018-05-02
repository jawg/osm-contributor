/**
 * Copyright (C) 2016 eBusiness Information
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
package io.jawg.osmcontributor.utils.core;


import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import org.greenrobot.eventbus.EventBus;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import io.jawg.osmcontributor.ui.managers.executor.PostExecutionThread;
import io.jawg.osmcontributor.ui.managers.executor.UIThread;
import io.jawg.osmcontributor.utils.ConfigManager;

@Module
@Singleton
public class CoreModule {

    @Provides
    EventBus getEventBus() {
        return EventBus.getDefault();
    }

    @Provides
    SharedPreferences getSharedPrefs(Application osmTemplateApplication) {
        return PreferenceManager.getDefaultSharedPreferences(osmTemplateApplication);
    }

    @Provides
    ConfigManager getConfigManager() {
        return new StoreConfigManager();
    }

    @Provides
    @Singleton
    PostExecutionThread providePostExecutionThread(UIThread uiThread) {
        return uiThread;
    }
}
