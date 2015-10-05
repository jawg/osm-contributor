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
import android.graphics.PathDashPathEffect;
import android.graphics.PathEffect;
import android.graphics.Rect;

import com.mapbox.mapboxsdk.overlay.Overlay;
import com.mapbox.mapboxsdk.views.MapView;
import com.mapbox.mapboxsdk.views.util.Projection;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import io.mapsquare.osmcontributor.utils.FlavorUtils;
import timber.log.Timber;

public class VectorialOverlay extends Overlay {

    private static final String TAG = "VectorialOverlay";
    private static final int PRECISION = 1000;

    private List<VectorialObject> vectorialObjectList;
    private String movingObjectId;
    private String selectedObjectId;

    private Clipper clipper = new Clipper();

    private TreeSet<Double> levels;

    // Paint for the borders of a closed vectorial object
    private Paint borderPaint;

    private double level = 0;

    private int zoomVectorial;

    private Paint movingPaint = new Paint();
    private Paint selectedPaint = new Paint();

    public VectorialOverlay(int zoomVectorial) {
        this.zoomVectorial = zoomVectorial;
        this.levels = new TreeSet<>();
        this.levels.add(0d);
        setOverlayIndex(PATHOVERLAY_INDEX);

        borderPaint = new Paint();
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setColor(Color.GRAY);

        // Settings for selected nodes in a way
        movingPaint.setColor(0xFFFF0000);
        movingPaint.setStrokeWidth(15);

        selectedPaint.setColor(0xFFFFFF00);
        selectedPaint.setStrokeWidth(15);
    }

    public VectorialOverlay(int zoomVectorial, Set<VectorialObject> vectorialObjects) {
        this(zoomVectorial);
        setVectorialObjects(vectorialObjects);
    }

    public VectorialOverlay(int zoomVectorial, Set<VectorialObject> vectorialObjects, TreeSet<Double> levels) {
        this(zoomVectorial, vectorialObjects);
        this.levels = levels;
    }

    public String getMovingObjectId() {
        return movingObjectId;
    }

    public void setMovingObjectId(String movingObjectId) {
        this.movingObjectId = movingObjectId;
    }

    public void setSelectedObjectId(String selectedObjectId) {
        this.selectedObjectId = selectedObjectId;
    }

    public void setVectorialObjects(Set<VectorialObject> vectorialObjects) {
        vectorialObjectList = new ArrayList<>(vectorialObjects);
        Collections.sort(vectorialObjectList);
        Timber.d("vectorial object size : %s ", vectorialObjects.size());
    }

    public double getLevel() {
        return level;
    }

    public void setLevel(double level) {
        this.level = (double) Math.round(level * PRECISION) / PRECISION;
    }

    public void levelUp() {
        Double l = levels.higher(level);
        if (l != null) {
            level = l;
        }
    }

    public void levelDown() {
        Double l = levels.lower(level);
        if (l != null) {
            level = l;
        }
    }

    public TreeSet<Double> getLevels() {
        return levels;
    }

    public void setLevels(TreeSet<Double> levels) {
        this.levels = levels;
    }

    public void addLevel(double level) {
        levels.add(level);
    }

    boolean isBeingEditObj = false;

    @Override
    protected void draw(Canvas canvas, MapView mapView, boolean shadow) {

        float zoomLevel = mapView.getZoomLevel();

        // nothing to paint
        if (shadow || zoomLevel < zoomVectorial || vectorialObjectList.isEmpty()) {
            return;
        }

        // Color to apply on the map
        if (FlavorUtils.isStore()) {
            canvas.drawColor(0x37FFFFFF);
        } else {
            canvas.drawColor(0xFFB9B9B9);
        }

        final Projection pj = mapView.getProjection();
        enlargeRect(pj.getScreenRect(), 100);
        clipper.setClippingBounds(enlargeRect(pj.getScreenRect(), 100));

        double scaleFactor = Math.pow(1.75, Math.floor(zoomLevel) - 18);

        Path p = new Path();
        p.addCircle(0, 0, (float) (3 * scaleFactor), Path.Direction.CCW);
        PathDashPathEffect dashPathEffect = new PathDashPathEffect(p, (float) (10 * scaleFactor), (float) (10 * scaleFactor), PathDashPathEffect.Style.TRANSLATE);

        Path path = new Path();
        double[] projectionResult = new double[2];

        for (VectorialObject vObject : vectorialObjectList) {
            isBeingEditObj = false;

            if (level != vObject.getLevel()) {
                continue;
            }
            final int size = vObject.getNumberOfPoints();

            // nothing to paint
            if (size == 0) {
                continue;
            }

            // paint a point
            if (size == 1) {
                final XY point = vObject.getXyList().get(0);
                pj.toMapPixelsTranslated(point.getAsDoubleArray(), projectionResult);

                XY currentPoint = applyChangesOnSelectedPoint(point, canvas);

                if (isBeingEditObj) {
                    canvas.drawPoint((float) currentPoint.getX(), (float) currentPoint.getY(), movingPaint);
                    isBeingEditObj = false;
                } else if (currentPoint.getNodeRefId().equals(selectedObjectId)) {
                    canvas.drawPoint((float) projectionResult[0], (float) projectionResult[1], selectedPaint);
                } else {
                    canvas.drawPoint((float) projectionResult[0], (float) projectionResult[1], vObject.getPaint());
                }
                continue;
            }

            // Compute points to screen coordinates
            List<XY> computedList = new ArrayList<>(size);
            for (final XY point : vObject.getXyList()) {
                XY currentPoint = applyChangesOnSelectedPoint(point, canvas);

                if (isBeingEditObj) {
                    computedList.add(currentPoint);
                    isBeingEditObj = false;
                } else {
                    pj.toMapPixelsTranslated(currentPoint.getAsDoubleArray(), projectionResult);
                    computedList.add(new XY(projectionResult[0], projectionResult[1], currentPoint.getNodeRefId()));
                }
            }

            // Clip lines and polygons to screen size
            List<XY> clippedList = clipper.clip(computedList, vObject.isFilled());

            // Nothing to draw
            if (clippedList.size() < 2) {
                continue;
            }

            // Make the path to draw
            path.reset();
            path.moveTo((float) clippedList.get(0).getX(), (float) clippedList.get(0).getY());
            for (int i = 1; i < clippedList.size(); i++) {
                path.lineTo((float) clippedList.get(i).getX(), (float) clippedList.get(i).getY());
            }

            // Draw path
            if (vObject.isFilled()) {
                canvas.drawPath(path, vObject.getPaint());
                canvas.drawPath(path, borderPaint);
            } else {
                final float realWidth = vObject.getPaint().getStrokeWidth();
                vObject.getPaint().setStrokeWidth((float) (realWidth * scaleFactor));
                PathEffect pathEffect = vObject.getPaint().getPathEffect();
                if (pathEffect != null) {
                    vObject.getPaint().setPathEffect(dashPathEffect);
                }
                canvas.drawPath(path, vObject.getPaint());
                vObject.getPaint().setPathEffect(pathEffect);
                vObject.getPaint().setStrokeWidth(realWidth);
            }
        }

    }

    private XY applyChangesOnSelectedPoint(XY point, Canvas canvas) {
        if (movingObjectId != null && movingObjectId.equals(point.getNodeRefId())) {
            XY res = new XY();
            res.setX(canvas.getClipBounds().exactCenterX());
            res.setY(canvas.getClipBounds().exactCenterY());
            res.setNodeRefId(point.getNodeRefId());
            isBeingEditObj = true;
            return res;
        }
        return point;
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
