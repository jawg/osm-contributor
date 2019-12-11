/**
 * Copyright (C) 2019 Takima
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

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.jawg.osmcontributor.R;
import io.jawg.osmcontributor.model.entities.PoiType;
import io.jawg.osmcontributor.model.entities.PoiTypeTag;
import io.jawg.osmcontributor.ui.utils.BitmapHandler;
import io.jawg.osmcontributor.utils.helper.ItemTouchHelperViewHolder;
import io.jawg.osmcontributor.utils.helper.SwipeItemTouchHelperAdapter;
import timber.log.Timber;

public class PoiTypeAdapter extends RecyclerView.Adapter<PoiTypeAdapter.PoiTypeViewHolder> implements SwipeItemTouchHelperAdapter, Filterable {

    private ItemFilter filter = new ItemFilter();
    private BitmapHandler bitmapHandler;
    private List<PoiType> originalValues = null;
    private List<PoiType> filteredValues = null;
    private PoiTypeAdapterListener listener = null;

    private PoiType lastRemovedItem = null;

    public PoiTypeAdapter(List<PoiType> poiTypes, BitmapHandler bitmapHandler) {
        this.originalValues = poiTypes;
        this.filteredValues = new ArrayList<>(poiTypes);
        this.bitmapHandler = bitmapHandler;
    }

    @Override
    public PoiTypeViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        final View v = inflater.inflate(R.layout.single_poi_type, parent, false);
        return new PoiTypeViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull PoiTypeViewHolder holder, int position) {
        holder.onBind(filteredValues.get(position));
    }

    @Override
    public int getItemCount() {
        return filteredValues.size();
    }

    @Override
    public long getItemId(int position) {
        return filteredValues.get(position).getId();
    }

    public PoiType getItem(int position) {
        return filteredValues.get(position);
    }

    @Override
    public void onItemDismiss(int position) {
        lastRemovedItem = getItem(position);
        originalValues.remove(lastRemovedItem);
        filteredValues.remove(lastRemovedItem);
        if (listener != null) {
            listener.onItemRemoved(lastRemovedItem);
        }
        notifyItemRemoved(position);
    }

    public void setPoiTypes(List<PoiType> poiTypes) {
        originalValues = poiTypes;
        filteredValues = poiTypes;
        notifyDataSetChanged();
    }

    public PoiType getItemById(Long id) {
        if (id != null) {
            for (PoiType poiType : originalValues) {
                if (id.equals(poiType.getId())) {
                    return poiType;
                }
            }
        }
        return null;
    }

    public int addItem(PoiType item) {
        int insertedIndex = -1;
        // If the item was not in the adapter, add it
        if (originalValues.indexOf(item) == -1) {
            originalValues.add(item);
            Collections.sort(originalValues);

            // Check if the new item should be displayed with the current filter
            if (item.getName() != null && filter.lastFilterConstraint != null && item.getName().toLowerCase().contains(filter.lastFilterConstraint)) {
                filteredValues.add(item);
                Collections.sort(filteredValues);
                insertedIndex = filteredValues.indexOf(item);
                notifyItemInserted(insertedIndex);
            }
        } else {
            // The item is already in the adapter, we must update the view
            Collections.sort(originalValues);

            filteredValues.remove(item);

            // Display the item depending on the filter
            if (item.getName() != null && filter.lastFilterConstraint != null && item.getName().toLowerCase().contains(filter.lastFilterConstraint)) {
                filteredValues.add(item);
                Collections.sort(filteredValues);
                insertedIndex = filteredValues.indexOf(item);
            }
            notifyDataSetChanged();
        }

        return insertedIndex;
    }

    public void undoLastRemoval() {
        final PoiType item = lastRemovedItem;

        if (item != null) {
            lastRemovedItem = null;
            addItem(item);
        }
    }

    public void notifyLastRemovalDone(PoiType poiType) {
        if (lastRemovedItem != null && lastRemovedItem.equals(poiType)) {
            lastRemovedItem = null;
        }
    }

    public void notifyTagRemoved(PoiTypeTag poiTypeTag) {
        PoiType poiType = poiTypeTag.getPoiType();
        Long id = poiType != null ? poiType.getId() : null;
        if (id == null) {
            return;
        }

        poiType = getItemById(id);
        if (poiType != null) {
            poiType.getTags().remove(poiTypeTag);

            int position = filteredValues.indexOf(poiType);
            if (position != -1) {
                notifyItemChanged(position);
            }
        }
    }

    @Override
    public Filter getFilter() {
        return filter;
    }

    public void setPoiTypeAdapterListener(PoiTypeAdapterListener listener) {
        this.listener = listener;
    }

    public class PoiTypeViewHolder extends RecyclerView.ViewHolder implements ItemTouchHelperViewHolder, View.OnClickListener, View.OnLongClickListener {

        View poiTypeLayout;
        @BindView(R.id.poi_type_icon)
        ImageView icon;
        @BindView(R.id.poi_type_name)
        TextView text;
        @BindView(R.id.poi_type_technical_name)
        TextView technicalName;
        @BindView(R.id.poi_type_details)
        TextView details;

        public PoiTypeViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            poiTypeLayout = itemView;
            poiTypeLayout.setOnClickListener(this);
            poiTypeLayout.setOnLongClickListener(this);
        }

        @Override
        public void onItemSelected() {

        }

        @Override
        public void onItemClear() {

        }

        private void onBind(PoiType item) {
            text.setText(item.getName());
            technicalName.setText(item.getTechnicalName());
            details.setText(itemView.getContext().getResources().getQuantityString(R.plurals.tag_number, item.getTags().size(), item.getTags().size()));
            icon.setImageDrawable(bitmapHandler.getDrawable(item.getIcon()));
        }

        @Override
        public void onClick(View view) {
            Timber.d("Clicked : " + getAdapterPosition());
            if (listener != null) {
                listener.onItemClick(getItem(getAdapterPosition()));
            }
        }

        @Override
        public boolean onLongClick(View view) {
            Timber.d("Long Clicked : " + getAdapterPosition());
            if (listener != null) {
                listener.onItemLongClick(getItem(getAdapterPosition()));
            }
            return false;
        }
    }

    private class ItemFilter extends Filter {

        private String lastFilterConstraint = "";

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {

            String filterString = constraint.toString().toLowerCase();
            lastFilterConstraint = filterString;
            FilterResults results = new FilterResults();

            if (filterString.isEmpty()) {
                results.values = originalValues;
                results.count = originalValues.size();
                return results;
            }

            final List<PoiType> list = originalValues;

            final ArrayList<PoiType> newValuesList = new ArrayList<>();

            for (int i = 0; i < list.size(); i++) {
                String filterableString = list.get(i).getKeyWords() + " " + list.get(i).getName() + " " + list.get(i).getTechnicalName();
                if (filterableString.toLowerCase().contains(filterString) || filterableString.equalsIgnoreCase(filterString)) {
                    newValuesList.add(list.get(i));
                }
            }

            results.values = newValuesList;
            results.count = newValuesList.size();

            return results;
        }

        @SuppressWarnings("unchecked")
        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            filteredValues = (ArrayList<PoiType>) results.values;
            notifyDataSetChanged();
        }
    }

    public interface PoiTypeAdapterListener {
        void onItemClick(PoiType item);

        void onItemLongClick(PoiType item);

        void onItemRemoved(PoiType item);
    }
}
