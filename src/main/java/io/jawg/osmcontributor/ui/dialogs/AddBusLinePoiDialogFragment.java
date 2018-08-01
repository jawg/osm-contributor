package io.jawg.osmcontributor.ui.dialogs;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AutoCompleteTextView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.jawg.osmcontributor.R;
import io.jawg.osmcontributor.model.entities.Poi;
import io.jawg.osmcontributor.model.entities.relation_display.RelationDisplay;
import io.jawg.osmcontributor.ui.adapters.BusLineAddPoiDialogAdapter;
import io.jawg.osmcontributor.ui.adapters.BusLineSuggestionAdapter;
import io.jawg.osmcontributor.ui.adapters.parser.BusLineRelationDisplayParser;
import io.jawg.osmcontributor.ui.managers.RelationManager;

public class AddBusLinePoiDialogFragment extends BaseOkCancelDialogFragment {

    private static final String TAG = AddBusLinePoiDialogFragment.class.getName();
    private static final int NB_RELATIONS_TO_DISPLAY = 3;

    private Poi poi;
    private View checkedItem;
    private RelationManager relationManager;
    private RelationDisplay checkedBusLine;
    private List<RelationDisplay> busLines;
    private List<RelationDisplay> nearbyBusLines;
    private List<RelationDisplay> suggestionBusLines;
    private BusLineRelationDisplayParser parser;
    private AddBusLineListener addBusLineListener;

    @BindView(R.id.add_bus_line_poi_list_view)
    RecyclerView recyclerView;

    @BindView(R.id.bus_line_input_edit_text)
    AutoCompleteTextView searchBusLineTextView;

    public interface AddBusLineListener {
        void onAddBusLine(RelationDisplay busLine);
    }

    @Override
    public boolean onDialogCreated(Bundle savedInstanceState, AlertDialog dialog) {
        View rootView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_add_bus_line_poi, null);
        dialog.setView(rootView);
        dialog.setTitle(R.string.tag_poi_rout_ref);
        ButterKnife.bind(this, rootView);

        TextWatcher tw = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void afterTextChanged(Editable editable) {
                if (editable.toString().trim().length() == 0) {
                    setEnabledOkButton(false);
                    displayList(nearbyBusLines);
                }
            }
        };
        searchBusLineTextView.addTextChangedListener(tw);

        suggestionBusLines = new ArrayList<>();
        BusLineSuggestionAdapter suggestionAdapter = new BusLineSuggestionAdapter(getContext(), relationManager, parser, busLines);
        searchBusLineTextView.setAdapter(suggestionAdapter);
        searchBusLineTextView.setOnItemClickListener((parent, view, position, id) -> {
            setEnabledOkButton(false);

            suggestionBusLines.clear();
            suggestionBusLines.add(suggestionAdapter.getItem(position));

            if (suggestionBusLines != null && suggestionBusLines.size() > 0) {
                displayList(suggestionBusLines);
            }
        });

        displayList(nearbyBusLines);

        return false;
    }

    public void displayList(List<RelationDisplay> busLines) {
        if (poi.getId() != null && busLines != null) {
            BusLineAddPoiDialogAdapter adapter = new BusLineAddPoiDialogAdapter(
                    this,
                    busLines.subList(0, nbRelationsToDisplay(busLines)),
                    parser
            );

            adapter.setOnBusLineClickListener((itemView, busLine) -> {
                setCheckedItem(itemView);
                setCheckedBusLine(busLines.get(0));
                setEnabledOkButton(true);
            });
            recyclerView.setLayoutManager(new LinearLayoutManager(this.getActivity()));
            recyclerView.setAdapter(adapter);

            if (busLines.size() == 1) {
                setCheckedBusLine(busLines.get(0));
                setEnabledOkButton(true);
            }
        }
    }

    /**
     * Called when the "ok" button is clicked.
     */
    @Override
    protected void onOkClicked() {
        addBusLineListener.onAddBusLine(checkedBusLine);
    }

    private int nbRelationsToDisplay(List<RelationDisplay> list) {
        return list.size() < NB_RELATIONS_TO_DISPLAY ?
                list.size() : NB_RELATIONS_TO_DISPLAY;
    }

    public final void setEnabledOkButton(boolean enabled) {
        setOkButtonEnabled(enabled);
    }

    private void setRelationManager(RelationManager relationManager) {
        this.relationManager = relationManager;
    }

    private void setParser(BusLineRelationDisplayParser parser) {
        this.parser = parser;
    }

    private void setAddBusLineListener(AddBusLineListener listener) {
        this.addBusLineListener = listener;
    }

    private void setPoi(Poi poi) {
        this.poi = poi;
    }

    private void setBusLines(List<RelationDisplay> busLines) {
        this.busLines = busLines;
    }

    private void setNearbyBusLines(List<RelationDisplay> nearbyBusLines) {
        this.nearbyBusLines = nearbyBusLines;
    }

    public void setCheckedBusLine(RelationDisplay relationDisplay) {
        this.checkedBusLine = relationDisplay;
    }

    public void setCheckedItem(View itemView) {
        this.checkedItem = itemView;
    }

    public View getCheckedItem() {
        return this.checkedItem;
    }

    public static void display(FragmentManager manager, RelationManager relationManager, BusLineRelationDisplayParser parser,
                               Poi poi, List<RelationDisplay> busLines, List<RelationDisplay> nearbyBusLines,
                               AddBusLineListener addBusLineListener) {

        AddBusLinePoiDialogFragment dialog = new AddBusLinePoiDialogFragment();

        dialog.setRelationManager(relationManager);
        dialog.setParser(parser);
        dialog.setPoi(poi);
        dialog.setBusLines(busLines);
        dialog.setNearbyBusLines(nearbyBusLines);
        dialog.setAddBusLineListener(addBusLineListener);

        dialog.show(manager, TAG);
    }
}
