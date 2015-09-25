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
package io.mapsquare.osmcontributor.type;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.NinePatchDrawable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.h6ah4i.android.widget.advrecyclerview.animator.GeneralItemAnimator;
import com.h6ah4i.android.widget.advrecyclerview.animator.SwipeDismissItemAnimator;
import com.h6ah4i.android.widget.advrecyclerview.draggable.RecyclerViewDragDropManager;
import com.h6ah4i.android.widget.advrecyclerview.swipeable.RecyclerViewSwipeManager;
import com.h6ah4i.android.widget.advrecyclerview.touchguard.RecyclerViewTouchActionGuardManager;
import com.h6ah4i.android.widget.advrecyclerview.utils.WrapperAdapterUtils;

import io.mapsquare.osmcontributor.R;
import io.mapsquare.osmcontributor.type.adapter.DragSwipeRecyclerAdapter;

public class DragSwipeRecyclerHelper {

    private RecyclerView recyclerView;
    private DragSwipeRecyclerAdapter adapter;
    private RecyclerView.Adapter wrappedAdapter;
    private RecyclerViewTouchActionGuardManager touchGuardManager;
    private RecyclerViewDragDropManager viewDragDropManager;
    private RecyclerViewSwipeManager viewSwipeManager;

    public DragSwipeRecyclerHelper(RecyclerView recyclerView, DragSwipeRecyclerAdapter adapter) {
        Context context = recyclerView.getContext();
        this.recyclerView = recyclerView;
        this.adapter = adapter;

        // Recycler layout
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        recyclerView.setHasFixedSize(true);

        // Touch guard manager is required to suppress scrolling while swipe-dismiss animation is running
        touchGuardManager = new RecyclerViewTouchActionGuardManager();
        touchGuardManager.setInterceptVerticalScrollingWhileAnimationRunning(true);
        touchGuardManager.setEnabled(true);

        // Drag & drop manager
        viewDragDropManager = new RecyclerViewDragDropManager();
        Drawable shadow = context.getResources().getDrawable(R.drawable.ms9_composite_shadow_z3);
        viewDragDropManager.setDraggingItemShadowDrawable((NinePatchDrawable) shadow);

        // Swipe manager
        viewSwipeManager = new RecyclerViewSwipeManager();

        // Adapter
        wrappedAdapter = viewDragDropManager.createWrappedAdapter(adapter);
        wrappedAdapter = viewSwipeManager.createWrappedAdapter(wrappedAdapter);
        recyclerView.setAdapter(wrappedAdapter);

        // Animations & decorations
        GeneralItemAnimator animator = new SwipeDismissItemAnimator();
        animator.setSupportsChangeAnimations(false);
        recyclerView.setItemAnimator(animator);

        recyclerView.addItemDecoration(new ItemDividerDecoration(context));

        // Initialization order gives the priority: TouchActionGuard > Swipe > DragAndDrop
        touchGuardManager.attachRecyclerView(recyclerView);
        viewSwipeManager.attachRecyclerView(recyclerView);
        viewDragDropManager.attachRecyclerView(recyclerView);
    }

    public void onPause() {
        viewDragDropManager.cancelDrag();
    }

    public void onDestroy() {
        viewDragDropManager.release();
        viewSwipeManager.release();
        touchGuardManager.release();
        recyclerView.setItemAnimator(null);
        recyclerView.setAdapter(null);
        WrapperAdapterUtils.releaseAll(wrappedAdapter);
        adapter.destroy();
    }
}
