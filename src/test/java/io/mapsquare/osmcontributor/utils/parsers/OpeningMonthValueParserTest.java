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
import org.junit.Before;
import org.junit.Test;
import org.robolectric.Robolectric;

import io.mapsquare.osmcontributor.model.utils.OpeningHours;
import io.mapsquare.osmcontributor.model.utils.OpeningMonth;
import io.mapsquare.osmcontributor.modules.DaggerOsmTemplateComponent;
import io.mapsquare.osmcontributor.modules.OsmTemplateModule;
import io.mapsquare.osmcontributor.ui.adapters.parser.OpeningMonthValueParser;

/**
 * @author Tommy Buonomo on 18/07/16.
 */

public class OpeningMonthValueParserTest {
    private OpeningMonthValueParser parser;

    @Before
    public void before() {
        parser = DaggerOsmTemplateComponent.builder()
                .osmTemplateModule(new OsmTemplateModule(Robolectric.application)).build().getOpeningMonthParser();
    }

    @Test
    public void fromValue1() {
        String monthValue = "Jan-Apr,Sep";

        OpeningMonth openingMonth = parser.fromValue(monthValue);

        OpeningMonth o = new OpeningMonth();
        o.setMonthActivated(0, true);
        o.setMonthActivated(1, true);
        o.setMonthActivated(2, true);
        o.setMonthActivated(3, true);
        o.setMonthActivated(8, true);

        Assert.assertEquals(o, openingMonth);
    }

    @Test
    public void fromValue2() {
        String monthValue = "Feb-Mar,May-Jun,Aug-Sep,Nov";

        OpeningMonth openingMonth = parser.fromValue(monthValue);

        OpeningMonth o = new OpeningMonth();
        o.setMonthActivated(1, true);
        o.setMonthActivated(2, true);
        o.setMonthActivated(4, true);
        o.setMonthActivated(5, true);
        o.setMonthActivated(7, true);
        o.setMonthActivated(8, true);
        o.setMonthActivated(10, true);
        Assert.assertEquals(o, openingMonth);
    }

    @Test
    public void parseMonths2() {
        OpeningMonth openingMonth = new OpeningMonth();
        openingMonth.setMonthActivated(5, true);
        Assert.assertEquals(parser.toValue(openingMonth), "Jun");
    }

    @Test
    public void parseMonths3() {
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
        Assert.assertEquals(parser.toValue(openingMonth), "Jan,Mar,Jun-Sep,Nov-Dec");
    }

    @Test
    public void nonStop1() {
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

        openingHours.setFromTime(new LocalTime(23, 59));
        openingHours.setToTime(new LocalTime(23, 58));

        Assert.assertEquals(parser.toValue(openingMonth), "24/7");
    }

    @Test
    public void nonStop2() {
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


        Assert.assertEquals(parser.toValue(openingMonth), "Jan-Feb,Aug-Sep,Nov: 24/7");
    }
}
