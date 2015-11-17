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
import com.google.gson.reflect.TypeToken;

import org.simpleframework.xml.core.Persister;

import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.mapsquare.osmcontributor.core.model.Poi;
import io.mapsquare.osmcontributor.core.model.PoiType;
import io.mapsquare.osmcontributor.sync.converter.PoiConverter;
import io.mapsquare.osmcontributor.sync.converter.PoiTypeConverter;
import io.mapsquare.osmcontributor.sync.dto.dma.PoiTypeDto;
import io.mapsquare.osmcontributor.sync.dto.osm.OsmDto;
import io.mapsquare.osmcontributor.utils.CloseableUtils;
import timber.log.Timber;

@Singleton
public class PoiAssetLoader {

    Application application;
    PoiConverter poiConverter;
    Gson gson;
    Persister simple;
    PoiTypeConverter poiTypeConverter;

    @Inject
    public PoiAssetLoader(Application application, PoiConverter poiConverter, Gson gson, Persister simple, PoiTypeConverter poiTypeConverter) {
        this.application = application;
        this.poiConverter = poiConverter;
        this.gson = gson;
        this.simple = simple;
        this.poiTypeConverter = poiTypeConverter;
    }

    /**
     * Load the PoiTypes from the poitypes.json file located in the assets directory.
     *
     * @return The loaded PoiTypes.
     */
    public List<PoiType> loadPoiTypesFromAssets() {
        Reader reader = null;
        try {
            reader = new InputStreamReader(application.getAssets().open("h2geo.json"));
            Type poiTypesType = new TypeToken<ArrayList<PoiTypeDto>>() { } .getType();
            List<PoiTypeDto> poiTypesDto = gson.fromJson(reader, poiTypesType);
            return poiTypeConverter.convert(poiTypesDto);
        } catch (Exception e) {
            Timber.e(e, "Error while loading POI Types from assets");
            throw new RuntimeException(e);
        } finally {
            CloseableUtils.closeQuietly(reader);
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
