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
package io.mapsquare.osmcontributor.model.utils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Tommy Buonomo on 05/07/16.
 */
public class OpeningTime {
    private boolean changed;

    private List<OpeningMonth> openingMonths;

    public OpeningTime() {
        openingMonths = new ArrayList<>();
    }

    public boolean isChanged() {
        for (OpeningMonth o : openingMonths) {
            if (o.isChanged()) {
                return true;
            }
        }
        return changed;
    }

    public void resetChanged() {
        for (OpeningMonth o : openingMonths) {
            o.setChanged(false);
        }
        changed = false;
    }

    public void addOpeningMonth(OpeningMonth o) {
        if (o != null) {
            openingMonths.add(o);
        }
    }

    public void setOpeningMonth(int i, OpeningMonth o) {
        if (o != null) {
            openingMonths.set(i, o);
        }
    }

    public List<OpeningMonth> getOpeningMonths() {
        return openingMonths;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof OpeningTime)) {
            return false;
        }

        OpeningTime that = (OpeningTime) o;

        return openingMonths != null ? openingMonths.equals(that.openingMonths) : that.openingMonths == null;

    }

    @Override
    public int hashCode() {
        return openingMonths != null ? openingMonths.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "OpeningTime{" +
                "changed=" + changed +
                ", openingMonths=" + openingMonths +
                '}';
    }
}
