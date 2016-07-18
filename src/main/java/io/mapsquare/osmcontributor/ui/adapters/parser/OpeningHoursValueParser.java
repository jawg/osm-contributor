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

import org.joda.time.LocalTime;
import org.joda.time.Period;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.mapsquare.osmcontributor.model.utils.OpeningHours;

/**
 * @author Tommy Buonomo on 18/07/16.
 */

@Singleton
public class OpeningHoursValueParser implements ValueParser<List<OpeningHours>> {
    public static final String TIME_SEP = ":";
    public static final String NON_STOP = "24/7";
    public static final String NON_STOP_HOURS = "00:00-24:00";
    public static final String RANGE_SEP = "-";
    public static final String RULE_SEP = ",";
    public static final String HOURS_SEP = ", ";

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormat.forPattern("kk:mm");

    @Inject
    public OpeningHoursValueParser() {
    }

    @Override
    public List<OpeningHours> fromValue(String value) {
        return null;
    }

    @Override
    public String toValue(List<OpeningHours> openingHours) {
        StringBuilder openingHoursBuilder = new StringBuilder();

        for (OpeningHours o : openingHours) {
            openingHoursBuilder.append(openingHoursBuilder.length() == 0 ? "" : HOURS_SEP);
            openingHoursBuilder.append(toSingleValue(o));
        }

        return openingHoursBuilder.toString();
    }

    private StringBuilder toSingleValue(OpeningHours openingHours) {
        StringBuilder hoursBuilder = new StringBuilder();

        if (openingHours.getFromTime() == null && openingHours.getToTime() == null) {
            return new StringBuilder("");
        }

        // Return 24/7 if opening hours id non-stop
        if (isNonStop(openingHours)) {
            return new StringBuilder(NON_STOP);
        } else {
            OpeningHours.Days[] days = openingHours.getDays();
            StringBuilder daysBuilder = new StringBuilder();
            for (int i = 0; i < days.length; i++) {
                OpeningHours.Days d = days[i];

                // Get the period in days
                if (d != null) {
                    // A day is detected, look if this is a period by iterate the next days
                    String fromDay = d.getData();
                    String toDay = null;
                    i++;
                    while (i < days.length && days[i] != null) {
                        toDay = days[i].getData();
                        i++;
                    }

                    daysBuilder.append(daysBuilder.length() == 0 ? "" : RULE_SEP);
                    daysBuilder.append(fromDay);
                    if (toDay != null) {
                        // This is a period
                        daysBuilder.append(RANGE_SEP)
                                .append(toDay);
                    }
                }
            }
            hoursBuilder.append(daysBuilder);
            hoursBuilder.append(" ");
            hoursBuilder.append(TIME_FORMATTER.print(openingHours.getFromTime()));
            hoursBuilder.append(RANGE_SEP);
            hoursBuilder.append(TIME_FORMATTER.print(openingHours.getToTime()));
        }

        return hoursBuilder;
    }

    public boolean isNonStop(OpeningHours openingHours) {
        for (OpeningHours.Days b : openingHours.getDays()) {
            if (b == null) {
                return false;
            }
        }
        return isNonStopHours(openingHours);
    }

    public boolean isNonStopHours(OpeningHours openingHours) {
        if (openingHours.getFromTime().equals(openingHours.getToTime())) {
            return true;
        }

        LocalTime diff = openingHours.getToTime().minus(new Period(openingHours.getFromTime().getHourOfDay(),
                openingHours.getFromTime().getMinuteOfHour(), 0, 0));

        return diff.minus(new Period(23, 50, 0, 0)).getMinuteOfHour() < 10;
    }
}
