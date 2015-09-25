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
package io.mapsquare.osmcontributor.upload;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import io.mapsquare.osmcontributor.R;

public class PoisAdapter extends BaseAdapter {


    private List<PoiUpdateWrapper> poisWrapper = null;
    private LayoutInflater inflater;
    private Context context;

    public PoisAdapter(Context context, List<PoiUpdateWrapper> wrapper) {
        this.poisWrapper = wrapper;
        this.context = context;
        inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return poisWrapper.size();
    }

    @Override
    public Object getItem(int position) {
        return poisWrapper.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        ViewHolder holder;

        if (view != null) {
            holder = (ViewHolder) view.getTag();
        } else {
            view = inflater.inflate(R.layout.single_poi_layout, parent, false);
            holder = new ViewHolder(view);
            view.setTag(holder);
        }

        final PoiUpdateWrapper poiWrapper = poisWrapper.get(position);

        holder.getPoiName().setText(poiWrapper.getName());
        switch (poiWrapper.getAction()) {
            case CREATE:
                holder.getPoiAction().setText(view.getContext().getString(R.string.created));
                break;
            case DELETED:
                holder.getPoiAction().setText(view.getContext().getString(R.string.deleted));
                break;
            case UPDATE:
                holder.getPoiAction().setText(view.getContext().getString(R.string.updated));
                break;
        }

        return view;
    }

    static class ViewHolder {
        @InjectView(R.id.poi_action)
        TextView poiAction;

        @InjectView(R.id.poi_name)
        TextView poiName;

        public TextView getPoiAction() {
            return poiAction;
        }

        public TextView getPoiName() {
            return poiName;
        }

        public ViewHolder(View view) {
            ButterKnife.inject(this, view);
        }
    }
}

