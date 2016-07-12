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
import java.util.Arrays;
import java.util.List;

/**
 * @author Tommy Buonomo on 11/07/16.
 */
public class OpeningMonth {
    public static final int MONTHS_COUNT = 12;

    public enum Month {
        JAN("Jan"),
        FEB("Feb"),
        MAR("Mar"),
        APR("Apr"),
        MAY("May"),
        JUN("Jun"),
        JUL("Jul"),
        AUG("Aug"),
        SEP("Sep"),
        OCT("Oct"),
        NOV("Nov"),
        DEC("Dec");

        private String data;

        Month(String data) {
            this.data = data;
        }
        public String getData() {
            return data;
        }
        public Month fromData(String data) {
            return valueOf(data.toUpperCase());
        }
    }
    private Month[] months;

    private boolean changed;

    private List<OpeningHours> openingHours;

    public OpeningMonth() {
        months = new Month[MONTHS_COUNT];
        openingHours = new ArrayList<>();
    }

    public Month[] getMonths() {
        return months;
    }

    public boolean isChanged() {
        for (OpeningHours o : openingHours) {
            if (o.isChanged()) {
                return true;
            }
        }
        return changed;
    }

    public void setMonthActivated(int i, boolean active) {
        if (i < Month.values().length && i >= 0) {
            this.months[i] = active ? Month.values()[i] : null;
            changed = true;
        }
    }

    public void setChanged(boolean changed) {
        this.changed = changed;
    }

    public void resetChanged() {
        for (OpeningHours o : openingHours) {
            o.setChanged(false);
        }
        changed = false;
    }

    public void addOpeningHours(OpeningHours o) {
        if (o != null) {
            openingHours.add(o);
        }
    }

    public void setOpeningHours(int i, OpeningHours o) {
        if (o != null) {
            openingHours.set(i, o);
        }
    }

    public void setMonths(Month[] months) {
        this.months = months;
    }

    public List<OpeningHours> getOpeningHours() {
        return openingHours;
    }

    @Override
    public String toString() {
        return "OpeningMonth{" +
                "months=" + Arrays.toString(months) +
                ", changed=" + changed +
                ", openingHours=" + openingHours +
                '}';
    }
}
