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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import org.greenrobot.eventbus.EventBus;
import io.mapsquare.osmcontributor.R;
import io.mapsquare.osmcontributor.utils.edition.AutocompleteValue;
import io.mapsquare.osmcontributor.ui.events.edition.PleaseApplyTagChange;

public class AutocompleteAdapter extends BaseAdapter {
    private final Context context;
    private final TextView textViewValue;
    private final String key;
    private EventBus eventBus;
    private List<AutocompleteValue> values = new ArrayList<>();
    private View cardView;

    public AutocompleteAdapter(Context context, List<String> values, TextView textViewValue, View cardView, String key) {
        this.context = context;
        this.textViewValue = textViewValue;
        this.eventBus = EventBus.getDefault();
        this.key = key;
        this.cardView = cardView;

        for (String autocompleteValue : values) {
            this.values.add(new AutocompleteValue(autocompleteValue));
        }
    }

    @Override
    public int getCount() {
        return values.size();
    }

    @Override
    public Object getItem(int position) {
        return values.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(final int position, View convertView, final ViewGroup parent) {
        View view;
        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(context);
            view = inflater.inflate(R.layout.tag, parent, false);
        } else {
            view = convertView;
        }

        final String value = values.get(position).getValue();
        Button button = (Button) view.findViewById(R.id.tag_txt);
        button.setText(value);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                textViewValue.setText(value);
                cardView.performClick();
                eventBus.postSticky(new PleaseApplyTagChange(key, value));
            }
        });

        return view;
    }
}