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
package io.jawg.osmcontributor.utils.parsers;

import junit.framework.Assert;

import org.joda.time.LocalTime;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.List;

import io.jawg.osmcontributor.ui.adapters.item.opening.OpeningHours;
import io.jawg.osmcontributor.ui.adapters.parser.OpeningHoursValueParser;

/**
 * @author Tommy Buonomo on 18/07/16.
 */
public class OpeningHoursValueParserTest {
    private static OpeningHoursValueParser parser;

    @BeforeClass
    public static void init() {
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

    @Test
    public void fromOpeningHoursValue1() {
        String value = "We,Fr-Sa 10:45-19:15, Tu,Sa-Su 05:30-17:30, We 08:00-18:00";

        List<OpeningHours> openingHoursList = parser.fromValue(value);

        Assert.assertTrue(openingHoursList.size() == 3);

        // OPENING 1
        OpeningHours openingHours1 = openingHoursList.get(0);
        OpeningHours openingHours1Expected = new OpeningHours();
        openingHours1Expected.setAllDaysActivated(false);
        openingHours1Expected.setDayActivated(2, true);
        openingHours1Expected.setDayActivated(4, true);
        openingHours1Expected.setDayActivated(5, true);

        openingHours1Expected.setFromTime(new LocalTime(10, 45));
        openingHours1Expected.setToTime(new LocalTime(19, 15));

        Assert.assertEquals(openingHours1Expected, openingHours1);

        // OPENING 2
        OpeningHours openingHours2 = openingHoursList.get(1);
        OpeningHours openingHours2Expected = new OpeningHours();
        openingHours2Expected.setAllDaysActivated(false);
        openingHours2Expected.setDayActivated(1, true);
        openingHours2Expected.setDayActivated(5, true);
        openingHours2Expected.setDayActivated(6, true);

        openingHours2Expected.setFromTime(new LocalTime(5, 30));
        openingHours2Expected.setToTime(new LocalTime(17, 30));

        Assert.assertEquals(openingHours2Expected, openingHours2);

        // OPENING 3
        OpeningHours openingHours3 = openingHoursList.get(2);
        OpeningHours openingHours3Expected = new OpeningHours();
        openingHours3Expected.setAllDaysActivated(false);
        openingHours3Expected.setDayActivated(2, true);

        openingHours3Expected.setFromTime(new LocalTime(8, 0));
        openingHours3Expected.setToTime(new LocalTime(18, 0));

        Assert.assertEquals(openingHours3Expected, openingHours3);
    }

    @Test
    public void fromOpeningHoursValue2() {
        String value = "05:30-17:30";

        List<OpeningHours> openingHoursList = parser.fromValue(value);

        Assert.assertTrue(openingHoursList.size() == 1);

        // OPENING 1
        OpeningHours openingHours1 = openingHoursList.get(0);
        OpeningHours openingHours1Expected = new OpeningHours();
        openingHours1Expected.setAllDaysActivated(false);

        openingHours1Expected.setFromTime(new LocalTime(5, 30));
        openingHours1Expected.setToTime(new LocalTime(17, 30));

        Assert.assertEquals(openingHours1Expected, openingHours1);
    }

    @Test
    public void fromOpeningHoursValue3() {
        String value = "07:30-17:30, Mo,Th 01:00-02:00";

        List<OpeningHours> openingHoursList = parser.fromValue(value);

        Assert.assertTrue(openingHoursList.size() == 2);

        // OPENING 1
        OpeningHours openingHours1 = openingHoursList.get(0);
        OpeningHours openingHours1Expected = new OpeningHours();
        openingHours1Expected.setAllDaysActivated(false);

        openingHours1Expected.setFromTime(new LocalTime(7, 30));
        openingHours1Expected.setToTime(new LocalTime(17, 30));

        Assert.assertEquals(openingHours1Expected, openingHours1);

        // OPENING 2
        OpeningHours openingHours2 = openingHoursList.get(1);
        OpeningHours openingHours2Expected = new OpeningHours();
        openingHours2Expected.setAllDaysActivated(false);
        openingHours2Expected.setDayActivated(0, true);
        openingHours2Expected.setDayActivated(3, true);

        openingHours2Expected.setFromTime(new LocalTime(1, 0));
        openingHours2Expected.setToTime(new LocalTime(2, 0));

        Assert.assertEquals(openingHours2Expected, openingHours2);
    }
}