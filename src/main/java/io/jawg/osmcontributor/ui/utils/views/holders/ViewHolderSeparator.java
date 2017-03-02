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
package io.jawg.osmcontributor.ui.utils.views.holders;


import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import butterknife.ButterKnife;
import butterknife.BindView;
import io.jawg.osmcontributor.R;

public class ViewHolderSeparator extends RecyclerView.ViewHolder {
    public enum SeparatorType {
        OPTIONAL,
        REQUIRED
    }

    @BindView(R.id.text_view)
    TextView textViewValue;

    public ViewHolderSeparator(View view, SeparatorType type) {
        super(view);
        ButterKnife.bind(this, view);

        if (type == SeparatorType.OPTIONAL) {
            textViewValue.setText(view.getContext().getString(R.string.optional));
        } else {
            textViewValue.setText(view.getContext().getString(R.string.required));
        }
    }
}
