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
import android.widget.ImageView;
import android.widget.LinearLayout;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.jawg.osmcontributor.R;
import io.jawg.osmcontributor.ui.adapters.item.ShelterType;

public class ShelterChoiceHolder extends RecyclerView.ViewHolder {
    public View poiTagLayout;
    private SelectionListener selectionListener;

    @BindView(R.id.shelter)
    ImageView shelterImg;
    @BindView(R.id.none)
    ImageView noneImg;
    @BindView(R.id.pole)
    ImageView poleImg;

    @BindView(R.id.content_layout)
    LinearLayout content;


    public ShelterChoiceHolder(View v) {
        super(v);
        this.poiTagLayout = v;
        ButterKnife.bind(this, v);
    }

    public SelectionListener getSelectionListener() {
        return selectionListener;
    }

    public void setSelectionListener(SelectionListener selectionListener) {
        this.selectionListener = selectionListener;
    }

    public LinearLayout getContent() {
        return content;
    }

    public void setContent(LinearLayout content) {
        this.content = content;
    }

    public ImageView getShelterImg() {
        return shelterImg;
    }

    public ImageView getNoneImg() {
        return noneImg;
    }

    public ImageView getPoleImg() {
        return poleImg;
    }

    @OnClick(R.id.shelter)
    public void onShelterClick(View v) {
        if (selectionListener != null) {
            selectionListener.shelterClicked(ShelterType.SHELTER);
        }
    }

    @OnClick(R.id.none)
    public void onNoneClick(View v) {
        if (selectionListener != null) {
            selectionListener.shelterClicked(ShelterType.NONE);
        }
    }

    @OnClick(R.id.pole)
    public void onPoleClick(View v) {
        if (selectionListener != null) {
            selectionListener.shelterClicked(ShelterType.POLE);
        }
    }

    public interface SelectionListener {
        void shelterClicked(ShelterType shelterType);
    }
}