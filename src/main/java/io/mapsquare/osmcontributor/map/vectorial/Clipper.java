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

import android.graphics.Rect;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Clipper {

    private interface Function1<R, P> {
        R apply(P param);
    }


    private static class Edge {
        final XY pointA, pointB;
        final Function1<Boolean, XY> insideCheck;

        public Edge(XY pointA, XY pointB, Function1<Boolean, XY> insideCheck) {
            this.pointA = pointA;
            this.pointB = pointB;
            this.insideCheck = insideCheck;
        }


        @Override
        public String toString() {
            return "Edge{" +
                    "pointA=" + pointA +
                    ", pointB=" + pointB +
                    '}';
        }
    }

    Edge[] edges = new Edge[4];

    public Clipper(Rect clippingBounds) {
        setClippingBounds(clippingBounds);
    }

    public Clipper() {

    }

    public void setClippingBounds(final Rect clippingBounds) {
        edges[0] = new Edge(new XY(clippingBounds.left, clippingBounds.top), new XY(clippingBounds.right, clippingBounds.top), new Function1<Boolean, XY>() {
            @Override
            public Boolean apply(XY param) {
                return param.getY() > clippingBounds.top;
            }
        });
        edges[1] = new Edge(new XY(clippingBounds.right, clippingBounds.top), new XY(clippingBounds.right, clippingBounds.bottom), new Function1<Boolean, XY>() {
            @Override
            public Boolean apply(XY param) {
                return param.getX() < clippingBounds.right;
            }
        });
        edges[2] = new Edge(new XY(clippingBounds.right, clippingBounds.bottom), new XY(clippingBounds.left, clippingBounds.bottom), new Function1<Boolean, XY>() {
            @Override
            public Boolean apply(XY param) {
                return param.getY() < clippingBounds.bottom;
            }
        });
        edges[3] = new Edge(new XY(clippingBounds.left, clippingBounds.bottom), new XY(clippingBounds.left, clippingBounds.top), new Function1<Boolean, XY>() {
            @Override
            public Boolean apply(XY param) {
                return param.getX() > clippingBounds.left;
            }
        });
    }


    /**
     * Lists used in the algorithm, we use the same lists for each invocation of the clipping method to minimise memory allocations.
     */
    private List<XY> resultList = new ArrayList<>(100);
    private List<XY> tmpList = new ArrayList<>(100);


    /**
     * Sutherland–Hodgman algorithm for clipping polygons.
     * Clip the polygon or line to the Rect of the Clipper
     *
     * This method is not threadsafe
     *
     * @param pointsToClip list of XY points forming a line or polygon to clip
     * @param isPolygon
     * @return list of XY points forming the clipped line or polygon, for memory allocations reasons,
     * this list will be modified on the next call to the clip method, be sure you make a copy of it
     * if you need to keep it !
     */
    public List<XY> clip(List<XY> pointsToClip, boolean isPolygon) {
        resultList.clear();
        resultList.addAll(pointsToClip);
        for (Edge edge : edges) {
            tmpList.clear();
            tmpList.addAll(resultList);
            resultList.clear();
            if (tmpList.isEmpty()) {
                return Collections.emptyList();
            }
            XY s;
            if (isPolygon) {
                s = tmpList.get(tmpList.size() - 1);
            } else {
                s = tmpList.get(0);
            }
            for (XY e : tmpList) {
                if (edge.insideCheck.apply(e)) {
                    if (!edge.insideCheck.apply(s)) {
                        resultList.add(intersection(s, e, edge.pointA, edge.pointB));
                    }
                    resultList.add(e);
                } else if (edge.insideCheck.apply(s)) {
                    resultList.add(intersection(s, e, edge.pointA, edge.pointB));
                }
                s = e;
            }

        }
        return resultList;
    }


    /**
     * Computes the existing intersection of two lines. The caller must ensure the intersection exists
     *
     * @param line1point1 first point on first line
     * @param line1point2 second point on first line
     * @param line2point1 first point on second line
     * @param line2point2 second point on second line
     * @return the intersection point
     */
    XY intersection(XY line1point1, XY line1point2, XY line2point1, XY line2point2) {
        double line1Denominator = line1point2.getX() - line1point1.getX();
        double line2Denominator = line2point2.getX() - line2point1.getX();
        double a = (line1point2.getY() - line1point1.getY()) / line1Denominator;
        double b = line1point1.getY() - a * line1point1.getX();
        double c = (line2point2.getY() - line2point1.getY()) / line2Denominator;
        double d = line2point1.getY() - c * line2point1.getX();
        double x = line1Denominator == 0 ? line1point1.getX() : (line2Denominator == 0 ? line2point1.getX() : -(b - d) / (a - c));
        double y = line1Denominator == 0 ? c * x + d : a * x + b;
        return new XY(x, y);
    }
}
