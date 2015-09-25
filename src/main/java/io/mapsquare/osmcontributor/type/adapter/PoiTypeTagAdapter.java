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
import android.widget.TextView;

import java.util.ArrayList;

import io.mapsquare.osmcontributor.R;
import io.mapsquare.osmcontributor.core.model.PoiTypeTag;

public class PoiTypeTagAdapter extends DragSwipeRecyclerAdapter<PoiTypeTag> {

    public PoiTypeTagAdapter(Callback<PoiTypeTag> listener) {
        super(listener, new ArrayList<PoiTypeTag>());
    }

    @Override
    protected BaseViewHolder<PoiTypeTag> onCreateNewViewHolder(ViewGroup parent, int viewType) {
        final LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        final View v = inflater.inflate(R.layout.single_poi_tag_draggable, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public long getItemId(PoiTypeTag item) {
        return item.getId();
    }

    private static class ViewHolder extends BaseViewHolder<PoiTypeTag> {

        private final TextView key;
        private final TextView value;
        private final TextView ordinal;
        private final View mandatory;

        public ViewHolder(View itemView) {
            super(itemView, R.id.handle);
            key = (TextView) itemView.findViewById(R.id.poi_tag_key);
            value = (TextView) itemView.findViewById(R.id.poi_tag_value);
            ordinal = (TextView) itemView.findViewById(R.id.poi_tag_ordinal);
            mandatory = itemView.findViewById(R.id.poi_tag_mandatory);
        }

        @Override
        public void onBind(PoiTypeTag item) {
            key.setText(item.getKey());
            value.setText(item.getValue());
            ordinal.setText(String.valueOf(item.getOrdinal()));
            mandatory.setVisibility(item.getMandatory() ? View.VISIBLE : View.GONE);
        }
    }
}