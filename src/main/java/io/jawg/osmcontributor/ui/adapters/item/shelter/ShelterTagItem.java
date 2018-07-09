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
package io.jawg.osmcontributor.ui.adapters.item.shelter;

import java.util.Map;

import static io.jawg.osmcontributor.ui.adapters.item.shelter.ShelterType.NONE;

public class ShelterTagItem extends TagItem {
    private ShelterType shelterType = NONE;

    private ShelterTagItem(ShelterTagItemBuilder builder) {
        super(builder);
        shelterType = builder.shelterType;
    }

    public static class ShelterTagItemBuilder extends TagItemBuilder {
        private ShelterType shelterType = NONE;

        ShelterTagItemBuilder(String key, String value) {
            super(key, value);
        }

        public ShelterTagItemBuilder shelterType(ShelterType shelterType) {
            this.shelterType = shelterType;
            return this;
        }

        @Override
        public ShelterTagItem build() {
            return new ShelterTagItem(this);
        }
    }


    @Override
    public Map<String, String> getOsmValues() {
        return shelterType.getOsmValues();
    }

    public ShelterType getShelterType() {
        return shelterType;
    }

    public void setShelterType(ShelterType shelterType) {
        this.shelterType = shelterType;
    }
}
