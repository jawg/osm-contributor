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

import android.support.v4.view.MotionEventCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.BindView;
import io.mapsquare.osmcontributor.R;
import io.mapsquare.osmcontributor.model.entities.PoiTypeTag;
import io.mapsquare.osmcontributor.utils.helper.DragSwipeItemTouchHelperAdapter;
import io.mapsquare.osmcontributor.utils.helper.ItemTouchHelperViewHolder;
import timber.log.Timber;

public class PoiTypeTagAdapter extends RecyclerView.Adapter<PoiTypeTagAdapter.PoiTypeTagViewHolder> implements DragSwipeItemTouchHelperAdapter {

    private List<PoiTypeTag> poiTypeTags;
    private PoiTypeTag lastRemovedItem;

    private PoiTypeTagAdapterListener listener;
    private OnStartDragListener startDragListener;

    private PoiTypeTag moving = null;

    public PoiTypeTagAdapter(List<PoiTypeTag> poiTypeTags) {
        this.poiTypeTags = poiTypeTags;
    }

    public void setPoiTypeTags(Collection<PoiTypeTag> poiTypeTags) {
        this.poiTypeTags.clear();
        this.poiTypeTags.addAll(poiTypeTags);
        Collections.sort(this.poiTypeTags);
        notifyDataSetChanged();
    }

    public void setPoiTypeTagAdapterListener(PoiTypeTagAdapterListener listener) {
        this.listener = listener;
    }

    public void setOnStartDragListener(OnStartDragListener listener) {
        this.startDragListener = listener;
    }

    @Override
    public PoiTypeTagViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        final View v = inflater.inflate(R.layout.single_poi_tag_draggable, parent, false);
        return new PoiTypeTagViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final PoiTypeTagViewHolder holder, int position) {
        holder.onBind(poiTypeTags.get(position));
        holder.handle.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (MotionEventCompat.getActionMasked(event) == MotionEvent.ACTION_DOWN) {
                    startDragListener.onStartDrag(holder);
                }
                return false;
            }
        });
    }

    @Override
    public int getItemCount() {
        return poiTypeTags.size();
    }

    @Override
    public long getItemId(int position) {
        return poiTypeTags.get(position).getId();
    }

    public PoiTypeTag getItem(int position) {
        return poiTypeTags.get(position);
    }

    public int addItem(PoiTypeTag poiTypeTag) {
        int oldPosition = poiTypeTags.indexOf(poiTypeTag);
        if (oldPosition == -1) {
            poiTypeTags.add(poiTypeTag);
        }

        Collections.sort(poiTypeTags);

        int position = poiTypeTags.indexOf(poiTypeTag);
        if (oldPosition != -1) {
            if (oldPosition != position) {
                notifyItemMoved(oldPosition, position);
            }
            notifyItemChanged(position);
        } else {
            notifyItemInserted(position);
        }
        return position;
    }

    @Override
    public void onItemMove(int fromPosition, int toPosition) {
        if (fromPosition != toPosition) {
            PoiTypeTag prev = poiTypeTags.remove(fromPosition);
            if (moving == null) {
                moving = prev;
            }
            poiTypeTags.add(toPosition > fromPosition ? toPosition - 1 : toPosition, prev);
            notifyItemMoved(fromPosition, toPosition);
        }
    }

    @Override
    public void onItemDrop(int fromPosition, int toPosition) {
        if (listener != null && moving != null) {
            listener.onItemMoved(moving, fromPosition, toPosition);
            moving = null;
        }
    }

    @Override
    public void onItemDismiss(int position) {
        lastRemovedItem = getItem(position);
        poiTypeTags.remove(lastRemovedItem);
        if (listener != null) {
            listener.onItemRemoved(lastRemovedItem);
        }
        notifyItemRemoved(position);
    }

    public void undoLastRemoval() {
        final PoiTypeTag item = lastRemovedItem;

        if (item != null) {
            lastRemovedItem = null;
            addItem(item);
        }
    }

    public void notifyLastRemovalDone(PoiTypeTag poiTypeTag) {
        if (lastRemovedItem != null && lastRemovedItem.equals(poiTypeTag)) {
            lastRemovedItem = null;
        }
    }

    public class PoiTypeTagViewHolder extends RecyclerView.ViewHolder implements ItemTouchHelperViewHolder, View.OnClickListener, View.OnLongClickListener {

        @BindView(R.id.poi_tag_key)
        TextView key;
        @BindView(R.id.poi_tag_value)
        TextView value;
        @BindView(R.id.poi_tag_ordinal)
        TextView ordinal;
        @BindView(R.id.poi_tag_mandatory)
        View mandatory;
        @BindView(R.id.handle)
        ImageView handle;

        public PoiTypeTagViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
        }

        public void onBind(PoiTypeTag item) {
            key.setText(item.getKey());
            value.setText(item.getValue());
            ordinal.setText(String.valueOf(item.getOrdinal()));
            mandatory.setVisibility(item.getMandatory() ? View.VISIBLE : View.GONE);
        }

        @Override
        public void onItemSelected() {

        }

        @Override
        public void onItemClear() {

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

    public interface PoiTypeTagAdapterListener {
        void onItemClick(PoiTypeTag item);

        void onItemLongClick(PoiTypeTag item);

        void onItemRemoved(PoiTypeTag item);

        void onItemMoved(PoiTypeTag item, int fromPosition, int toPosition);
    }

    public interface OnStartDragListener {
        /**
         * Called when a view is requesting a start of a drag.
         *
         * @param viewHolder The holder of the view to drag.
         */
        void onStartDrag(RecyclerView.ViewHolder viewHolder);
    }
}
