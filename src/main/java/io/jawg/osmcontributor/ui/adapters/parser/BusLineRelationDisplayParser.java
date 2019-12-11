/**
 * Copyright (C) 2019 Takima
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

import android.text.TextUtils;

import java.util.Collection;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.jawg.osmcontributor.model.entities.relation_display.RelationDisplay;
import io.jawg.osmcontributor.model.entities.relation_display.RelationDisplayTag;

@Singleton
public class BusLineRelationDisplayParser {
    private static final String NETWORK = "network";
    private static final String NAME = "name";
    private static final String REF = "ref";
    private static final String DIR = "→ ";
    private static final String TO = "to";

    @Inject
    public BusLineRelationDisplayParser() {
        //empty
    }

    /**
     * Get bus line tag 'NETWORK' from the relation tags
     * @param relationDisplay the relationDisplay Object
     * @return a string or null if nothing found
     */
    public String getBusLineNetwork(RelationDisplay relationDisplay) {
        return getValue(NETWORK, relationDisplay.getTags());
    }

    /**
     * Get bus line tag 'NAME' from the relation tags
     * @param relationDisplay the relationDisplay Object
     * @return a string or null if nothing found
     */
    public String getBusLineName(RelationDisplay relationDisplay) {
        return getValue(NAME, relationDisplay.getTags());
    }

    /**
     * Get bus line tag 'REF' from the relation tags
     * @param relationDisplay the relationDisplay Object
     * @return a string or null if nothing found
     */
    public String getBusLineRef(RelationDisplay relationDisplay) {
        return getValue(REF, relationDisplay.getTags());
    }

    /**
     * Get bus line tag 'TO' from the relation tags
     * @param relationDisplay the relationDisplay Object
     * @return a string or null if nothing found
     */
    public String getBusLineDestination(RelationDisplay relationDisplay) {
        String destination = getValue(TO, relationDisplay.getTags());
        if (destination != null) {
            destination = DIR + destination;
        }
        return destination;
    }

    /**
     * Get a formated string made of some bus line infos from the relation tags
     * First try with NETWORK + REF + TO, then with NAME
     * @param relationDisplay the relationDisplay Object
     * @return a string or null if nothing found
     */
    public String getBusLine(RelationDisplay relationDisplay) {
        final String network = getBusLineNetwork(relationDisplay);
        final String name = getBusLineName(relationDisplay);
        final String ref = getBusLineRef(relationDisplay);
        final String to = getBusLineDestination(relationDisplay);

        String busLine = network + " " + ref;
        if (!busLine.isEmpty() && !TextUtils.isEmpty(to)) {
            return busLine + " " + to;
        }
        if (!TextUtils.isEmpty(name)) {
            return name;
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
            if (t.getKey() != null && t.getKey().equals(tag) && !TextUtils.isEmpty(t.getValue())) {
                return t.getValue();
            }
        }
        return null;
    }
}
