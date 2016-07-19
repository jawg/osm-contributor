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
import io.mapsquare.osmcontributor.model.utils.OpeningTime;

/**
 * @author Tommy Buonomo on 05/07/16.
 */
@Singleton
public class OpeningTimeValueParser implements ValueParser<OpeningTime> {
    public static final String RULESET_SEP = "; ";
    public static final String MONTH_SEP = ": ";

    @Inject
    OpeningMonthValueParser openingMonthValueParser;

    @Inject
    public OpeningTimeValueParser() {
    }


    @Override
    public String toValue(OpeningTime openingTime) {
        StringBuilder builder = new StringBuilder();

        for (OpeningMonth openingMonth : openingTime.getOpeningMonths()) {
            builder.append(builder.length() == 0 || builder.substring(builder.length() - 2).equals(RULESET_SEP) ? "" : RULESET_SEP);
            builder.append(openingMonthValueParser.toValue(openingMonth));
        }

        return builder.toString();
    }

    @Override
    public OpeningTime fromValue(String data) {
        /**TODO Jan-Apr,Sep: Mo-Tu,Th,Sa 11:27-09:25, Mo-We,Fr 10:12-18:15; Feb,Apr-May: Mo-Fr 10:00-18:00 */
        OpeningTime openingTime = new OpeningTime();
        String[] openingTimes = data.split(RULESET_SEP);

        for (String s : openingTimes) {
            OpeningMonth openingMonth = openingMonthValueParser.fromValue(s);
            if (openingMonth != null) {
                openingTime.addOpeningMonth(openingMonth);
            }
        }

        return openingTime;
    }

}
