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
package io.mapsquare.osmcontributor.sync.assets;

import android.app.Application;

import com.google.gson.Gson;

import org.simpleframework.xml.core.Persister;

import java.io.InputStreamReader;
import java.io.Reader;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import de.greenrobot.event.EventBus;
import io.mapsquare.osmcontributor.core.PoiManager;
import io.mapsquare.osmcontributor.core.model.Poi;
import io.mapsquare.osmcontributor.core.model.PoiType;
import io.mapsquare.osmcontributor.map.BitmapHandler;
import io.mapsquare.osmcontributor.sync.assets.events.DbInitializedEvent;
import io.mapsquare.osmcontributor.sync.assets.events.InitDbEvent;
import io.mapsquare.osmcontributor.sync.converter.PoiConverter;
import io.mapsquare.osmcontributor.sync.converter.PoiTypeConverter;
import io.mapsquare.osmcontributor.sync.dto.dma.PoiTypesDto;
import io.mapsquare.osmcontributor.sync.dto.osm.OsmDto;
import io.mapsquare.osmcontributor.utils.FlavorUtils;
import timber.log.Timber;

@Singleton
public class PoiAssetLoader {

    Application application;
    PoiConverter poiConverter;
    Gson gson;
    Persister simple;
    PoiTypeConverter poiTypeConverter;
    PoiManager poiManager;
    EventBus bus;
    BitmapHandler bitmapHandler;

    @Inject
    public PoiAssetLoader(Application application, PoiConverter poiConverter, Gson gson, Persister simple, PoiTypeConverter poiTypeConverter, PoiManager poiManager, BitmapHandler bitmapHandler, EventBus bus) {
        this.application = application;
        this.poiConverter = poiConverter;
        this.gson = gson;
        this.simple = simple;
        this.poiTypeConverter = poiTypeConverter;
        this.poiManager = poiManager;
        this.bus = bus;
        this.bitmapHandler = bitmapHandler;
    }

    public void onEventBackgroundThread(InitDbEvent event) {
        if (!FlavorUtils.isPoiStorage()) {
            Timber.d("Initializing database ...");
            initDb();
            bus.postSticky(new DbInitializedEvent());
        }
    }

    /**
     * Initialize the database with the data from the assets files.
     */
    public void initDb() {
        if (!poiManager.isDbInitialized()) {
            // No data, initializing from assets
            List<PoiType> poiTypes = loadPoiTypesFromAssets();
            if (poiTypes != null) {
                Timber.d("Loaded %s poiTypes, trying to insert them", poiTypes.size());
                for (PoiType poiType : poiTypes) {
                    Timber.d("saving poiType %s", poiType);
                    poiManager.savePoiType(poiType);
                    Timber.d("poiType saved");
                }
            }

            List<Poi> pois = loadPoisFromAssets();
            Timber.d("Loaded %s poi, trying to insert them", pois.size());
            for (Poi poi : pois) {
                Timber.d("saving poi %s", poi);
                poiManager.savePoi(poi);
                Timber.d("poi saved");
            }
        }
        Timber.d("Database initialized");
    }

    /**
     * Load the PoiTypes from the poitypes.json file located in the assets directory.
     *
     * @return The loaded PoiTypes.
     */
    public List<PoiType> loadPoiTypesFromAssets() {
        Reader reader = null;
        try {
            reader = new InputStreamReader(application.getAssets().open("poitypes.json"));
            PoiTypesDto poiTypesDto = gson.fromJson(reader, PoiTypesDto.class);
            return poiTypeConverter.convert(poiTypesDto.getTypes());
        } catch (Exception e) {
            Timber.e(e, "Error while loading POI Types from assets");
            throw new RuntimeException(e);
        } finally {
            // TODO get a close quietly method
            if (reader != null) {
                try {
                    reader.close();
                } catch (Exception ignored) {
                }
            }
        }
    }

    /**
     * Load the POIs from the pois.osm file located in the assets directory.
     *
     * @return the loaded POIs.
     */
    public List<Poi> loadPoisFromAssets() {
        try {
            OsmDto read = simple.read(OsmDto.class, application.getAssets().open("pois.osm"));
            List<Poi> pois = poiConverter.convertDtosToPois(read.getNodeDtoList());
            pois.addAll(poiConverter.convertDtosToPois(read.getWayDtoList()));
            return pois;
        } catch (Exception e) {
            Timber.e(e, "Error while loading POIS from assets");
            throw new RuntimeException(e);
        }
    }
}
