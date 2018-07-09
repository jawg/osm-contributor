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
package io.jawg.osmcontributor.rest.dtos.osm;

import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import java.util.List;

@Root(name = "osm", strict = false)
public class OsmBlockDto implements OsmDtoInterface {

    @ElementList(inline = true, required = false)
    private List<BlockDto> blockList;

    @ElementList(inline = true, required = false)
    private List<RelationDto> relationDtoList;

    public OsmBlockDto() {
    }

    @Override
    public List<BlockDto> getBlockList() {
        return blockList;
    }

    public void setBlockList(List<BlockDto> blockList) {
        this.blockList = blockList;
    }

    @Override
    public List<RelationDto> getRelationDtoList() {
        return relationDtoList;
    }

    public void setRelationDtoList(List<RelationDto> relationDtoList) {
        this.relationDtoList = relationDtoList;
    }
}

