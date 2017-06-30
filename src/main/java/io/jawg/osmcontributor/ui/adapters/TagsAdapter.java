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
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;

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
import io.jawg.osmcontributor.R;
import io.jawg.osmcontributor.model.entities.Poi;
import io.jawg.osmcontributor.model.entities.PoiTypeTag;
import io.jawg.osmcontributor.model.utils.OpeningMonth;
import io.jawg.osmcontributor.model.utils.OpeningTime;
import io.jawg.osmcontributor.rest.mappers.PoiTypeMapper;
import io.jawg.osmcontributor.ui.adapters.item.TagItem;
import io.jawg.osmcontributor.ui.adapters.parser.OpeningTimeValueParser;
import io.jawg.osmcontributor.ui.adapters.parser.ParserManager;
import io.jawg.osmcontributor.ui.events.edition.PleaseApplyOpeningTimeChange;
import io.jawg.osmcontributor.ui.events.edition.PleaseApplyTagChange;
import io.jawg.osmcontributor.ui.events.edition.PleaseApplyTagChangeView;
import io.jawg.osmcontributor.ui.utils.views.DividerItemDecoration;
import io.jawg.osmcontributor.ui.utils.views.holders.TagItemAutoCompleteViewHolder;
import io.jawg.osmcontributor.ui.utils.views.holders.TagItemConstantViewHolder;
import io.jawg.osmcontributor.ui.utils.views.holders.TagItemOpeningTimeViewHolder;
import io.jawg.osmcontributor.ui.utils.views.holders.TagRadioChoiceHolder;
import io.jawg.osmcontributor.utils.ConfigManager;
import io.jawg.osmcontributor.utils.StringUtils;
import io.jawg.osmcontributor.utils.edition.PoiChanges;

public class TagsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final String TAG = "TagsAdapter";

    private List<TagItem> tagItemList = new ArrayList<>();
    private Map<String, TagItem> keyTagItem = new HashMap<>();
    private Poi poi;
    private Activity activity;
    private EventBus eventBus;
    private ConfigManager configManager;
    private boolean change = false;
    private boolean expertMode = false;

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
        this.eventBus = EventBus.getDefault();
        eventBus.register(this);
    }

    /*=========================================*/
    /*---------------ADAPTER-------------------*/
    /*=========================================*/
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        /* Create different views depending of type of the tag */
        switch (TagItem.Type.values()[viewType]) {
            case CONSTANT:
                View poiTagImposedLayout = LayoutInflater.from(parent.getContext()).inflate(R.layout.tag_item_constant, parent, false);
                return new TagItemConstantViewHolder(poiTagImposedLayout);

            case OPENING_HOURS:
                View poiTagOpeningHoursLayout = LayoutInflater.from(parent.getContext()).inflate(R.layout.tag_item_opening_time, parent, false);
                return new TagItemOpeningTimeViewHolder(poiTagOpeningHoursLayout);

            case TIME:
                View poiTime = LayoutInflater.from(parent.getContext()).inflate(R.layout.tag_item_opening_time, parent, false);
                return new TagItemOpeningTimeViewHolder(poiTime);

            case SINGLE_CHOICE:
                View booleanChoiceLayout = LayoutInflater.from(parent.getContext()).inflate(R.layout.tag_item_radio, parent, false);
                return new TagRadioChoiceHolder(booleanChoiceLayout);
            default:
                View autoCompleteLayout = LayoutInflater.from(parent.getContext()).inflate(R.layout.tag_item_multi_choice, parent, false);
                return new TagItemAutoCompleteViewHolder(autoCompleteLayout);
        }
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
        TagItem tagItem = new TagItem(key, value, false, possibleValues, key.contains("hours") ? TagItem.Type.OPENING_HOURS : TagItem.Type.TEXT, true);
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
    private void addTag(String key, String value, boolean mandatory, List<String> values, int position, boolean updatable, TagItem.Type type) {
        // Parse value if needed
        String valueFormatted = ParserManager.getValue(value, type);
        type = type == null ? TagItem.Type.TEXT : type;
        type = key.equals("collection_times") ? TagItem.Type.TIME : type;
        type = key.equals("opening_hours") ? TagItem.Type.OPENING_HOURS : type;

        TagItem tagItem = new TagItem(key, value, mandatory, values, updatable ? type : TagItem.Type.CONSTANT, valueFormatted != null || type == TagItem.Type.NUMBER);
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
            values.add(split[0]);
        }
        return values;
    }

    private void loadTags(Map<String, String> poiTags, Map<String, List<String>> tagValueSuggestionsMap) {
        int nbMandatory = 0;
        int nbImposed = 0;

        for (PoiTypeTag poiTypeTag : poi.getType().getTags()) {
            List<String> values = removeDuplicate(getPossibleValuesAsList(poiTypeTag.getPossibleValues()),
                    tagValueSuggestionsMap.get(poiTypeTag.getKey()));
            // Tags not in the PoiType should not be displayed if we are not in expert mode
            if (poiTypeTag.getValue() == null) {
                String key = poiTypeTag.getKey();
                // Display tags as mandatory if they are mandatory and we are not in expert mode
                if (poiTypeTag.getMandatory() && !expertMode) {
                    addTag(key, poiTags.get(key), true, values, nbMandatory + nbImposed, true, poiTypeTag.getTagType());
                    nbMandatory++;
                } else {
                    addTag(key, poiTags.get(key), false, values, this.getItemCount(), true, poiTypeTag.getTagType());
                }
            } else if (expertMode) {
                // Display the tags of the poi that are not in the PoiType
                String key = poiTypeTag.getKey();
                addTag(key, poiTags.get(key), true, values, nbImposed, false, poiTypeTag.getTagType());
                nbImposed++;
            }
        }

        if (expertMode) {
            for (String key : poiTags.keySet()) {
                addTag(key, poiTags.get(key), false, removeDuplicate(tagValueSuggestionsMap.get(key),
                        Collections.singletonList(poiTags.get(key))), this.getItemCount(), true, TagItem.Type.TEXT);
            }
        }
    }

    /*=========================================*/
    /*---------------HOLDERS-------------------*/
    /*=========================================*/
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder,
                                 int position) {
        switch (tagItemList.get(position).getTagType()) {
            case CONSTANT:
                onBindViewHolder((TagItemConstantViewHolder) holder, position);
                break;

            case OPENING_HOURS:
                onBindViewHolder((TagItemOpeningTimeViewHolder) holder, position);
                return;

            case TIME:
                onBindViewHolder((TagItemOpeningTimeViewHolder) holder, position);
                return;

            case SINGLE_CHOICE:
                onBindViewHolder((TagRadioChoiceHolder) holder, position);
                break;

            default:
                onBindViewHolder((TagItemAutoCompleteViewHolder) holder, position);
                break;
        }
    }

    /**
     * View for tag type text.
     * @param holder holder
     * @param position position of tag item
     */
    private void onBindViewHolder(final TagItemAutoCompleteViewHolder holder, int position) {
        final TagItem tagItem = tagItemList.get(holder.getAdapterPosition());

        // Set values input
        holder.getTextViewKey().setText(ParserManager.parseTagName(tagItem.getKey()));
        holder.getTextViewValue().setText(tagItem.getValue());
        holder.getTextInputLayout().setHint(tagItem.getKey());

        // If phone type if phone, set input type to number
        if (tagItem.getTagType() == TagItem.Type.NUMBER) {
            holder.getTextViewValue().setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        }

        // Get possible values
        final List<String> values = tagItem.getValues();

        holder.getTextViewValue().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (i1 != i2) {
                    eventBus.post(new PleaseApplyTagChange(holder.getTextViewKey().getText().toString(), charSequence.toString()));
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        if (!tagItem.isConform() && holder.getContent().getChildAt(1).getId() != R.id.malformated_layout) {
            holder.getContent().addView(LayoutInflater.from(activity).inflate(
                    R.layout.malformated_layout, holder.getContent(), false), 1);
            String currentValue = activity.getString(R.string.malformated_value) + " " + tagItem.getValue();
            ((TextView) ((LinearLayout) holder.getContent().getChildAt(1)).getChildAt(1)).setText(currentValue);
        }
    }

    /**
     * Holder for tag type constant.
     * @param holder holder
     * @param position tag item position
     */
    private void onBindViewHolder(TagItemConstantViewHolder holder, int position) {
        final TagItem tagItem = tagItemList.get(position);
        holder.getTextViewKey().setText(ParserManager.parseTagName(tagItem.getKey()));
        holder.getTextViewValue().setText(tagItem.getValue());
    }

    /**
     * Binding tag item for opening time
     * @param holder
     * @param position
     */
    public void onBindViewHolder(final TagItemOpeningTimeViewHolder holder, int position) {
        final TagItem tagItem = tagItemList.get(position);
        holder.getTextViewKey().setText(ParserManager.parseTagName(tagItem.getKey()));

        OpeningTime openingTime = null;
        try {
             openingTime = openingTimeValueParser.fromValue(tagItem.getValue());
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            tagItem.setConform(false);
        }

        if (openingTime == null) {
            openingTime = new OpeningTime();
        }

        final OpeningMonthAdapter adapter = new OpeningMonthAdapter(openingTime, activity);
        adapter.setTime(tagItem.getValue());
        if (tagItem.getTagType() == TagItem.Type.TIME) {
            adapter.hideMonth(true);
        }
        holder.getOpeningTimeRecyclerView().setAdapter(adapter);
        holder.getOpeningTimeRecyclerView().setLayoutManager(new LinearLayoutManager(activity));
        holder.getOpeningTimeRecyclerView().setHasFixedSize(false);
        RecyclerView.ItemAnimator itemAnimator = new DefaultItemAnimator();
        itemAnimator.setAddDuration(300);
        holder.getOpeningTimeRecyclerView().setItemAnimator(itemAnimator);
        holder.getOpeningTimeRecyclerView().addItemDecoration(new DividerItemDecoration(activity));

        final OpeningTime finalOpeningTime = openingTime;
        holder.getAddButton().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finalOpeningTime.addOpeningMonth(new OpeningMonth());
                adapter.notifyItemInserted(adapter.getItemCount() - 1);
            }
        });

        if (!tagItem.isConform() && holder.getContent().getChildAt(1).getId() != R.id.malformated_layout) {
            holder.getContent().addView(LayoutInflater.from(activity).inflate(
                    R.layout.malformated_layout, holder.getContent(), false), 1);
            String currentValue = activity.getString(R.string.malformated_value) + " " + tagItem.getValue();
            ((TextView) ((LinearLayout) holder.getContent().getChildAt(1)).getChildAt(1)).setText(currentValue);
        }

    }

    /**
     * Bind view for list of 6 elements max.
     * @param holder holder
     * @param position position
     */
    private void onBindViewHolder(TagRadioChoiceHolder holder, int position) {
        // Get tag item from list
        final TagItem tagItem = tagItemList.get(position);

        // Set key text view
        holder.getTextViewKey().setText(ParserManager.parseTagName(tagItem.getKey()));

        // Check if size of possible values are 3, means special action to organize layout
        List<String> values = tagItem.getValues();
        boolean isFourElements = values.size() == 3;

        // List of radio buttons without undefined. Undefined is always showing
        RadioButton[] radioButtons = holder.getRadioButtons();

        // `undefined` radio button
        RadioButton undefinedRadioButton = holder.getUndefinedRadioButton();

        // Get if the tag is mandatory
        final boolean mandatory = tagItemList.get(position).isMandatory();

        // If the tag is required, we disable the `undefined` radio button
        if (mandatory) {
            undefinedRadioButton.setEnabled(false);
        }

        // If the tag value is `undefined`, we check the undefined radio button
        if (tagItem.getValue() != null && tagItem.getValue().equals(TagItem.VALUE_UNDEFINED)) {
            undefinedRadioButton.setChecked(true);
        }

        // Access element for values
        int pos = 0;
        for (int i = 0; i < radioButtons.length; i++) {
            if (!values.isEmpty()) {
                // If values is not empty...
                if (isFourElements && i == 1) {
                    // ... and list contains four values, skip one radio to have a 2/2 side by side printing
                    radioButtons[i].setVisibility(View.INVISIBLE);
                    i++;
                    isFourElements = false;
                }

                if (pos < values.size()) {
                    // Set value of radio button and show it
                    radioButtons[i].setText(values.get(pos));
                    radioButtons[i].setVisibility(View.VISIBLE);

                    // Select radio if value is not undefined
                    if (tagItem.getValue() != null && tagItem.getValue().equals(values.get(pos))) {
                        radioButtons[i].setChecked(true);
                    }
                    pos++;
                } else {
                    // If all values are set, hide radio button not used
                    radioButtons[i].setVisibility(View.INVISIBLE);
                }
            }
        }

        if (!tagItem.isConform() && holder.getContent().getChildAt(1).getId() != R.id.malformated_layout) {
            holder.getContent().addView(LayoutInflater.from(activity).inflate(
                    R.layout.malformated_layout, holder.getContent(), false), 1);
            String currentValue = activity.getString(R.string.malformated_value) + " " + tagItem.getValue();
            ((TextView) ((LinearLayout) holder.getContent().getChildAt(1)).getChildAt(1)).setText(currentValue);
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