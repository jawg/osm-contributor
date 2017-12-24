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
package io.jawg.osmcontributor.model.entities;


import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;
import com.mapbox.mapboxsdk.geometry.LatLng;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.jawg.osmcontributor.utils.core.MapElement;
import timber.log.Timber;

@DatabaseTable(tableName = Poi.TABLE_NAME)
public class Poi implements Cloneable, MapElement {
    public static final String TABLE_NAME = "POI";

    public static final String ID = "ID";
    public static final String LONGITUDE = "LONGITUDE";
    public static final String LATITUDE = "LATITUDE";
    public static final String NAME = "NAME";
    public static final String BACKEND_ID = "BACKEND_ID";
    public static final String VERSION = "VERSION";
    public static final String UPDATE_DATE = "UPDATE_DATE";
    public static final String VISIBLE = "VISIBLE";
    public static final String UPDATED = "UPDATED";
    public static final String WAY = "WAY";
    public static final String TO_DELETE = "TO_DELETE";
    public static final String LEVEL = "LEVEL";
    public static final String POI_TYPE_ID = "POI_TYPE_ID";
    public static final String OLD = "OLD";
    public static final String OLD_POI_ID = "OLD_POI_ID";

    public enum State {
        NORMAL, SELECTED, MOVING, NOT_SYNCED
    }

    public static State computeState(boolean selected, boolean edition, boolean needsSync) {

        if (edition) {
            return State.MOVING;
        }

        if (selected) {
            return State.SELECTED;
        }

        if (needsSync) {
            return State.NOT_SYNCED;
        }

        return State.NORMAL;
    }

    @DatabaseField(generatedId = true, columnName = ID)
    private Long id;

    @DatabaseField(columnName = LONGITUDE, canBeNull = false, index = true)
    private Double longitude;

    @DatabaseField(columnName = LATITUDE, canBeNull = false, index = true)
    private Double latitude;

    @DatabaseField(columnName = NAME)
    private String name;

    @DatabaseField(columnName = BACKEND_ID)
    private String backendId;

    @DatabaseField(columnName = VERSION)
    private String version = "1";

    @DatabaseField(columnName = UPDATE_DATE)
    private DateTime updateDate;

    @DatabaseField(columnName = VISIBLE)
    private Boolean visible;

    @DatabaseField(columnName = OLD)
    private boolean old;

    @DatabaseField(columnName = UPDATED, canBeNull = false)
    private Boolean updated;

    @DatabaseField(columnName = WAY, canBeNull = false)
    private Boolean way = false;

    @DatabaseField(columnName = TO_DELETE, canBeNull = false)
    private Boolean toDelete = false;

    @DatabaseField(columnName = LEVEL)
    private String level;

    @DatabaseField(columnName = OLD_POI_ID)
    private Long oldPoiId;

    @DatabaseField(foreign = true, columnName = POI_TYPE_ID, foreignAutoRefresh = true)
    private PoiType type;

    @ForeignCollectionField
    private Collection<PoiTag> tags = new ArrayList<>();

    @ForeignCollectionField(orderColumnName = PoiNodeRef.ORDINAL)
    private Collection<PoiNodeRef> nodeRefs = new ArrayList<>();

    private Set<Double> levels = null;

    public Poi() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBackendId() {
        return backendId;
    }

    public void setBackendId(String backendId) {
        this.backendId = backendId;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public DateTime getUpdateDate() {
        return updateDate;
    }

    public void setUpdateDate(DateTime updateDate) {
        this.updateDate = updateDate;
    }

    public Boolean getVisible() {
        return visible;
    }

    public void setVisible(Boolean visible) {
        this.visible = visible;
    }

    public Boolean getUpdated() {
        return updated;
    }

    public void setUpdated(Boolean updated) {
        this.updated = updated;
    }

    public Boolean getWay() {
        return way;
    }

    public void setWay(Boolean way) {
        this.way = way;
    }

    public PoiType getType() {
        return type;
    }

    public void setType(PoiType type) {
        this.type = type;
    }

    public Collection<PoiTag> getTags() {
        return tags;
    }

    public void setTags(Collection<PoiTag> tags) {
        this.tags = tags;
    }

    public Collection<PoiNodeRef> getNodeRefs() {
        return nodeRefs;
    }

    public void setNodeRefs(Collection<PoiNodeRef> nodeRefs) {
        this.nodeRefs = nodeRefs;
    }

    public Boolean getToDelete() {
        return toDelete;
    }

    public void setToDelete(Boolean toDelete) {
        this.toDelete = toDelete;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public boolean getOld() {
        return old;
    }

    public void setOld(boolean old) {
        this.old = old;
    }

    public Long getOldPoiId() {
        return oldPoiId;
    }

    public void setOldPoiId(Long oldPoiId) {
        this.oldPoiId = oldPoiId;
    }

    public LatLng getPosition() {
        return new LatLng(latitude, longitude);
    }

    //fill levels set if there isn't any levels adding level 0
    private void initLevel() {
        levels = getLevelsFromString();
        if (levels.isEmpty()) {
            levels.add(0d);
        }
    }

    private Set<Double> getLevelsFromString() {
        Set<Double> dlevels = new HashSet<>();
        if (level == null) {
            dlevels.add(0d);

        } else {
            // levels from OSM looks like "0.5;2;-2"
            String[] levels = level.split(";");

            //convert the string in a set of double
            for (String strLevel : levels) {
                try {
                    double dlevel = Double.parseDouble(strLevel);
                    dlevels.add(dlevel);
                } catch (NumberFormatException e) {
                    dlevels.clear();
                    dlevels.add(0d);
                }
            }
        }
        return dlevels;
    }

    public void setLevel(Set<Double> level) {
        if (this.levels != null) {
            this.levels.clear();
        } else {
            this.levels = new HashSet<>();
        }
        levels.addAll(level);
        setLevelTag(level);
    }

    //save the level in a tag "level"
    private void setLevelTag(Set<Double> levels) {
        if (!(levels.size() == 1 && levels.contains(0d))) {
            boolean hasTagLevel = false;
            String strLevels = "";
            int i = 1;

            for (Double d : levels) {
                strLevels += d;
                if (i != levels.size()) {
                    strLevels += ";";
                }
                i++;
            }

            for (PoiTag poiTag : tags) {
                if ("level".equals(poiTag.getKey())) {
                    poiTag.setValue(strLevels);
                    hasTagLevel = true;
                    break;
                }
            }

            if (!hasTagLevel) {
                PoiTag tempPoiTag = new PoiTag();
                tempPoiTag.setKey("level");
                tempPoiTag.setValue(strLevels);
                tempPoiTag.setPoi(this);
                tags.add(tempPoiTag);
            }
        }
    }

    public boolean isAtLevel(Double level) {
        initLevel();
        return levels.contains(level);
    }

    public boolean isOnLevels(Double[] levels) {
        initLevel();
        for (Double d : levels) {
            if (this.levels.contains(d)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return "Poi{" +
                "id=" + id +
                ", longitude=" + longitude +
                ", latitude=" + latitude +
                ", name='" + name + '\'' +
                ", backendId='" + backendId + '\'' +
                ", version='" + version + '\'' +
                ", updateDate=" + updateDate +
                ", visible=" + visible +
                ", old=" + old +
                ", updated=" + updated +
                ", way=" + way +
                ", toDelete=" + toDelete +
                ", level='" + level + '\'' +
                ", type=" + type +
                ", tags=" + tags +
                ", nodeRefs=" + nodeRefs +
                ", levels=" + levels +
                '}';
    }

    public Map<String, String> getTagsMap() {
        Map<String, String> hashMap = new HashMap<>();
        if (tags != null) {
            for (PoiTag poiTag : tags) {
                hashMap.put(poiTag.getKey(), poiTag.getValue());
            }
        }
        return hashMap;
    }

    public void applyChanges(Map<String, String> tagsMap) {
        if (tags == null) {
            tags = new ArrayList<>();
        }

        List<PoiTag> tagsToDelete = new ArrayList<>();

        // Apply the new values to the existing tags
        for (PoiTag poiTag : tags) {
            String newValue = tagsMap.remove(poiTag.getKey());
            if (newValue != null) {
                if (newValue.trim().isEmpty()) {
                    tagsToDelete.add(poiTag);
                    if ("name".equalsIgnoreCase(poiTag.getKey())) {
                        setName("");
                    }
                    if ("level".equalsIgnoreCase(poiTag.getKey())) {
                        setLevel("");
                    }
                } else if (!newValue.equals(poiTag.getValue())) {
                    poiTag.setValue(newValue);
                }
            }
        }

        tags.removeAll(tagsToDelete);

        // Add the new tags to the Poi
        for (Map.Entry<String, String> tagToAdd : tagsMap.entrySet()) {
            //The default level is zero, we don't send this tag if it's the default value
            if (!("level".equals(tagToAdd.getKey()) && "0".equals(tagToAdd.getValue()))) {
                PoiTag tempPoiTag = new PoiTag();
                tempPoiTag.setKey(tagToAdd.getKey());
                tempPoiTag.setValue(tagToAdd.getValue());
                tempPoiTag.setPoi(this);
                tags.add(tempPoiTag);
            }
        }

        // Set the name and level of the Poi to the value of the tags
        for (PoiTag tag : tags) {
            if ("name".equalsIgnoreCase(tag.getKey())) {
                setName(tag.getValue());
            }
            if ("level".equalsIgnoreCase(tag.getKey())) {
                setLevel(tag.getValue());
            }
        }
    }

    public boolean hasChanges(Map<String, String> tagsMap) {
        if (tags == null) {
            tags = new ArrayList<>();
        }

        int count = tagsMap.entrySet().size();

        // Apply the new values to the existing tags
        for (PoiTag poiTag : tags) {
            String newValue = tagsMap.get(poiTag.getKey());
            if (newValue != null) {
                count--;
                if (!newValue.equals(poiTag.getValue())) {
                    return true;
                }
            }
        }
        return count > 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Poi poi = (Poi) o;

        return backendId != null ? (backendId.equals(poi.backendId) && old == poi.old) : id != null && id.equals(poi.id);
    }

    @Override
    public int hashCode() {
        return backendId != null ? backendId.hashCode() : super.hashCode();
    }

    public Poi getCopy() {
        Poi poi;

        try {
            poi = (Poi) clone();
        } catch (CloneNotSupportedException e) {
            Timber.e(e, "could not clone Poi");
            return null;
        }

        poi.setId(null);

        List<PoiTag> poiTagsOld = new ArrayList<>();
        for (PoiTag poiTag : getTags()) {
            PoiTag poiTagOld = new PoiTag();
            poiTagOld.setValue(poiTag.getValue());
            poiTagOld.setKey(poiTag.getKey());
            poiTagsOld.add(poiTagOld);
        }
        poi.setTags(poiTagsOld);

        List<PoiNodeRef> poiNodeRefsOld = new ArrayList<>();
        for (PoiNodeRef poiNodeRef : getNodeRefs()) {
            PoiNodeRef poiNodeRefOld = new PoiNodeRef();
            poiNodeRefOld.setLatitude(poiNodeRef.getLatitude());
            poiNodeRefOld.setLongitude(poiNodeRef.getLongitude());
            poiNodeRefOld.setNodeBackendId(poiNodeRef.getNodeBackendId());
            poiNodeRefOld.setOrdinal(poiNodeRef.getOrdinal());
            poiNodeRefOld.setUpdated(poiNodeRef.getUpdated());

            poiNodeRefsOld.add(poiNodeRefOld);
        }
        poi.setNodeRefs(poiNodeRefsOld);

        return poi;
    }

}
