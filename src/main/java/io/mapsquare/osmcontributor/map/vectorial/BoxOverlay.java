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

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;

import com.mapbox.mapboxsdk.overlay.Overlay;
import com.mapbox.mapboxsdk.views.MapView;
import com.mapbox.mapboxsdk.views.util.Projection;

import java.util.ArrayList;
import java.util.List;

import io.mapsquare.osmcontributor.utils.Box;

public class BoxOverlay extends Overlay {

    private Box box;
    private Clipper clipper;
    private Paint paint;
    private List<XY> pointsToClip;
    private List<XY> clipPoints;
    private double[] projection;
    private Projection pj;
    private double[] projectionResult;
    private List<XY> points;
    private Path path = new Path();


    public BoxOverlay(Box box) {
        this.box = box;
        clipper = new Clipper();
        pointsToClip = new ArrayList<>();
        points = new ArrayList<>();
        paint = new Paint();

        paint.setColor(Color.RED);
        paint.setStrokeWidth(8);
        paint.setStyle(Paint.Style.STROKE);

        projection = Projection.latLongToPixelXY(box.getNorth(), box.getWest());
        points.add(new XY(projection[0], projection[1]));
        projection = Projection.latLongToPixelXY(box.getNorth(), box.getEast());
        points.add(new XY(projection[0], projection[1]));
        projection = Projection.latLongToPixelXY(box.getSouth(), box.getEast());
        points.add(new XY(projection[0], projection[1]));
        projection = Projection.latLongToPixelXY(box.getSouth(), box.getWest());
        points.add(new XY(projection[0], projection[1]));
        projection = Projection.latLongToPixelXY(box.getNorth(), box.getWest());
        points.add(new XY(projection[0], projection[1]));

    }

    @Override
    protected void draw(Canvas canvas, MapView mapView, boolean b) {
        if (b) {
            pointsToClip.clear();
            path.reset();

            pj = mapView.getProjection();
            clipper.setClippingBounds(enlargeRect(pj.getScreenRect(), 100));

            projectionResult = new double[2];

            for (XY point : points) {
                pj.toMapPixelsTranslated(point.getAsDoubleArray(), projectionResult);
                pointsToClip.add(new XY(projectionResult[0], projectionResult[1]));
            }

            clipPoints = clipper.clip(pointsToClip, true);

            if (clipPoints != null && clipPoints.size() > 0) {
                path.moveTo((float) clipPoints.get(0).getX(), (float) clipPoints.get(0).getY());
            }

            // we moved to the first point so we can skip the first element
            for (int i = 1; i < clipPoints.size(); i++) {
                path.lineTo((float) clipPoints.get(i).getX(), (float) clipPoints.get(i).getY());
            }

            // Draw path
            canvas.drawPath(path, paint);
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
