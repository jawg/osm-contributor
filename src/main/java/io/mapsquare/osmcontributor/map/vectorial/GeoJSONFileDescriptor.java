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

import java.util.Set;

public class GeoJSONFileDescriptor {

    private String name;
    private double north;
    private double south;
    private double east;
    private double west;

    private boolean parsed = false;

    private Set<VectorialObject> vectorialObjects;

    private Set<Double> levels;

    public GeoJSONFileDescriptor() {
    }

    public GeoJSONFileDescriptor(String name) {
        this.name = name;
    }

    public GeoJSONFileDescriptor(String name, double north, double south, double west, double east) {
        this.name = name;
        this.north = north;
        this.south = south;
        this.west = west;
        this.east = east;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public boolean isParsed() {
        return parsed;
    }

    public void setParsed(boolean parsed) {
        this.parsed = parsed;
    }

    public Set<VectorialObject> getVectorialObjects() {
        return vectorialObjects;
    }

    public void setVectorialObjects(Set<VectorialObject> vectorialObjects) {
        this.vectorialObjects = vectorialObjects;
    }

    public Set<Double> getLevels() {
        return levels;
    }

    public void setLevels(Set<Double> levels) {
        this.levels = levels;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("GeoJSONFileDescriptor{");
        sb.append("name='").append(name).append('\'');
        sb.append(", north=").append(north);
        sb.append(", south=").append(south);
        sb.append(", east=").append(east);
        sb.append(", west=").append(west);
        sb.append('}');
        return sb.toString();
    }

    public boolean inBounds(double lat, double lng) {
        return lat <= north && lat >= south && lng <= east && lng >= west;
    }

    public boolean inBounds(double north, double east, double south, double west) {
        double maxLonWest = Math.max(this.west, west);
        double minLonEast = Math.min(this.east, east);
        double maxLatNorth = Math.min(this.north, north);
        double minLatSouth = Math.max(this.south, south);

        if (maxLonWest < minLonEast && maxLatNorth > minLatSouth) {
            return true;
        }
        return false;
    }
}
