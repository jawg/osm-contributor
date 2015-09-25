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
package io.mapsquare.osmcontributor.map.events;

import java.util.Set;
import java.util.TreeSet;

import io.mapsquare.osmcontributor.map.vectorial.VectorialObject;


public class VectorialTilesLoadedEvent {

    private Set<VectorialObject> vectorialObjects;
    private TreeSet<Double> levels;

    public VectorialTilesLoadedEvent(Set<VectorialObject> vectorialObjects, TreeSet<Double> levels) {
        this.vectorialObjects = vectorialObjects;
        this.levels = levels;
    }

    public Set<VectorialObject> getVectorialObjects() {
        return vectorialObjects;
    }

    public TreeSet<Double> getLevels() {
        return levels;
    }
}
