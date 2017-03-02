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
package io.jawg.osmcontributor.rest.dtos.osm;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import java.util.List;

@Root(name = "osm", strict = false)
public class OsmDto {
    @ElementList(inline = true, required = false)
    private List<NodeDto> nodeDtoList;

    @ElementList(inline = true, required = false)
    private List<NoteDto> noteDtoList;

    @ElementList(inline = true, required = false)
    private List<WayDto> wayDtoList;

    @Element(name = "changeset", required = false)
    private ChangeSetDto changeSetDto;

    @Element(name = "permissions", required = false)
    private PermissionsDto permissionsDto;

    public OsmDto() {
    }

    public ChangeSetDto getChangeSetDto() {
        return changeSetDto;
    }

    public void setChangeSetDto(ChangeSetDto changeSetDto) {
        this.changeSetDto = changeSetDto;
    }

    public List<NodeDto> getNodeDtoList() {
        return nodeDtoList;
    }

    public void setNodeDtoList(List<NodeDto> nodeDtoList) {
        this.nodeDtoList = nodeDtoList;
    }

    public List<WayDto> getWayDtoList() {
        return wayDtoList;
    }

    public void setWayDtoList(List<WayDto> wayDtoList) {
        this.wayDtoList = wayDtoList;
    }

    public PermissionsDto getPermissionsDto() {
        return permissionsDto;
    }

    public List<NoteDto> getNoteDtoList() {
        return noteDtoList;
    }

    public void setNoteDtoList(List<NoteDto> noteDtoList) {
        this.noteDtoList = noteDtoList;
    }

    public void setPermissionsDto(PermissionsDto permissionsDto) {
        this.permissionsDto = permissionsDto;
    }


    @Override
    public String toString() {
        return "OsmDto{" +
                "nodeDtoList=" + nodeDtoList +
                ", wayDtoList=" + wayDtoList +
                ", changeSetDto=" + changeSetDto +
                '}';
    }
}

