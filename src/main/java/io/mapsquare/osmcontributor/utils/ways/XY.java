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
package io.mapsquare.osmcontributor.utils.ways;


public class XY {
    private double x;
    private double y;
    private String nodeRefId;

    public XY() {

    }

    public XY(double x, double y) {
        this.x = x;
        this.y = y;
        this.nodeRefId = null;
    }

    public XY(double x, double y, String nodeRefId) {
        this.x = x;
        this.y = y;
        this.nodeRefId = nodeRefId;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public double[] getAsDoubleArray() {
        return new double[]{x, y};
    }

    public String getNodeRefId() {
        return nodeRefId;
    }

    public void setNodeRefId(String nodeRefId) {
        this.nodeRefId = nodeRefId;
    }

    @Override
    public String toString() {
        return "XY{" +
                "x=" + x +
                ", y=" + y +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        XY xy = (XY) o;

        return Double.compare(xy.x, x) == 0 && Double.compare(xy.y, y) == 0;
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        temp = Double.doubleToLongBits(x);
        result = (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(y);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }
}
