package io.jawg.osmcontributor.ui.utils;

import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.geometry.LatLngBounds;

public class LatLngBoundsUtils {

    private LatLngBoundsUtils() {
        //empty for utils class
    }

    public static LatLngBounds enlarge(LatLngBounds viewLatLngBounds, double factor) {
        double n = viewLatLngBounds.getLatNorth();
        double e = viewLatLngBounds.getLonEast();
        double s = viewLatLngBounds.getLatSouth();
        double w = viewLatLngBounds.getLonWest();
        double f = (factor - 1) / 2;
        return new LatLngBounds.Builder()
                .include(new LatLng(n + f * (n - s), e + f * (e - w)))
                .include(new LatLng(s - f * (n - s), w - f * (e - w)))
                .build();
    }
}
