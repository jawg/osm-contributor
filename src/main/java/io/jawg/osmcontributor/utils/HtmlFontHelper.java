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
package io.jawg.osmcontributor.utils;

/**
 * Use to add html tags to change font, color...
 */
public class HtmlFontHelper {
    public static final String BOLD_STR_FORMAT = "<b>%s</b>";
    public static final String COLOR_STR_FORMAT = "<font color='%s'>%s</font>";
    public static final String RED = "#E53935";
    public static final String GREEN = "#43A047";
    public static final String ORANGE = "#FB8C00";


    /**
     * Add Html tags around the string to colorize it.
     *
     * @param value The string to colorize.
     * @param color The color of the output string.
     * @return A Html string with the given color.
     */
    public static String addColor(String value, String color) {
        return String.format(COLOR_STR_FORMAT, color, value);
    }

    public static String getBold(String s) {
        return String.format(BOLD_STR_FORMAT, s);
    }
}
