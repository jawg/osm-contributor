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
package io.jawg.osmcontributor.ui.adapters.parser;

import java.util.Collection;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.jawg.osmcontributor.model.entities.relation_display.RelationDisplay;
import io.jawg.osmcontributor.model.entities.relation_display.RelationDisplayTag;

@Singleton
public class BusLineRelationDisplayParser {
    private static final String NAME = "name";
    private static final String FROM = "from";
    private static final String TO = "to";
    private static final String DIR = " → ";

    @Inject
    public BusLineRelationDisplayParser() {
        //empty
    }

    /**
     * try to get bus line name from the relation tags
     * try with NAME and then with FROM and TO
     * @param relationDisplay the relationDisplay Object
     * @return a string or null if nothing found
     */
    public String getBusLineName(RelationDisplay relationDisplay) {
        String value = getValue(NAME, relationDisplay.getTags());
        if (value != null) {
            return value;
        }
        value = getValue(FROM, relationDisplay.getTags());
        if (value != null) {
            String value2 = getValue(TO, relationDisplay.getTags());
            if (value2 != null)
                return value + DIR + value2;
        }
        return null;
    }

    /**
     * get the value of a specific tag
     * @param tag the researched key
     * @param tags list of tags
     * @return the value if found, null otherwise
     */
    private String getValue(String tag, Collection<RelationDisplayTag> tags) {
        for (RelationDisplayTag t : tags) {
            if (t.getKey() != null && t.getKey().equals(tag) && t.getValue() != null && !t.getValue().isEmpty())
                return t.getValue();
        }
        return null;
    }

}
