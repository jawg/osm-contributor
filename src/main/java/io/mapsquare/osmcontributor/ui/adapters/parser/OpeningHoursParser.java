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
package io.mapsquare.osmcontributor.ui.adapters.parser;

import io.mapsquare.osmcontributor.ui.adapters.OpeningHours;

/**
 * @author Tommy Buonomo on 05/07/16.
 */
public class OpeningHoursParser {
    public static final String NON_STOP = "27/7";
    public static final String NON_STOP_HOURS = "00:00-24:00";

    public static String parseToData(OpeningHours openingHours) {
        if (isNonStop(openingHours)) {
            return NON_STOP;
        } else {

        }
        return null;
    }

    public static boolean isNonStop(OpeningHours openingHours) {
        for (Boolean b : openingHours.getDays()) {
            if (!b) {
                return false;
            }
        }
        if (openingHours.getFromTime().getHourOfDay() != openingHours.getFromTime().getHourOfDay()) {
            return false;
        }

        if (Math.abs(openingHours.getFromTime().getMinuteOfHour() - openingHours.getToTime().getMinuteOfHour()) > 5) {
            return false;
        }
        return true;
    }

    public static boolean isNonStopHours(OpeningHours openingHours) {
        if (openingHours.getFromTime().getHourOfDay() != openingHours.getFromTime().getHourOfDay()) {
            return false;
        }

        if (Math.abs(openingHours.getFromTime().getMinuteOfHour() - openingHours.getToTime().getMinuteOfHour()) > 5) {
            return false;
        }
        return true;
    }
}
