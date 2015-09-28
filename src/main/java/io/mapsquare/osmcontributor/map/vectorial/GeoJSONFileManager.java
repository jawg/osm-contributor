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
package io.mapsquare.osmcontributor.map.vectorial;

import android.app.Application;

import com.cocoahero.android.geojson.FeatureCollection;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mapbox.mapboxsdk.geometry.BoundingBox;
import com.mapbox.mapboxsdk.util.DataLoadingUtils;

import org.json.JSONException;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.inject.Inject;
import javax.inject.Singleton;

import de.greenrobot.event.EventBus;
import io.mapsquare.osmcontributor.map.events.GeoJSONInitializedEvent;
import io.mapsquare.osmcontributor.map.events.InitGeoJSONEvent;
import io.mapsquare.osmcontributor.map.events.PleaseLoadVectorialTileEvent;
import io.mapsquare.osmcontributor.map.events.VectorialTilesLoadedEvent;
import timber.log.Timber;


@Singleton
public class GeoJSONFileManager {

    private List<GeoJSONFileDescriptor> fileDescriptors = new ArrayList<>();

    Application application;
    EventBus eventBus;

    @Inject
    public GeoJSONFileManager(Application application, EventBus eventBus) {
        this.application = application;
        this.eventBus = eventBus;
    }

    public void onEventBackgroundThread(InitGeoJSONEvent event) {
        try {
            init();
        } finally {
            eventBus.postSticky(new GeoJSONInitializedEvent());
        }
    }


    public void init() {
        Reader reader = null;
        try {
            Timber.d("GeoJSONFileManager init");
            if (Arrays.asList(application.getAssets().list("")).contains(VECTORIAL_FILES)) {
                Gson gson = new Gson();
                reader = new InputStreamReader(application.getAssets().open(VECTORIAL_FILES));
                fileDescriptors = gson.fromJson(reader, (new TypeToken<List<GeoJSONFileDescriptor>>() {
                }).getType());
            }
        } catch (IOException e) {
            Timber.e(e, "Failed to initialize GeoJSONFilesManager");
            throw new RuntimeException(e);
        }
    }

    public GeoJSONFileContent getVectorialObjects(double north, double east, double south, double west) {
        Set<VectorialObject> objects = new HashSet<>();
        TreeSet<Double> levels = new TreeSet<>();
        GeoJSONFileContent parseResult;
        FeatureCollection parsed;
        for (GeoJSONFileDescriptor descriptor : fileDescriptors) {
            if (descriptor.inBounds(north, east, south, west)) {
                if (!descriptor.isParsed()) {
                    try {
                        parsed = DataLoadingUtils.loadGeoJSONFromAssets(application, descriptor.getName());
                        parseResult = GeoJSONParser.parseGeoJson(parsed);
                        descriptor.setVectorialObjects(parseResult.getVectorialObjects());
                        descriptor.setLevels(parseResult.getLevels());
                        descriptor.setParsed(true);
                    } catch (IOException e) {
                        Timber.e(e, "Error while opening and reading %s", descriptor.getName());
                    } catch (JSONException e) {
                        Timber.e(e, "Error while parsing %s", descriptor.getName());
                    }
                }
                objects.addAll(descriptor.getVectorialObjects());
                levels.addAll(descriptor.getLevels());
            }
        }
        return new GeoJSONFileContent(objects, levels);
    }

    public GeoJSONFileContent getVectorialObjects(BoundingBox boundingBox) {
        return getVectorialObjects(boundingBox.getLatNorth(), boundingBox.getLonEast(), boundingBox.getLatSouth(), boundingBox.getLonWest());
    }

    public void onEventAsync(PleaseLoadVectorialTileEvent event) {
        GeoJSONFileContent response = getVectorialObjects(event.getBoundingBox());
        Timber.d("nb of vectorialObject : " + response.getVectorialObjects().size());
        eventBus.post(new VectorialTilesLoadedEvent(response.getVectorialObjects(), response.getLevels()));
    }
}
