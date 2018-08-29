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

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.jawg.osmcontributor.R;
import io.jawg.osmcontributor.model.entities.relation_display.RelationDisplay;
import io.jawg.osmcontributor.ui.adapters.parser.BusLineRelationDisplayParser;
import io.jawg.osmcontributor.utils.edition.RelationDisplayDto;

public class PoiBusLineAddingAdapter extends RecyclerView.Adapter<PoiBusLineAddingAdapter.BusLineHolder> {

    private static final int NB_MAX_RELATIONS_TO_DISPLAY = 3;
    private static final int DEFAULT_COLOR = R.color.active_text;
    private static final String TAG_COLOUR = "colour";

    private final Context context;
    private final BusLineRelationDisplayParser busLineParser;
    private final List<RelationDisplay> busLines;

    private AddBusLineListener addBusLineListener;

    public interface AddBusLineListener {
        void onBusLineClick(int position, RelationDisplay busLine);
    }

    public void setAddBusLineListener(AddBusLineListener addBusLineListener) {
        this.addBusLineListener = addBusLineListener;
    }

    public PoiBusLineAddingAdapter(Context context, BusLineRelationDisplayParser parser, List<RelationDisplay> busLines) {
        this.context = context;
        this.busLineParser = parser;
        this.busLines = busLines;
    }

    @NonNull
    @Override
    public BusLineHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View rootView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_bus_line, parent, false);
        return new BusLineHolder(rootView);
    }

    @Override
    public void onBindViewHolder(@NonNull final BusLineHolder holder, int position) {
        final RelationDisplay busLine = busLines.get(position);
        final String color = new RelationDisplayDto(busLine).getTagValue(TAG_COLOUR);
        String dest = busLineParser.getBusLineDestination(busLine);

        if (TextUtils.isEmpty(dest)) {
            dest = busLineParser.getBusLineName(busLine);
        }

        holder.tvBusLineNetwork.setText(busLineParser.getBusLineNetwork(busLine));
        holder.tvBusLineDestination.setText(dest);
        holder.tvBusLineRef.setText(busLineParser.getBusLineRef(busLine));
        holder.tvBusLineRef.setTypeface(holder.tvBusLineRef.getTypeface(), Typeface.BOLD);

        holder.tvBusLineRef.setTextColor(color == null ?
                ContextCompat.getColor(context, DEFAULT_COLOR) : Color.parseColor(color));

        holder.layoutBusLinePoi.setOnClickListener(view ->
                addBusLineListener.onBusLineClick(position, busLine));
    }

    @Override
    public int getItemCount() {
        return busLines.size() < NB_MAX_RELATIONS_TO_DISPLAY ?
                busLines.size() : NB_MAX_RELATIONS_TO_DISPLAY;
    }

    public static class BusLineHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.layout_item_bus_line)
        LinearLayout layoutBusLinePoi;

        @BindView(R.id.tv_bus_line_ref)
        TextView tvBusLineRef;

        @BindView(R.id.tv_bus_line_network)
        TextView tvBusLineNetwork;

        @BindView(R.id.tv_bus_line_destination)
        TextView tvBusLineDestination;

        @BindView(R.id.item_bus_line_delete_button)
        View deleteButton;

        BusLineHolder(View view) {
            super(view);
            ButterKnife.bind(this, itemView);

            deleteButton.setVisibility(View.GONE);
        }
    }
}
