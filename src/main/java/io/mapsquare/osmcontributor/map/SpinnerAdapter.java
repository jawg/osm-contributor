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
package io.mapsquare.osmcontributor.map;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import io.mapsquare.osmcontributor.R;
import io.mapsquare.osmcontributor.core.model.PoiType;
import io.mapsquare.osmcontributor.utils.FlavorUtils;

public class SpinnerAdapter extends BaseAdapter {
    private List<PoiType> mItems = new ArrayList<>();
    private Context context;
    private LayoutInflater inflater;
    BitmapHandler bitmapHandler;

    public SpinnerAdapter(Context context, BitmapHandler bitmapHandler) {
        this.context = context;
        this.bitmapHandler = bitmapHandler;
        inflater = LayoutInflater.from(context);
    }

    public void clear() {
        mItems.clear();
    }

    public void addItem(PoiType type) {
        mItems.add(type);
        Collections.sort(mItems);
    }

    public void addItems(Collection<PoiType> types) {
        mItems.addAll(types);
        Collections.sort(mItems);
    }

    @Override
    public int getCount() {
        if (!FlavorUtils.isPoiStorage()) {
            return mItems.size() + 1;
        }
        return mItems.size();
    }

    @Override
    public Object getItem(int position) {
        if (position == mItems.size()) {
            return null;
        }
        return mItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getDropDownView(int position, View view, ViewGroup parent) {
        return getView(position, view, parent, R.layout.spinner_poi_type_item, true);
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        return getView(position, view, parent, R.layout.spinner_poi_type, false);
    }

    @NonNull
    private View getView(int position, View view, ViewGroup parent, int spinnerPoiTypeItem, boolean isItemDrop) {
        ViewHolder holder;

        if (view != null && view.getTag() instanceof ViewHolder && ((ViewHolder) view.getTag()).isItemDrop) {
            holder = (ViewHolder) view.getTag();
        } else {
            view = inflater.inflate(spinnerPoiTypeItem, parent, false);
            holder = new ViewHolder(view, isItemDrop);
            view.setTag(holder);
        }


        // if note
        if (mItems.size() == position && !FlavorUtils.isPoiStorage()) {
            holder.getTextView().setCompoundDrawablesWithIntrinsicBounds(R.drawable.open_book, 0, 0, 0);
        } else {
            Integer resourceId = bitmapHandler.getIconDrawableId(mItems.get(position).getId());
            holder.getTextView().setCompoundDrawablesWithIntrinsicBounds(resourceId, 0, 0, 0);
        }

        holder.getTextView().setText(getTitle(position));

        return view;
    }


    static class ViewHolder {
        @InjectView(R.id.text_view_spinner)
        TextView textView;

        boolean isItemDrop = false;


        public TextView getTextView() {
            return textView;
        }

        public ViewHolder(View view, boolean isItemDrop) {
            ButterKnife.inject(this, view);
            this.isItemDrop = isItemDrop;
        }
    }

    private String getTitle(int position) {
        if (position == mItems.size()) {
            return context.getResources().getString(R.string.note_spinner_element);
        }
        return position >= 0 && position < mItems.size() ? mItems.get(position).getName() : "";
    }

    @Override
    public void notifyDataSetChanged() {
        Collections.sort(mItems);
        super.notifyDataSetChanged();
    }

    public int getPoiTypePosition(Long id) {
        for (PoiType poiType : mItems) {
            if (poiType.getId().equals(id)) {
                return mItems.indexOf(poiType);
            }
        }
        return 0;
    }
}