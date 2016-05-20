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
package io.mapsquare.osmcontributor.core;

import android.app.Application;

import com.mapbox.mapboxsdk.geometry.BoundingBox;
import com.mapbox.mapboxsdk.geometry.LatLng;

import javax.inject.Singleton;

import io.mapsquare.osmcontributor.BuildConfig;
import io.mapsquare.osmcontributor.R;

@Singleton
public class TemplateConfigManager implements ConfigManager {

    private Application application;

    public TemplateConfigManager(Application application) {
        this.application = application;
    }

    @Override
    public int getZoomVectorial() {
        return Integer.parseInt(application.getResources().getString(R.string.zoomVectorial));
    }

    @Override
    public int getDefaultZoom() {
        return Integer.parseInt(application.getString(R.string.defaultZoom));
    }

    @Override
    public int getZoomMaxProvider() {
        return Integer.parseInt(application.getString(R.string.zoomMaxProvider));
    }

    @Override
    public float getZoomMax() {
        return Float.parseFloat(application.getString(R.string.zoomMax));
    }

    @Override
    public boolean hasPoiModification() {
        return application.getResources().getBoolean(R.bool.pointsModification);
    }

    @Override
    public boolean hasPoiAddition() {
        return application.getResources().getBoolean(R.bool.pointsAddition);
    }

    @Override
    public boolean hasBounds() {
        return application.getResources().getBoolean(R.bool.hasBounds);
    }

    @Override
    public BoundingBox getBoundingBox() {
        return new BoundingBox(Double.parseDouble(application.getResources().getString(R.string.northBound)),
                Double.parseDouble(application.getResources().getString(R.string.eastBound)),
                Double.parseDouble(application.getResources().getString(R.string.southBound)),
                Double.parseDouble(application.getResources().getString(R.string.westBound)));
    }

    @Override
    public LatLng getDefaultCenter() {
        return new LatLng(Double.parseDouble(application.getResources().getString(R.string.centerLat)),
                Double.parseDouble(application.getResources().getString(R.string.centerLng)));
    }

    @Override
    public String getMapUrl() {
        return application.getString(R.string.mapUrl);
    }

    @Override
    public String getBasePoiApiUrl() {
        return application.getString(R.string.baseOsmApiUrl);
    }

    @Override
    public String getBaseOverpassApiUrl() {
        return application.getString(R.string.baseOverpassUrl);
    }

    @Override
    public String getBingApiKey() {
        return BuildConfig.BING_API_KEY;
    }
}
