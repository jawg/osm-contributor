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

import io.mapsquare.osmcontributor.model.utils.OpeningMonth;

/**
 * @author Tommy Buonomo on 18/07/16.
 */

@Singleton
public class OpeningMonthValueParser implements ValueParser<OpeningMonth> {
    public static final String RULE_SEP = ",";
    public static final String RANGE_SEP = "-";

    @Inject
    public OpeningMonthValueParser() {
    }

    @Override
    public OpeningMonth fromValue(String value) {
        return null;
    }

    @Override
    public String toValue(OpeningMonth openingMonth) {
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

        return monthsBuilder.toString();
    }
}
