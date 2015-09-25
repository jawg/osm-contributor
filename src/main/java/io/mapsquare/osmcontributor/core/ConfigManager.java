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
package io.mapsquare.osmcontributor.core;

import com.mapbox.mapboxsdk.geometry.BoundingBox;
import com.mapbox.mapboxsdk.geometry.LatLng;

import io.mapsquare.osmcontributor.utils.Box;

/**
 * Manager allowing to get all the informations of the application configuration.
 */
public interface ConfigManager {

    /**
     * Get the box where POIs are preloaded into the assets.
     *
     * @return the box
     */
    Box getPreloadedBox();

    /**
     * Get the level of zoom where we should use the vectorial map.
     *
     * @return the level of zoom for vectorial display
     */
    int getZoomVectorial();

    /**
     * Get the default level of zoom of the map.
     *
     * @return the default level of zoom
     */
    int getDefaultZoom();

    /**
     * Get whether the Poi modification feature is enabled.
     *
     * @return whether the Poi modification feature is enabled
     */
    boolean hasPoiModification();

    /**
     * Get whether the Poi addition feature is enabled.
     *
     * @return whether the Poi addition feature is enabled
     */
    boolean hasPoiAddition();

    /**
     * Get whether the map has bounds.
     *
     * @return whether the map has bounds
     */
    boolean hasBounds();

    /**
     * Get the bounding box of the map.
     *
     * @return the BoundingBox of the map
     */
    BoundingBox getBoundingBox();

    /**
     * Get the default center of the map at startup.
     *
     * @return the default center
     */
    LatLng getDefaultCenter();

    /**
     * Get the url of the map tiles provider.
     *
     * @return url of the map tiles provider
     */
    String getMapUrl();

    /**
     * Get the url of the API for POIs.
     *
     * @return the url of the api
     */
    String getBasePoiApiUrl();

    /**
     * Get the url of the Overpass API.
     *
     * @return the url of the Overpass API
     */
    String getBaseOverpassApiUrl();
}
