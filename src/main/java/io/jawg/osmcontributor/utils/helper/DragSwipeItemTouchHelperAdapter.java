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
package io.jawg.osmcontributor.utils.helper;


public interface DragSwipeItemTouchHelperAdapter extends SwipeItemTouchHelperAdapter {

    /**
     * Called when an item has been dragged far enough to trigger a move. This is called every time
     * an item is shifted, and not at the end of a "drop" event.
     *
     * @param fromPosition The start position of the moved item.
     * @param toPosition   Then end position of the moved item.
     * @see android.support.v7.widget.RecyclerView#getAdapterPositionFor(android.support.v7.widget.RecyclerView.ViewHolder)
     * @see android.support.v7.widget.RecyclerView.ViewHolder#getAdapterPosition()
     */
    void onItemMove(int fromPosition, int toPosition);

    /**
     * Called when an item is dropped at the end of a drag.
     *
     * @param fromPosition Original position of the dragged item.
     * @param toPosition Final position of the dragged item, where it was dropped.
     */
    void onItemDrop(int fromPosition, int toPosition);
}
