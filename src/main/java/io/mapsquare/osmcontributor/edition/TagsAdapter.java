/**
 * Copyright (C) 2015 eBusiness Information
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
package io.mapsquare.osmcontributor.edition;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import de.greenrobot.event.EventBus;
import io.mapsquare.osmcontributor.R;
import io.mapsquare.osmcontributor.core.ConfigManager;
import io.mapsquare.osmcontributor.core.model.Poi;
import io.mapsquare.osmcontributor.core.model.PoiTypeTag;
import io.mapsquare.osmcontributor.edition.events.PleaseApplyTagChange;
import io.mapsquare.osmcontributor.edition.events.PleaseApplyTagChangeView;
import io.mapsquare.osmcontributor.edition.holder.ViewHolderPoiTagFewValues;
import io.mapsquare.osmcontributor.edition.holder.ViewHolderPoiTagImposed;
import io.mapsquare.osmcontributor.edition.holder.ViewHolderPoiTagManyValues;
import io.mapsquare.osmcontributor.edition.holder.ViewHolderSeparator;
import io.mapsquare.osmcontributor.utils.StringUtils;
import io.mapsquare.osmcontributor.utils.ViewAnimation;

public class TagsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    public static final int NB_AUTOCOMPLETE_LIMIT = 6;
    private List<CardModel> cardModelList = new ArrayList<>();
    private Poi poi;
    private Context context;
    private EventBus eventBus;
    private ConfigManager configManager;
    private boolean change = false;
    private boolean expertMode = false;

    public TagsAdapter(Poi poi, List<CardModel> cardModelList, Context context, Map<String, List<String>> tagValueSuggestionsMap, ConfigManager configManager, boolean expertMode) {
        this.poi = poi;
        this.context = context;
        this.configManager = configManager;
        this.expertMode = expertMode;

        if (cardModelList == null) {
            loadTags(poi.getTagsMap(), tagValueSuggestionsMap);
        } else {
            this.cardModelList = cardModelList;
            notifyDataSetChanged();
        }

        this.eventBus = EventBus.getDefault();
        eventBus.registerSticky(this);
    }

    private void loadTags(Map<String, String> poiTags, Map<String, List<String>> tagValueSuggestionsMap) {
        int nbMandatory = 0;
        int nbImposed = 0;

        for (PoiTypeTag poiTypeTag : poi.getType().getTags()) {
            // Tags not in the PoiType should not be displayed if we are not in expert mode
            if (poiTypeTag.getValue() == null) {
                String key = poiTypeTag.getKey();
                // Display tags as mandatory if they are mandatory and we are not in expert mode
                if (poiTypeTag.getMandatory() && !expertMode) {
                    add(key, poiTags.remove(key), true, tagValueSuggestionsMap.get(key), nbMandatory + nbImposed, false, true);
                    nbMandatory++;
                } else {
                    add(key, poiTags.remove(key), false, tagValueSuggestionsMap.get(key), this.getItemCount(), false, true);
                }
            } else if (expertMode) {
                // Display the tags of the poi that are not in the PoiType
                String key = poiTypeTag.getKey();
                add(key, poiTags.remove(key), true, tagValueSuggestionsMap.get(key), nbImposed, false, false);
                nbImposed++;
            }
        }
        if (expertMode) {
            for (String key : poiTags.keySet()) {
                add(key, poiTags.get(key), false, Collections.singletonList(poiTags.get(key)), this.getItemCount(), false, true);
            }
        }
        addSeparator(nbMandatory, nbImposed);
    }

    /**
     * @return a poiChange object containing all changes made by the user
     */
    public PoiChanges getPoiChanges() {
        PoiChanges result = new PoiChanges(poi.getId());
        for (CardModel cardModel : cardModelList) {
            if (cardModel.isTag()) {
                // Only mandatory tags and optional tags that have been changed are saved
                if (cardModel.isMandatory() || cardModel.getValue() != null) {
                    result.getTagsMap().put(cardModel.getKey(), cardModel.getValue());
                }
            }
        }
        return result;
    }

    public List<CardModel> getCardModelList() {
        return cardModelList;
    }

    public boolean isChange() {
        return change;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        switch (CardModel.CardType.values()[viewType]) {
            case TAG_IMPOSED:
                View poiTagImposedLayout = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_tag_imposed, parent, false);
                return new ViewHolderPoiTagImposed(poiTagImposedLayout);

            case TAG_MANY_VALUES:
                View poiTagFewValuesLayout = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_tag_many_values_layout, parent, false);
                return new ViewHolderPoiTagManyValues(poiTagFewValuesLayout);

            case TAG_FEW_VALUES:
                View poiTagManyValuesLayout = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_tag_few_values_layout, parent, false);
                return new ViewHolderPoiTagFewValues(poiTagManyValuesLayout);

            case HEADER_OPTIONAL:
                return new ViewHolderSeparator(LayoutInflater.from(parent.getContext()).inflate(R.layout.card_tag_separator, parent, false), ViewHolderSeparator.SeparatorType.OPTIONAL);

            case HEADER_REQUIRED:
                return new ViewHolderSeparator(LayoutInflater.from(parent.getContext()).inflate(R.layout.card_tag_separator, parent, false), ViewHolderSeparator.SeparatorType.REQUIRED);

            default:
                return null;
        }
    }

    public void onBindViewHolder(final ViewHolderPoiTagManyValues holder,
                                 final int position) {

        final CardModel cardModel = cardModelList.get(position);

        holder.getTextViewKey().setText(cardModel.getKey());
        holder.getTextViewValue().setText(cardModel.getValue());

        if (configManager.hasPoiModification()) {
            View.OnClickListener editTagClickListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, PickValueActivity.class);
                    intent.putExtra(PickValueActivity.KEY, cardModel.getKey());
                    intent.putExtra(PickValueActivity.VALUE, cardModel.getValue());
                    intent.putExtra(PickValueActivity.AUTOCOMPLETE, cardModel.getAutocompleteValues().toArray(new String[cardModel.getAutocompleteValues().size()]));
                    ((Activity) context).startActivityForResult(intent, PickValueActivity.PICK_VALUE_ACTIVITY_CODE);

                }
            };

            holder.getPoiTagLayout().setOnClickListener(editTagClickListener);
            holder.getEditButton().setOnClickListener(editTagClickListener);
        } else {
            holder.getEditButton().setVisibility(View.GONE);
        }
    }

    public void onBindViewHolder(final ViewHolderPoiTagFewValues holder,
                                 final int position) {
        final CardModel cardModel = cardModelList.get(position);

        if (configManager.hasPoiModification()) {

            holder.getGridView().setAdapter(new AutocompleteAdapter(context, cardModel.getAutocompleteValues(), holder.getTextViewValue(), holder.getPoiTagLayout(), cardModel.getKey()));

            if (cardModel.getAutocompleteValues().size() > 0) {
                holder.getNoValueTextView().setVisibility(View.GONE);
                holder.getGridView().setVisibility(View.VISIBLE);
            } else {
                holder.getNoValueTextView().setVisibility(View.VISIBLE);
                holder.getGridView().setVisibility(View.GONE);
            }

            View.OnClickListener expandCardClickListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    cardModel.setOpen(!cardModel.isOpen());

                    ViewAnimation.animate(holder.getGridViewLayoutWrapper(), cardModel.isOpen());

                    if (cardModel.isOpen()) {
                        holder.getExpendButton().setImageResource(R.drawable.chevron_up);
                    } else {
                        holder.getExpendButton().setImageResource(R.drawable.chevron_down);
                    }
                }
            };

            if (cardModel.isOpen()) {
                holder.getGridViewLayoutWrapper().setVisibility(View.VISIBLE);
            } else {
                holder.getGridViewLayoutWrapper().setVisibility(View.GONE);
            }

            holder.getPoiTagLayout().setOnClickListener(expandCardClickListener);
            holder.getExpendButton().setOnClickListener(expandCardClickListener);
            holder.getTextViewKey().setText(cardModel.getKey());
            holder.getTextViewValue().setText(cardModel.getValue());
            holder.getRelativeLayoutEdition().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AddValueDialogFragment dialog = AddValueDialogFragment.newInstance(cardModel.getKey(), cardModel.getValue(), holder.getPoiTagLayout());
                    dialog.show(((Activity) context).getFragmentManager(), "dialog");
                }
            });
        } else {
            holder.getExpendButton().setVisibility(View.GONE);
            holder.getGridViewLayoutWrapper().setVisibility(View.GONE);
        }
    }

    public void onBindViewHolder(ViewHolderPoiTagImposed holder, int position) {
        final CardModel cardModel = cardModelList.get(position);
        holder.getTextViewKey().setText(cardModel.getKey());
        holder.getTextViewValue().setText(cardModel.getValue());
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder,
                                 int position) {
        switch (cardModelList.get(position).getType()) {
            case TAG_MANY_VALUES:
                onBindViewHolder((ViewHolderPoiTagManyValues) holder, position);
                return;

            case TAG_FEW_VALUES:
                onBindViewHolder((ViewHolderPoiTagFewValues) holder, position);
                return;

            case TAG_IMPOSED:
                onBindViewHolder((ViewHolderPoiTagImposed) holder, position);
                return;

            default:
        }

    }

    @Override
    public int getItemViewType(int position) {
        return cardModelList.get(position).getType().ordinal();
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return cardModelList.size();
    }

    public void add(String key, String value, boolean mandatory, List<String> autocompleteValues, int position, boolean open, boolean updatable) {
        if (!updatable) {
            cardModelList.add(position, new CardModel(key, value, mandatory, autocompleteValues, open, CardModel.CardType.TAG_IMPOSED));
        } else {
            if (autocompleteValues.size() > NB_AUTOCOMPLETE_LIMIT) {
                cardModelList.add(position, new CardModel(key, value, mandatory, autocompleteValues, open, CardModel.CardType.TAG_MANY_VALUES));
            } else {
                cardModelList.add(position, new CardModel(key, value, mandatory, autocompleteValues, open, CardModel.CardType.TAG_FEW_VALUES));
            }
        }
        notifyItemInserted(position);
    }

    /**
     * Add an element at the end of the List
     *
     * @return The position of the inserted element
     */
    public int addLast(String key, String value, List<String> autocompleteValues, boolean open, boolean updatable) {
        add(key, value, false, autocompleteValues, cardModelList.size(), open, updatable);
        return cardModelList.size() - 1;
    }

    public void addSeparator(int nbMandatory, int nbImposed) {
        // if there is only mandatory or only optional tags we hide the separators
        if (nbMandatory != 0 && nbMandatory != this.getItemCount()) {
            cardModelList.add(nbMandatory + nbImposed, new CardModel("", "", false, null, false, CardModel.CardType.HEADER_OPTIONAL));
            cardModelList.add(nbImposed, new CardModel("", "", false, null, false, CardModel.CardType.HEADER_REQUIRED));
            notifyItemInserted(nbMandatory + nbImposed);
            notifyItemInserted(nbImposed);
        }
    }

    public void onEventBackgroundThread(PleaseApplyTagChange event) {
        eventBus.removeStickyEvent(event);
        for (CardModel cardModel : cardModelList) {
            if (cardModel.getKey().equals(event.getKey())) {
                cardModel.setValue(event.getValue());
                change = true;
            }
        }
    }

    public void onEventMainThread(PleaseApplyTagChangeView event) {
        eventBus.removeStickyEvent(event);
        int position = 0;
        for (CardModel cardModel : cardModelList) {
            if (cardModel.getKey().equals(event.getKey())) {
                cardModel.setValue(event.getValue());
                notifyItemChanged(position);
                change = true;
            }
            position++;
        }
    }

    public boolean isValidChanges() {
        for (CardModel cardModel : cardModelList) {
            if (cardModel.isTag() && cardModel.isMandatory() && StringUtils.isEmpty(cardModel.getValue())) {
                return false;
            }
        }
        return true;
    }
}


