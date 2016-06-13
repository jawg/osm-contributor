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
package io.mapsquare.osmcontributor.utils;

import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.geometry.LatLngBounds;

/**
 * Manager allowing to get all the information of the application configuration.
 */
public interface ConfigManager {

    /**
     * Get the level of zoom where we should use the vectorial map.
     *
     * @return The level of zoom for vectorial display.
     */
    int getZoomVectorial();

    /**
     * Get the default level of zoom of the map.
     *
     * @return The default level of zoom.
     */
    int getDefaultZoom();

    /**
     * Get the max zoom of the tile provider.
     *
     * @return The provider max zoom.
     */
    int getZoomMaxProvider();

    /**
     * Get the max zoom level of the map.
     *
     * @return The max zoom level.
     */
    float getZoomMax();

    /**
     * Get whether the Poi modification feature is enabled.
     *
     * @return Whether the Poi modification feature is enabled.
     */
    boolean hasPoiModification();

    /**
     * Get whether the Poi addition feature is enabled.
     *
     * @return Whether the Poi addition feature is enabled.
     */
    boolean hasPoiAddition();

    /**
     * Get whether the map has bounds.
     *
     * @return whether the map has bounds.
     */
    boolean hasBounds();

    /**
     * Get the bounding box of the map.
     *
     * @return The BoundingBox of the map.
     */
    LatLngBounds getLatLngBounds();

    /**
     * Get the default center of the map at startup.
     *
     * @return The default center.
     */
    LatLng getDefaultCenter();

    /**
     * Get the url of the map tiles provider.
     *
     * @return Url of the map tiles provider.
     */
    String getMapUrl();

    /**
     * Get the url of the API for POIs.
     *
     * @return The url of the API.
     */
    String getBasePoiApiUrl();

    /**
     * Get the url of the Overpass API.
     *
     * @return The url of the Overpass API.
     */
    String getBaseOverpassApiUrl();

    /**
     * Get the Bing API key.
     *
     * @return The Bing API key.
     */
    String getBingApiKey();
}
