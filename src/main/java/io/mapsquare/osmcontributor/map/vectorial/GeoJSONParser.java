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

import com.cocoahero.android.geojson.Feature;
import com.cocoahero.android.geojson.FeatureCollection;
import com.cocoahero.android.geojson.LineString;
import com.cocoahero.android.geojson.MultiLineString;
import com.cocoahero.android.geojson.MultiPolygon;
import com.cocoahero.android.geojson.Polygon;
import com.mapbox.mapboxsdk.views.util.Projection;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

import timber.log.Timber;

import static io.mapsquare.osmcontributor.map.vectorial.Paints.BRIDGE_PAINT;
import static io.mapsquare.osmcontributor.map.vectorial.Paints.BRIDLEWAY_PAINT;
import static io.mapsquare.osmcontributor.map.vectorial.Paints.BUILDING_PAINT;
import static io.mapsquare.osmcontributor.map.vectorial.Paints.CEMETERY_PAINT;
import static io.mapsquare.osmcontributor.map.vectorial.Paints.COMMERCIAL_PAINT;
import static io.mapsquare.osmcontributor.map.vectorial.Paints.CONSTRUCTION_PAINT;
import static io.mapsquare.osmcontributor.map.vectorial.Paints.CYCLEWAY_PAINT;
import static io.mapsquare.osmcontributor.map.vectorial.Paints.DEFAULT_PAINT;
import static io.mapsquare.osmcontributor.map.vectorial.Paints.FOOTWAY_PAINT;
import static io.mapsquare.osmcontributor.map.vectorial.Paints.FOREST_PAINT;
import static io.mapsquare.osmcontributor.map.vectorial.Paints.GOLF_PAINT;
import static io.mapsquare.osmcontributor.map.vectorial.Paints.GRASS_PAINT;
import static io.mapsquare.osmcontributor.map.vectorial.Paints.INDUSTRIAL_PAINT;
import static io.mapsquare.osmcontributor.map.vectorial.Paints.LIVING_STREET_PAINT;
import static io.mapsquare.osmcontributor.map.vectorial.Paints.MOTORWAY_LINK_PAINT;
import static io.mapsquare.osmcontributor.map.vectorial.Paints.MOTORWAY_PAINT;
import static io.mapsquare.osmcontributor.map.vectorial.Paints.PARKING_PAINT;
import static io.mapsquare.osmcontributor.map.vectorial.Paints.PARK_PAINT;
import static io.mapsquare.osmcontributor.map.vectorial.Paints.PATH_PAINT;
import static io.mapsquare.osmcontributor.map.vectorial.Paints.PEDESTRIAN_AREA_PAINT;
import static io.mapsquare.osmcontributor.map.vectorial.Paints.PEDESTRIAN_PAINT;
import static io.mapsquare.osmcontributor.map.vectorial.Paints.PITCH_PAINT;
import static io.mapsquare.osmcontributor.map.vectorial.Paints.PLAYGROUND_PAINT;
import static io.mapsquare.osmcontributor.map.vectorial.Paints.PRIMARY_PAINT;
import static io.mapsquare.osmcontributor.map.vectorial.Paints.RESIDENTIAL_AREA_PAINT;
import static io.mapsquare.osmcontributor.map.vectorial.Paints.RESIDENTIAL_PAINT;
import static io.mapsquare.osmcontributor.map.vectorial.Paints.RETAIL_PAINT;
import static io.mapsquare.osmcontributor.map.vectorial.Paints.SCHOOL_PAINT;
import static io.mapsquare.osmcontributor.map.vectorial.Paints.SECONDARY_PAINT;
import static io.mapsquare.osmcontributor.map.vectorial.Paints.SERVICE_PAINT;
import static io.mapsquare.osmcontributor.map.vectorial.Paints.STADIUM_PAINT;
import static io.mapsquare.osmcontributor.map.vectorial.Paints.STEPS_PAINT;
import static io.mapsquare.osmcontributor.map.vectorial.Paints.TERTIARY_PAINT;
import static io.mapsquare.osmcontributor.map.vectorial.Paints.TRACK_PAINT;
import static io.mapsquare.osmcontributor.map.vectorial.Paints.TRANSPARENT_PAINT;
import static io.mapsquare.osmcontributor.map.vectorial.Paints.TRUNK_PAINT;
import static io.mapsquare.osmcontributor.map.vectorial.Paints.UNCLASSIFIED_PAINT;
import static io.mapsquare.osmcontributor.map.vectorial.Paints.WATER_PAINT;

public class GeoJSONParser {

    private static final String ID = "id";
    private static final String COORDINATES = "coordinates";
    private static final String LEVEL = "level";
    private static final String SPLIT = ";";


    public static GeoJSONFileContent parseGeoJson(FeatureCollection featureCollection) throws JSONException {
        Set<VectorialObject> vectorialObjects = new HashSet<>();
        TreeSet<Double> levelsSet = new TreeSet<>();
        levelsSet.add(0d);
        VectorialObject vObject;
        JSONArray points;
        for (Feature f : featureCollection.getFeatures()) {
            if (f.getGeometry() instanceof LineString) {
                points = (JSONArray) f.getGeometry().toJSON().get(COORDINATES);
                vObject = parseLineString(points);
                vObject.setId(f.toJSON().getString(ID));
                styleHighway(vObject, f);
                addVectorialObjectWithLevel(vectorialObjects, f, vObject, levelsSet);
            } else if (f.getGeometry() instanceof MultiLineString) {
                JSONArray lines = (JSONArray) f.getGeometry().toJSON().get(COORDINATES);
                for (int k = 0; k < lines.length(); k++) {
                    points = (JSONArray) lines.get(k);
                    vObject = parseLineString(points);
                    vObject.setId(f.toJSON().getString(ID));
                    styleHighway(vObject, f);
                    addVectorialObjectWithLevel(vectorialObjects, f, vObject, levelsSet);
                }
            } else if (f.getGeometry() instanceof Polygon) {
                points = (JSONArray) f.getGeometry().toJSON().get(COORDINATES);

                for (int r = 0; r < points.length(); r++) {
                    vObject = parsePolygonRing(points, r);
                    vObject.setId(f.toJSON().getString(ID));
                    styleSurfaces(vObject, f);
                    addVectorialObjectWithLevel(vectorialObjects, f, vObject, levelsSet);
                }
            } else if (f.getGeometry() instanceof MultiPolygon) {
                JSONArray polygons = (JSONArray) f.getGeometry().toJSON().get(COORDINATES);
                for (int p = 0; p < polygons.length(); p++) {
                    points = (JSONArray) polygons.get(p);
                    for (int r = 0; r < points.length(); r++) {
                        vObject = parsePolygonRing(points, r);
                        vObject.setId(f.toJSON().getString(ID));
                        styleSurfaces(vObject, f);
                        addVectorialObjectWithLevel(vectorialObjects, f, vObject, levelsSet);
                    }
                }
            }
        }
        return new GeoJSONFileContent(vectorialObjects, levelsSet);
    }

    private static void addVectorialObjectWithLevel(Set<VectorialObject> vectorialObjects, Feature f, VectorialObject vObject, TreeSet<Double> levelsSet) {
        String levels = f.getProperties().optString(LEVEL);
        if (levels != null && !levels.isEmpty()) {
            for (String level : levels.split(SPLIT)) {
                try {
                    double l = Double.parseDouble(level);
                    vObject = new VectorialObject(vObject);
                    vObject.setLevel(l);
                    levelsSet.add(l);
                    vectorialObjects.add(vObject);
                } catch (NumberFormatException e) {
                    Timber.e(e, "Level %s is not a valid number", level);
                }
            }
        } else {
            vectorialObjects.add(vObject);
        }
    }

    private static VectorialObject parsePolygonRing(JSONArray points, int r) throws JSONException {
        VectorialObject vObject = new VectorialObject(true);
        JSONArray ring = (JSONArray) points.get(r);
        JSONArray coordinates;
        // we re-wind inner rings of GeoJSON polygons in order
        // to render them as transparent in the canvas layer.

        // first ring should have windingOrder = true,
        // all others should have winding order == false
        if ((r == 0 && !windingOrder(ring)) || (r != 0 && windingOrder(ring))) {
            for (int j = 0; j < ring.length(); j++) {
                coordinates = (JSONArray) ring.get(j);
                vObject.addPoint(getXy(coordinates));
            }
        } else {
            for (int j = ring.length() - 1; j >= 0; j--) {
                coordinates = (JSONArray) ring.get(j);
                vObject.addPoint(getXy(coordinates));
            }
        }
        return vObject;
    }

    private static VectorialObject parseLineString(JSONArray points) throws JSONException {
        VectorialObject vObject = new VectorialObject(false);
        JSONArray coordinates;
        for (int j = 0; j < points.length(); j++) {
            coordinates = (JSONArray) points.get(j);
            vObject.addPoint(getXy(coordinates));
        }
        return vObject;
    }

    private static XY getXy(JSONArray coordinates) throws JSONException {
        double[] precomputed;
        double lon = (Double) coordinates.get(0);
        double lat = (Double) coordinates.get(1);
        precomputed = Projection.latLongToPixelXY(lat, lon);
        return new XY(precomputed[0], precomputed[1]);
    }

    private static boolean windingOrder(JSONArray ring) throws JSONException {
        float area = 0;

        if (ring.length() > 2) {
            for (int i = 0; i < ring.length() - 1; i++) {
                JSONArray p1 = (JSONArray) ring.get(i);
                JSONArray p2 = (JSONArray) ring.get(i + 1);
                area += rad((Double) p2.get(0) - (Double) p1.get(0)) * (2 + Math.sin(
                        rad((Double) p1.get(1))) + Math.sin(rad((Double) p2.get(1))));
            }
        }

        return area > 0;
    }

    private static double rad(double d) {
        return d * Math.PI / 180f;
    }


    private static void styleHighway(VectorialObject vectorialObject, Feature f) {
        String highway = f.getProperties().optString("highway");
        switch (highway) {
            case "motorway":
                vectorialObject.setPaint(MOTORWAY_PAINT);
                vectorialObject.setPriority(1);
                break;
            case "motorway_link":
                vectorialObject.setPaint(MOTORWAY_LINK_PAINT);
                vectorialObject.setPriority(1);
                break;
            case "trunk":
                vectorialObject.setPaint(TRUNK_PAINT);
                vectorialObject.setPriority(2);
                break;
            case "primary":
                vectorialObject.setPaint(PRIMARY_PAINT);
                vectorialObject.setPriority(3);
                break;
            case "secondary":
                vectorialObject.setPaint(SECONDARY_PAINT);
                vectorialObject.setPriority(4);
                break;
            case "tertiary":
                vectorialObject.setPaint(TERTIARY_PAINT);
                vectorialObject.setPriority(5);
                break;
            case "unclassified":
                vectorialObject.setPaint(UNCLASSIFIED_PAINT);
                vectorialObject.setPriority(6);
                break;
            case "residential":
                vectorialObject.setPaint(RESIDENTIAL_PAINT);
                vectorialObject.setPriority(6);
                break;
            case "service":
                vectorialObject.setPaint(SERVICE_PAINT);
                vectorialObject.setPriority(7);
                break;
            case "living_street":
                vectorialObject.setPaint(LIVING_STREET_PAINT);
                vectorialObject.setPriority(8);
                break;
            case "pedestrian":
                vectorialObject.setPaint(PEDESTRIAN_PAINT);
                vectorialObject.setPriority(9);
                break;
            case "bridleway":
                vectorialObject.setPaint(BRIDLEWAY_PAINT);
                vectorialObject.setPriority(10);
                break;
            case "cycleway":
                vectorialObject.setPaint(CYCLEWAY_PAINT);
                vectorialObject.setPriority(10);
                break;
            case "footway":
                vectorialObject.setPaint(FOOTWAY_PAINT);
                vectorialObject.setPriority(10);
                break;
            case "path":
                vectorialObject.setPaint(PATH_PAINT);
                vectorialObject.setPriority(10);
                break;
            case "steps":
                vectorialObject.setPaint(STEPS_PAINT);
                vectorialObject.setPriority(10);
                break;
            default:
                vectorialObject.setPaint(DEFAULT_PAINT);
                vectorialObject.setPriority(11);
                break;
        }
    }


    private static void styleSurfaces(VectorialObject vectorialObject, Feature f) {
        vectorialObject.setPriority(12);
        vectorialObject.setPaint(TRANSPARENT_PAINT);
        if (f.getProperties().has("building") || f.getProperties().has("building:part")) {
            vectorialObject.setPaint(BUILDING_PAINT);
        }

        if ("pedestrian".equals(f.getProperties().optString("highway"))) {
            vectorialObject.setPaint(PEDESTRIAN_AREA_PAINT);
        }

        if (f.getProperties().has("water") || f.getProperties().has("waterway") || "water".equals(f.getProperties().optString("natural"))) {
            vectorialObject.setPaint(WATER_PAINT);
        }

        if ("bridge".equals(f.getProperties().opt("man_made"))) {
            vectorialObject.setPaint(BRIDGE_PAINT);
        }

        if (f.getProperties().has("shop")) {
            vectorialObject.setPaint(BUILDING_PAINT);
        }

        switch (f.getProperties().optString("amenity")) {
            case "grave_yard":
                vectorialObject.setPaint(CEMETERY_PAINT);
                break;
            case "library":
            case "college":
            case "university":
            case "kindergarten":
            case "school":
                vectorialObject.setPaint(SCHOOL_PAINT);
                break;
            case "parking":
                vectorialObject.setPaint(PARKING_PAINT);
        }

        switch (f.getProperties().optString("leisure")) {
            case "playground":
                vectorialObject.setPaint(PLAYGROUND_PAINT);
                break;
            case "park":
            case "recreation_ground":
                vectorialObject.setPaint(PARK_PAINT);
                break;
            case "common":
            case "garden":
                vectorialObject.setPaint(GRASS_PAINT);
                break;
            case "marina":
                vectorialObject.setPaint(PEDESTRIAN_AREA_PAINT);
                break;
            case "golf_course":
            case "miniature_golf":
                vectorialObject.setPaint(GOLF_PAINT);
                break;
            case "sport_center":
            case "stadium":
                vectorialObject.setPaint(STADIUM_PAINT);
                break;
            case "track":
                vectorialObject.setPaint(TRACK_PAINT);
                break;
            case "pitch":
                vectorialObject.setPaint(PITCH_PAINT);
                break;
            case "swimming_pool":
                vectorialObject.setPaint(WATER_PAINT);
                break;
        }

        switch (f.getProperties().optString("landuse")) {
            case "residential":
                vectorialObject.setPaint(RESIDENTIAL_AREA_PAINT);
                break;
            case "commercial":
                vectorialObject.setPaint(COMMERCIAL_PAINT);
                break;
            case "retail":
                vectorialObject.setPaint(RETAIL_PAINT);
                break;
            case "industrial":
            case "railway":
                vectorialObject.setPaint(INDUSTRIAL_PAINT);
                break;
            case "brownfield":
            case "greenfield":
            case "construction":
            case "landfill":
                vectorialObject.setPaint(CONSTRUCTION_PAINT);
                break;
            case "grass":
            case "recreation_ground":
            case "village_green":
                vectorialObject.setPaint(GRASS_PAINT);
                break;
            case "religion":
            case "cemetery":
                vectorialObject.setPaint(CEMETERY_PAINT);
                break;
            case "forest":
                vectorialObject.setPaint(FOREST_PAINT);
                break;
        }
    }
}
