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
import android.net.Uri;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.facebook.drawee.view.SimpleDraweeView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ImageAdapter extends BaseAdapter {

    /**
     * List of photos url. Also use as cache.
     */
    private static Map<Long, List<String>> photoUrlsCached = new HashMap<>();

    private List<String> photosUrl;

    /**
     * Context.
     */
    private Context context;

    public ImageAdapter(Context context, Long poiId) {
        this.context = context;
        photosUrl = photoUrlsCached.get(poiId);
        if (photosUrl == null) {
            photosUrl = new ArrayList<>();
            photoUrlsCached.put(poiId, photosUrl);
        }
    }

    @Override
    public int getCount() {
        return photosUrl.size();
    }

    @Override
    public Object getItem(int position) {
        return !photosUrl.isEmpty() ? photosUrl.get(position) : null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    /**
     * Add photo if not in the cache.
     * @param url photo url to add.
     */
    public void addPhoto(String url, Long poiId) {
        if (!photoUrlsCached.get(poiId).contains(url)) {
            photoUrlsCached.get(poiId).add(url);
            notifyDataSetChanged();
        }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        SimpleDraweeView image = new SimpleDraweeView(context);
        image.setImageURI(Uri.parse(photosUrl.get(position)));
        image.setLayoutParams(new ViewGroup.LayoutParams(
                (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 110, context.getResources().getDisplayMetrics()),
                (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 110, context.getResources().getDisplayMetrics())));
        return image;
    }

    /**
     * Get cache.
     * @return cache of photo urls
     */
    public static List<String> getPhotoUrlsCached(Long poiId) {
        return photoUrlsCached.get(poiId);
    }
}
