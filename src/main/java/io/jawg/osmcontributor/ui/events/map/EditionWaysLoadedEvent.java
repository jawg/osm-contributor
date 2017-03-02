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
package io.jawg.osmcontributor.ui.events.map;

import java.util.Set;
import java.util.TreeSet;

import io.jawg.osmcontributor.model.entities.Way;

public class EditionWaysLoadedEvent {

    private final Set<Way> ways;
    private final TreeSet<Double> levels;
    private final boolean refreshFromOverpass;

    public EditionWaysLoadedEvent(Set<Way> ways, TreeSet<Double> levels, boolean refreshFromOverpass) {
        this.ways = ways;
        this.levels = levels;
        this.refreshFromOverpass = refreshFromOverpass;
    }

    public Set<Way> getWays() {
        return ways;
    }

    public TreeSet<Double> getLevels() {
        return levels;
    }

    public boolean isRefreshFromOverpass() {
        return refreshFromOverpass;
    }
}
