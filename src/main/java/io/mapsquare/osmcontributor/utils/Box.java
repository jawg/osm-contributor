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
package io.mapsquare.osmcontributor.utils;

import com.mapbox.mapboxsdk.geometry.BoundingBox;

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

    /**
     * Convert a {@link com.mapbox.mapboxsdk.geometry.BoundingBox} to a box.
     *
     * @param boundingBox The boundingBox to convert.
     * @return The box.
     */
    public static Box convertFromBoundingBox(BoundingBox boundingBox) {
        Box box = new Box();
        box.setEast(boundingBox.getLonEast());
        box.setNorth(boundingBox.getLatNorth());
        box.setSouth(boundingBox.getLatSouth());
        box.setWest(boundingBox.getLonWest());
        return box;
    }
}
