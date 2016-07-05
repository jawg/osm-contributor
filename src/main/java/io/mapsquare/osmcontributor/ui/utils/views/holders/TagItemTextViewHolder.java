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
package io.mapsquare.osmcontributor.ui.utils.views.holders;


import android.support.design.widget.TextInputEditText;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.greenrobot.eventbus.EventBus;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.mapsquare.osmcontributor.R;
import io.mapsquare.osmcontributor.ui.events.edition.PleaseApplyTagChangeView;
import io.mapsquare.osmcontributor.ui.utils.SimpleTextWatcher;

public class TagItemTextViewHolder extends RecyclerView.ViewHolder {
    public View poiTagLayout;

    private EventBus eventBus;

    @BindView(R.id.poi_key)
    TextView textViewKey;

    @BindView(R.id.poi_value)
    TextInputEditText textViewValue;

    @BindView(R.id.grid_layout_wrapper)
    LinearLayout gridViewLayoutWrapper;

    @BindView(R.id.edition)
    RelativeLayout relativeLayoutEdition;


    public TagItemTextViewHolder(View v) {
        super(v);
        poiTagLayout = v;
        ButterKnife.bind(this, v);
        eventBus = EventBus.getDefault();

        textViewValue.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (i1 != i2) {
                    eventBus.post(new PleaseApplyTagChangeView(textViewKey.getText().toString(), charSequence.toString()));
                }
            }
        });
    }

    public View getPoiTagLayout() {
        return poiTagLayout;
    }

    public TextView getTextViewKey() {
        return textViewKey;
    }

    public TextInputEditText getTextViewValue() {
        return textViewValue;
    }

    public LinearLayout getGridViewLayoutWrapper() {
        return gridViewLayoutWrapper;
    }

    public RelativeLayout getRelativeLayoutEdition() {
        return relativeLayoutEdition;
    }
}