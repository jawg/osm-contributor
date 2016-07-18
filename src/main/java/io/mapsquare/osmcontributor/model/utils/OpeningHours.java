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
package io.mapsquare.osmcontributor.model.utils;

import org.joda.time.LocalTime;

import java.util.Arrays;

/**
 * @author Tommy Buonomo on 08/07/16.
 */
public class OpeningHours {
    public static final int DAY_COUNT = 7;

    public enum Days {
        MON("Mo"),
        TUE("Tu"),
        WED("We"),
        THU("Th"),
        FRI("Fr"),
        SAT("Sa"),
        SUN("Su");

        private String data;

        Days(String data) {
            this.data = data;
        }

        public String getData() {
            return data;
        }

    }

    private Days[] days;

    private boolean changed;
    private LocalTime fromTime, toTime;

    public OpeningHours() {
        days = new Days[DAY_COUNT];
        fromTime = new LocalTime(8, 0);
        toTime = new LocalTime(18, 0);
        for (int i = 0; i < 5; i++) {
            setDayActivated(i, true);
        }
    }

    public void setDayActivated(int i, boolean active) {
        if (i < Days.values().length && i >= 0) {
            this.days[i] = active ? Days.values()[i] : null;
            changed = true;
        }
    }

    public void setAllDaysActivated(boolean active) {
        for (int i = 0; i < days.length; i++) {
            setDayActivated(i, active);
        }
    }

    public Days[] getDays() {
        return days;
    }

    public void setDays(Days[] days) {
        this.days = days;
    }

    public LocalTime getFromTime() {
        return fromTime;
    }

    public void setFromTime(LocalTime fromTime) {
        this.fromTime = fromTime;
        changed = true;
    }

    public LocalTime getToTime() {
        return toTime;
    }

    public void setToTime(LocalTime toTime) {
        this.toTime = toTime;
        changed = true;
    }

    public boolean isChanged() {
        return changed;
    }

    void setChanged(boolean changed) {
        this.changed = changed;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof OpeningHours)) {
            return false;
        }

        OpeningHours that = (OpeningHours) o;

        if (changed != that.changed) {
            return false;
        }
        // Probably incorrect - comparing Object[] arrays with Arrays.equals
        if (!Arrays.equals(days, that.days)) {
            return false;
        }
        if (fromTime != null ? !fromTime.equals(that.fromTime) : that.fromTime != null) {
            return false;
        }
        return toTime != null ? toTime.equals(that.toTime) : that.toTime == null;

    }

    @Override
    public int hashCode() {
        int result = Arrays.hashCode(days);
        result = 31 * result + (changed ? 1 : 0);
        result = 31 * result + (fromTime != null ? fromTime.hashCode() : 0);
        result = 31 * result + (toTime != null ? toTime.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "OpeningHours{" +
                "days=" + Arrays.toString(days) +
                ", changed=" + changed +
                ", fromTime=" + fromTime +
                ", toTime=" + toTime +
                '}';
    }
}
