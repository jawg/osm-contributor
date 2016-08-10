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

import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.mapbox.mapboxsdk.offline.OfflineRegion;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.mapsquare.osmcontributor.R;
import io.mapsquare.osmcontributor.service.OfflineAreaDownloadService;

/**
 * @author Tommy Buonomo on 11/07/16.
 */
public class OfflineRegionsAdapter extends RecyclerView.Adapter<OfflineRegionsAdapter.OfflineRegionHolder> {
    private final List<OfflineRegion> offlineRegions;
    private EventBus eventBus;
    private CardView cardViewSelected;
    private Context context;
    private int selectedPosition = -1;
    private int previousSelectedPosition = -1;

    public OfflineRegionsAdapter(List<OfflineRegion> offlineRegions) {
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
    public void onBindViewHolder(final OfflineRegionHolder holder, int position) {
        OfflineRegion region = offlineRegions.get(position);
        String mapTag = OfflineAreaDownloadService.decodeMetadata(region.getMetadata());
        holder.offlineRegionTextView.setText(mapTag);

        if (position == selectedPosition) {
            holder.cardView.setCardElevation(context.getResources().getDimension(R.dimen.cardview_default_elevation) * 4);
            holder.cardView.animate().scaleX(1.1f).scaleY(1.1f).start();
        } else if (position == previousSelectedPosition) {
            holder.cardView.setCardElevation(context.getResources().getDimension(R.dimen.cardview_default_elevation));
            holder.cardView.animate().scaleX(1.0f).scaleY(1.0f).start();
        }
    }

    @Override
    public int getItemCount() {
        return offlineRegions.size();
    }

    public List<OfflineRegion> getOfflineRegions() {
        return offlineRegions;
    }

    public OfflineRegion getOfflineRegion(int position) {
        return offlineRegions.get(position);
    }

    public void addOfflineRegion(OfflineRegion region) {
        if (region != null && offlineRegions != null) {
            offlineRegions.add(region);
            notifyDataSetChanged();
        }
    }

    public void setSelectedPosition(int position) {
        if (selectedPosition != -1) {
            previousSelectedPosition = selectedPosition;
        }
        selectedPosition = position;
        notifyDataSetChanged();
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
