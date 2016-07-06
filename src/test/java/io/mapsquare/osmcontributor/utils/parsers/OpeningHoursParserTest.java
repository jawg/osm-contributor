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
package io.mapsquare.osmcontributor.utils.parsers;

import junit.framework.Assert;

import org.joda.time.LocalTime;
import org.junit.Test;

import io.mapsquare.osmcontributor.ui.adapters.OpeningHours;
import io.mapsquare.osmcontributor.ui.adapters.parser.OpeningHoursParser;

/**
 * @author Tommy Buonomo on 05/07/16.
 */

public class OpeningHoursParserTest {

    @Test
    public void isNonStop1() {
        OpeningHours hours = new OpeningHours(new boolean[]{true, true, true, true, true, true, true});
        hours.setFromTime(new LocalTime(0, 0));
        hours.setToTime(new LocalTime(0, 0));

        Assert.assertTrue(OpeningHoursParser.isNonStop(hours));
    }

    @Test
    public void isNonStop2() {
        OpeningHours hours = new OpeningHours(new boolean[]{true, true, true, true, true, true, true});
        hours.setFromTime(new LocalTime(0, 0));
        hours.setToTime(new LocalTime(23, 59));

        Assert.assertTrue(OpeningHoursParser.isNonStop(hours));
    }
}
