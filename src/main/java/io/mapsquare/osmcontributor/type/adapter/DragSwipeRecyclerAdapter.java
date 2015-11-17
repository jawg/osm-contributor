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
package io.mapsquare.osmcontributor.type.adapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Build;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.FrameLayout;

import com.h6ah4i.android.widget.advrecyclerview.draggable.DraggableItemAdapter;
import com.h6ah4i.android.widget.advrecyclerview.draggable.ItemDraggableRange;
import com.h6ah4i.android.widget.advrecyclerview.draggable.RecyclerViewDragDropManager;
import com.h6ah4i.android.widget.advrecyclerview.swipeable.RecyclerViewSwipeManager;
import com.h6ah4i.android.widget.advrecyclerview.swipeable.SwipeableItemAdapter;
import com.h6ah4i.android.widget.advrecyclerview.utils.AbstractDraggableSwipeableItemViewHolder;

import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import io.mapsquare.osmcontributor.R;

public abstract class DragSwipeRecyclerAdapter<T extends Comparable<T>>
        extends RecyclerView.Adapter<DragSwipeRecyclerAdapter.BaseViewHolder<T>>
        implements SwipeableItemAdapter<DragSwipeRecyclerAdapter.BaseViewHolder<T>>,
        DraggableItemAdapter<DragSwipeRecyclerAdapter.BaseViewHolder<T>> {

    private static final int[] NORMAL_STATE = new int[]{};
    private static final int[] PRESSED_STATE = new int[]{android.R.attr.state_pressed, android.R.attr.state_enabled};

    private WeakReference<DragSwipeRecyclerAdapter<T>> adapterReference;
    private Callback<T> listener;
    private List<T> data;

    private T lastRemovedItem;
    private int lastRemovedPosition;

    public DragSwipeRecyclerAdapter(Callback<T> listener, final List<T> data) {
        setHasStableIds(true);
        this.listener = listener;
        this.data = data;
        this.adapterReference = new WeakReference<>(this);
    }

    @Override
    public final BaseViewHolder<T> onCreateViewHolder(ViewGroup parent, int viewType) {
        BaseViewHolder<T> viewHolder = onCreateNewViewHolder(parent, viewType);
        viewHolder.setAdapterWrapper(adapterReference);
        return viewHolder;
    }

    protected abstract BaseViewHolder<T> onCreateNewViewHolder(ViewGroup parent, int viewType);

    @Override
    public final void onBindViewHolder(BaseViewHolder<T> holder, int position) {
        final int dragState = holder.getDragStateFlags();

        if ((dragState & RecyclerViewDragDropManager.STATE_FLAG_IS_UPDATED) != 0) {
            Drawable drawable;
            View handle = holder.handle;
            if (handle != null && (drawable = handle.getBackground()) != null && drawable.isStateful()) {
                if ((dragState & RecyclerViewDragDropManager.STATE_FLAG_IS_ACTIVE) != 0) {
                    drawable.setState(PRESSED_STATE);
                    drawable.jumpToCurrentState();
                } else {
                    drawable.setState(NORMAL_STATE);
                }
            }
        }

        T item = data.get(position);
        holder.onBind(item);
        onViewHolderBound(holder, item);
    }

    protected void onViewHolderBound(BaseViewHolder<T> holder, T item) {
    }

    @Override
    public final int getItemCount() {
        return data.size();
    }

    @Override
    public final long getItemId(int position) {
        return getItemId(data.get(position));
    }

    public abstract long getItemId(T item);

    public final T getItemById(long id) {
        for (T item : data) {
            if (getItemId(item) == id) {
                return item;
            }
        }
        return null;
    }

    public final int getItemPosition(T item) {
        return data.indexOf(item);
    }

    public void clearAndAddAll(Collection<T> items) {
        data.clear();
        data.addAll(items);
        notifyDataSetChanged();
    }

    public int addItem(T item) {
        int oldPosition = data.indexOf(item);
        if (oldPosition == -1) {
            data.add(item);
        }

        Collections.sort(data);

        int position = data.indexOf(item);
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
    public final boolean onCheckCanStartDrag(BaseViewHolder<T> holder, int position, int x, int y) {
        View handle = holder.handle;
        return handle != null
                && x >= handle.getLeft() && x <= handle.getRight()
                && y >= handle.getTop() && y <= handle.getBottom();
    }

    @Override
    public final ItemDraggableRange onGetItemDraggableRange(BaseViewHolder<T> viewHolder, int position) {
        // No drag-sortable range specified
        return null;
    }

    @Override
    public final void onMoveItem(int fromPosition, int toPosition) {
        if (fromPosition != toPosition) {
            T item = data.remove(fromPosition);
            data.add(toPosition, item);
            notifyItemMoved(fromPosition, toPosition);
            listener.onItemMoved(item, fromPosition, toPosition);
        }
    }

    @Override
    public final int onGetSwipeReactionType(BaseViewHolder<T> holder, int position, int x, int y) {
        if (onCheckCanStartDrag(holder, position, x, y)) {
            return RecyclerViewSwipeManager.REACTION_CAN_NOT_SWIPE_BOTH_WITH_RUBBER_BAND_EFFECT;
        } else {
            return RecyclerViewSwipeManager.REACTION_CAN_SWIPE_BOTH;
        }
    }

    @Override
    public final void onSetSwipeBackground(BaseViewHolder<T> holder, int position, int type) {
        int background;
        switch (type) {
            case RecyclerViewSwipeManager.DRAWABLE_SWIPE_LEFT_BACKGROUND:
            case RecyclerViewSwipeManager.DRAWABLE_SWIPE_RIGHT_BACKGROUND:
                background = R.color.list_swipe_bg;
                break;
            default:
                background = 0;
        }
        holder.itemView.setBackgroundResource(background);
    }

    @Override
    public final int onSwipeItem(BaseViewHolder<T> holder, int position, int result) {
        switch (result) {
            case RecyclerViewSwipeManager.RESULT_SWIPED_RIGHT:
            case RecyclerViewSwipeManager.RESULT_SWIPED_LEFT:
                return RecyclerViewSwipeManager.AFTER_SWIPE_REACTION_MOVE_TO_SWIPED_DIRECTION;
            case RecyclerViewSwipeManager.RESULT_CANCELED:
            default:
                return RecyclerViewSwipeManager.AFTER_SWIPE_REACTION_DEFAULT;
        }
    }

    @Override
    public final void onPerformAfterSwipeReaction(BaseViewHolder<T> holder, int position, int result, int reaction) {
        if (reaction == RecyclerViewSwipeManager.AFTER_SWIPE_REACTION_MOVE_TO_SWIPED_DIRECTION) {
            T item = data.remove(position);
            lastRemovedItem = item;
            lastRemovedPosition = position;
            notifyItemRemoved(position);
            listener.onItemRemoved(item);
        }
    }

    public final void undoLastRemoval() {
        final int position = lastRemovedPosition;
        final T item = lastRemovedItem;

        if (item != null) {
            lastRemovedItem = null;
            data.add(position, item);
            notifyItemInserted(position);
        }
    }

    public final void notifyLastRemovalDone(T item) {
        if (lastRemovedItem == item) {
            lastRemovedItem = null;
        }
    }

    public void destroy() {
        adapterReference.clear();
        lastRemovedItem = null;
        listener = null;
    }

    private void onItemClicked(int position) {
        listener.onItemClicked(data.get(position));
    }

    private void onItemLongClicked(int position) {
        listener.onItemLongClicked(data.get(position));
    }

    public interface Callback<T> {

        void onItemClicked(T item);

        void onItemLongClicked(T item);

        void onItemRemoved(T item);

        void onItemMoved(T item, int fromPosition, int toPosition);
    }

    protected abstract static class BaseViewHolder<T extends Comparable<T>>
            extends AbstractDraggableSwipeableItemViewHolder
            implements View.OnClickListener, View.OnLongClickListener {

        private WeakReference<DragSwipeRecyclerAdapter<T>> adapterReference;
        private final View content;
        private final View handle;

        public BaseViewHolder(View view, int dragHandleId) {
            super(wrapView(view));

            content = view;

            if (dragHandleId != 0) {
                handle = view.findViewById(dragHandleId);
                if (handle == null) {
                    throw new NullPointerException("handle not found in layout");
                }
            } else {
                handle = null;
            }

            // Add a touchable background if the current one does not support touch reaction
            Drawable background = content.getBackground();
            if (background == null || !background.isStateful()) {
                Context context = view.getContext();
                TypedValue typedValue = new TypedValue();
                if (context.getTheme().resolveAttribute(R.attr.selectableItemBackground, typedValue, true)) {
                    Drawable touchBackground;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        touchBackground = context.getTheme().getDrawable(typedValue.resourceId);
                    } else {
                        touchBackground = context.getResources().getDrawable(typedValue.resourceId);
                    }

                    if (background != null) {
                        background = new LayerDrawable(new Drawable[]{background, touchBackground});
                    } else {
                        background = touchBackground;
                    }
                    content.setBackgroundDrawable(background);
                }
            }

            content.setOnClickListener(this);
            content.setOnLongClickListener(this);
        }

        @Override
        public final View getSwipeableContainerView() {
            return content;
        }

        @Override
        public final void onClick(View v) {
            if (v == content) {
                DragSwipeRecyclerAdapter adapter = adapterReference.get();
                if (adapter != null) {
                    adapter.onItemClicked(getAdapterPosition());
                }
            }
        }

        @Override
        public final boolean onLongClick(View v) {
            if (v == content) {
                DragSwipeRecyclerAdapter adapter = adapterReference.get();
                if (adapter != null) {
                    adapter.onItemLongClicked(getAdapterPosition());
                    return true;
                }
            }
            return false;
        }

        public abstract void onBind(T item);

        private void setAdapterWrapper(WeakReference<DragSwipeRecyclerAdapter<T>> adapterReference) {
            this.adapterReference = adapterReference;
        }

        private static View wrapView(View view) {
            ViewGroup root = new FrameLayout(view.getContext());
            root.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
            root.addView(view);
            return root;
        }
    }
}
