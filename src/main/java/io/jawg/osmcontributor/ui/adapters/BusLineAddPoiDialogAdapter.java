package io.jawg.osmcontributor.ui.adapters;

import android.support.annotation.NonNull;
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
import io.jawg.osmcontributor.ui.dialogs.AddBusLinePoiDialogFragment;

public class BusLineAddPoiDialogAdapter extends RecyclerView.Adapter<BusLineAddPoiDialogAdapter.BusLineHolder> {

    private BusLineClickListener busLineClickListener;

    private final AddBusLinePoiDialogFragment fragment;
    private final List<RelationDisplay> busLines;
    private final BusLineRelationDisplayParser busLineParser;

    public interface BusLineClickListener {
        void onCheckBusLine(View itemView, RelationDisplay busLine);
    }

    public void setOnBusLineClickListener(BusLineClickListener busLineClickListener) {
        this.busLineClickListener = busLineClickListener;
    }

    public BusLineAddPoiDialogAdapter(AddBusLinePoiDialogFragment fragment, List<RelationDisplay> busLines, BusLineRelationDisplayParser busLineParser) {
        this.fragment = fragment;
        this.busLines = busLines;
        this.busLineParser = busLineParser;
    }

    @NonNull
    @Override
    public BusLineHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View rootView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_bus_line_poi, parent, false);
        return new BusLineHolder(rootView);
    }

    @Override
    public void onBindViewHolder(@NonNull final BusLineAddPoiDialogAdapter.BusLineHolder holder, int position) {
        holder.bind(fragment, busLines, busLines.get(position), busLineParser, busLineClickListener);
    }

    @Override
    public int getItemCount() {
        return busLines.size();
    }

    public static class BusLineHolder extends RecyclerView.ViewHolder {

        private static final int activeTextColor = R.color.active_text;
        private static final int holoBlueDarkColor = 0xff0099cc; // Color = android:color/holo_blue_dark

        @BindView(R.id.tv_bus_line_poi)
        TextView textViewBusLine;

        BusLineHolder(View view) {
            super(view);
            ButterKnife.bind(this, itemView);
        }

        public void bind(AddBusLinePoiDialogFragment fragment, List<RelationDisplay> busLines, RelationDisplay busLine,
                         BusLineRelationDisplayParser busLineParser, BusLineClickListener busLineClickListener) {

            textViewBusLine.setText(busLineParser.getBusLineName(busLine));

            if (busLines.size() == 1) {
                textViewBusLine.setTextColor(holoBlueDarkColor);
            }

            itemView.setOnClickListener(view -> {
                View checkedItem = fragment.getCheckedItem();

                if (itemView != checkedItem) {
                    if (checkedItem != null) {
                        TextView textViewCheckedItem = checkedItem.findViewById(R.id.tv_bus_line_poi);
                        textViewCheckedItem.setTextColor(itemView.getResources().getColor(activeTextColor));
                    }
                    textViewBusLine.setTextColor(holoBlueDarkColor);
                    busLineClickListener.onCheckBusLine(itemView, busLine);
                }
            });
        }
    }
}
