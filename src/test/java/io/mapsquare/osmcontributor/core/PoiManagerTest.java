/**
 * Copyright (C) 2015 eBusiness Information
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
package io.mapsquare.osmcontributor.core;


import com.j256.ormlite.android.apptools.OpenHelperManager;

import org.joda.time.DateTime;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.mapsquare.osmcontributor.DaggerOsmTemplateComponent;
import io.mapsquare.osmcontributor.OsmTemplateComponent;
import io.mapsquare.osmcontributor.OsmTemplateModule;
import io.mapsquare.osmcontributor.core.model.KeyWord;
import io.mapsquare.osmcontributor.core.model.Poi;
import io.mapsquare.osmcontributor.core.model.PoiTag;
import io.mapsquare.osmcontributor.core.model.PoiType;
import io.mapsquare.osmcontributor.core.model.PoiTypeTag;

import static org.fest.assertions.api.Assertions.assertThat;

@RunWith(RobolectricTestRunner.class)
public class PoiManagerTest {

    OsmTemplateComponent component;

    @Before
    public void before() {
        component = DaggerOsmTemplateComponent.builder()
                .osmTemplateModule(new OsmTemplateModule(Robolectric.application)).build();
    }

    @After
    public void after() {
        OpenHelperManager.releaseHelper();
    }

    @Test
    public void testSaveAndQuery() {
        PoiManager poiManager = component.getPoiManager();
        Poi poi = getPoi(poiManager.savePoiType(getPoiType()), 1);
        Poi saved = poiManager.savePoi(poi);
        Poi queried = poiManager.queryForId(saved.getId());
        assertThat(queried.getName()).isEqualTo("MyPoi1");
        assertThat(queried.getLatitude()).isEqualTo(42.0);
        assertThat(queried.getLongitude()).isEqualTo(73.0);
        assertThat(queried.getUpdated()).isFalse();
    }

    @Test
    public void testBulkSaveAndBulkUpdate() {
        PoiManager poiManager = component.getPoiManager();
        PoiType poiType = poiManager.savePoiType(getPoiType());
        // try to save and then update 1000 pois.
        // 1000 because it can happen in real life and pose problems if we try to do an "IN" sql clause
        List<Poi> pois = new ArrayList<>(1000);
        for (int i = 0; i < 1000; i++) {
            pois.add(getPoi(poiType, i));
        }
        poiManager.savePois(pois);
        for (Poi poi : pois) {
            assertThat(poi.getId()).isNotNull();
        }
        for (Poi poi : pois) {
            Map<String, String> tags = new HashMap<>();
            tags.put("tag2", "value2");
            poi.getTags().clear();
            poi.applyChanges(tags);
        }
        List<Poi> savedPois = poiManager.savePois(pois);
        for (Poi poi : savedPois) {
            assertThat(poi.getTags()).hasSize(1);
            PoiTag tag = poi.getTags().iterator().next();
            assertThat(tag.getKey()).isEqualTo("tag2");
            assertThat(tag.getValue()).isEqualTo("value2");
        }
    }

    private Poi getPoi(PoiType poiType, int i) {
        Poi poi = new Poi();
        poi.setType(poiType);
        poi.setName("MyPoi" + i);
        poi.setLatitude(42.0);
        poi.setLongitude(73.0);
        poi.setUpdated(false);
        Map<String, String> tags = new HashMap<>();
        tags.put("tag1", "value1");
        poi.applyChanges(tags);
        return poi;
    }

    private PoiType getPoiType() {
        PoiType poiType = new PoiType();
        PoiTypeTag poiTypeTag = new PoiTypeTag();
        poiTypeTag.setKey("toto");
        poiTypeTag.setOrdinal(0);
        poiTypeTag.setMandatory(true);
        poiType.setTags(Collections.singletonList(poiTypeTag));
        poiType.setName("PoiType");
        poiType.setLastUse(new DateTime());
        poiType.setKeyWords(new ArrayList<KeyWord>());
        return poiType;
    }

}