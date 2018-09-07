/**
 * Copyright (C) 2016 eBusiness Information
 * <p>
 * This file is part of OSM Contributor.
 * <p>
 * OSM Contributor is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * OSM Contributor is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with OSM Contributor.  If not, see <http://www.gnu.org/licenses/>.
 */

package io.jawg.osmcontributor.utils.edition;

import java.util.List;

import io.jawg.osmcontributor.model.entities.relation_display.RelationDisplay;

public class RelationDisplayUtils {

    /**
     * Check if the list of bus lines already contains the bus line "busLine"
     * or a bus line whose tag value corresponding to tag key "tagKey" is equal
     *
     * @param busLines  List of bus lines
     * @param busLine   Bus line to be checked
     * @param tagKey    Tag key whose value to be checked
     * @return True if busLine is already in busLines or if busLines contains a bus line whose tag value is equal, otherwise false
     */
    public static boolean isBusLineOrTagEqual(List<RelationDisplay> busLines, RelationDisplay busLine, String tagKey) {
        final String tagVal = new RelationDisplayDto(busLine).getTagValue(tagKey);

        for (RelationDisplay line : busLines) {
            final String tagRefValCurrent = new RelationDisplayDto(line).getTagValue(tagKey);

            if (line.equals(busLine) || tagRefValCurrent.equals(tagVal)) {
                return true;
            }
        }
        return false;
    }
}
