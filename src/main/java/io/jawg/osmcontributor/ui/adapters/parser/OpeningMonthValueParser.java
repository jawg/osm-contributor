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
package io.jawg.osmcontributor.ui.adapters.parser;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.jawg.osmcontributor.ui.adapters.item.opening.OpeningHours;
import io.jawg.osmcontributor.ui.adapters.item.opening.OpeningMonth;

/**
 * @author Tommy Buonomo on 18/07/16.
 */

@Singleton
public class OpeningMonthValueParser implements ValueParser<OpeningMonth> {
    public static final String RULE_SEP = ",";
    public static final String RANGE_SEP = "-";
    public static final String MONTH_SEP = ": ";
    public static final String PATTERN_MONTH = "(\\bJan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec\\b)";
    public static final String PATTERN = PATTERN_MONTH + "(-" + PATTERN_MONTH + ")?" + "(,\\s?" + PATTERN_MONTH +
            "(-" + PATTERN_MONTH + ")?)*?";


    @Inject
    OpeningHoursValueParser openingHoursValueParser;

    @Inject
    public OpeningMonthValueParser() {
    }

    @Override
    public String toValue(OpeningMonth openingMonth) {
        StringBuilder builder = new StringBuilder();
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
                builder.append(builder.length() == 0 ? "" : RULE_SEP);
                builder.append(fromMonth);
                if (toMonth != null) {
                    // This is a period
                    builder.append(RANGE_SEP)
                            .append(toMonth);
                }
            }
        }

        String dayPart = openingHoursValueParser.toValue(openingMonth.getOpeningHours());

        builder.append(builder.toString().trim().isEmpty() || dayPart.length() == 0 ? "" : MONTH_SEP);
        builder.append(dayPart);

        return builder.toString();
    }

    @Override
    public OpeningMonth fromValue(String value) {
        if (value.trim().isEmpty()) {
            return null;
        }

        OpeningMonth openingMonth = new OpeningMonth();

        String[] split = value.split(MONTH_SEP);

        if (isMonthPresent(value)) {
            // Get the left part of the expression
            String monthsValue = split[0];
            String[] periods = monthsValue.split(RULE_SEP);

            for (String period : periods) {
                if (isPeriod(period)) {
                    OpeningMonth.Month[] months = OpeningMonth.Month.fromDatas(period.split(RANGE_SEP));
                    for (int i = months[0].ordinal(); i <= months[1].ordinal(); i++) {
                        openingMonth.setMonthActivated(i, true);
                    }
                } else {
                    openingMonth.setMonthActivated(OpeningMonth.Month.fromData(period).ordinal(), true);
                }
            }
        } else {
            List<OpeningHours> openingHours = openingHoursValueParser.fromValue(value);
            if (openingHours != null) {
                openingMonth.addOpeningHours(openingHours);
            }
        }

        if (split.length == 2) {
            String openingHoursValue = split[1];
            if (openingHoursValue != null && !openingHoursValue.trim().isEmpty()) {
                List<OpeningHours> openingHours = openingHoursValueParser.fromValue(openingHoursValue);
                if (openingHours != null) {
                    openingMonth.addOpeningHours(openingHours);
                }
            }
        }

        return openingMonth;
    }

    /**
     * Return true if the month expression is a period
     * For example 'Jan-May' is a period, 'Sep' is not.
     * @param period
     * @return
     */
    private boolean isPeriod(String period) {
        return period.contains(RANGE_SEP);
    }

    public boolean isMonthPresent(String expr) {
        return expr.contains(MONTH_SEP) || expr.matches(PATTERN);
    }

    @Override
    public int getPriority() {
        return ParserManager.PRIORITY_HIGH;
    }
}
