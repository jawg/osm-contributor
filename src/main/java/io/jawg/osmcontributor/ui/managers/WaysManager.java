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
package io.jawg.osmcontributor.ui.managers;

import android.app.Application;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.inject.Inject;

import io.jawg.osmcontributor.database.dao.PoiNodeRefDao;
import io.jawg.osmcontributor.model.entities.Way;
import io.jawg.osmcontributor.model.mappers.WayMapper;
import io.jawg.osmcontributor.model.entities.Poi;
import io.jawg.osmcontributor.model.entities.PoiNodeRef;
import io.jawg.osmcontributor.ui.events.map.EditionWaysLoadedEvent;
import io.jawg.osmcontributor.ui.events.map.PleaseLoadEditWaysEvent;
import io.jawg.osmcontributor.ui.events.map.PleaseSelectNodeRefByID;
import timber.log.Timber;

public class WaysManager {
    private Set<Way> waysEdition = new HashSet<>();
    private Application application;
    private PoiManager poiManager;
    private PoiNodeRefDao poiNodeRefDao;
    private EventBus bus;

    private float scaledDensity;

    @Inject
    public WaysManager(Application application, PoiManager poiManager, PoiNodeRefDao poiNodeRefDao, EventBus bus) {
        this.application = application;
        this.poiManager = poiManager;
        this.poiNodeRefDao = poiNodeRefDao;
        this.bus = bus;
        scaledDensity = application.getResources().getDisplayMetrics().scaledDensity;
        if (scaledDensity < 3) {
            scaledDensity = 3;
        }
    }

    public void buildWaysEdition(List<Poi> pois, boolean isRefreshFromOverpass) {
        Timber.d("Showing nodesRefs : " + pois.size());
        waysEdition.clear();
        waysEdition = WayMapper.poisToWays(pois);

        TreeSet<Double> levels = new TreeSet<>();
        levels.add(0d);

        bus.post(new EditionWaysLoadedEvent(waysEdition, levels, isRefreshFromOverpass));
    }

    @Subscribe(threadMode = ThreadMode.ASYNC)
    public void onPleaseLoadEditWaysEvent(PleaseLoadEditWaysEvent event) {
        List<Poi> poisFromDB = poiManager.queryForAllWays();
        buildWaysEdition(poisFromDB, event.isRefreshFromOverpass());
    }

    @Subscribe(threadMode = ThreadMode.ASYNC)
    public void onPleaseSelectNodeRefByID(PleaseSelectNodeRefByID event) {
        List<PoiNodeRef> poisFromDB = new ArrayList<>();
        poisFromDB.add(poiNodeRefDao.queryForId(event.getNodeRefSelectedId()));
    }
}