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
package io.jawg.osmcontributor.ui.adapters;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import io.jawg.osmcontributor.OsmTemplateApplication;
import io.jawg.osmcontributor.model.entities.Poi;
import io.jawg.osmcontributor.model.entities.PoiTypeTag;
import io.jawg.osmcontributor.rest.mappers.PoiTypeMapper;
import io.jawg.osmcontributor.ui.adapters.binding.AutoCompleteViewBinder;
import io.jawg.osmcontributor.ui.adapters.binding.ConstantViewBinder;
import io.jawg.osmcontributor.ui.adapters.binding.OpeningHoursViewBinder;
import io.jawg.osmcontributor.ui.adapters.binding.RadioChoiceViewBinder;
import io.jawg.osmcontributor.ui.adapters.binding.TagViewBinder;
import io.jawg.osmcontributor.ui.adapters.binding.CheckedTagViewBinder;
import io.jawg.osmcontributor.ui.adapters.item.TagItem;
import io.jawg.osmcontributor.ui.adapters.parser.OpeningTimeValueParser;
import io.jawg.osmcontributor.ui.adapters.parser.ParserManager;
import io.jawg.osmcontributor.ui.events.edition.PleaseApplyOpeningTimeChange;
import io.jawg.osmcontributor.ui.events.edition.PleaseApplyTagChange;
import io.jawg.osmcontributor.ui.events.edition.PleaseApplyTagChangeView;
import io.jawg.osmcontributor.utils.ConfigManager;
import io.jawg.osmcontributor.utils.StringUtils;
import io.jawg.osmcontributor.utils.edition.PoiChanges;

public class TagsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final String TAG = "TagsAdapter";

    private List<TagItem> tagItemList = new ArrayList<>();
    private Map<String, TagItem> keyTagItem = new HashMap<>();
    private List<CheckedTagViewBinder> checkedViews = new ArrayList<>();
    private Poi poi;
    private Activity activity;
    private ConfigManager configManager;
    private boolean change = false;
    private boolean expertMode = false;
    private List<TagViewBinder> viewBinders = new ArrayList<>();

    @Inject
    EventBus eventBus;

    @Inject
    OpeningTimeValueParser openingTimeValueParser;

    public TagsAdapter(Poi poi, List<TagItem> tagItemList, Activity activity, Map<String, List<String>> tagValueSuggestionsMap, ConfigManager configManager, boolean expertMode) {
        this.poi = poi;
        this.activity = activity;
        this.configManager = configManager;
        this.expertMode = expertMode;
        ((OsmTemplateApplication) activity.getApplication()).getOsmTemplateComponent().inject(this);

        if (tagItemList == null) {
            loadTags(poi.getTagsMap(), tagValueSuggestionsMap);
        } else {
            this.tagItemList = tagItemList;
            notifyDataSetChanged();
        }

        eventBus.register(this);

        viewBinders.add(new AutoCompleteViewBinder(activity, eventBus));
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
     * @return the position of the inserted element
     */
    public int addLast(String key, String value, List<String> possibleValues) {
        TagItem tagItem = new TagItem.TagItemBuilder(key, value).mandatory(false)
                .values(possibleValues)
                .type(key.contains("hours") ? TagItem.Type.OPENING_HOURS : TagItem.Type.TEXT)
                .isConform(true)
                .show(true).build();
        // Add into the list
        tagItemList.add(tagItem);
        keyTagItem.put(key, tagItem);

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
    /*----------------EVENTS-------------------*/
    /*=========================================*/
    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onPleaseApplyTagChange(PleaseApplyTagChange event) {
        eventBus.removeStickyEvent(event);
        TagItem tagItem = keyTagItem.get(ParserManager.deparseTagName(event.getKey()));
        tagItem = tagItemList.get(tagItemList.indexOf(tagItem));
        if (tagItem != null) {
            editTag(tagItem, event.getValue());
        }
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onPleaseApplyOpeningTimeChange(PleaseApplyOpeningTimeChange event) {
        eventBus.removeStickyEvent(event);
        TagItem tagItem = keyTagItem.get("opening_hours");
        tagItem = tagItemList.get(tagItemList.indexOf(tagItem));
        if (tagItem != null) {
            editTag(tagItem, openingTimeValueParser.toValue(event.getOpeningTime()));
        }
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onPleaseApplyTagChangeView(PleaseApplyTagChangeView event) {
        eventBus.removeStickyEvent(event);
        TagItem tagItem = keyTagItem.get(ParserManager.deparseTagName(event.getKey()));
        if (tagItem != null) {
            editTag(tagItem, event.getValue());
            notifyItemChanged(tagItemList.indexOf(tagItem));
        }
    }

    /*=========================================*/
    /*------------PRIVATE CODE-----------------*/
    /*=========================================*/

    /**
     * Edit tag value
     * @param tagItem tag item to edit
     * @param newValue new value
     */
    private void editTag(TagItem tagItem, String newValue) {
        tagItem.setValue(newValue);
        change = true;
    }

    /**
     * Add tag into global list of tag
     * @param key tag key
     * @param value tag value
     * @param mandatory is mandatory
     * @param values possible values for the tag, can be empty or null
     * @param position position inside the list
     * @param updatable is updatable
     */
    private void addTag(String key, String value, boolean mandatory, List<String> values, int position, boolean updatable, TagItem.Type type, boolean show) {
        // Parse value if needed
        String valueFormatted = ParserManager.getValue(value, type);
        type = type == null ? TagItem.Type.TEXT : type;
        type = key.equals("collection_times") ? TagItem.Type.TIME : type;
        type = key.equals("opening_hours") ? TagItem.Type.OPENING_HOURS : type;

        TagItem tagItem = new TagItem.TagItemBuilder(key, value).mandatory(mandatory)
                .values(values)
                .type(updatable ? type : TagItem.Type.CONSTANT)
                .isConform(valueFormatted != null || type == TagItem.Type.NUMBER)
                .show(show).build();

        // Add into the list
        if (!tagItemList.contains(tagItem)) {
            tagItemList.add(position, tagItem);
        }
        keyTagItem.put(key, tagItem);

        // Notify changes
        notifyItemInserted(position);
    }

    /**
     * Get a list of all possible values without duplicate
     * @param possibleValues possible values from h2geo
     * @param autoCompleteValues proposition from last used values
     * @return a merged list
     */
    private List<String> removeDuplicate(List<String> possibleValues, List<String> autoCompleteValues) {
        // Create a default empty list to avoid null pointer exception
        List<String> values = new ArrayList<>();

        if (possibleValues != null) {
            // If possible values are not null, init values with it
            values.addAll(possibleValues);
        }

        if (autoCompleteValues != null) {
            // If auto complete values are not null and values are empty, fill it with it
            if (values.isEmpty()) {
                values.addAll(autoCompleteValues);
            }

            // For each auto complete values, if the value does not exist, add it to the new list
            for (String possibleValue : autoCompleteValues) {
                if (!values.contains(possibleValue) && (!possibleValue.isEmpty() || !possibleValue.trim().isEmpty())) {
                    values.add(possibleValue);
                }
            }
        }

        // Sometimes, with yes value there is no value, this is a non sens
        if (values.contains("yes") && !values.contains("no")) {
            values.add("no");
        } else if (values.contains("no") && !values.contains("yes")) {
            values.add("yes");
        }
        return values;
    }

    /**
     * Convert possible values from String to List.
     * @param possibleValuesAsString string of possible values
     * @return list of possible values
     */
    private List<String> getPossibleValuesAsList(String possibleValuesAsString) {
        if (possibleValuesAsString == null || possibleValuesAsString.isEmpty()) {
            return null;
        }
        // Split into an array of value:label
        String[] valuesAndLabels = possibleValuesAsString.split(PoiTypeMapper.ITEM_SEPARATOR);
        List<String> values = new ArrayList<>(valuesAndLabels.length);
        // FIXME will have to change possible values into a map
        for (String valueAndLabel : valuesAndLabels) {
            String[] split = valueAndLabel.split(PoiTypeMapper.VALUE_SEPARATOR);
            values.add(split[1]);
        }
        return values;
    }

    private void loadTags(Map<String, String> poiTags, Map<String, List<String>> tagValueSuggestionsMap) {
        int nbMandatory = 0;
        int nbImposed = 0;

        for (PoiTypeTag poiTypeTag : poi.getType().getTags()) {
            List<String> values = removeDuplicate(getPossibleValuesAsList(poiTypeTag.getPossibleValues()),
                    tagValueSuggestionsMap.get(poiTypeTag.getKey()));

            boolean show = true;
            if (poiTypeTag.getShow() != null) {
                show = poiTypeTag.getShow().booleanValue();
            }
            // Tags not in the PoiType should not be displayed if we are not in expert mode
            if (poiTypeTag.getValue() == null) {
                String key = poiTypeTag.getKey();

                // Display tags as mandatory if they are mandatory and we are not in expert mode
                if (poiTypeTag.getMandatory() && !expertMode) {
                    addTag(key, poiTags.get(key), true, values, nbMandatory + nbImposed, true, poiTypeTag.getTagType(), show);
                    nbMandatory++;
                } else {
                    addTag(key, poiTags.get(key), false, values, this.getItemCount(), true, poiTypeTag.getTagType(), show);
                }
            } else if (expertMode) {
                // Display the tags of the poi that are not in the PoiType
                String key = poiTypeTag.getKey();
                addTag(key, poiTags.get(key), true, values, nbImposed, false, poiTypeTag.getTagType(), show);
                nbImposed++;
            }
        }

        if (expertMode) {
            for (String key : poiTags.keySet()) {
                addTag(key, poiTags.get(key), false, removeDuplicate(tagValueSuggestionsMap.get(key),
                        Collections.singletonList(poiTags.get(key))), this.getItemCount(), true, TagItem.Type.TEXT, true);
            }
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
            if (tagItem.isMandatory() || (tagItem.getValue() != null && tagItem.getValue().trim().length() != 0)) {
                result.getTagsMap().put(tagItem.getKey().trim(), tagItem.getValue().trim());
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
}
