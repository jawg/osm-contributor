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
package io.mapsquare.osmcontributor.database;

import android.app.Application;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;

import com.google.gson.Gson;

import org.simpleframework.xml.core.Persister;

import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.mapsquare.osmcontributor.R;
import io.mapsquare.osmcontributor.model.entities.Group;
import io.mapsquare.osmcontributor.model.entities.Poi;
import io.mapsquare.osmcontributor.model.entities.PoiType;
import io.mapsquare.osmcontributor.rest.dtos.dma.H2GeoDto;
import io.mapsquare.osmcontributor.rest.dtos.dma.PoiTypeDto;
import io.mapsquare.osmcontributor.rest.dtos.osm.OsmDto;
import io.mapsquare.osmcontributor.rest.mappers.PoiMapper;
import io.mapsquare.osmcontributor.rest.mappers.PoiTypeMapper;
import io.mapsquare.osmcontributor.utils.CloseableUtils;
import timber.log.Timber;

@Singleton
public class PoiAssetLoader {

    Application application;
    PoiMapper poiMapper;
    Gson gson;
    Persister simple;
    PoiTypeMapper poiTypeMapper;
    SharedPreferences sharedPreferences;

    @Inject
    public PoiAssetLoader(Application application, PoiMapper poiMapper, Gson gson, Persister simple, PoiTypeMapper poiTypeMapper, SharedPreferences sharedPreferences) {
        this.application = application;
        this.poiMapper = poiMapper;
        this.gson = gson;
        this.simple = simple;
        this.poiTypeMapper = poiTypeMapper;
        this.sharedPreferences = sharedPreferences;
    }

    /**
     * Load the PoiTypes from the h2geo.json file located in the assets directory.
     * @return The loaded PoiTypes.
     */
    public List<PoiType> loadPoiTypesByDefault() {
        InputStreamReader reader = null;
        try {
            reader = new InputStreamReader(application.getAssets().open("h2geo.json"));
            return loadPoiTypesFromStream(reader);
        } catch (Exception e) {
            Timber.e(e, "Error while loading POI Types from assets");
            throw new RuntimeException(e);
        } finally {
            CloseableUtils.closeQuietly(reader);
        }
    }

    /**
     * Load the PoiTypes from the poitypes.json file located in the assets directory.
     * @param inputStreamReader, on the json to load or nu for default.
     * @return The loaded PoiTypes.
     */
    public List<PoiType> loadPoiTypesFromStream(InputStreamReader inputStreamReader) {
        try {
            // Save the h2Geo version and generation date in the shared preferences.
            H2GeoDto h2GeoDto = gson.fromJson(inputStreamReader, H2GeoDto.class);
            sharedPreferences.edit()
                    .putString(application.getString(R.string.shared_prefs_h2geo_version), h2GeoDto.getVersion())
                    .putString(application.getString(R.string.shared_prefs_h2geo_date), h2GeoDto.getLastUpdate())
                    .apply();
            return loadPoiTypesFromH2GeoDto(h2GeoDto);
        } catch (Exception e) {
            Timber.e(e, "Error while loading POI Types from assets");
            throw new RuntimeException(e);
        } finally {
            CloseableUtils.closeQuietly(inputStreamReader);
        }
    }

    /**
     * Load the PoiTypes from the h2GeoDtoObject.
     * @param h2GeoDto
     * @return The loaded PoiTypes.
     */
    public List<PoiType> loadPoiTypesFromH2GeoDto(@NonNull H2GeoDto h2GeoDto) {
        List<PoiType> types = new ArrayList<>();
        for (Group<PoiTypeDto> group : h2GeoDto.getGroups()) {
            types.addAll(poiTypeMapper.convert(group.getItems()));
        }
        return types;
    }

    /**
     * Load the POIs from the pois.osm file located in the assets directory.
     *
     * @return the loaded POIs.
     */
    public List<Poi> loadPoisFromAssets() {
        try {
            OsmDto read = simple.read(OsmDto.class, application.getAssets().open("pois.osm"));
            List<Poi> pois = poiMapper.convertDtosToPois(read.getNodeDtoList());
            pois.addAll(poiMapper.convertDtosToPois(read.getWayDtoList()));
            return pois;
        } catch (Exception e) {
            Timber.e(e, "Error while loading POIS from assets");
            throw new RuntimeException(e);
        }
    }
}
