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
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.mapsquare.osmcontributor.rest.dtos.osm.SuggestionsDataDto;
import io.mapsquare.osmcontributor.ui.managers.TypeManager;
import io.mapsquare.osmcontributor.rest.dtos.osm.SuggestionsDto;
import timber.log.Timber;

public class SuggestionAdapter extends BaseAdapter implements Filterable {

    private static final int LAYOUT = android.R.layout.simple_dropdown_item_1line;
    private static final int MAX_RESULTS = 5;

    private LayoutInflater layoutInflater;
    private List<SuggestionsDataDto> suggestionsList;
    private TypeManager typeManager;
    private Filter filter;

    public SuggestionAdapter(Context context, TypeManager typeManager) {
        layoutInflater = LayoutInflater.from(context);
        suggestionsList = new ArrayList<>();

        this.typeManager = typeManager;
        filter = new SuggestionFilter();
    }

    @Override
    public int getCount() {
        return suggestionsList.size();
    }

    @Override
    public SuggestionsDataDto getItem(int position) {
        return suggestionsList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view;
        ViewHolder holder;

        if (convertView == null) {
            view = layoutInflater.inflate(LAYOUT, parent, false);
            holder = new ViewHolder(view);
            view.setTag(holder);
        } else {
            view = convertView;
            holder = (ViewHolder) convertView.getTag();
        }

        holder.bind(suggestionsList.get(position));

        return view;
    }

    @Override
    public Filter getFilter() {
        return filter;
    }

    private static class ViewHolder {

        private final TextView textView;

        public ViewHolder(View root) {
            textView = (TextView) root.findViewById(android.R.id.text1);
        }

        public void bind(SuggestionsDataDto item) {
            textView.setText(item.getKey());
        }
    }

    private class SuggestionFilter extends Filter {

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            if (constraint == null) {
                Timber.d("Null constraint in performFiltering");
                return null;
            }

            String query = constraint.toString().trim();
            SuggestionsDto suggestionsDto = typeManager.getSuggestionsBlocking(query, 1, MAX_RESULTS);
            List values = suggestionsDto != null ? suggestionsDto.getData() : Collections.EMPTY_LIST;

            FilterResults results = new FilterResults();
            results.values = values;
            results.count = values.size();
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            if (results == null) {
                Timber.d("Null results in publishResults");
                return;
            }

            //noinspection unchecked
            suggestionsList = (List<SuggestionsDataDto>) results.values;
            if (results.count > 0) {
                notifyDataSetChanged();
            } else {
                notifyDataSetInvalidated();
            }
        }

        @Override
        public CharSequence convertResultToString(Object resultValue) {
            return ((SuggestionsDataDto) resultValue).getKey();
        }
    }
}
