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
package io.mapsquare.osmcontributor.map.utils;

public class Mercator {

    private static final double R_MAJOR = 6378137.0;
    private static final double R_MINOR = 6356752.3142;

    /**
     * Compute mercator projection of lat and lon in meters.
     *
     * @param lat latitude
     * @param lon longitude
     * @return the mercator projection
     */
    public static double[] merc(double lat, double lon) {
        return new double[]{mercX(lon), mercY(lat)};
    }

    /**
     * Compute X value of mercator projection.
     *
     * @param lon longitude
     * @return the X value of mercator projection
     */
    public static double mercX(double lon) {
        return R_MAJOR * Math.toRadians(lon);
    }

    /**
     * Compute Y value of mercator projection.
     *
     * @param lat latitude
     * @return the Y value of mercator projection
     */
    public static double mercY(double lat) {
        if (lat > 89.5) {
            lat = 89.5;
        }
        if (lat < -89.5) {
            lat = -89.5;
        }
        double temp = R_MINOR / R_MAJOR;
        double es = 1.0 - (temp * temp);
        double eccent = Math.sqrt(es);
        double phi = Math.toRadians(lat);
        double sinphi = Math.sin(phi);
        double con = eccent * sinphi;
        double com = 0.5 * eccent;
        con = Math.pow(((1.0 - con) / (1.0 + con)), com);
        double ts = Math.tan(0.5 * ((Math.PI * 0.5) - phi)) / con;
        double y = 0 - R_MAJOR * Math.log(ts);
        return y;
    }

    public static double y2lat(double aY) {
        return Math.toDegrees(2 * Math.atan(Math.exp(Math.toRadians(aY / R_MAJOR))) - Math.PI / 2);
    }
    public static double lat2y(double aLat) {
        return Math.log(Math.tan(Math.PI / 4 + Math.toRadians(aLat) / 2)) * R_MAJOR;
    }
    public static double x2lon(double aX) {
        return Math.toDegrees(aX / R_MAJOR);
    }
    public static double lon2x(double aLong) {
        return Math.toRadians(aLong) * R_MAJOR;
    }
}
