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
package io.mapsquare.osmcontributor.rest.dtos.osm;


import org.joda.time.DateTime;
import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import java.util.ArrayList;
import java.util.List;

@Root(name = "way", strict = false)
public class WayDto implements PoiDto {

    @Attribute(required = false)
    private String id;

    @Attribute(required = false)
    private String changeset;

    @Attribute(required = false)
    private DateTime timestamp;

    @Attribute(required = false)
    private Boolean visible;

    @Attribute(required = false)
    private int version;

    @Attribute(required = false)
    private String user;

    @Attribute(required = false)
    private String uid;

    @Element(name = "center", required = false)
    private CenterDto center;

    @ElementList(inline = true, required = false)
    private List<NdDto> ndDtoList;

    @ElementList(inline = true, required = false)
    private List<TagDto> tagsDtoList = new ArrayList<>();

    @Override
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getChangeset() {
        return changeset;
    }

    public void setChangeset(String changeset) {
        this.changeset = changeset;
    }

    @Override
    public DateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(DateTime timestamp) {
        this.timestamp = timestamp;
    }

    public Boolean getVisible() {
        return visible;
    }

    public void setVisible(Boolean visible) {
        this.visible = visible;
    }

    @Override
    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public CenterDto getCenter() {
        return center;
    }

    public void setCenter(CenterDto center) {
        this.center = center;
    }

    @Override
    public double getLat() {
        return center != null ? center.getLat() : 0;
    }

    @Override
    public double getLon() {
        return center != null ? center.getLon() : 0;
    }

    @Override
    public List<NdDto> getNdDtoList() {
        return ndDtoList;
    }

    public void setNdDtoList(List<NdDto> ndDtoList) {
        this.ndDtoList = ndDtoList;
    }

    @Override
    public List<TagDto> getTagsDtoList() {
        return tagsDtoList;
    }

    public void setTagsDtoList(List<TagDto> tagsDtoList) {
        this.tagsDtoList = tagsDtoList;
    }

    @Override
    public boolean isWay() {
        return true;
    }

    @Override
    public String toString() {
        return "WayDto{" +
                "id='" + id + '\'' +
                ", changeset='" + changeset + '\'' +
                ", timestamp='" + timestamp + '\'' +
                ", visible=" + visible +
                ", version=" + version +
                ", user='" + user + '\'' +
                ", uid='" + uid + '\'' +
                ", center=" + center +
                ", ndDtoList=" + ndDtoList +
                ", tagsDtoList=" + tagsDtoList +
                '}';
    }
}
