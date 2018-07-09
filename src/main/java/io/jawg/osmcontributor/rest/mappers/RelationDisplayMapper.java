package io.jawg.osmcontributor.rest.mappers;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import io.jawg.osmcontributor.model.entities.relation_display.RelationDisplay;
import io.jawg.osmcontributor.model.entities.relation_display.RelationDisplayTag;
import io.jawg.osmcontributor.rest.dtos.osm.RelationDto;
import io.jawg.osmcontributor.rest.dtos.osm.TagDto;

public class RelationDisplayMapper {
   @Inject
    public RelationDisplayMapper(){

    }

    public List<RelationDisplay> convertDTOstoRelationDisplays(List<RelationDto> dtos) {
        List<RelationDisplay> result = new ArrayList<>();
        if (dtos != null) {
            for (RelationDto dto : dtos) {
                RelationDisplay relation = convertDTOtoRelation(dto);
                if (relation != null) {
                    result.add(relation);
                }
            }
        }
        return result;
    }

    public RelationDisplay convertDTOtoRelation(RelationDto dto) {
        List<RelationDisplayTag> tags = new ArrayList<>();

        RelationDisplay relation = new RelationDisplay();
        relation.setBackendId(dto.getId());

        for (TagDto tagDto : dto.getTagsDtoList()) {
            RelationDisplayTag tag = new RelationDisplayTag();
            tag.setRelationDisplay(relation);
            tag.setKey(tagDto.getKey());
            tag.setValue(tagDto.getValue());
            tags.add(tag);
        }
        relation.setTags(tags);

        return relation;
    }

}
