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
import android.widget.RadioButton;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.BindViews;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.jawg.osmcontributor.R;

public class TagRadioChoiceHolder extends RecyclerView.ViewHolder {
    public View poiTagLayout;
    private CheckBoxListener checkBoxListener;

    @BindView(R.id.poi_key)
    TextView textViewKey;

    @BindViews({R.id.rb_2, R.id.rb_3, R.id.rb_4, R.id.rb_5, R.id.rb_6})
    RadioButton[] radioButtons;

    @BindView(R.id.rb_1)
    RadioButton undefinedRadioButton;

    @BindView(R.id.content_layout)
    LinearLayout content;

    public TagRadioChoiceHolder(View v) {
        super(v);
        poiTagLayout = v;
        ButterKnife.bind(this, v);
    }

    public CheckBoxListener getCheckBoxListener() {
        return checkBoxListener;
    }

    public void setCheckBoxListener(CheckBoxListener checkBoxListener) {
        this.checkBoxListener = checkBoxListener;
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

    public LinearLayout getContent() {
        return content;
    }

    @OnClick({R.id.rb_1, R.id.rb_2, R.id.rb_3, R.id.rb_4, R.id.rb_5, R.id.rb_6})
    public void onClick(View v) {
        // Uncheck all radio buttons
        RadioButton radioButtonSelected = (RadioButton) v;

        if (undefinedRadioButton.isChecked() && undefinedRadioButton.getId() != v.getId()) {
            undefinedRadioButton.setChecked(false);
        }

        for (int i = 0; i < radioButtons.length; i++) {
            if (radioButtons[i].getId() != v.getId() && radioButtons[i].isChecked()) {
                radioButtons[i].setChecked(false);
            }
        }

        if (checkBoxListener != null) {
            checkBoxListener.onCheckBoxSelected(radioButtonSelected.getText().toString());
        }


        // Apply change
//        eventBus.post(new PleaseApplyTagChange(textViewKey.getText().toString(), radioButtonSelected.getText().toString()));
    }

    public interface CheckBoxListener {
        void onCheckBoxSelected(String value);
    }
}