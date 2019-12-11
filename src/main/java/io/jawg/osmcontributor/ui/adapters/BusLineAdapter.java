/**
 * Copyright (C) 2019 Takima
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
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.jawg.osmcontributor.R;
import io.jawg.osmcontributor.model.entities.relation_display.RelationDisplay;
import io.jawg.osmcontributor.ui.adapters.parser.BusLineRelationDisplayParser;
import io.jawg.osmcontributor.utils.edition.RelationDisplayDto;

/**
 * Adapter used to display the list of bus lines in the cardview when creating/editing a POI
 */
public class BusLineAdapter extends RecyclerView.Adapter<BusLineAdapter.BusLineHolder> {

    private static final int DEFAULT_COLOR = R.color.active_text;
    private static final String TAG_COLOUR = "colour";

    private Context context;
    private List<RelationDisplay> busLines;
    private RemoveBusListener removeBusListener;
    private BusLineRelationDisplayParser relationNameParser;

    public void setRemoveBusListener(RemoveBusListener removeBusListener) {
        this.removeBusListener = removeBusListener;
    }

    public interface RemoveBusListener {
        void onBusLineClick(RelationDisplay busLine, int position);
    }

    public BusLineAdapter(Context context, BusLineRelationDisplayParser parser) {
        this.context = context;
        this.relationNameParser = parser;
        this.busLines = new ArrayList<>();
    }

    public RelationDisplay getItem(int position) {
        return busLines.get(position);
    }

    public List<RelationDisplay> getItems() {
        return busLines;
    }

    public void setItems(List<RelationDisplay> busLines) {
        if (busLines != null) {
            this.busLines.clear();
            this.busLines.addAll(busLines);
            notifyDataSetChanged();
        }
    }

    public void addItem(int position, RelationDisplay busLine) {
        busLines.add(position, busLine);
        notifyItemInserted(position);
    }

    public void removeItem(int position) {
        busLines.remove(position);
        notifyItemRemoved(position);
    }

    @NonNull
    @Override
    public BusLineHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View viewRoot = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_bus_line, parent, false);
        return new BusLineHolder(viewRoot);
    }

    @Override
    public void onBindViewHolder(@NonNull final BusLineHolder holder, int position) {
        final RelationDisplay busLine = busLines.get(position);
        String color = new RelationDisplayDto(busLine).getTagValue(TAG_COLOUR);

        String dest = relationNameParser.getBusLineDestination(busLine);

        if (TextUtils.isEmpty(dest)) {
            dest = relationNameParser.getBusLineName(busLine);
        }

        holder.tvBusLineRef.setText(relationNameParser.getBusLineRef(busLine));
        holder.tvBusLineNetwork.setText(relationNameParser.getBusLineNetwork(busLine));
        holder.tvBusLineDestination.setText(dest);
        holder.tvBusLineRef.setTypeface(holder.tvBusLineRef.getTypeface(), Typeface.BOLD);

        holder.tvBusLineRef.setTextColor(color == null ?
                ContextCompat.getColor(context, DEFAULT_COLOR) : Color.parseColor(color));

        holder.deleteButton.setOnClickListener(view ->
                removeBusListener.onBusLineClick(busLine, position));
    }

    @Override
    public int getItemCount() {
        return busLines.size();
    }

    public static class BusLineHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.tv_bus_line_ref)
        TextView tvBusLineRef;

        @BindView(R.id.tv_bus_line_network)
        TextView tvBusLineNetwork;

        @BindView(R.id.tv_bus_line_destination)
        TextView tvBusLineDestination;

        @BindView(R.id.item_bus_line_delete_button)
        View deleteButton;

        BusLineHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
