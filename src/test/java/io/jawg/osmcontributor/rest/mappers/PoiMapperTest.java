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
package io.jawg.osmcontributor.rest.mappers;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;

import io.jawg.osmcontributor.database.dao.PoiTypeDao;
import io.jawg.osmcontributor.model.entities.Poi;
import io.jawg.osmcontributor.model.entities.PoiType;
import io.jawg.osmcontributor.model.entities.PoiTypeTag;
import io.jawg.osmcontributor.rest.dtos.osm.NodeDto;
import io.jawg.osmcontributor.rest.dtos.osm.TagDto;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.when;


@RunWith(MockitoJUnitRunner.class)
public class PoiMapperTest {


    @Mock
    PoiTypeDao poiTypeDao;

    @Test
    public void type_basic() {
        when(poiTypeDao.queryForAll()).thenReturn(singletonList(poiType("t1", "key1", "value")));
        List<Poi> pois = new PoiMapper(poiTypeDao, null).convertDtosToPois(singletonList(getNodeDto("key1", "value")));
        assertThat(pois).hasSize(1);
        assertThat(pois.get(0).getType().getName()).isEqualTo("t1");
    }

    @Test
    public void type_ignoreUnknown() {
        when(poiTypeDao.queryForAll()).thenReturn(singletonList(poiType("t1", "key1", "value")));
        List<Poi> pois = new PoiMapper(poiTypeDao, null).convertDtosToPois(asList(getNodeDto("foo", "bar"), getNodeDto("key1", "value")));
        assertThat(pois).hasSize(1);
        assertThat(pois.get(0).getType().getName()).isEqualTo("t1");
    }

    @Test
    public void type_multiple() {
        when(poiTypeDao.queryForAll()).thenReturn(asList(poiType("t1", "key1", "value"), poiType("t2", "key1", "value2")));
        List<Poi> pois = new PoiMapper(poiTypeDao, null).convertDtosToPois(asList(getNodeDto("foo", "bar"), getNodeDto("key1", "value2")));
        assertThat(pois).hasSize(1);
        assertThat(pois.get(0).getType().getName()).isEqualTo("t2");
    }


    private NodeDto getNodeDto(String... tags) {
        NodeDto nodeDto = new NodeDto();
        nodeDto.setLat(1.0);
        nodeDto.setLon(1.0);
        List<TagDto> tagDtos = new ArrayList<>();
        if (tags.length % 2 != 0) {
            throw new AssertionError("Malformed tags ! Array must be an even number");
        }
        for (int i = 0; i < tags.length; i += 2) {
            tagDtos.add(tagDto(tags[i], tags[i + 1]));
        }
        nodeDto.setTagsDtoList(tagDtos);
        return nodeDto;
    }

    private PoiType poiType(String name, String... tags) {
        PoiType t1 = new PoiType();
        t1.setName(name);
        List<PoiTypeTag> poiTypeTags = new ArrayList<>();
        if (tags.length % 2 != 0) {
            throw new AssertionError("Malformed tags ! Array must be an even number");
        }
        for (int i = 0; i < tags.length; i += 2) {
            poiTypeTags.add(poiTypeTag(tags[i], tags[i + 1]));
        }
        t1.setTags(poiTypeTags);
        return t1;
    }

    private TagDto tagDto(String key1, String value) {
        TagDto tagDto = new TagDto();
        tagDto.setKey(key1);
        tagDto.setValue(value);
        return tagDto;
    }

    private PoiTypeTag poiTypeTag(String key, String value) {
        PoiTypeTag poiTypeTag = new PoiTypeTag();
        poiTypeTag.setKey(key);
        poiTypeTag.setValue(value);
        return poiTypeTag;
    }

}