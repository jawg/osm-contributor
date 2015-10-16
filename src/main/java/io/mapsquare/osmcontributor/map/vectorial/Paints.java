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

import android.graphics.Color;

import com.mapbox.mapboxsdk.views.safecanvas.SafeDashPathEffect;
import com.mapbox.mapboxsdk.views.safecanvas.SafePaint;

public class Paints {

    /**
     * Paints used for the different types of highway
     */
    public static final SafePaint MOTORWAY_PAINT = new SafePaint();
    public static final SafePaint MOTORWAY_LINK_PAINT = new SafePaint();
    public static final SafePaint TRUNK_PAINT = new SafePaint();
    public static final SafePaint PRIMARY_PAINT = new SafePaint();
    public static final SafePaint SECONDARY_PAINT = new SafePaint();
    public static final SafePaint TERTIARY_PAINT = new SafePaint();
    public static final SafePaint UNCLASSIFIED_PAINT = new SafePaint();
    public static final SafePaint RESIDENTIAL_PAINT = new SafePaint();
    public static final SafePaint SERVICE_PAINT = new SafePaint();
    public static final SafePaint LIVING_STREET_PAINT = new SafePaint();
    public static final SafePaint PEDESTRIAN_PAINT = new SafePaint();
    public static final SafePaint BRIDLEWAY_PAINT = new SafePaint();
    public static final SafePaint CYCLEWAY_PAINT = new SafePaint();
    public static final SafePaint FOOTWAY_PAINT = new SafePaint();
    public static final SafePaint PATH_PAINT = new SafePaint();
    public static final SafePaint STEPS_PAINT = new SafePaint();
    public static final SafePaint DEFAULT_PAINT = new SafePaint();

    static {
        MOTORWAY_PAINT.setStyle(SafePaint.Style.STROKE);
        MOTORWAY_PAINT.setColor(0xff89a4cb);
        MOTORWAY_PAINT.setStrokeWidth(18);
        MOTORWAY_PAINT.setStrokeCap(SafePaint.Cap.ROUND);

        MOTORWAY_LINK_PAINT.setStyle(SafePaint.Style.STROKE);
        MOTORWAY_LINK_PAINT.setColor(0xff89a4cb);
        MOTORWAY_LINK_PAINT.setStrokeWidth(15.5f);
        MOTORWAY_LINK_PAINT.setStrokeCap(SafePaint.Cap.ROUND);

        TRUNK_PAINT.setStyle(SafePaint.Style.STROKE);
        TRUNK_PAINT.setColor(0xff94d494);
        TRUNK_PAINT.setStrokeWidth(18);
        TRUNK_PAINT.setStrokeCap(SafePaint.Cap.ROUND);

        PRIMARY_PAINT.setStyle(SafePaint.Style.STROKE);
        PRIMARY_PAINT.setColor(0xffdd9f9f);
        PRIMARY_PAINT.setStrokeWidth(18);
        PRIMARY_PAINT.setStrokeCap(SafePaint.Cap.ROUND);

        SECONDARY_PAINT.setStyle(SafePaint.Style.STROKE);
        SECONDARY_PAINT.setColor(0xfff9d6aa);
        SECONDARY_PAINT.setStrokeWidth(18);
        SECONDARY_PAINT.setStrokeCap(SafePaint.Cap.ROUND);

        TERTIARY_PAINT.setStyle(SafePaint.Style.STROKE);
        TERTIARY_PAINT.setColor(0xfff8f8ba);
        TERTIARY_PAINT.setStrokeWidth(15.5f);
        TERTIARY_PAINT.setStrokeCap(SafePaint.Cap.ROUND);

        UNCLASSIFIED_PAINT.setStyle(SafePaint.Style.STROKE);
        UNCLASSIFIED_PAINT.setColor(0xff89a4cb);
        UNCLASSIFIED_PAINT.setStrokeWidth(15.5f);
        UNCLASSIFIED_PAINT.setStrokeCap(SafePaint.Cap.ROUND);

        RESIDENTIAL_PAINT.setStyle(SafePaint.Style.STROKE);
        RESIDENTIAL_PAINT.setColor(Color.WHITE);
        RESIDENTIAL_PAINT.setStrokeWidth(15.5f);
        RESIDENTIAL_PAINT.setStrokeCap(SafePaint.Cap.ROUND);

        SERVICE_PAINT.setStyle(SafePaint.Style.STROKE);
        SERVICE_PAINT.setColor(Color.WHITE);
        SERVICE_PAINT.setStrokeWidth(7);
        SERVICE_PAINT.setStrokeCap(SafePaint.Cap.ROUND);

        LIVING_STREET_PAINT.setStyle(SafePaint.Style.STROKE);
        LIVING_STREET_PAINT.setColor(0xffcccccc);
        LIVING_STREET_PAINT.setStrokeWidth(14);
        LIVING_STREET_PAINT.setStrokeCap(SafePaint.Cap.ROUND);

        PEDESTRIAN_PAINT.setStyle(SafePaint.Style.STROKE);
        PEDESTRIAN_PAINT.setColor(0xffededed);
        PEDESTRIAN_PAINT.setStrokeWidth(14);
        PEDESTRIAN_PAINT.setStrokeCap(SafePaint.Cap.ROUND);

        BRIDLEWAY_PAINT.setStyle(SafePaint.Style.STROKE);
        BRIDLEWAY_PAINT.setColor(Color.GREEN);
        BRIDLEWAY_PAINT.setPathEffect(new SafeDashPathEffect(new float[]{20, 10}, 0, 2.4f));

        CYCLEWAY_PAINT.setStyle(SafePaint.Style.STROKE);
        CYCLEWAY_PAINT.setColor(Color.BLUE);
        CYCLEWAY_PAINT.setPathEffect(new SafeDashPathEffect(new float[]{20, 10}, 0, 2.4f));

        FOOTWAY_PAINT.setStyle(SafePaint.Style.STROKE);
        FOOTWAY_PAINT.setColor(0xfffa8072);
        FOOTWAY_PAINT.setPathEffect(new SafeDashPathEffect(new float[]{20, 10}, 0, 3f));

        PATH_PAINT.setStyle(SafePaint.Style.STROKE);
        PATH_PAINT.setColor(Color.BLACK);
        PATH_PAINT.setPathEffect(new SafeDashPathEffect(new float[]{20, 10}, 0, 2f));

        STEPS_PAINT.setStyle(SafePaint.Style.STROKE);
        STEPS_PAINT.setColor(0xfffa8072);
        STEPS_PAINT.setPathEffect(new SafeDashPathEffect(new float[]{20, 10}, 0, 2f));

        DEFAULT_PAINT.setStyle(SafePaint.Style.STROKE);
        DEFAULT_PAINT.setColor(Color.BLACK);
        DEFAULT_PAINT.setPathEffect(new SafeDashPathEffect(new float[]{20, 10}, 0, 1f));
    }


    /**
     * SafePaints used for the areas
     */
    public static final SafePaint BRIDGE_PAINT = new SafePaint();
    public static final SafePaint BUILDING_PAINT = new SafePaint();
    public static final SafePaint CEMETERY_PAINT = new SafePaint();
    public static final SafePaint COMMERCIAL_PAINT = new SafePaint();
    public static final SafePaint CONSTRUCTION_PAINT = new SafePaint();
    public static final SafePaint FOREST_PAINT = new SafePaint();
    public static final SafePaint GOLF_PAINT = new SafePaint();
    public static final SafePaint GRASS_PAINT = new SafePaint();
    public static final SafePaint INDUSTRIAL_PAINT = new SafePaint();
    public static final SafePaint PARK_PAINT = new SafePaint();
    public static final SafePaint PARKING_PAINT = new SafePaint();
    public static final SafePaint PEDESTRIAN_AREA_PAINT = new SafePaint();
    public static final SafePaint PITCH_PAINT = new SafePaint();
    public static final SafePaint PLAYGROUND_PAINT = new SafePaint();
    public static final SafePaint RESIDENTIAL_AREA_PAINT = new SafePaint();
    public static final SafePaint RETAIL_PAINT = new SafePaint();
    public static final SafePaint SCHOOL_PAINT = new SafePaint();
    public static final SafePaint STADIUM_PAINT = new SafePaint();
    public static final SafePaint TRACK_PAINT = new SafePaint();
    public static final SafePaint WATER_PAINT = new SafePaint();

    static {
        BRIDGE_PAINT.setColor(0xa0ededed);
        BUILDING_PAINT.setColor(0xa0d9d0c9);
        CEMETERY_PAINT.setColor(0xa0aacbaf);
        COMMERCIAL_PAINT.setColor(0xa0F2DAD9);
        CONSTRUCTION_PAINT.setColor(0xa0b6b592);
        FOREST_PAINT.setColor(0xa09FD082);
        GOLF_PAINT.setColor(0xa0b5e3b5);
        GRASS_PAINT.setColor(0xa0cfeca8);
        INDUSTRIAL_PAINT.setColor(0xa0EBDBE8);
        PARK_PAINT.setColor(0xa0cdf7c9);
        PARKING_PAINT.setColor(0xa0F6EEB6);
        PEDESTRIAN_AREA_PAINT.setColor(0xa0ededed);
        PITCH_PAINT.setColor(0xa08ad3af);
        PLAYGROUND_PAINT.setColor(0xa0ccfff1);
        RESIDENTIAL_AREA_PAINT.setColor(0xa0b9b9b9);
        RETAIL_PAINT.setColor(0xa0FFD6D1);
        SCHOOL_PAINT.setColor(0xa0f0f0d8);
        STADIUM_PAINT.setColor(0xa030c090);
        TRACK_PAINT.setColor(0xa074dcba);
        WATER_PAINT.setColor(0xa0b5d0d0);
    }

    public static final SafePaint TRANSPARENT_PAINT = new SafePaint();

    static {
        TRANSPARENT_PAINT.setAlpha(0);
    }
}
