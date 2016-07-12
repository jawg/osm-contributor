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
import io.mapsquare.osmcontributor.model.utils.OpeningMonth;
import io.mapsquare.osmcontributor.model.utils.OpeningTime;

/**
 * @author Tommy Buonomo on 05/07/16.
 */
@Singleton
public class OpeningTimeParser implements ValueParser<OpeningTime> {
    private static final String TAG = "OpeningTimeParser";

    public static final String RANGE_SEP = "-";
    public static final String RULE_SEP = ",";
    public static final String HOURS_SEP = ", ";
    public static final String RULESET_SEP = "; ";
    public static final String MONTH_SEP = ": ";
    public static final String TIME_SEP = ":";
    public static final String NON_STOP = "24/7";
    public static final String NON_STOP_HOURS = "00:00-24:00";

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormat.forPattern("kk:mm");

    @Inject
    public OpeningTimeParser() {
    }

    @Override
    public String toValue(OpeningTime openingTime) {
        StringBuilder builder = new StringBuilder();

        for (OpeningMonth openingMonth : openingTime.getOpeningMonths()) {
            builder.append(builder.length() == 0 || builder.substring(builder.length() - 2).equals(RULESET_SEP) ? "" : RULESET_SEP);

            StringBuilder monthPart = buildMonthPart(openingMonth);
            StringBuilder dayPart = buildHoursPart(openingMonth.getOpeningHours());

            builder.append(monthPart);
            builder.append(builder.toString().trim().isEmpty() || dayPart.length() == 0 || monthPart.length() == 0 ? "" : MONTH_SEP);
            builder.append(dayPart);
        }

        return builder.toString();
    }

    public StringBuilder buildMonthPart(OpeningMonth openingMonth) {
        StringBuilder monthsBuilder = new StringBuilder();
        OpeningMonth.Month[] months = openingMonth.getMonths();

        for (int i = 0; i < months.length; i++) {
            OpeningMonth.Month m = months[i];
            // Get the period in month
            if (m != null) {
                // A month is detected, look if this is a period by iterate the next months
                String fromMonth = m.getData();
                String toMonth = null;
                i++;
                while (i < months.length && months[i] != null) {
                    toMonth = months[i].getData();
                    i++;
                }

                monthsBuilder.append(monthsBuilder.length() == 0 ? "" : RULE_SEP);
                monthsBuilder.append(fromMonth);
                if (toMonth != null) {
                    // This is a period
                    monthsBuilder.append(RANGE_SEP)
                            .append(toMonth);
                }
            }
        }

        return monthsBuilder;
    }

    public StringBuilder buildHoursPart(List<OpeningHours> openingHours) {
        StringBuilder openingHoursBuilder = new StringBuilder();

        for (OpeningHours o : openingHours) {
            openingHoursBuilder.append(openingHoursBuilder.length() == 0 ? "" : HOURS_SEP);
            openingHoursBuilder.append(buildHoursPart(o));
        }

        return openingHoursBuilder;
    }

    public StringBuilder buildHoursPart(OpeningHours openingHours) {
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

    @Override
    public OpeningTime fromValue(String data) {
        /**TODO Jan-Apr,Sep: Mo-Tu,Th,Sa 11:27-09:25, Mo-We,Fr 10:12-18:15; Feb,Apr-May: Mo-Fr 10:00-18:00 */
        OpeningTime openingTime = new OpeningTime();
        String[] openingTimes = data.split(RULESET_SEP);

        for (String s : openingTimes) {
            if (s.contains(MONTH_SEP)) {
                String[] rule = s.split(MONTH_SEP);
                OpeningMonth openingMonth = fromOpeningMonthString(rule[0]);

                if (openingMonth == null) {
                    openingMonth = new OpeningMonth();
                }
                String[] openingsHours = rule[1].split(HOURS_SEP);
                for (String o : openingsHours) {
                    OpeningHours openingHours = fromOpeningHoursString(o);
                    openingMonth.addOpeningHours(openingHours);
                }

                openingTime.addOpeningMonth(openingMonth);
            }
        }


        return null;
    }

    private OpeningHours fromOpeningHoursString(String openingHours) {

        return null;
    }

    private OpeningMonth fromOpeningMonthString(String openingMonthString) {
        /**TODO Jan-Apr,Sep: Mo-Tu,Th,Sa 11:27-09:25, Mo-We,Fr 10:12-18:15; Feb,Apr-May: Mo-Fr 10:00-18:00 */
        OpeningMonth openingMonth = new OpeningMonth();
        String[] periods = openingMonthString.split(RULE_SEP);

        for (String period : periods) {
            if (period.contains(RANGE_SEP)) {
                String[] months = period.split(RANGE_SEP);
//                OpeningMonth.Month m1 = OpeningMonth.Monthmonths[0];
            }
        }

        return null;
    }
}
