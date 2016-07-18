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

import javax.inject.Inject;
import javax.inject.Singleton;

import io.mapsquare.osmcontributor.model.utils.OpeningHours;
import io.mapsquare.osmcontributor.model.utils.OpeningMonth;
import io.mapsquare.osmcontributor.model.utils.OpeningTime;

/**
 * @author Tommy Buonomo on 05/07/16.
 */
@Singleton
public class OpeningTimeValueParser implements ValueParser<OpeningTime> {
    private static final String TAG = "OpeningTimeValueParser";

    public static final String RANGE_SEP = "-";
    public static final String RULE_SEP = ",";
    public static final String HOURS_SEP = ", ";
    public static final String RULESET_SEP = "; ";
    public static final String MONTH_SEP = ": ";

    @Inject
    OpeningMonthValueParser openingMonthValueParser;

    @Inject
    OpeningHoursValueParser openingHoursValueParser;

    @Inject
    public OpeningTimeValueParser() {
    }


    @Override
    public String toValue(OpeningTime openingTime) {
        StringBuilder builder = new StringBuilder();

        for (OpeningMonth openingMonth : openingTime.getOpeningMonths()) {
            builder.append(builder.length() == 0 || builder.substring(builder.length() - 2).equals(RULESET_SEP) ? "" : RULESET_SEP);

            String monthPart = openingMonthValueParser.toValue(openingMonth);
            String dayPart = openingHoursValueParser.toValue(openingMonth.getOpeningHours());

            builder.append(monthPart);
            builder.append(builder.toString().trim().isEmpty() || dayPart.length() == 0 || monthPart.length() == 0 ? "" : MONTH_SEP);
            builder.append(dayPart);
        }

        return builder.toString();
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
