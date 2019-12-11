/**
 * Copyright (C) 2019 Takima
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
package io.jawg.osmcontributor.model.entities;

import android.graphics.Color;

import com.mapbox.mapboxsdk.annotations.PolylineOptions;
import com.mapbox.mapboxsdk.geometry.LatLng;

import java.util.HashSet;
import java.util.Set;

public class Way {
    private PolylineOptions polylineOptions;
    private Set<PoiNodeRef> poiNodeRefs;
    private Poi poi;

    public Way(Poi poi) {
        this.poi = poi;
        polylineOptions = new PolylineOptions().alpha(0.8f).width(1.8f).color(Color.parseColor("#1565C0"));
        poiNodeRefs = new HashSet<>();
    }

    public void add(PoiNodeRef poiNodeRef) {
        polylineOptions.add(new LatLng(poiNodeRef.getLatitude(), poiNodeRef.getLongitude()));
        poiNodeRefs.add(poiNodeRef);
    }

    public PolylineOptions getPolylineOptions() {
        return polylineOptions;
    }

    public Set<PoiNodeRef> getPoiNodeRefs() {
        return poiNodeRefs;
    }

    public Poi getPoi() {
        return poi;
    }
}
