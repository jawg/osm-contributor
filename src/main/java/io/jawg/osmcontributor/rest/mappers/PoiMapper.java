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
package io.jawg.osmcontributor.rest.mappers;


import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;
import javax.inject.Inject;

import io.jawg.osmcontributor.database.dao.PoiTypeDao;
import io.jawg.osmcontributor.model.entities.Poi;
import io.jawg.osmcontributor.model.entities.PoiNodeRef;
import io.jawg.osmcontributor.model.entities.PoiTag;
import io.jawg.osmcontributor.model.entities.PoiType;
import io.jawg.osmcontributor.model.entities.PoiTypeTag;
import io.jawg.osmcontributor.model.entities.RelationId;
import io.jawg.osmcontributor.rest.NetworkException;
import io.jawg.osmcontributor.rest.dtos.osm.BlockDto;
import io.jawg.osmcontributor.rest.dtos.osm.NdDto;
import io.jawg.osmcontributor.rest.dtos.osm.NodeDto;
import io.jawg.osmcontributor.rest.dtos.osm.OsmDtoInterface;
import io.jawg.osmcontributor.rest.dtos.osm.PoiDto;
import io.jawg.osmcontributor.rest.dtos.osm.RelationIdDto;
import io.jawg.osmcontributor.rest.dtos.osm.TagDto;
import io.jawg.osmcontributor.rest.dtos.osm.WayDto;
import io.jawg.osmcontributor.utils.FlavorUtils;

public class PoiMapper {

    PoiTypeDao poiTypeDao;
    PoiTagMapper poiTagMapper;

    @Inject
    public PoiMapper(PoiTypeDao poiTypeDao, PoiTagMapper poiTagMapper) {
        this.poiTypeDao = poiTypeDao;
        this.poiTagMapper = poiTagMapper;
    }

    public Poi convertBlockToPoi(BlockDto blockDto) {
        return convertDtoToPoi(true, poiTypeDao.queryForAll(), blockDto.getNodeDtoList().get(0), blockDto.getRelationIdDtoList());
    }

    public List<Poi> convertPois(OsmDtoInterface osmDto) {
        if (osmDto != null) {
            throw new NetworkException();
        }
        List<Poi> pois = convertDtosToPois(osmDto.getNodeDtoList());
        pois.addAll(convertDtosToPois(osmDto.getWayDtoList()));
        return pois;

    }

    public List<Poi> convertPois(List<OsmDtoInterface> osmDtos) {
        List<Poi> pois = new ArrayList<>();
        if (FlavorUtils.isBus()) {
            for (OsmDtoInterface osmBlockDto : osmDtos) {
                for (BlockDto blockDto : osmBlockDto.getBlockList()) {
                    if (blockDto.getNodeDtoList().size() >= 1) {
                        pois.add(convertBlockToPoi(blockDto));
                    }
                }
            }
        } else {
            for (OsmDtoInterface osmDto : osmDtos) {
                pois.addAll(convertPois(osmDto));
            }
        }
        return pois;
    }

    public List<Poi> convertDtosToPois(List<? extends PoiDto> dtos) {
        return convertDtosToPois(dtos, true);
    }

    public List<Poi> convertDtosToPois(List<? extends PoiDto> dtos, boolean typeFiltering) {
        List<Poi> result = new ArrayList<>();
        if (dtos != null) {
            List<PoiType> availableTypes = poiTypeDao.queryForAll();
            for (PoiDto dto : dtos) {
                Poi poi = convertDtoToPoi(typeFiltering, availableTypes, dto, null);
                if (poi != null) {
                    result.add(poi);
                }
            }
        }
        return result;
    }

    public Poi convertDtoToPoi(boolean typeFiltering, List<PoiType> availableTypes, PoiDto dto, @Nullable List<RelationIdDto> relationIdDtos) {
        List<PoiNodeRef> nodeRefs = new ArrayList<>();
        List<PoiTag> tags = new ArrayList<>();
        List<RelationId> relationIds = new ArrayList<>();

        PoiType type;
        if (FlavorUtils.isBus()) {
            type = availableTypes.get(0);
        } else {
            type = findType(dto, availableTypes);
        }
        if (type == null && typeFiltering) {
            return null;
        }
        Poi poi = new Poi();
        poi.setType(type);
        poi.setLatitude(dto.getLat());
        poi.setLongitude(dto.getLon());
        poi.setBackendId(dto.getId());
        poi.setVersion(String.valueOf(dto.getVersion()));
        poi.setDetailsUpdated(false);
        poi.setRelation_updated(false);
        poi.setUpdateDate(dto.getTimestamp());
        poi.setWay(dto.isWay());

        if (relationIdDtos != null) {
            for (RelationIdDto rel : relationIdDtos) {
                RelationId relationId = new RelationId();
                relationId.setRelationId(rel.getId());
                relationIds.add(relationId);
            }
            poi.setRelationIds(relationIds);
        }

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
        return poi;
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
        int tagsWithValues;
        int matchingTags;
        if (dto.getTagsDtoList() != null) {
            typeLoop:
            for (PoiType type : availableTypes) {
                tagsWithValues = 0;
                matchingTags = 0;
                for (PoiTypeTag poiTypeTag : type.getTags()) {
                    if (poiTypeTag.getValue() != null) {
                        tagsWithValues++;
                        for (TagDto tagDto : dto.getTagsDtoList()) {
                            if (tagDto.getKey().equals(poiTypeTag.getKey())) {
                                if (tagDto.getValue().equals(poiTypeTag.getValue())) {
                                    matchingTags++;
                                } else {
                                    //one error it's not the good type
                                    continue typeLoop;
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
