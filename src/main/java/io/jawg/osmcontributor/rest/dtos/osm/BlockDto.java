package io.jawg.osmcontributor.rest.dtos.osm;

import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import java.util.List;

/**
 * This class is made to fit the custom request fetching nodes with their relations
 * <block>
 * <node></node> (one node)
 * <relation id=xxx ></relation> (0 or more relations)
 * <relation id=xxx ></relation>
 * <relation id=xxx ></relation>
 * </block>
 * and then the relations to display with their tags
 * <relation>
 *     <tag></tag>
 * </relation>
 */
@Root(name = "block", strict = false)
public class BlockDto {
    @ElementList(inline = true, required = false)
    private List<NodeDto> nodeDtoList;


    @ElementList(inline = true, required = false)
    private List<RelationIdDto> relationIdDtoList;

    @Override
    public String toString() {
        return "BlockDto{" +
                "nodeDtoList=" + nodeDtoList +
                ", relationIdDtoList=" + relationIdDtoList +
                '}';
    }

    public List<NodeDto> getNodeDtoList() {
        return nodeDtoList;
    }

    public void setNodeDtoList(List<NodeDto> nodeDtoList) {
        this.nodeDtoList = nodeDtoList;
    }

    public List<RelationIdDto> getRelationIdDtoList() {
        return relationIdDtoList;
    }

    public void setRelationIdDtoList(List<RelationIdDto> relationIdDtoList) {
        this.relationIdDtoList = relationIdDtoList;
    }


}
