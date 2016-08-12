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

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Color;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.mapsquare.osmcontributor.R;
import io.mapsquare.osmcontributor.offline.OfflineRegionManager;

/**
 * @author Tommy Buonomo on 11/07/16.
 */
public class OfflineRegionsAdapter extends RecyclerView.Adapter<OfflineRegionsAdapter.OfflineRegionHolder> {
    private final List<OfflineRegionItem> offlineRegions;
    private EventBus eventBus;
    private Context context;

    public OfflineRegionsAdapter(List<OfflineRegionItem> offlineRegions) {
        this.offlineRegions = offlineRegions;
        eventBus = EventBus.getDefault();
    }

    @Override
    public OfflineRegionHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View viewRoot = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_offline_region, parent, false);
        this.context = parent.getContext();
        return new OfflineRegionHolder(viewRoot);
    }

    @Override
    public void onBindViewHolder(final OfflineRegionHolder holder, final int position) {
        OfflineRegionItem region = offlineRegions.get(position);

        String regionName = OfflineRegionManager.decodeRegionName(region.getOfflineRegion().getMetadata());
        holder.offlineRegionTextView.setText(regionName);

        if (region.isSelected()) {
            Animator animX = ObjectAnimator.ofFloat(holder.cardView, View.SCALE_X, 1.15f);
            Animator animY = ObjectAnimator.ofFloat(holder.cardView, View.SCALE_Y, 1.15f);
            AnimatorSet animSet = new AnimatorSet();
            animSet.playTogether(animX, animY);
            animSet.start();
        } else {
            Animator animX = ObjectAnimator.ofFloat(holder.cardView, View.SCALE_X, 1.0f);
            Animator animY = ObjectAnimator.ofFloat(holder.cardView, View.SCALE_Y, 1.0f);
            AnimatorSet animSet = new AnimatorSet();
            animSet.playTogether(animX, animY);
            animSet.start();
        }

        if (region.getStatus().isComplete()) {
            holder.cardView.setBackgroundColor(ContextCompat.getColor(context, R.color.colorPrimaryDark));
            holder.offlineRegionTextView.setTextColor(Color.WHITE);
        } else {
            holder.cardView.setBackgroundColor(ContextCompat.getColor(context, R.color.active_text));
            holder.offlineRegionTextView.setTextColor(ContextCompat.getColor(context, R.color.disable_text));
        }
    }

    @Override
    public int getItemCount() {
        return offlineRegions.size();
    }

    public List<OfflineRegionItem> getOfflineRegionItems() {
        return offlineRegions;
    }

    public OfflineRegionItem getOfflineRegion(int position) {
        return offlineRegions.get(position);
    }

    public void addOfflineRegion(OfflineRegionItem region) {
        if (region != null && offlineRegions != null) {
            offlineRegions.add(region);
            notifyDataSetChanged();
        }
    }

    public void removeOfflineRegion(OfflineRegionItem offlineRegion) {
        if (offlineRegion != null && offlineRegions != null) {
            offlineRegions.remove(offlineRegion);
            notifyDataSetChanged();
        }
    }

    public static class OfflineRegionHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.item_offline_region_text)
        TextView offlineRegionTextView;

        @BindView(R.id.item_offline_region_card)
        CardView cardView;

        public OfflineRegionHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
