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
package io.mapsquare.osmcontributor.ui.adapters;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.Intent;
import android.support.design.widget.TextInputLayout;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.joda.time.LocalTime;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import io.mapsquare.osmcontributor.R;
import io.mapsquare.osmcontributor.model.entities.Poi;
import io.mapsquare.osmcontributor.model.entities.PoiTypeTag;
import io.mapsquare.osmcontributor.ui.activities.PickValueActivity;
import io.mapsquare.osmcontributor.ui.adapters.item.TagItem;
import io.mapsquare.osmcontributor.ui.adapters.parser.TagParser;
import io.mapsquare.osmcontributor.ui.dialogs.EditDaysTagDialogFragment;
import io.mapsquare.osmcontributor.ui.events.edition.PleaseApplyTagChange;
import io.mapsquare.osmcontributor.ui.events.edition.PleaseApplyTagChangeView;
import io.mapsquare.osmcontributor.ui.utils.views.holders.TagItemMultiChoiceViewHolder;
import io.mapsquare.osmcontributor.ui.utils.views.holders.TagItemOpeningHoursViewHolder;
import io.mapsquare.osmcontributor.ui.utils.views.holders.TagItemTextImposedViewHolder;
import io.mapsquare.osmcontributor.ui.utils.views.holders.TagItemTextViewHolder;
import io.mapsquare.osmcontributor.utils.ConfigManager;
import io.mapsquare.osmcontributor.utils.StringUtils;
import io.mapsquare.osmcontributor.utils.edition.PoiChanges;

public class TagsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final String TAG = "TagsAdapter";
    public static final int NB_AUTOCOMPLETE_LIMIT = 6;
    private List<TagItem> tagItemList = new ArrayList<>();
    private Poi poi;
    private Activity context;
    private EventBus eventBus;
    private ConfigManager configManager;
    private boolean change = false;
    private boolean expertMode = false;
    private TagParser tagParser;

    public TagsAdapter(Poi poi, List<TagItem> tagItemList, Activity context, TagParser tagParser, Map<String, List<String>> tagValueSuggestionsMap, ConfigManager configManager, boolean expertMode) {
        this.poi = poi;
        this.context = context;
        this.configManager = configManager;
        this.expertMode = expertMode;
        this.tagParser = tagParser;

        if (tagItemList == null) {
            loadTags(poi.getTagsMap(), tagValueSuggestionsMap);
        } else {
            this.tagItemList = tagItemList;
            notifyDataSetChanged();
        }

        this.eventBus = EventBus.getDefault();
        eventBus.register(this);
    }

    private void loadTags(Map<String, String> poiTags, Map<String, List<String>> tagValueSuggestionsMap) {
        int nbMandatory = 0;
        int nbImposed = 0;

        Map<String, TagItem.TagType> tagTypeMap = tagParser.getTagTypeMap(poi.getType());

        for (PoiTypeTag poiTypeTag : poi.getType().getTags()) {
            // Tags not in the PoiType should not be displayed if we are not in expert mode
            if (poiTypeTag.getValue() == null) {
                String key = poiTypeTag.getKey();
                // Display tags as mandatory if they are mandatory and we are not in expert mode
                if (poiTypeTag.getMandatory() && !expertMode) {
                    addTag(key, poiTags.remove(key), true, tagValueSuggestionsMap.get(key), nbMandatory + nbImposed, tagTypeMap.get(key), true);
                    nbMandatory++;
                } else {
                    addTag(key, poiTags.remove(key), false, tagValueSuggestionsMap.get(key), this.getItemCount(), tagTypeMap.get(key), true);
                }
            } else if (expertMode) {
                // Display the tags of the poi that are not in the PoiType
                String key = poiTypeTag.getKey();
                addTag(key, poiTags.remove(key), true, tagValueSuggestionsMap.get(key), nbImposed, tagTypeMap.get(key), false);
                nbImposed++;
            }
        }
        if (expertMode) {
            for (String key : poiTags.keySet()) {
                addTag(key, poiTags.get(key), false, Collections.singletonList(poiTags.get(key)), this.getItemCount(), tagTypeMap.get(key), true);
            }
        }
    }

    /**
     * @return a poiChange object containing all changes made by the user
     */
    public PoiChanges getPoiChanges() {
        PoiChanges result = new PoiChanges(poi.getId());
        for (TagItem tagItem : tagItemList) {
            // Only mandatory tags and optional tags that have been changed are saved
            if (tagItem.isMandatory() || tagItem.getValue() != null) {
                result.getTagsMap().put(tagItem.getKey(), tagItem.getValue());
            }
        }
        return result;
    }

    public List<TagItem> getTagItemList() {
        return tagItemList;
    }

    public boolean isChange() {
        return change;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        /* Create different views depending of type of the tag */
        switch (TagItem.TagType.values()[viewType]) {
            case TEXT_IMPOSED:
                View poiTagImposedLayout = LayoutInflater.from(parent.getContext()).inflate(R.layout.tag_item_text_imposed, parent, false);
                return new TagItemTextImposedViewHolder(poiTagImposedLayout);

            case MULTI_CHOICE:
                View poiTagFewValuesLayout = LayoutInflater.from(parent.getContext()).inflate(R.layout.tag_item_multi_choice, parent, false);
                return new TagItemMultiChoiceViewHolder(poiTagFewValuesLayout);

            case TEXT:
                View poiTagManyValuesLayout = LayoutInflater.from(parent.getContext()).inflate(R.layout.tag_item_text, parent, false);
                return new TagItemTextViewHolder(poiTagManyValuesLayout);

            case OPENING_HOURS:
                View poiTagOpeningHoursLayout = LayoutInflater.from(parent.getContext()).inflate(R.layout.tag_item_opening_hours, parent, false);
                return new TagItemOpeningHoursViewHolder(poiTagOpeningHoursLayout);

            default:
                return null;
        }
    }

    public void onBindViewHolder(final TagItemMultiChoiceViewHolder holder,
                                 final int position) {

        final TagItem tagItem = tagItemList.get(position);

        holder.getTextViewKey().setText(tagParser.parseTagName(tagItem.getKey()));
        holder.getTextViewValue().setText(tagItem.getValue());

        if (configManager.hasPoiModification()) {
            View.OnClickListener editTagClickListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, PickValueActivity.class);
                    intent.putExtra(PickValueActivity.KEY, tagParser.parseTagName(tagItem.getKey()));
                    intent.putExtra(PickValueActivity.VALUE, tagItem.getValue());
                    intent.putExtra(PickValueActivity.AUTOCOMPLETE, tagItem.getAutocompleteValues().toArray(new String[tagItem.getAutocompleteValues().size()]));
                    ((Activity) context).startActivityForResult(intent, PickValueActivity.PICK_VALUE_ACTIVITY_CODE);

                }
            };

            holder.getPoiTagLayout().setOnClickListener(editTagClickListener);
            holder.getEditButton().setOnClickListener(editTagClickListener);
        } else {
            holder.getEditButton().setVisibility(View.GONE);
        }
    }

    public void onBindViewHolder(final TagItemTextViewHolder holder,
                                 final int position) {
        final TagItem tagItem = tagItemList.get(position);

        if (configManager.hasPoiModification()) {
            holder.getGridViewLayoutWrapper().setVisibility(View.VISIBLE);

            holder.getTextViewKey().setText(tagParser.parseTagName(tagItem.getKey()));

            ((TextInputLayout) (holder.getTextViewValue().getParent())).setHint(tagItem.getKey());
            holder.getTextViewValue().setText(tagItem.getValue());
        } else {
            holder.getGridViewLayoutWrapper().setVisibility(View.GONE);
        }
    }

    public void onBindViewHolder(TagItemTextImposedViewHolder holder, int position) {
        final TagItem tagItem = tagItemList.get(position);
        holder.getTextViewKey().setText(tagParser.parseTagName(tagItem.getKey()));
        holder.getTextViewValue().setText(tagItem.getValue());
    }

    public void onBindViewHolder(TagItemOpeningHoursViewHolder holder, int position) {
        final TagItem tagItem = tagItemList.get(position);
        holder.getTextViewKey().setText(tagParser.parseTagName(tagItem.getKey()));

        View.OnClickListener onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditDaysTagDialogFragment fragment = new EditDaysTagDialogFragment();
                fragment.setOnEditDaysTagListener(new EditDaysTagDialogFragment.OnEditDaysTagListener() {
                    @Override
                    public void onOpeningDaysChanged(boolean[] days) {

                    }

                    @Override
                    public void onOpeningHoursChanged(LocalTime from, LocalTime to) {

                    }
                });
                fragment.show(context.getFragmentManager(), DialogFragment.class.getSimpleName());
            }
        };

        holder.getTextViewDaysValue().setOnClickListener(onClickListener);
        holder.getTextViewHoursValue().setOnClickListener(onClickListener);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder,
                                 int position) {
        switch (tagItemList.get(position).getTagType()) {
            case MULTI_CHOICE:
                onBindViewHolder((TagItemMultiChoiceViewHolder) holder, position);
                return;

            case TEXT:
                onBindViewHolder((TagItemTextViewHolder) holder, position);
                return;

            case TEXT_IMPOSED:
                onBindViewHolder((TagItemTextImposedViewHolder) holder, position);
                return;

            case OPENING_HOURS:
                onBindViewHolder((TagItemOpeningHoursViewHolder) holder, position);
                return;

            default:
        }

    }

    @Override
    public int getItemViewType(int position) {
        return tagItemList.get(position).getTagType().ordinal();
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return tagItemList.size();
    }

    public void addTag(String key, String value, boolean mandatory, List<String> autocompleteValues, int position, TagItem.TagType tagType, boolean updatable) {
        if (!updatable) {
            tagItemList.add(position, new TagItem(key, value, mandatory, autocompleteValues, TagItem.TagType.TEXT_IMPOSED));
        } else {
            tagItemList.add(position, new TagItem(key, value, mandatory, autocompleteValues, tagType == null ? TagItem.TagType.TEXT : tagType));
        }
        notifyItemInserted(position);
    }

    /**
     * Add an element at the end of the List
     *
     * @return The position of the inserted element
     */
    public int addLast(String key, String value, List<String> autocompleteValues, TagItem.TagType tagType, boolean updatable) {
        addTag(key, value, false, autocompleteValues, tagItemList.size(), tagType == null ? TagItem.TagType.TEXT : tagType, updatable);
        return tagItemList.size() - 1;
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.ASYNC)
    public void onPleaseApplyTagChange(PleaseApplyTagChange event) {
        eventBus.removeStickyEvent(event);
        for (TagItem tagItem : tagItemList) {
            if (tagItem.getKey().equals(event.getKey())) {
                tagItem.setValue(event.getValue());
                change = true;
            }
        }
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onPleaseApplyTagChangeView(PleaseApplyTagChangeView event) {
        eventBus.removeStickyEvent(event);
        for (TagItem tagItem : tagItemList) {
            if (tagParser.parseTagName(tagItem.getKey()).equals(event.getKey())) {
                tagItem.setValue(event.getValue());
                change = true;
            }
        }
    }

    private void editTag(TagItem tagItem, String newValue) {
        tagItem.setValue(newValue);
        change = true;
    }

    public boolean isValidChanges() {
        for (TagItem tagItem : tagItemList) {
            if (tagItem.isMandatory() && StringUtils.isEmpty(tagItem.getValue())) {
                return false;
            }
        }
        return true;
    }
}


