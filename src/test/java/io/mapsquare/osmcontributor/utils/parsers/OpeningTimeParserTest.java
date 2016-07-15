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
import io.mapsquare.osmcontributor.model.utils.OpeningMonth;
import io.mapsquare.osmcontributor.model.utils.OpeningTime;
import io.mapsquare.osmcontributor.ui.adapters.parser.OpeningTimeParser;
import io.mapsquare.osmcontributor.ui.adapters.parser.OpeningTimeParserImpl;

/**
 * @author Tommy Buonomo on 05/07/16.
 */

public class OpeningTimeParserTest {
    private static OpeningTimeParser parser;


    @BeforeClass
    public static void init() {
        parser = new OpeningTimeParser();
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
    public void nonStop1() {
        OpeningTime openingTime = new OpeningTime();
        OpeningMonth openingMonth = new OpeningMonth();
        OpeningHours openingHours = new OpeningHours();
        openingHours.setDayActivated(0, true);
        openingHours.setDayActivated(1, true);
        openingHours.setDayActivated(2, true);
        openingHours.setDayActivated(3, true);
        openingHours.setDayActivated(4, true);
        openingHours.setDayActivated(5, true);
        openingHours.setDayActivated(6, true);

        openingMonth.addOpeningHours(openingHours);
        openingTime.addOpeningMonth(openingMonth);

        openingHours.setFromTime(new LocalTime(23, 59));
        openingHours.setToTime(new LocalTime(23, 58));

        Assert.assertEquals(parser.toValue(openingTime), "24/7");
    }

    @Test
    public void nonStop2() {
        OpeningTime openingTime = new OpeningTime();
        OpeningMonth openingMonth = new OpeningMonth();
        OpeningHours openingHours = new OpeningHours();
        openingMonth.setMonthActivated(0, true);
        openingMonth.setMonthActivated(1, true);
        openingMonth.setMonthActivated(7, true);
        openingMonth.setMonthActivated(8, true);
        openingMonth.setMonthActivated(10, true);

        openingMonth.addOpeningHours(openingHours);

        openingHours.setDayActivated(0, true);
        openingHours.setDayActivated(1, true);
        openingHours.setDayActivated(2, true);
        openingHours.setDayActivated(3, true);
        openingHours.setDayActivated(4, true);
        openingHours.setDayActivated(5, true);
        openingHours.setDayActivated(6, true);

        openingHours.setFromTime(new LocalTime(23, 59));
        openingHours.setToTime(new LocalTime(23, 58));

        openingTime.addOpeningMonth(openingMonth);

        Assert.assertEquals(parser.toValue(openingTime), "Jan-Feb,Aug-Sep,Nov: 24/7");
    }

    @Test
    public void parseMonths1() {
        OpeningTime openingTime = new OpeningTime();
        OpeningMonth openingMonth = new OpeningMonth();
        openingMonth.setMonthActivated(0, true);
        openingMonth.setMonthActivated(1, true);
        openingMonth.setMonthActivated(7, true);
        openingMonth.setMonthActivated(8, true);
        openingMonth.setMonthActivated(10, true);

        openingTime.addOpeningMonth(openingMonth);
        Assert.assertEquals(parser.toValue(openingTime), "Jan-Feb,Aug-Sep,Nov");
    }

    @Test
    public void parseMonths2() {
        OpeningTime openingTime = new OpeningTime();
        OpeningMonth openingMonth = new OpeningMonth();
        openingMonth.setMonthActivated(5, true);
        openingTime.addOpeningMonth(openingMonth);
        Assert.assertEquals(parser.toValue(openingTime), "Jun");
    }

    @Test
    public void parseMonths3() {
        OpeningTime openingTime = new OpeningTime();
        OpeningMonth openingMonth = new OpeningMonth();
        openingMonth.setMonthActivated(0, true);
        openingMonth.setMonthActivated(2, true);
        openingMonth.setMonthActivated(4, true);
        openingMonth.setMonthActivated(5, true);
        openingMonth.setMonthActivated(6, true);
        openingMonth.setMonthActivated(7, true);
        openingMonth.setMonthActivated(8, true);
        openingMonth.setMonthActivated(10, true);
        openingMonth.setMonthActivated(11, true);

        openingMonth.setMonthActivated(4, false);
        openingTime.addOpeningMonth(openingMonth);
        Assert.assertEquals(parser.toValue(openingTime), "Jan,Mar,Jun-Sep,Nov-Dec");
    }

    @Test
    public void parseMonths4() {
        OpeningTime openingTime = new OpeningTime();
        Assert.assertTrue(parser.toValue(openingTime).length() == 0);
    }

    @Test
    public void parseDays1() {
        OpeningTime openingTime = new OpeningTime();
        OpeningMonth openingMonth = new OpeningMonth();
        OpeningHours openingHours = new OpeningHours();
        openingHours.setDayActivated(0, true);
        openingHours.setDayActivated(1, true);
        openingHours.setDayActivated(6, true);

        openingMonth.addOpeningHours(openingHours);
        openingTime.addOpeningMonth(openingMonth);
        Assert.assertEquals(parser.toValue(openingTime), "Mo-Tu,Su 08:00-18:00");
    }

    @Test
    public void parseDays2() {
        OpeningTime openingTime = new OpeningTime();
        OpeningMonth openingMonth = new OpeningMonth();
        OpeningHours openingHours = new OpeningHours();
        openingMonth.addOpeningHours(openingHours);
        openingTime.addOpeningMonth(openingMonth);
        openingHours.setDayActivated(5, true);
        Assert.assertEquals(parser.toValue(openingTime), "Sa 08:00-18:00");
    }

    @Test
    public void parseDays3() {
        OpeningTime openingTime = new OpeningTime();
        OpeningMonth openingMonth = new OpeningMonth();
        OpeningHours openingHours = new OpeningHours();
        openingMonth.addOpeningHours(openingHours);

        openingHours.setDayActivated(0, true);
        openingHours.setDayActivated(1, true);
        openingHours.setDayActivated(2, true);
        openingHours.setDayActivated(3, true);
        openingHours.setDayActivated(4, true);
        openingHours.setDayActivated(5, true);
        openingHours.setDayActivated(6, true);

        openingHours.setDayActivated(5, false);

        openingTime.addOpeningMonth(openingMonth);

        Assert.assertEquals(parser.toValue(openingTime), "Mo-Fr,Su 08:00-18:00");
    }

    @Test
    public void parseMonthAndDays1() {
        OpeningTime openingTime = new OpeningTime();
        OpeningMonth openingMonth = new OpeningMonth();
        OpeningHours openingHours = new OpeningHours();
        openingMonth.addOpeningHours(openingHours);

        openingMonth.setMonthActivated(0, true);
        openingMonth.setMonthActivated(2, true);
        openingMonth.setMonthActivated(4, true);
        openingMonth.setMonthActivated(5, true);
        openingMonth.setMonthActivated(6, true);
        openingMonth.setMonthActivated(7, true);
        openingMonth.setMonthActivated(8, true);
        openingMonth.setMonthActivated(10, true);
        openingMonth.setMonthActivated(11, true);

        openingHours.setDayActivated(0, true);
        openingHours.setDayActivated(1, true);
        openingHours.setDayActivated(6, true);

        openingTime.addOpeningMonth(openingMonth);

        Assert.assertEquals(parser.toValue(openingTime), "Jan,Mar,May-Sep,Nov-Dec: Mo-Tu,Su 08:00-18:00");
    }

    @Test
    public void parseMonthAndDays2() {
        OpeningTime openingTime = new OpeningTime();
        OpeningMonth openingMonth = new OpeningMonth();
        OpeningHours openingHours = new OpeningHours();
        openingMonth.addOpeningHours(openingHours);

        openingMonth.setMonthActivated(2, true);
        openingHours.setDayActivated(2, true);

        openingTime.addOpeningMonth(openingMonth);

        Assert.assertEquals(parser.toValue(openingTime), "Mar: We 08:00-18:00");
    }

    @Test
    public void parseOpeningHours1() {
        OpeningTime openingTime = new OpeningTime();
        OpeningMonth openingMonth = new OpeningMonth();
        OpeningHours openingHours1 = new OpeningHours();
        OpeningHours openingHours2 = new OpeningHours();

        openingMonth.addOpeningHours(openingHours1);
        openingMonth.addOpeningHours(openingHours2);

        openingMonth.setMonthActivated(2, true);

        openingHours1.setDayActivated(2, true);

        openingHours2.setDayActivated(5, true);
        openingHours2.setFromTime(new LocalTime(5, 30));
        openingHours2.setToTime(new LocalTime(17, 30));

        openingTime.addOpeningMonth(openingMonth);

        Assert.assertEquals(parser.toValue(openingTime), "Mar: We 08:00-18:00, Sa 05:30-17:30");
    }

    @Test
    public void parseOpeningHours2() {
        OpeningTime openingTime = new OpeningTime();
        OpeningMonth openingMonth = new OpeningMonth();
        OpeningHours openingHours1 = new OpeningHours();
        OpeningHours openingHours2 = new OpeningHours();
        OpeningHours openingHours3 = new OpeningHours();

        openingMonth.addOpeningHours(openingHours1);
        openingMonth.addOpeningHours(openingHours2);
        openingMonth.addOpeningHours(openingHours3);

        openingMonth.setMonthActivated(2, true);
        openingMonth.setMonthActivated(3, true);
        openingMonth.setMonthActivated(5, true);
        openingMonth.setMonthActivated(6, true);
        openingMonth.setMonthActivated(10, true);

        openingHours1.setDayActivated(2, true);
        openingHours1.setDayActivated(4, true);
        openingHours1.setDayActivated(5, true);
        openingHours1.setFromTime(new LocalTime(10, 45));
        openingHours1.setToTime(new LocalTime(19, 15));

        openingHours2.setDayActivated(5, true);
        openingHours2.setDayActivated(6, true);
        openingHours2.setDayActivated(1, true);
        openingHours2.setFromTime(new LocalTime(5, 30));
        openingHours2.setToTime(new LocalTime(17, 30));

        openingHours3.setDayActivated(2, true);

        openingTime.addOpeningMonth(openingMonth);

        Assert.assertEquals(parser.toValue(openingTime), "Mar-Apr,Jun-Jul,Nov: We,Fr-Sa 10:45-19:15, Tu,Sa-Su 05:30-17:30, We 08:00-18:00");
    }

    @Test
    public void parseAllFields1() {
        OpeningTime openingTime = new OpeningTime();
        OpeningMonth openingMonth1 = new OpeningMonth();
        OpeningHours openingHours1 = new OpeningHours();
        OpeningHours openingHours2 = new OpeningHours();

        openingMonth1.addOpeningHours(openingHours1);
        openingMonth1.addOpeningHours(openingHours2);

        openingMonth1.setMonthActivated(1, true);
        openingMonth1.setMonthActivated(2, true);

        openingHours1.setDayActivated(2, true);

        openingHours2.setDayActivated(5, true);
        openingHours2.setFromTime(new LocalTime(5, 30));
        openingHours2.setToTime(new LocalTime(17, 30));

        OpeningMonth openingMonth2 = new OpeningMonth();
        OpeningHours openingHours3 = new OpeningHours();
        OpeningHours openingHours4 = new OpeningHours();

        openingMonth2.addOpeningHours(openingHours3);
        openingMonth2.addOpeningHours(openingHours4);

        openingMonth2.setMonthActivated(3, true);
        openingMonth2.setMonthActivated(4, true);

        openingHours3.setDayActivated(6, true);

        openingHours4.setDayActivated(5, true);

        openingHours4.setFromTime(new LocalTime(8, 10));
        openingHours4.setToTime(new LocalTime(19, 45));

        openingTime.addOpeningMonth(openingMonth1);
        openingTime.addOpeningMonth(openingMonth2);

        Assert.assertEquals(parser.toValue(openingTime), "Feb-Mar: We 08:00-18:00, Sa 05:30-17:30; Apr-May: Su 08:00-18:00, Sa 08:10-19:45");
    }

    @Test
    public void regexOpeningTime() {
        OpeningTimeParserImpl openingTimeParser = new OpeningTimeParserImpl();
        System.out.println("One period with month");
        Assert.assertTrue(openingTimeParser.support("May,Jun: Th,Su-Fr 08:00-18:00,Th,Su-Fr 08:00-18:00,Th,Su-Fr 08:00-18:00,Th,Su-Fr 08:00-18:00"));
        System.out.println("Without month");
        Assert.assertTrue(openingTimeParser.support("Th,Su-Fr 08:00-18:00,Th,Su-Fr 08:00-18:00,Th,Su-Fr 08:00-18:00,Th,Su-Fr 08:00-18:00"));
        System.out.println("Without month, only a day");
        Assert.assertTrue(openingTimeParser.support("Th 08:00-18:00,Th,Su-Fr 08:00-18:00,Th,Su-Fr 08:00-18:00,Th,Su-Fr 08:00-18:00"));
        System.out.println("Two period month");
        Assert.assertTrue(openingTimeParser.support("May,Jun: Th 08:00-18:00,Th,Su-Fr 08:00-18:00,Th,Su-Fr 08:00-18:00,Th,Su-Fr 08:00-18:00; May: Th 08:00-18:00,Th,Su 08:00-18:00,Th,Su-Fr 08:00-18:00,Th,Su-Fr 08:00-18:00"));
    }

}
