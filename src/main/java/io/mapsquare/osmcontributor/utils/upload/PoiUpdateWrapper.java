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
package io.mapsquare.osmcontributor.utils.upload;

import android.text.Layout;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.mapsquare.osmcontributor.model.entities.Poi;
import io.mapsquare.osmcontributor.model.entities.PoiNodeRef;
import io.mapsquare.osmcontributor.model.entities.PoiTag;

public class PoiUpdateWrapper {

    public enum PoiAction {
        CREATE,
        UPDATE,
        DELETED
    }

    private Poi newPoi;
    private Poi oldPoi;
    private PoiNodeRef nodeRef;
    private PoiAction action;
    private final Boolean isPoi;
    private List<PoiDiffWrapper> poiDiff = new ArrayList<>();
    private boolean open = false;
    private boolean selected = true;


    public PoiUpdateWrapper(boolean isPoi, Poi newPoi, Poi oldPoi, PoiNodeRef nodeRef, PoiAction action) {
        this.oldPoi = oldPoi;
        this.newPoi = newPoi;
        this.nodeRef = nodeRef;
        this.isPoi = isPoi;
        this.action = action;
        initDescriptions();
    }

    public String getName() {
        return newPoi == null ? oldPoi == null ? "" : oldPoi.getName() : newPoi.getName();
    }

    public String getPoiType() {
        return newPoi == null ? oldPoi == null ? "" : oldPoi.getType().getName() : newPoi.getType().getName();
    }

    public Long getId() {
        if (isPoi) {
            return newPoi == null ? oldPoi == null ? null : oldPoi.getId() : newPoi.getId();
        }
        return nodeRef == null ? null : nodeRef.getId();
    }

    public Poi getNewPoi() {
        return newPoi;
    }

    public void setNewPoi(Poi newPoi) {
        this.newPoi = newPoi;
    }

    public PoiAction getAction() {
        return action;
    }

    public void setAction(PoiAction action) {
        this.action = action;
    }

    public PoiNodeRef getNodeRef() {
        return nodeRef;
    }

    public Boolean getIsPoi() {
        return isPoi;
    }

    public Layout getDetailContent() {
        return null;
    }

    public List<PoiDiffWrapper> getPoiDiff() {
        return poiDiff;
    }

    public void setPoiDiff(List<PoiDiffWrapper> poiDiff) {
        this.poiDiff = poiDiff;
    }

    public boolean isOpen() {
        return open;
    }

    public void setOpen(boolean open) {
        this.open = open;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    private void initDescriptions() {
        Collection<PoiTag> oldTags = oldPoi == null ? new ArrayList<PoiTag>() : oldPoi.getTags();
        Collection<PoiTag> newTags = newPoi == null ? new ArrayList<PoiTag>() : newPoi.getTags();
        Map<String, String> newTagsMap = new HashMap<>();

        //if the poi is deleted there is not any new values
        if (action != PoiAction.DELETED) {
            // add all new tags in a map
            for (PoiTag poiTag : newTags) {
                newTagsMap.put(poiTag.getKey(), poiTag.getValue());
            }
        }

        // add all old tags with the new value if there is one
        for (PoiTag poiTagOld : oldTags) {
            String key = poiTagOld.getKey();
            String newTagValue = null;
            if (newTagsMap.containsKey(key)) {
                newTagValue = newTagsMap.remove(key);
            }
            poiDiff.add(new PoiDiffWrapper(key, poiTagOld.getValue(), newTagValue));
        }

        //adding all tags created by the user
        for (String key : newTagsMap.keySet()) {
            poiDiff.add(new PoiDiffWrapper(key, null, newTagsMap.get(key)));
        }
    }

    public boolean isPositionChanged() {
        if (oldPoi == null || newPoi == null) {
            return false;
        }
        return !oldPoi.getLatitude().equals(newPoi.getLatitude()) || !oldPoi.getLongitude().equals(newPoi.getLongitude());
    }
}