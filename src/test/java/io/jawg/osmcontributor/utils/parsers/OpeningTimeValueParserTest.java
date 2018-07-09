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
import org.junit.Before;
import org.junit.Test;
import org.robolectric.RuntimeEnvironment;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.jawg.osmcontributor.modules.DaggerOsmTemplateComponent;
import io.jawg.osmcontributor.modules.OsmTemplateModule;
import io.jawg.osmcontributor.ui.adapters.item.opening.OpeningHours;
import io.jawg.osmcontributor.ui.adapters.item.opening.OpeningMonth;
import io.jawg.osmcontributor.ui.adapters.item.opening.OpeningTime;
import io.jawg.osmcontributor.ui.adapters.parser.OpeningTimeTagParserImpl;
import io.jawg.osmcontributor.ui.adapters.parser.OpeningTimeValueParser;

/**
 * @author Tommy Buonomo on 05/07/16.
 */

public class OpeningTimeValueParserTest {
    private OpeningTimeValueParser parser;

    @Before
    public void before() {
        parser = DaggerOsmTemplateComponent.builder()
                .osmTemplateModule(new OsmTemplateModule(RuntimeEnvironment.application)).build().getOpeningTimeParser();
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
    public void parseDays1() {
        OpeningTime openingTime = new OpeningTime();
        OpeningMonth openingMonth = new OpeningMonth();
        OpeningHours openingHours = new OpeningHours();
        openingHours.setAllDaysActivated(false);
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
        openingHours.setAllDaysActivated(false);
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
        openingHours.setAllDaysActivated(false);
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
        openingHours.setAllDaysActivated(false);
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

        openingHours1.setAllDaysActivated(false);
        openingHours1.setDayActivated(2, true);

        openingHours2.setAllDaysActivated(false);
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

        openingHours1.setAllDaysActivated(false);
        openingHours1.setDayActivated(2, true);
        openingHours1.setDayActivated(4, true);
        openingHours1.setDayActivated(5, true);
        openingHours1.setFromTime(new LocalTime(10, 45));
        openingHours1.setToTime(new LocalTime(19, 15));

        openingHours2.setAllDaysActivated(false);
        openingHours2.setDayActivated(5, true);
        openingHours2.setDayActivated(6, true);
        openingHours2.setDayActivated(1, true);
        openingHours2.setFromTime(new LocalTime(5, 30));
        openingHours2.setToTime(new LocalTime(17, 30));

        openingHours3.setAllDaysActivated(false);
        openingHours3.setDayActivated(2, true);

        openingTime.addOpeningMonth(openingMonth);

        Assert.assertEquals(parser.toValue(openingTime), "Mar-Apr,Jun-Jul,Nov: We,Fr-Sa 10:45-19:15, Tu,Sa-Su 05:30-17:30, We 08:00-18:00");
    }

    @Test
    public void fromValue1() {
        String value = "Apr-May: Mo-Fr 10:15-01:30";
        OpeningTime openingTime = parser.fromValue(value);
        OpeningTime openingTimeExpected = new OpeningTime();
        OpeningMonth openingMonth = new OpeningMonth();
        openingTimeExpected.addOpeningMonth(openingMonth);
        openingMonth.setMonthActivated(3, true);
        openingMonth.setMonthActivated(4, true);

        OpeningHours openingHours = new OpeningHours();
        openingMonth.addOpeningHours(openingHours);

        openingHours.setFromTime(new LocalTime(10, 15));
        openingHours.setToTime(new LocalTime(1, 30));

        Assert.assertEquals(openingTimeExpected, openingTime);
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

        openingHours1.setAllDaysActivated(false);
        openingHours1.setDayActivated(2, true);

        openingHours2.setAllDaysActivated(false);
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

        openingHours3.setAllDaysActivated(false);
        openingHours3.setDayActivated(6, true);

        openingHours4.setAllDaysActivated(false);
        openingHours4.setDayActivated(5, true);

        openingHours4.setFromTime(new LocalTime(8, 10));
        openingHours4.setToTime(new LocalTime(19, 45));

        openingTime.addOpeningMonth(openingMonth1);
        openingTime.addOpeningMonth(openingMonth2);

        Assert.assertEquals(parser.toValue(openingTime), "Feb-Mar: We 08:00-18:00, Sa 05:30-17:30; Apr-May: Su 08:00-18:00, Sa 08:10-19:45");
    }

    public static final String PATTERN_MONTH = "(\\bJan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec\\b)";

    public static final String PATTERN_DAY = "(\\bMo|Tu|We|Th|Fr|Sa|Su\\b)";

    public static final String PATTERN_HOUR = "\\d{2}:\\d{2}-\\d{2}:\\d{2}";

    public static final String PATTERN =
            "(" + PATTERN_MONTH + "(-" + PATTERN_MONTH + ")?" + "(,\\s?" + PATTERN_MONTH +
                    "(-" + PATTERN_MONTH + ")?)*:\\s?)?" +
                    "(" + PATTERN_DAY + "(-" + PATTERN_DAY + ")?" +
                    "((,\\s?" + PATTERN_DAY + "(-" + PATTERN_DAY + ")?)?)*\\s" +
                    PATTERN_HOUR + ")(,\\s?(" + PATTERN_DAY +
                    "(-" + PATTERN_DAY + ")?" +
                    "((,\\s?" + PATTERN_DAY +
                    "(-" + PATTERN_DAY + ")?)?)*\\s" +
                    PATTERN_HOUR + "))*";

    public static final String PATTERN_FINAL = PATTERN + "(;\\s?" + PATTERN + ")*";

    @Test
    public void fromValueTest1() {
        String s = "May,Jun: Th 08:00-18:00,Th,Su-Fr 08:00-18:00,Th,Su-Fr 08:00-18:00,Th,Su-Fr 08:00-18:00; May: Th 08:00-18:00,Th,Su 08:00-18:00,Th,Su-Fr 08:00-18:00,Th,Su-Fr 08:00-18:00";

        Pattern pattern = Pattern.compile(PATTERN_FINAL);
        Matcher matcher = pattern.matcher(s);

    }

    @Test
    public void regexOpeningTime1() {
        OpeningTimeTagParserImpl openingTimeParser = new OpeningTimeTagParserImpl();
        System.out.println("One period with month");
        Assert.assertTrue(openingTimeParser.supports("May,Jun: Th,Su-Fr 08:00-18:00,Th,Su-Fr 08:00-18:00,Th,Su-Fr 08:00-18:00,Th,Su-Fr 08:00-18:00"));
    }

    @Test
    public void regexOpeningTime2() {
        OpeningTimeTagParserImpl openingTimeParser = new OpeningTimeTagParserImpl();
        System.out.println("Without month");
        Assert.assertTrue(openingTimeParser.supports("May,Jun: Th,Su-Fr 08:00-18:00,Th,Su-Fr 08:00-18:00,Th,Su-Fr 08:00-18:00,Th,Su-Fr 08:00-18:00"));
    }

    @Test
    public void regexOpeningTime3() {
        OpeningTimeTagParserImpl openingTimeParser = new OpeningTimeTagParserImpl();
        System.out.println("Without month, only a day");
        Assert.assertTrue(openingTimeParser.supports("Th 08:00-18:00,Th,Su-Fr 08:00-18:00,Th,Su-Fr 08:00-18:00,Th,Su-Fr 08:00-18:00"));
    }

    @Test
    public void regexOpeningTime4() {
        OpeningTimeTagParserImpl openingTimeParser = new OpeningTimeTagParserImpl();
        System.out.println("Two period month");
        Assert.assertTrue(openingTimeParser.supports("May,Jun: Th 08:00-18:00,Th,Su-Fr 08:00-18:00,Th,Su-Fr 08:00-18:00,Th,Su-Fr 08:00-18:00; May: Th 08:00-18:00,Th,Su 08:00-18:00,Th,Su-Fr 08:00-18:00,Th,Su-Fr 08:00-18:00"));
    }
}
