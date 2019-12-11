/**
 * Copyright (C) 2019 Takima
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
package io.jawg.osmcontributor.rest.mappers;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.simpleframework.xml.transform.Transform;

import io.jawg.osmcontributor.utils.DateTimeUtils;
import timber.log.Timber;

public class JodaTimeDateTimeTransform implements Transform<DateTime> {

    @Override
    public DateTime read(String value) {
        DateTime dateTime = null;
        try {
            dateTime = new DateTime(value, DateTimeZone.UTC); // OSM expects UTC, let's get everything as such
        } catch (Exception e) {
            Timber.e("Joda-Time DateTime Transform failed. Exception: " + e);
        }
        return dateTime;
    }

    @Override
    public String write(DateTime value) throws Exception {
        return DateTimeUtils.UTC_DATE_TIME_FORMATTER.print(value);
    }
}
