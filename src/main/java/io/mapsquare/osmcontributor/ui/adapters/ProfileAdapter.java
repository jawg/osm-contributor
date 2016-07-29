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
import android.widget.TextView;
import butterknife.BindView;
import butterknife.ButterKnife;
import io.mapsquare.osmcontributor.R;
import io.mapsquare.osmcontributor.model.entities.H2GeoPresets;
import io.mapsquare.osmcontributor.model.entities.H2GeoPresetsItem;
import java.util.Map;
import java.util.TreeMap;

public class ProfileAdapter extends BaseAdapter {
    private Map<String, H2GeoPresetsItem> presets = new TreeMap<>();
    private String[] keys;
    private Context context;
    private ProfileSelectedListener profileSelectedListenerListener;

    public ProfileAdapter(Context context, ProfileSelectedListener profileSelectedListener) {
        this.context = context;
        this.profileSelectedListenerListener = profileSelectedListener;
    }

    @Override public int getCount() {
        return presets.size() + 1;
    }

    @Override public H2GeoPresetsItem getItem(int position) {
        if (position == 0) {
            return null;
        }
        return presets.get(keys[position - 1]);
    }

    @Override public long getItemId(int position) {
        return 0;
    }

    @Override public View getView(final int position, View view, final ViewGroup parent) {
        LayoutInflater inflater =
            (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final H2GeoPresetsItem h2GeoPreset = getItem(position);
        final ViewHolder holder;

        if (view != null) {
            holder = (ViewHolder) view.getTag();
        } else {
            view = inflater.inflate(R.layout.single_profile_layout, parent, false);
            holder = new ViewHolder(view);
            view.setTag(holder);
        }

        holder.getNameTextView().setText(h2GeoPreset == null ? "Default" : h2GeoPreset.getName());
        view.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View view) {
                if (profileSelectedListenerListener != null) {
                    if (h2GeoPreset == null) {
                        profileSelectedListenerListener.resetProfile();
                    } else {
                        profileSelectedListenerListener.profileClicked(h2GeoPreset);
                    }
                }
            }
        });
        return view;
    }

    public void addAll(H2GeoPresets h2GeoPresets) {
        this.presets.putAll(h2GeoPresets.getPresets());
        keys = presets.keySet().toArray(new String[presets.size()]);
    }

    static class ViewHolder {
        @BindView(R.id.text_view) TextView name;

        public ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }

        public TextView getNameTextView() {
            return name;
        }
    }

    public interface ProfileSelectedListener {
        void profileClicked(H2GeoPresetsItem h2GeoPresetsItem);

        void resetProfile();
    }
}
