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
import android.graphics.DashPathEffect;
import android.graphics.Paint;

public class Paints {

    /**
     * Paints used for the different types of highway
     */
    public static final Paint MOTORWAY_PAINT = new Paint();
    public static final Paint MOTORWAY_LINK_PAINT = new Paint();
    public static final Paint TRUNK_PAINT = new Paint();
    public static final Paint PRIMARY_PAINT = new Paint();
    public static final Paint SECONDARY_PAINT = new Paint();
    public static final Paint TERTIARY_PAINT = new Paint();
    public static final Paint UNCLASSIFIED_PAINT = new Paint();
    public static final Paint RESIDENTIAL_PAINT = new Paint();
    public static final Paint SERVICE_PAINT = new Paint();
    public static final Paint LIVING_STREET_PAINT = new Paint();
    public static final Paint PEDESTRIAN_PAINT = new Paint();
    public static final Paint BRIDLEWAY_PAINT = new Paint();
    public static final Paint CYCLEWAY_PAINT = new Paint();
    public static final Paint FOOTWAY_PAINT = new Paint();
    public static final Paint PATH_PAINT = new Paint();
    public static final Paint STEPS_PAINT = new Paint();
    public static final Paint DEFAULT_PAINT = new Paint();

    static {
        MOTORWAY_PAINT.setStyle(Paint.Style.STROKE);
        MOTORWAY_PAINT.setColor(0xff89a4cb);
        MOTORWAY_PAINT.setStrokeWidth(18);
        MOTORWAY_PAINT.setStrokeCap(Paint.Cap.ROUND);

        MOTORWAY_LINK_PAINT.setStyle(Paint.Style.STROKE);
        MOTORWAY_LINK_PAINT.setColor(0xff89a4cb);
        MOTORWAY_LINK_PAINT.setStrokeWidth(15.5f);
        MOTORWAY_LINK_PAINT.setStrokeCap(Paint.Cap.ROUND);

        TRUNK_PAINT.setStyle(Paint.Style.STROKE);
        TRUNK_PAINT.setColor(0xff94d494);
        TRUNK_PAINT.setStrokeWidth(18);
        TRUNK_PAINT.setStrokeCap(Paint.Cap.ROUND);

        PRIMARY_PAINT.setStyle(Paint.Style.STROKE);
        PRIMARY_PAINT.setColor(0xffdd9f9f);
        PRIMARY_PAINT.setStrokeWidth(18);
        PRIMARY_PAINT.setStrokeCap(Paint.Cap.ROUND);

        SECONDARY_PAINT.setStyle(Paint.Style.STROKE);
        SECONDARY_PAINT.setColor(0xfff9d6aa);
        SECONDARY_PAINT.setStrokeWidth(18);
        SECONDARY_PAINT.setStrokeCap(Paint.Cap.ROUND);

        TERTIARY_PAINT.setStyle(Paint.Style.STROKE);
        TERTIARY_PAINT.setColor(0xfff8f8ba);
        TERTIARY_PAINT.setStrokeWidth(15.5f);
        TERTIARY_PAINT.setStrokeCap(Paint.Cap.ROUND);

        UNCLASSIFIED_PAINT.setStyle(Paint.Style.STROKE);
        UNCLASSIFIED_PAINT.setColor(0xff89a4cb);
        UNCLASSIFIED_PAINT.setStrokeWidth(15.5f);
        UNCLASSIFIED_PAINT.setStrokeCap(Paint.Cap.ROUND);

        RESIDENTIAL_PAINT.setStyle(Paint.Style.STROKE);
        RESIDENTIAL_PAINT.setColor(Color.WHITE);
        RESIDENTIAL_PAINT.setStrokeWidth(15.5f);
        RESIDENTIAL_PAINT.setStrokeCap(Paint.Cap.ROUND);

        SERVICE_PAINT.setStyle(Paint.Style.STROKE);
        SERVICE_PAINT.setColor(Color.WHITE);
        SERVICE_PAINT.setStrokeWidth(7);
        SERVICE_PAINT.setStrokeCap(Paint.Cap.ROUND);

        LIVING_STREET_PAINT.setStyle(Paint.Style.STROKE);
        LIVING_STREET_PAINT.setColor(0xffcccccc);
        LIVING_STREET_PAINT.setStrokeWidth(14);
        LIVING_STREET_PAINT.setStrokeCap(Paint.Cap.ROUND);

        PEDESTRIAN_PAINT.setStyle(Paint.Style.STROKE);
        PEDESTRIAN_PAINT.setColor(0xffededed);
        PEDESTRIAN_PAINT.setStrokeWidth(14);
        PEDESTRIAN_PAINT.setStrokeCap(Paint.Cap.ROUND);

        DashPathEffect dashPathEffect = new DashPathEffect(new float[]{20, 10}, 0);

        BRIDLEWAY_PAINT.setStyle(Paint.Style.STROKE);
        BRIDLEWAY_PAINT.setColor(Color.GREEN);
        BRIDLEWAY_PAINT.setStrokeWidth(2.4f);
        BRIDLEWAY_PAINT.setPathEffect(dashPathEffect);

        CYCLEWAY_PAINT.setStyle(Paint.Style.STROKE);
        CYCLEWAY_PAINT.setColor(Color.BLUE);
        CYCLEWAY_PAINT.setStrokeWidth(2.4f);
        CYCLEWAY_PAINT.setPathEffect(dashPathEffect);

        FOOTWAY_PAINT.setStyle(Paint.Style.STROKE);
        FOOTWAY_PAINT.setColor(0xfffa8072);
        FOOTWAY_PAINT.setStrokeWidth(3f);
        FOOTWAY_PAINT.setPathEffect(dashPathEffect);

        PATH_PAINT.setStyle(Paint.Style.STROKE);
        PATH_PAINT.setColor(Color.BLACK);
        PATH_PAINT.setStrokeWidth(2);
        PATH_PAINT.setPathEffect(dashPathEffect);

        STEPS_PAINT.setStyle(Paint.Style.STROKE);
        STEPS_PAINT.setColor(0xfffa8072);
        STEPS_PAINT.setStrokeWidth(2);
        STEPS_PAINT.setPathEffect(dashPathEffect);

        DEFAULT_PAINT.setStyle(Paint.Style.STROKE);
        DEFAULT_PAINT.setColor(Color.BLACK);
        DEFAULT_PAINT.setStrokeWidth(1);
        DEFAULT_PAINT.setPathEffect(dashPathEffect);
    }


    /**
     * Paints used for the areas
     */
    public static final Paint BRIDGE_PAINT = new Paint();
    public static final Paint BUILDING_PAINT = new Paint();
    public static final Paint CEMETERY_PAINT = new Paint();
    public static final Paint COMMERCIAL_PAINT = new Paint();
    public static final Paint CONSTRUCTION_PAINT = new Paint();
    public static final Paint FOREST_PAINT = new Paint();
    public static final Paint GOLF_PAINT = new Paint();
    public static final Paint GRASS_PAINT = new Paint();
    public static final Paint INDUSTRIAL_PAINT = new Paint();
    public static final Paint PARK_PAINT = new Paint();
    public static final Paint PARKING_PAINT = new Paint();
    public static final Paint PEDESTRIAN_AREA_PAINT = new Paint();
    public static final Paint PITCH_PAINT = new Paint();
    public static final Paint PLAYGROUND_PAINT = new Paint();
    public static final Paint RESIDENTIAL_AREA_PAINT = new Paint();
    public static final Paint RETAIL_PAINT = new Paint();
    public static final Paint SCHOOL_PAINT = new Paint();
    public static final Paint STADIUM_PAINT = new Paint();
    public static final Paint TRACK_PAINT = new Paint();
    public static final Paint WATER_PAINT = new Paint();

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

    public static final Paint TRANSPARENT_PAINT = new Paint();

    static {
        TRANSPARENT_PAINT.setAlpha(0);
    }
}
