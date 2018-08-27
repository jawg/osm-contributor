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
package io.jawg.osmcontributor.ui.adapters;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.util.Pair;
import android.view.ViewGroup;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import javax.inject.Inject;

import io.jawg.osmcontributor.OsmTemplateApplication;
import io.jawg.osmcontributor.model.entities.Poi;
import io.jawg.osmcontributor.model.entities.relation_display.RelationDisplay;
import io.jawg.osmcontributor.model.entities.relation_save.RelationEdition;
import io.jawg.osmcontributor.ui.adapters.binding.AutoCompleteViewBinder;
import io.jawg.osmcontributor.ui.adapters.binding.BusLinesViewBinder;
import io.jawg.osmcontributor.ui.adapters.binding.CheckedTagViewBinder;
import io.jawg.osmcontributor.ui.adapters.binding.ConstantViewBinder;
import io.jawg.osmcontributor.ui.adapters.binding.OpeningHoursViewBinder;
import io.jawg.osmcontributor.ui.adapters.binding.RadioChoiceViewBinder;
import io.jawg.osmcontributor.ui.adapters.binding.ShelterChoiceViewBinder;
import io.jawg.osmcontributor.ui.adapters.binding.TagViewBinder;
import io.jawg.osmcontributor.ui.adapters.item.shelter.SingleTagItem;
import io.jawg.osmcontributor.ui.adapters.item.shelter.TagItem;
import io.jawg.osmcontributor.ui.adapters.parser.OpeningTimeValueParser;
import io.jawg.osmcontributor.ui.events.edition.PleaseApplyOpeningTimeChange;
import io.jawg.osmcontributor.utils.StringUtils;
import io.jawg.osmcontributor.utils.edition.PoiChanges;

import static io.jawg.osmcontributor.ui.adapters.item.shelter.TagMapper.getTagItems;

public class TagsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements CheckedTagViewBinder.TagItemChangeListener {
    private final BusLinesViewBinder busLinesViewBinder;
    private List<TagItem> tagItemList;
    private List<RelationEdition> relationEditions = new ArrayList<>();
    private List<CheckedTagViewBinder> checkedViews = new ArrayList<>();
    private Poi poi;
    private boolean change = false;
    private List<TagViewBinder> viewBinders = new ArrayList<>();

    @Inject
    EventBus eventBus;

    @Inject
    OpeningTimeValueParser openingTimeValueParser;

    public TagsAdapter(Poi poi, List<TagItem> tagItemList, Activity activity, Map<String, List<String>> tagValueSuggestionsMap, boolean expertMode) {
        this.poi = poi;
        ((OsmTemplateApplication) activity.getApplication()).getOsmTemplateComponent().inject(this);

        if (tagItemList == null) {
            this.tagItemList = getTagItems(poi, tagValueSuggestionsMap, expertMode);
        } else {
            this.tagItemList = tagItemList;
            notifyDataSetChanged();
        }

        viewBinders.add(new ShelterChoiceViewBinder(activity, this));
        viewBinders.add(new AutoCompleteViewBinder(activity, this));
        viewBinders.add(new ConstantViewBinder(activity));
        viewBinders.add(new OpeningHoursViewBinder(activity, this));
        viewBinders.add(new RadioChoiceViewBinder(activity, this));
        busLinesViewBinder = new BusLinesViewBinder(activity, this);
        viewBinders.add(busLinesViewBinder);

        eventBus.register(this);
    }

    /*=========================================*/
    /*---------------ADAPTER-------------------*/
    /*=========================================*/
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        TagViewBinder t = pickViewBinder(TagItem.Type.values()[viewType]);
        if (t == null) {
            throw new IllegalStateException("Invalid view type");
        }
        return t.onCreateViewHolder(parent);
    }

    /**
     * Given the type of the Tag, check if it corresponds to
     * a supported viewBinder
     *
     * @param type TagItem type
     * @return Return the supported viewBinder or null
     */
    private TagViewBinder pickViewBinder(TagItem.Type type) {
        for (TagViewBinder t : viewBinders) {
            if (t.supports(type)) {
                return t;
            }
        }
        return null;
    }

    @Override
    public int getItemViewType(int position) {
        return tagItemList.get(position).getTagType().ordinal();
    }

    @Override
    public int getItemCount() {
        return tagItemList.size();
    }

    /*=========================================*/
    /*------------CODE-------------------------*/
    /*=========================================*/

    /**
     * Add an element at the end of the List.
     *
     * @return the position of the inserted element
     */
    public int addLast(String key, String value, Map<String, String> possibleValues) {
        TagItem tagItem = new SingleTagItem.SingleTagItemBuilder(key, value).mandatory(false)
                .values(possibleValues)
                .type(key.contains("hours") ? TagItem.Type.OPENING_HOURS : TagItem.Type.TEXT)
                .isConform(true)
                .show(true).build();
        // Add into the list
        tagItemList.add(tagItemList.size(), tagItem);

        // Notify changes
        notifyItemInserted(tagItemList.size() - 1);
        change = true;
        return tagItemList.size() - 1;
    }


    public boolean isValidChanges() {
        for (TagItem tagItem : tagItemList) {
            if (tagItem.isMandatory() && StringUtils.isEmpty(tagItem.getValue())) {
                return false;
            }
        }
        return true;
    }

    public void refreshErrorStatus() {
        notifyDataSetChanged();
    }

    /*=========================================*/
    /*---------------HOLDERS-------------------*/
    /*=========================================*/
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder,
                                 int position) {
        TagItem tagItem = tagItemList.get(position);
        TagViewBinder tagViewBinder = pickViewBinder(tagItem.getTagType());
        if (tagViewBinder == null) {
            throw new IllegalStateException("Invalid tag type. Should not happen");
        }
        tagViewBinder.onBindViewHolder(holder, tagItem);

        // Save Holder for checking operations later
        if (tagViewBinder instanceof CheckedTagViewBinder) {
            checkedViews.add((CheckedTagViewBinder) tagViewBinder);
        }
    }

    /*=========================================*/
    /*------------GETTER/SETTER----------------*/
    /*=========================================*/

    /**
     * Add an element at the end of the List
     *
     * @return The position of the inserted element
     */
    public PoiChanges getPoiChanges() {
        PoiChanges result = new PoiChanges(poi.getId());
        for (TagItem tagItem : tagItemList) {
            // Only mandatory tags and optional tags that have been changed are saved
            if (tagItem.isMandatory() || (tagItem.getValue() != null)) {
                result.getTagsMap().putAll(tagItem.getOsmValues());
            }
        }
        return result;
    }

    public List<RelationEdition> getChangedRelations() {
        return relationEditions;
    }

    public boolean isChange() {
        for (TagItem tagItem : tagItemList) {
            if (tagItem.hasChanged()) {
                return true;
            }
        }
        if (relationEditions.size() > 0) {
            return true;
        }
        return change;
    }

    @Override
    public void onTagItemUpdated(TagItem updatedTag) {
        for (TagItem tagItem : tagItemList) {
            if (tagItem.getKey().compareTo(updatedTag.getKey()) == 0) {
                editTag(tagItem, updatedTag.getValue());
            }
        }
    }

    @Override
    public void onRelationForBusUpdated(Pair<RelationDisplay, RelationEdition.RelationModificationType> relationIDAndModification) {
        //first remove all previous modification made on the relation
        ListIterator<RelationEdition> iter = relationEditions.listIterator();
        while (iter.hasNext()) {
            if (iter.next().getBackendId().equals(relationIDAndModification.first.getBackendId())) {
                iter.remove();
            }
        }
        relationEditions.add(new RelationEdition(relationIDAndModification.first.getBackendId(), poi, relationIDAndModification.second));
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onPleaseApplyOpeningTimeChange(PleaseApplyOpeningTimeChange event) {
        eventBus.removeStickyEvent(event);
        for (TagItem tagItem : tagItemList) {
            if (tagItem.getKey().compareTo("opening_hours") == 0) {
                editTag(tagItem, openingTimeValueParser.toValue(event.getOpeningTime()));
            }
        }
    }

    /**
     * Edit tag value
     *
     * @param tagItem  tag item to edit
     * @param newValue new value
     */
    private void editTag(TagItem tagItem, String newValue) {
        // If event comes from SingleChoiceTag
        if (tagItem.getTagType() == TagItem.Type.SINGLE_CHOICE) {
            // Get the key from value, safe cause no duplicate
            String key = "";
            for (Map.Entry<String, String> entry : tagItem.getValues().entrySet()) {
                if (newValue.equals(entry.getValue())) {
                    key = entry.getKey();
                    break;
                }
            }
            tagItem.setValue(key);
        } else {
            tagItem.setValue(newValue);
        }
        change = true;
    }

    public void setRelationDisplays(List<RelationDisplay> relationDisplays) {
        busLinesViewBinder.setCurrentBusLines(relationDisplays);
        for (TagItem tagItem : tagItemList) {
            if (tagItem.getType() == TagItem.Type.BUS_LINE) {
                notifyItemChanged(tagItemList.indexOf(tagItem));
                break;
            }
        }
    }

    public void setRelationDisplaysNearby(List<RelationDisplay> relationDisplays) {
        busLinesViewBinder.setBusLinesNearby(relationDisplays);
    }
}