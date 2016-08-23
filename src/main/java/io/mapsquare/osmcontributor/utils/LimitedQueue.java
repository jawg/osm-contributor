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
package io.mapsquare.osmcontributor.utils;

import java.util.ArrayDeque;

/**
 * @author Tommy Buonomo on 16/06/16.
 */
public class LimitedQueue<T> extends ArrayDeque<T> {
    private int sizeMax;

    public LimitedQueue(int sizeMax) {
        super(sizeMax);
        this.sizeMax = sizeMax;
    }

    @Override
    public boolean add(T t) {
       super.addFirst(t);
        if (size() > sizeMax) {
            super.removeLast();
        }
        return true;
    }
}
