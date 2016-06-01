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
package io.mapsquare.osmcontributor.map.ways;


public class VectorialObject {
//
//
//    private String id;
//
//    /**
//     * Stores points, converted to the map projection.
//     */
//    private List<XY> xyList;
//
//    /**
//     * Define if the path must be filled
//     */
//    private boolean filled;
//
//    /**
//     * Color of the path
//     */
//    private SafePaint paint;
//
//    private double level = 0;
//
//    private int priority = 0;
//
//    public VectorialObject(boolean filled) {
//        this.filled = filled;
//        this.paint = new SafePaint();
//        paint.setColor(0x880000FF);
//        if (filled) {
//            paint.setStyle(Paint.Style.FILL);
//        } else {
//            paint.setStyle(Paint.Style.STROKE);
//        }
//        xyList = new ArrayList<>();
//    }
//
//    public VectorialObject(SafePaint paint, boolean filled) {
//        this.filled = filled;
//        this.paint = paint;
//        if (filled) {
//            paint.setStyle(Paint.Style.FILL);
//        } else {
//            paint.setStyle(Paint.Style.STROKE);
//        }
//        xyList = new ArrayList<>();
//    }
//
//    public VectorialObject(VectorialObject vectorialObject) {
//        this.id = vectorialObject.id;
//        this.filled = vectorialObject.filled;
//        this.paint = vectorialObject.paint;
//        this.xyList = vectorialObject.getXyList();
//    }
//
//    public List<XY> getXyList() {
//        return xyList;
//    }
//
//    public void setXyList(List<XY> xyList) {
//        this.xyList = xyList;
//    }
//
//    public void addPoint(XY xy) {
//        this.xyList.add(xy);
//    }
//
//    public int getNumberOfPoints() {
//        return this.xyList.size();
//    }
//
//    public boolean isFilled() {
//        return filled;
//    }
//
//    public void setFilled(boolean filled) {
//        this.filled = filled;
//    }
//
//    public SafePaint getPaint() {
//        return paint;
//    }
//
//    public void setPaint(SafePaint paint) {
//        this.paint = paint;
//    }
//
//    public double getLevel() {
//        return level;
//    }
//
//    public void setLevel(double level) {
//        this.level = level;
//    }
//
//    public String getId() {
//        return id;
//    }
//
//    public void setId(String id) {
//        this.id = id;
//    }
//
//    public int getPriority() {
//        return priority;
//    }
//
//    public void setPriority(int priority) {
//        this.priority = priority;
//    }
//
//    @Override
//    public boolean equals(Object o) {
//        if (this == o) {
//            return true;
//        }
//        if (o == null || getClass() != o.getClass()) {
//            return false;
//        }
//
//        VectorialObject that = (VectorialObject) o;
//
//        if (Double.compare(that.level, level) != 0) {
//            return false;
//        }
//        return !(id != null ? !id.equals(that.id) : that.id != null);
//
//    }
//
//    @Override
//    public int hashCode() {
//        return id != null ? id.hashCode() : 0;
//    }
//
//    @Override
//    public int compareTo(VectorialObject another) {
//        return another.priority - this.priority;
//    }
}
