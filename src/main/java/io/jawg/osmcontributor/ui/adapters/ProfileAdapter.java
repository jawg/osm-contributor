/**
 * Copyright (C) 2019 Takima
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
package io.jawg.osmcontributor.ui.adapters;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Map;
import java.util.TreeMap;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.jawg.osmcontributor.R;
import io.jawg.osmcontributor.model.entities.H2GeoPresets;
import io.jawg.osmcontributor.model.entities.H2GeoPresetsItem;
import io.jawg.osmcontributor.rest.dtos.dma.H2GeoDto;
import io.jawg.osmcontributor.rest.mappers.H2GeoPresetsItemMapper;
import io.jawg.osmcontributor.ui.utils.views.customs.DraweeView16x9;

public class ProfileAdapter extends BaseAdapter {
    @Inject
    SharedPreferences sharedPreferences;

    @Inject
    H2GeoPresetsItemMapper mapper;

    private Map<String, H2GeoPresetsItem> presets = new TreeMap<>();
    private String[] keys;
    private Context context;
    private ProfileSelectedListener profileSelectedListenerListener;
    private H2GeoDto defaultPreset;

    public ProfileAdapter(Context context, ProfileSelectedListener profileSelectedListener) {
        this.context = context;
        this.profileSelectedListenerListener = profileSelectedListener;
        this.defaultPreset = H2GeoDto.getDefaultPreset(context);
    }

    @Override
    public int getCount() {
        return presets.size() + 1;
    }

    @Override
    public H2GeoPresetsItem getItem(int position) {
        if (position == 0) {
            return null;
        }
        return presets.get(keys[position - 1]);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(final int position, View view, final ViewGroup parent) {
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

        holder.getNameTextView().setText(h2GeoPreset == null ? mapper.getCurrentLanguage(defaultPreset.getName()) : h2GeoPreset.getName());
        holder.getDescriptionTextView().setText(h2GeoPreset == null ? mapper.getCurrentLanguage(defaultPreset.getDescription()) : h2GeoPreset.getDescription());
        holder.getImage().setImageURI(h2GeoPreset == null ? defaultPreset.getImage() : h2GeoPreset.getImage());
        if (sharedPreferences.getString(context.getString(R.string.shared_prefs_preset_selected), "Default").equals(holder.getNameTextView().getText())) {
            holder.getIsSeleted().setVisibility(View.VISIBLE);
        } else {
            holder.getIsSeleted().setVisibility(View.INVISIBLE);
        }
        view.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View view) {
                if (profileSelectedListenerListener != null) {
                    profileSelectedListenerListener.profileClicked(h2GeoPreset);
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
        @BindView(R.id.title_preset)
        TextView name;

        @BindView(R.id.description_preset)
        TextView description;

        @BindView(R.id.banner)
        DraweeView16x9 banner;

        @BindView(R.id.is_selected)
        ImageView isSeleted;

        public ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }

        public TextView getNameTextView() {
            return name;
        }

        public TextView getDescriptionTextView() {
            return description;
        }

        public DraweeView16x9 getImage() {
            return banner;
        }

        public ImageView getIsSeleted() {
            return isSeleted;
        }
    }

    public interface ProfileSelectedListener {
        void profileClicked(H2GeoPresetsItem h2GeoPresetsItem);
    }
}
