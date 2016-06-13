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


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import io.mapsquare.osmcontributor.database.dao.PoiTypeDao;
import io.mapsquare.osmcontributor.model.entities.Poi;
import io.mapsquare.osmcontributor.model.entities.PoiNodeRef;
import io.mapsquare.osmcontributor.model.entities.PoiTag;
import io.mapsquare.osmcontributor.model.entities.PoiType;
import io.mapsquare.osmcontributor.model.entities.PoiTypeTag;
import io.mapsquare.osmcontributor.rest.dtos.osm.NdDto;
import io.mapsquare.osmcontributor.rest.dtos.osm.NodeDto;
import io.mapsquare.osmcontributor.rest.dtos.osm.PoiDto;
import io.mapsquare.osmcontributor.rest.dtos.osm.TagDto;
import io.mapsquare.osmcontributor.rest.dtos.osm.WayDto;

public class PoiMapper {

    PoiTypeDao poiTypeDao;
    PoiTagMapper poiTagMapper;

    @Inject
    public PoiMapper(PoiTypeDao poiTypeDao, PoiTagMapper poiTagMapper) {
        this.poiTypeDao = poiTypeDao;
        this.poiTagMapper = poiTagMapper;
    }

    public Poi convertNodeDtoToPoi(PoiDto dto) {
        if (dto != null) {
            return convertDtosToPois(Collections.singletonList(dto)).get(0);
        } else {
            return null;
        }
    }

    public List<Poi> convertDtosToPois(List<? extends PoiDto> dtos) {
        return convertDtosToPois(dtos, true);
    }

    public List<Poi> convertDtosToPois(List<? extends PoiDto> dtos, boolean typeFiltering) {
        List<Poi> result = new ArrayList<>();
        if (dtos != null) {
            List<PoiType> availableTypes = poiTypeDao.queryForAll();
            for (PoiDto dto : dtos) {
                PoiType type = findType(dto, availableTypes);
                if (type == null && typeFiltering) {
                    continue; // poi not of an available type
                }
                Poi poi = new Poi();
                poi.setType(type);
                poi.setLatitude(dto.getLat());
                poi.setLongitude(dto.getLon());
                poi.setBackendId(dto.getId());
                poi.setVersion(String.valueOf(dto.getVersion()));
                poi.setUpdated(false);
                poi.setUpdateDate(dto.getTimestamp());
                poi.setWay(dto.isWay());
                List<PoiTag> tags = new ArrayList<>(dto.getTagsDtoList().size());
                for (TagDto tagDto : dto.getTagsDtoList()) {
                    PoiTag tag = new PoiTag();
                    tag.setPoi(poi);
                    tag.setKey(tagDto.getKey());
                    tag.setValue(tagDto.getValue());
                    tags.add(tag);
                    if (tag.getKey().equals("name")) {
                        poi.setName(tag.getValue());
                    }
                    if (tag.getKey().equals("level")) {
                        poi.setLevel(tag.getValue());
                    }
                }
                poi.setTags(tags);

                List<PoiNodeRef> nodeRefs = new ArrayList<>();
                int counter = 0;
                for (NdDto ndDto : dto.getNdDtoList()) {
                    PoiNodeRef nodeRef = new PoiNodeRef();
                    nodeRef.setPoi(poi);
                    nodeRef.setNodeBackendId(ndDto.getRef());
                    nodeRef.setOrdinal(counter++);
                    nodeRef.setLatitude(ndDto.getLat());
                    nodeRef.setLongitude(ndDto.getLon());
                    nodeRef.setUpdated(false);
                    nodeRefs.add(nodeRef);
                }
                poi.setNodeRefs(nodeRefs);
                result.add(poi);
            }
        }
        return result;
    }

    public List<NodeDto> convertPoisToNodeDtos(List<Poi> pois, String changeSetId) {
        List<NodeDto> result = new ArrayList<>();
        for (Poi poi : pois) {
            result.add(convertPoiToNodeDto(poi, changeSetId));
        }

        return result;
    }

    /**
     * @param poi         the poi to convert
     * @param changeSetId an optional changeSetId to be added to the created NodeDto
     * @return the converted NodeDto
     */
    public NodeDto convertPoiToNodeDto(Poi poi, String changeSetId) {
        NodeDto nodeDto = new NodeDto();

        nodeDto.setLat(poi.getLatitude());
        nodeDto.setLon(poi.getLongitude());
        nodeDto.setId(poi.getBackendId());
        nodeDto.setChangeset(changeSetId);
        if (poi.getVersion() != null) {
            nodeDto.setVersion(Integer.parseInt(poi.getVersion()));
        }
        nodeDto.setTagsDtoList(poiTagMapper.convertFromPoiTag(poi.getTags()));

        return nodeDto;
    }

    public WayDto convertPoiToWayDto(Poi poi, String changeSetId) {
        WayDto wayDto = new WayDto();
        wayDto.setId(poi.getBackendId());
        wayDto.setChangeset(changeSetId);
        if (poi.getVersion() != null) {
            wayDto.setVersion(Integer.parseInt(poi.getVersion()));
        }
        wayDto.setTagsDtoList(poiTagMapper.convertFromPoiTag(poi.getTags()));

        List<NdDto> ndDtos = new ArrayList<>();
        for (PoiNodeRef poiNodeRef : poi.getNodeRefs()) {
            NdDto ndDto = new NdDto();
            ndDto.setRef(poiNodeRef.getNodeBackendId());
        }
        wayDto.setNdDtoList(ndDtos);

        return wayDto;
    }

    private PoiType findType(PoiDto dto, List<PoiType> availableTypes) {
        if (dto.getTagsDtoList() != null) {
            for (PoiType type : availableTypes) {
                int tagsWithValues = 0;
                int matchingTags = 0;
                for (PoiTypeTag poiTypeTag : type.getTags()) {
                    if (poiTypeTag.getValue() != null) {
                        tagsWithValues++;
                        for (TagDto tagDto : dto.getTagsDtoList()) {
                            if (tagDto.getKey().equals(poiTypeTag.getKey())) {
                                if (tagDto.getValue().equals(poiTypeTag.getValue())) {
                                    matchingTags++;
                                }
                            }
                        }
                    }
                }
                if (tagsWithValues == matchingTags) {
                    return type;
                }
            }
        }
        return null;
    }

}
