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


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import io.mapsquare.osmcontributor.R;
import io.mapsquare.osmcontributor.core.model.PoiType;
import io.mapsquare.osmcontributor.map.BitmapHandler;

public class PoiTypeAdapter extends DragSwipeRecyclerAdapter<PoiType> {

    private BitmapHandler bitmapHandler;

    public PoiTypeAdapter(Callback<PoiType> listener, BitmapHandler bitmapHandler) {
        super(listener, new ArrayList<PoiType>());
        this.bitmapHandler = bitmapHandler;
    }

    @Override
    protected ViewHolder onCreateNewViewHolder(ViewGroup parent, int viewType) {
        final LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        final View v = inflater.inflate(R.layout.single_poi_type, parent, false);
        return new ViewHolder(v);
    }

    @Override
    protected void onViewHolderBound(BaseViewHolder<PoiType> holder, PoiType item) {
        ((ViewHolder) holder).icon.setImageDrawable(bitmapHandler.getDrawable(item.getIcon()));
    }

    @Override
    public long getItemId(PoiType item) {
        return item.getId();
    }

    private static class ViewHolder extends BaseViewHolder<PoiType> {

        public final ImageView icon;
        private final TextView text;
        private final TextView details;

        public ViewHolder(View itemView) {
            super(itemView, 0);
            icon = (ImageView) itemView.findViewById(R.id.poi_type_icon);
            text = (TextView) itemView.findViewById(R.id.poi_type_name);
            details = (TextView) itemView.findViewById(R.id.poi_type_details);
        }

        @Override
        public void onBind(PoiType item) {
            text.setText(item.getName());
            details.setText(itemView.getContext().getString(R.string.tag_number, item.getTags().size()));
        }
    }
}
