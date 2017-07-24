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
package io.jawg.osmcontributor.utils;

import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.geometry.LatLngBounds;

/**
 * Class representing a box delimited by north, east, south and west bounds.
 */
public class Box {
    private double north, east, south, west;

    public Box() {
    }

    public Box(double north, double east, double south, double west) {
        this.north = north;
        this.east = east;
        this.south = south;
        this.west = west;
    }

    public double getNorth() {
        return north;
    }

    public void setNorth(double north) {
        this.north = north;
    }

    public double getEast() {
        return east;
    }

    public void setEast(double east) {
        this.east = east;
    }

    public double getSouth() {
        return south;
    }

    public void setSouth(double south) {
        this.south = south;
    }

    public double getWest() {
        return west;
    }

    public void setWest(double west) {
        this.west = west;
    }

    public boolean contains(Box box) {
        return box != null && (north >= box.getNorth() && east >= box.getEast() && south <= box.getSouth() && west <= box.getWest());
    }

    public String osmFormat() {
        return "(" + this.south + "," +
                this.west + "," +
                this.north + "," +
                this.east + ");";
    }

    /**
     * Convert a {@link com.mapbox.mapboxsdk.geometry.LatLngBounds} to a box.
     *
     * @param latLngBounds The boundingBox to convert.
     * @return The box.
     */
    public static Box convertFromLatLngBounds(LatLngBounds latLngBounds) {
        Box box = new Box();
        box.setEast(latLngBounds.getLonEast());
        box.setNorth(latLngBounds.getLatNorth());
        box.setSouth(latLngBounds.getLatSouth());
        box.setWest(latLngBounds.getLonWest());
        return box;
    }

    /**
     * Convert the box to a {@link com.mapbox.mapboxsdk.geometry.LatLngBounds}.
     *
     * @return boundingBox The boundingBox to convert.
     */
    public LatLngBounds getLatLngBounds() {
        return new LatLngBounds.Builder().include(new LatLng(north, east)).include(new LatLng(south, west)).build();
    }

    /**
     * Enlarge the Box.
     *
     * @param viewLatLngBounds The box to enlarge.
     * @param factor The factor to enlarge the box.
     * @return Box the Box enlarged.
     */
    public static LatLngBounds enlarge(LatLngBounds viewLatLngBounds, double factor) {
        double n = viewLatLngBounds.getLatNorth();
        double e = viewLatLngBounds.getLonEast();
        double s = viewLatLngBounds.getLatSouth();
        double w = viewLatLngBounds.getLonWest();
        double f = (factor - 1) / 2;
        return new LatLngBounds.Builder()
                .include(new LatLng(n + f * (n - s), e + f * (e - w)))
                .include(new LatLng(s - f * (n - s), w - f * (e - w)))
                .build();
    }

}
