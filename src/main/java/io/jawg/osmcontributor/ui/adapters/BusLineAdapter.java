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
package io.jawg.osmcontributor.ui.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.jawg.osmcontributor.R;
import io.jawg.osmcontributor.model.entities.relation_display.RelationDisplay;
import io.jawg.osmcontributor.ui.adapters.parser.BusLineRelationDisplayParser;

public class BusLineAdapter extends RecyclerView.Adapter<BusLineAdapter.BusLineHolder> {
    private List<RelationDisplay> busLines;
    private RemoveBusListener removeBusListener;
    private BusLineRelationDisplayParser relationNameParser;

    public void setRemoveBusListener(RemoveBusListener removeBusListener) {
        this.removeBusListener = removeBusListener;
    }

    public interface RemoveBusListener {
        void onLineRemoved(RelationDisplay busLine);
    }

    public BusLineAdapter(List<RelationDisplay> busLines, BusLineRelationDisplayParser busLineRelationDisplayParser) {
        this.relationNameParser = busLineRelationDisplayParser;
        this.busLines = busLines;
    }

    @Override
    public BusLineHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View viewRoot = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_bus_line, parent, false);
        return new BusLineHolder(viewRoot);
    }

    @Override
    public void onBindViewHolder(final BusLineHolder holder, int position) {
        final RelationDisplay busLine = busLines.get(position);
        holder.getTextViewLineBus().setText(relationNameParser.getBusLineName(busLine));
        holder.getDeleteButton().setOnClickListener(view -> {
            busLines.remove(busLine);
            removeBusListener.onLineRemoved(busLine);
            notifyItemRemoved(position);
        });
    }

    @Override
    public int getItemCount() {
        return busLines.size();
    }


    public static class BusLineHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.poi_bus_line_value)
        TextView textViewMonthValue;

        @BindView(R.id.item_bus_line_delete_button)
        View deleteButton;

        public BusLineHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        public TextView getTextViewLineBus() {
            return textViewMonthValue;
        }

        public View getDeleteButton() {
            return deleteButton;
        }

    }

}
