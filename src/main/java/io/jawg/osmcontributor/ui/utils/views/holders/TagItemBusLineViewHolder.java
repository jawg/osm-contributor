/**
 * Copyright (C) 2016 eBusiness Information
 * <p>
 * This file is part of OSM Contributor.
 * <p>
 * OSM Contributor is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * OSM Contributor is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with OSM Contributor.  If not, see <http://www.gnu.org/licenses/>.
 */
package io.jawg.osmcontributor.ui.utils.views.holders;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.jawg.osmcontributor.R;

public class TagItemBusLineViewHolder extends RecyclerView.ViewHolder {

    @BindView(R.id.opening_time_list_view)
    RecyclerView busLineRecyclerView;

    @BindView(R.id.poi_key)
    TextView textViewKey;

    @BindView(R.id.tag_item_edit_bus_line_add_layout)
    View editAddLayout;

    @BindView(R.id.tag_item_edit_bus_line_add_button)
    View editAddButton;

    @BindView(R.id.content_layout)
    LinearLayout content;

    public TagItemBusLineViewHolder(View v) {
        super(v);
        ButterKnife.bind(this, v);
    }

    public View getEditAddLayout() {
        return editAddLayout;
    }

    public TextView getTextViewKey() {
        return textViewKey;
    }

    public RecyclerView getBusLineRecyclerView() {
        return busLineRecyclerView;
    }

    public LinearLayout getContent() {
        return content;
    }
}