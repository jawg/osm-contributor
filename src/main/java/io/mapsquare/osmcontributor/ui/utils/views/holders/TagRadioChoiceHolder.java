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


import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.RadioButton;
import android.widget.TextView;

import org.greenrobot.eventbus.EventBus;

import butterknife.BindView;
import butterknife.BindViews;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.mapsquare.osmcontributor.R;
import io.mapsquare.osmcontributor.ui.events.edition.PleaseApplyTagChangeView;

public class TagRadioChoiceHolder extends RecyclerView.ViewHolder {
    public View poiTagLayout;

    private EventBus eventBus;

    @BindView(R.id.poi_key)
    TextView textViewKey;

    @BindViews({R.id.rb_2, R.id.rb_3, R.id.rb_4, R.id.rb_5, R.id.rb_6})
    RadioButton[] radioButtons;

    @BindView(R.id.rb_1)
    RadioButton undefinedRadioButton;

    public TagRadioChoiceHolder(View v) {
        super(v);
        poiTagLayout = v;
        ButterKnife.bind(this, v);
        eventBus = EventBus.getDefault();
    }

    public View getPoiTagLayout() {
        return poiTagLayout;
    }

    public TextView getTextViewKey() {
        return textViewKey;
    }

    public RadioButton[] getRadioButtons() {
        return radioButtons;
    }

    public RadioButton getRadioButton(int pos) {
        return radioButtons[pos];
    }

    public RadioButton getUndefinedRadioButton() {
        return undefinedRadioButton;
    }

    @OnClick({R.id.rb_1, R.id.rb_2, R.id.rb_3, R.id.rb_4, R.id.rb_5, R.id.rb_6})
    public void onClick(View v) {
        // Uncheck all radio buttons
        undefinedRadioButton.setChecked(false);
        for (RadioButton radioButton : radioButtons) {
            radioButton.setChecked(false);
        }
        // Check selected radio button
        RadioButton radioButton = (RadioButton) v;
        radioButton.setChecked(true);

        // Apply change
        eventBus.post(new PleaseApplyTagChangeView(textViewKey.getText().toString(), radioButton.getText().toString()));
    }
}