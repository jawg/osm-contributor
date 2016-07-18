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
import org.junit.BeforeClass;
import org.junit.Test;

import io.mapsquare.osmcontributor.model.utils.OpeningHours;
import io.mapsquare.osmcontributor.ui.adapters.parser.OpeningHoursValueParser;

/**
 * @author Tommy Buonomo on 18/07/16.
 */
public class OpeningHoursValueParserTest {

    private static OpeningHoursValueParser parser;

    @BeforeClass
    private static void init() {
        parser = new OpeningHoursValueParser();
    }

    @Test
    public void isNonStop1() {
        OpeningHours openingHours = new OpeningHours();
        openingHours.setFromTime(new LocalTime(0, 0));
        openingHours.setToTime(new LocalTime(0, 0));
        openingHours.setDays(OpeningHours.Days.values());

        Assert.assertTrue(parser.isNonStop(openingHours));
    }

    @Test
    public void isNonStop2() {
        OpeningHours openingHours = new OpeningHours();
        openingHours.setFromTime(new LocalTime(0, 0));
        openingHours.setToTime(new LocalTime(23, 50));
        openingHours.setDays(OpeningHours.Days.values());

        Assert.assertTrue(parser.isNonStop(openingHours));
    }

    @Test
    public void isNonStop3() {
        OpeningHours openingHours = new OpeningHours();
        openingHours.setFromTime(new LocalTime(0, 0));
        openingHours.setToTime(new LocalTime(23, 49));
        openingHours.setDays(OpeningHours.Days.values());

        Assert.assertFalse(parser.isNonStop(openingHours));
    }

    @Test
    public void isNonStop4() {
        OpeningHours openingHours = new OpeningHours();
        openingHours.setFromTime(new LocalTime(21, 59));
        openingHours.setToTime(new LocalTime(21, 58));
        openingHours.setDays(OpeningHours.Days.values());

        Assert.assertTrue(parser.isNonStop(openingHours));
    }

    @Test
    public void isNonStop5() {
        OpeningHours openingHours = new OpeningHours();
        openingHours.setFromTime(new LocalTime(7, 36));
        openingHours.setToTime(new LocalTime(7, 33));
        openingHours.setDays(OpeningHours.Days.values());

        Assert.assertTrue(parser.isNonStop(openingHours));
    }

    @Test
    public void isNonStop6() {
        OpeningHours openingHours = new OpeningHours();
        openingHours.setFromTime(new LocalTime(0, 2));
        openingHours.setToTime(new LocalTime(0, 1));
        openingHours.setDays(OpeningHours.Days.values());

        Assert.assertTrue(parser.isNonStop(openingHours));
    }

    @Test
    public void isNonStop7() {
        OpeningHours openingHours = new OpeningHours();
        openingHours.setFromTime(new LocalTime(23, 59));
        openingHours.setToTime(new LocalTime(23, 58));
        openingHours.setDays(OpeningHours.Days.values());

        Assert.assertTrue(parser.isNonStop(openingHours));
    }
}
