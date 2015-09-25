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
package io.mapsquare.osmcontributor.edition.holder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import butterknife.ButterKnife;
import butterknife.InjectView;
import io.mapsquare.osmcontributor.R;

public class ViewHolderPoiTagManyValues extends RecyclerView.ViewHolder {
    public View poiTagLayout;

    @InjectView(R.id.poi_key)
    TextView textViewKey;

    @InjectView(R.id.poi_value)
    TextView textViewValue;

    @InjectView(R.id.edit_btn)
    ImageButton editButton;

    public ViewHolderPoiTagManyValues(View v) {
        super(v);
        poiTagLayout = v;
        ButterKnife.inject(this, v);
    }

    public View getPoiTagLayout() {
        return poiTagLayout;
    }

    public void setPoiTagLayout(View poiTagLayout) {
        this.poiTagLayout = poiTagLayout;
    }

    public TextView getTextViewKey() {
        return textViewKey;
    }

    public void setTextViewKey(TextView textViewKey) {
        this.textViewKey = textViewKey;
    }

    public TextView getTextViewValue() {
        return textViewValue;
    }

    public void setTextViewValue(TextView textViewValue) {
        this.textViewValue = textViewValue;
    }

    public ImageButton getEditButton() {
        return editButton;
    }

    public void setEditButton(ImageButton editButton) {
        this.editButton = editButton;
    }
}