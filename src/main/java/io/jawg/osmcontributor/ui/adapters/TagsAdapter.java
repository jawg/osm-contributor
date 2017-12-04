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
import android.view.ViewGroup;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import io.jawg.osmcontributor.OsmTemplateApplication;
import io.jawg.osmcontributor.model.entities.Poi;
import io.jawg.osmcontributor.ui.adapters.binding.AutoCompleteViewBinder;
import io.jawg.osmcontributor.ui.adapters.binding.CheckedTagViewBinder;
import io.jawg.osmcontributor.ui.adapters.binding.ConstantViewBinder;
import io.jawg.osmcontributor.ui.adapters.binding.OpeningHoursViewBinder;
import io.jawg.osmcontributor.ui.adapters.binding.RadioChoiceViewBinder;
import io.jawg.osmcontributor.ui.adapters.binding.ShelterChoiceViewBinder;
import io.jawg.osmcontributor.ui.adapters.binding.TagViewBinder;
import io.jawg.osmcontributor.ui.adapters.item.SingleTagItem;
import io.jawg.osmcontributor.ui.adapters.item.TagItem;
import io.jawg.osmcontributor.ui.adapters.parser.OpeningTimeValueParser;
import io.jawg.osmcontributor.utils.StringUtils;
import io.jawg.osmcontributor.utils.edition.PoiChanges;

import static io.jawg.osmcontributor.ui.adapters.item.TagMapper.getTagItems;

public class TagsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private List<TagItem> tagItemList = new ArrayList<>();
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

        viewBinders.add(new ShelterChoiceViewBinder(activity));
        viewBinders.add(new AutoCompleteViewBinder(activity));
        viewBinders.add(new ConstantViewBinder(activity));
        viewBinders.add(new OpeningHoursViewBinder(activity));
        viewBinders.add(new RadioChoiceViewBinder(activity));
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

    public void showInvalidityForAll() {
        for (CheckedTagViewBinder binder : checkedViews) {
            binder.showValidation();
        }
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

    public boolean isChange() {
        for (TagItem tagItem : tagItemList) {
            if (tagItem.hasChanged()) {
                return true;
            }
        }
        return change;
    }
}
