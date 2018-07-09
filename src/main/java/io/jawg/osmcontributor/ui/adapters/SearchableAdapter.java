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
package io.jawg.osmcontributor.ui.adapters;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.jawg.osmcontributor.R;

public class SearchableAdapter extends BaseAdapter implements Filterable {

    private List<String> originalValues = null;
    private List<String> filteredValues = null;
    private LayoutInflater inflater;
    private ItemFilter filter = new ItemFilter();
    private EditText editText;
    private Context context;

    public SearchableAdapter(Context context, List<String> values, EditText editText) {
        this.filteredValues = values;
        Collections.sort(filteredValues, new SortIgnoreCase());
        this.originalValues = filteredValues;
        this.editText = editText;
        this.context = context;
        inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return filteredValues.size();
    }

    @Override
    public Object getItem(int position) {
        return filteredValues.get(position);
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
            view = inflater.inflate(R.layout.single_autocomplete_layout, parent, false);
            holder = new ViewHolder(view);
            view.setTag(holder);
        }

        final String value = filteredValues.get(position);
        holder.getTextView().setText(value);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editText.setText(value);
                closeKeyboard();
            }
        });
        return view;
    }

    private void closeKeyboard() {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
    }

    static class ViewHolder {
        @BindView(R.id.text_view)
        TextView textView;

        public TextView getTextView() {
            return textView;
        }

        public ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }

    @Override
    public Filter getFilter() {
        return filter;
    }

    private class ItemFilter extends Filter {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {

            String filterString = constraint.toString().toLowerCase();
            FilterResults results = new FilterResults();

            final List<String> list = originalValues;

            int count = list.size();
            final ArrayList<String> newValuesList = new ArrayList<>(count);

            for (int i = 0; i < count; i++) {
                String filterableString = list.get(i);
                if (filterableString.toLowerCase().contains(filterString)) {
                    newValuesList.add(filterableString);
                }
            }

            results.values = newValuesList;
            results.count = newValuesList.size();

            return results;
        }

        @SuppressWarnings("unchecked")
        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            filteredValues = (ArrayList<String>) results.values;
            notifyDataSetChanged();
        }

    }

    public class SortIgnoreCase implements Comparator<Object> {
        public int compare(Object o1, Object o2) {
            String s1 = (String) o1;
            String s2 = (String) o2;
            return s1.toLowerCase().compareTo(s2.toLowerCase());
        }
    }
}

