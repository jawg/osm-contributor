package io.jawg.osmcontributor.rest.dtos.osm;

import java.util.List;

public interface OsmDtoInterface {

    default List<BlockDto> getBlockList() {
        return null;
    }

    default List<NodeDto> getNodeDtoList() {
        return null;
    }

    default List<WayDto> getWayDtoList() {
        return null;
    }

    default List<RelationDto> getRelationDtoList() {
        return null;
    }


}
