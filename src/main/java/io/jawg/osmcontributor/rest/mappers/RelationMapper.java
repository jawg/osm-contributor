package io.jawg.osmcontributor.rest.mappers;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import io.jawg.osmcontributor.model.entities.relation.FullOSMRelation;
import io.jawg.osmcontributor.model.entities.relation.RelationMember;
import io.jawg.osmcontributor.model.entities.relation.RelationTag;
import io.jawg.osmcontributor.rest.dtos.osm.RelationDto;
import io.jawg.osmcontributor.rest.dtos.osm.RelationMemberDto;
import io.jawg.osmcontributor.rest.dtos.osm.TagDto;

public class RelationMapper {
    @Inject
    public RelationMapper() {

    }

    public List<FullOSMRelation> convertDTOstoRelations(List<RelationDto> dtos) {
        List<FullOSMRelation> result = new ArrayList<>();
        if (dtos != null) {
            for (RelationDto dto : dtos) {
                FullOSMRelation fullOSMRelation = convertDTOtoRelation(dto);
                if (fullOSMRelation != null) {
                    result.add(fullOSMRelation);
                }
            }
        }
        return result;
    }

    private FullOSMRelation convertDTOtoRelation(RelationDto dto) {
        List<RelationMember> relationMembers = new ArrayList<>();
        List<RelationTag> tags = new ArrayList<>();


        FullOSMRelation fullOSMRelation = new FullOSMRelation();
        fullOSMRelation.setBackendId(dto.getId());
        fullOSMRelation.setChangeset(dto.getChangeset());
        fullOSMRelation.setUpdated(false);
        fullOSMRelation.setVersion(String.valueOf(dto.getVersion()));
        fullOSMRelation.setUpdateDate(dto.getTimestamp());

        for (RelationMemberDto re : dto.getMemberDTOlist()) {
            RelationMember relationMember = new RelationMember();
            relationMember.setFullOSMRelation(fullOSMRelation);
            if (re.getRef() != null)
                relationMember.setRef(re.getRef());
            if (re.getRole() != null)
                relationMember.setRole(re.getRole());
            if (re.getType() != null)
                relationMember.setType(re.getType());
            relationMembers.add(relationMember);
        }
        fullOSMRelation.setMembers(relationMembers);

        for (TagDto tagDto : dto.getTagsDtoList()) {
            RelationTag tag = new RelationTag();
            tag.setRelationDisplay(fullOSMRelation);
            if (tagDto.getKey() != null && tagDto.getValue() != null) {
                tag.setKey(tagDto.getKey());
                tag.setValue(tagDto.getValue());
            }
            tags.add(tag);
            if (tag.getKey().equals("name")) {
                fullOSMRelation.setName(tag.getValue());
            }
        }
        fullOSMRelation.setTags(tags);
        return fullOSMRelation;
    }

    public RelationDto convertRelationToDTO(FullOSMRelation fullOSMRelation, String changeSetId) {
        RelationDto relationDto = new RelationDto();
        List<RelationMemberDto> relationMemberDtos = new ArrayList<>();
        List<TagDto> tagDtos = new ArrayList<>();

        if (fullOSMRelation.getMembers() != null) {
            for (RelationMember re : fullOSMRelation.getMembers()) {
                RelationMemberDto relationMemberDto = new RelationMemberDto();
                relationMemberDto.setRef(re.getRef());
                relationMemberDto.setRole(re.getRole());
                relationMemberDto.setType(re.getType());
                relationMemberDtos.add(relationMemberDto);
            }
        }

        if (fullOSMRelation.getTags() != null) {
            for (RelationTag ta : fullOSMRelation.getTags()) {
                TagDto tagDto = new TagDto();
                tagDto.setKey(ta.getKey());
                tagDto.setValue(ta.getValue());
                tagDtos.add(tagDto);
            }
        }
        relationDto.setMemberDTOlist(relationMemberDtos);
        relationDto.setTagsDtoList(tagDtos);
        relationDto.setId(fullOSMRelation.getBackendId());
        relationDto.setChangeset(changeSetId);
        relationDto.setVersion(Integer.valueOf(fullOSMRelation.getVersion()));

        return relationDto;
    }

}
