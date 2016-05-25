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
package io.mapsquare.osmcontributor.map.vectorial;

import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;

import com.mapbox.mapboxsdk.geometry.LatLngBounds;
import com.mapbox.mapboxsdk.maps.Projection;
import com.mapbox.mapboxsdk.overlay.SafeDrawOverlay;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BoxOverlay extends SafeDrawOverlay {

    private Clipper clipper;
    private SafePaint paint;
    private List<XY> clipPoints = new ArrayList<>();
    private Projection pj;
    private double[] projectionResult;
    private XY nw, ne, se, sw;


    public BoxOverlay(LatLngBounds box) {
        clipper = new Clipper();
        paint = new SafePaint();

        paint.setColor(Color.RED);
        paint.setStrokeWidth(8);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeCap(Paint.Cap.ROUND);

        double[] projection;
        projection = Projection.latLongToPixelXY(box.getLatNorth(), box.getLonWest());
        nw = new XY(projection[0], projection[1]);

        projection = Projection.latLongToPixelXY(box.getLatNorth(), box.getLonEast());
        ne = new XY(projection[0], projection[1]);

        projection = Projection.latLongToPixelXY(box.getLatSouth(), box.getLonEast());
        se = new XY(projection[0], projection[1]);

        projection = Projection.latLongToPixelXY(box.getLatSouth(), box.getLonWest());
        sw = new XY(projection[0], projection[1]);
    }

    @Override
    protected void drawSafe(ISafeCanvas canvas, MapView mapView, boolean b) {
        if (b) {
            pj = mapView.getProjection();
            clipper.setClippingBounds(enlargeRect(pj.getScreenRect(), 100));

            drawClipLine(nw, ne, canvas);
            drawClipLine(ne, se, canvas);
            drawClipLine(se, sw, canvas);
            drawClipLine(sw, nw, canvas);
        }
    }

    private void drawClipLine(XY a, XY b, ISafeCanvas canvas) {
        XY ap, bp;
        projectionResult = new double[2];

        pj.toMapPixelsTranslated(a.getAsDoubleArray(), projectionResult);
        ap = new XY(projectionResult[0], projectionResult[1]);

        pj.toMapPixelsTranslated(b.getAsDoubleArray(), projectionResult);
        bp = new XY(projectionResult[0], projectionResult[1]);

        clipPoints.clear();
        clipPoints = clipper.clip(Arrays.asList(ap, bp), false);

        if (clipPoints.size() == 2) {
            // Draw path
            canvas.drawLine(clipPoints.get(0).getX(), clipPoints.get(0).getY(), clipPoints.get(1).getX(), clipPoints.get(1).getY(), paint);
        }
    }

    private Rect enlargeRect(Rect rect, int width) {
        Rect res = new Rect();
        res.left = rect.left - width;
        res.top = rect.top - width;
        res.right = rect.right + width;
        res.bottom = rect.bottom + width;
        return res;
    }
}
