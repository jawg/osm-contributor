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
package io.mapsquare.osmcontributor.rest.mappers;

import org.joda.time.format.ISODateTimeFormat;

import java.util.ArrayList;
import java.util.Map;

import javax.inject.Inject;

import io.mapsquare.osmcontributor.database.dao.PoiTypeDao;
import io.mapsquare.osmcontributor.model.entities.Poi;
import io.mapsquare.osmcontributor.model.entities.PoiTag;
import io.mapsquare.osmcontributor.model.entities.PoiType;
import io.mapsquare.osmcontributor.model.entities.PoiTypeTag;
import io.mapsquare.osmcontributor.sync.dto.poistorage.LatLng;
import io.mapsquare.osmcontributor.sync.dto.poistorage.PoiDto;
import io.mapsquare.osmcontributor.sync.dto.poistorage.TypeDto;

public class PoiStorageConverter {

    PoiTypeDao poiTypeDao;

    @Inject
    public PoiStorageConverter(PoiTypeDao poiTypeDao) {
        this.poiTypeDao = poiTypeDao;
    }

    public Poi convertPoi(PoiDto poiDto) {
        PoiType type = poiTypeDao.findByBackendId(poiDto.getTypeBackendId());

        // We don't keep the Poi if we don't know it's PoiType
        if (type == null) {
            return null;
        }

        Poi poi = new Poi();

        poi.setType(type);
        poi.setBackendId(poiDto.getBackendId());
        poi.setLatitude(poiDto.getLatLng().getLatitude());
        poi.setLongitude(poiDto.getLatLng().getLongitude());
        poi.setLevel(poiDto.getLevel().toString());
        poi.setVersion(poiDto.getRevision().toString());
        poi.setUpdateDate(ISODateTimeFormat.localDateOptionalTimeParser().parseDateTime(poiDto.getLastUpdate()));
        poi.setName(poiDto.getName());
        poi.setUpdated(false);

        if (poiDto.getFields() != null) {
            poi.setTags(new ArrayList<PoiTag>(poiDto.getFields().size()));
            for (Map.Entry<String, String> field : poiDto.getFields().entrySet()) {
                PoiTag poiTag = new PoiTag();
                poiTag.setKey(field.getKey());
                poiTag.setValue(field.getValue());
                poiTag.setPoi(poi);
                poi.getTags().add(poiTag);
            }
        } else {
            poi.setTags(new ArrayList<PoiTag>());
        }

        return poi;
    }

    public PoiDto convertPoiDto(Poi poi) {
        LatLng latLng = new LatLng();
        latLng.setLatitude(poi.getLatitude());
        latLng.setLongitude(poi.getLongitude());

        PoiDto poiDto = new PoiDto();
        poiDto.setName(poi.getName() != null ? poi.getName() : "");
        poiDto.setLatLng(latLng);
        poiDto.setLevel(poi.getLevel() != null ? Float.parseFloat(poi.getLevel()) : 0);
        poiDto.setBackendId(poi.getBackendId());
        poiDto.setFields(!poi.getTagsMap().isEmpty() ? poi.getTagsMap() : null);
        poiDto.setRevision(poi.getVersion() != null ? Integer.parseInt(poi.getVersion()) : null);
        poiDto.setTypeBackendId(poi.getType() != null ? poi.getType().getBackendId() : null);
        return poiDto;
    }

    public PoiType convertPoiType(TypeDto dto) {
        PoiType type = new PoiType();
        type.setBackendId(dto.getId());
        type.setName(dto.getName());
        type.setIcon(dto.getIconUrl());
        ArrayList<PoiTypeTag> tags = new ArrayList<>();
        int i = 0;
        if (dto.getKeys() != null) {
            for (String key : dto.getKeys()) {
                PoiTypeTag tag = new PoiTypeTag();
                tag.setKey(key);
                tag.setOrdinal(i++);
                tag.setPoiType(type);
                tag.setMandatory(true);
                tags.add(tag);
            }
        }
        type.setTags(tags);
        return type;
    }


}
