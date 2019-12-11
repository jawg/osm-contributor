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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class BusLineValueParserImpl implements ValueParser<List<String>> {
    private final String SEP = ";";

    @Inject
    public BusLineValueParserImpl() {
        //empty
    }

    @Override
    public String toValue(List<String> busLines) {
        StringBuilder builder = new StringBuilder();

        for (int i = 0; i < busLines.size(); i++) {
            if (i > 0) {
                builder.append(SEP);
            }
            builder.append(busLines.get(i));
        }
        return builder.toString();
    }

    public String cleanValue(String value) {
        return value.trim();
    }

    @Override
    public List<String> fromValue(String data) {
        if (data == null || data.trim().isEmpty()) {
            return Collections.emptyList();
        }
        String[] lines = data.split(SEP);
        List<String> toReturn = new ArrayList<>();
        for (String s : Arrays.asList(lines)) {
            if (s != null && !s.trim().isEmpty()) {
                toReturn.add(s.trim());
            }
        }
        return toReturn;
    }

    @Override
    public int getPriority() {
        return ParserManager.PRIORITY_HIGH;
    }

    public boolean lineContainsMultipleValues(String valueEnteredByUser) {
        return valueEnteredByUser.contains(SEP);
    }

}
