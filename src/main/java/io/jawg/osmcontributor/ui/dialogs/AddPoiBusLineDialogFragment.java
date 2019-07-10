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
package io.jawg.osmcontributor.ui.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AutoCompleteTextView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnTextChanged;
import io.jawg.osmcontributor.OsmTemplateApplication;
import io.jawg.osmcontributor.R;
import io.jawg.osmcontributor.model.entities.relation_display.RelationDisplay;
import io.jawg.osmcontributor.model.events.BusLinesSuggestionForPoiLoadedEvent;
import io.jawg.osmcontributor.ui.adapters.BusLineSuggestionAdapter;
import io.jawg.osmcontributor.ui.adapters.PoiBusLineAddingAdapter;
import io.jawg.osmcontributor.ui.adapters.parser.BusLineRelationDisplayParser;
import io.jawg.osmcontributor.ui.events.type.PleaseLoadBusLinesSuggestionForPoiEvent;
import io.jawg.osmcontributor.ui.managers.RelationManager;
import io.jawg.osmcontributor.ui.utils.views.DividerItemDecoration;
import io.jawg.osmcontributor.utils.edition.RelationDisplayUtils;

public class AddPoiBusLineDialogFragment extends DialogFragment {

    @Inject
    EventBus eventBus;

    @Inject
    RelationManager relationManager;

    @Inject
    BusLineRelationDisplayParser busLineParser;

    @BindView(R.id.add_bus_line_poi_list_view)
    RecyclerView recyclerView;

    @BindView(R.id.bus_line_input_edit_text)
    AutoCompleteTextView searchBusLineTextView;

    @BindView(R.id.clear_suggestion_button)
    View clearButton;

    @OnTextChanged(R.id.bus_line_input_edit_text)
    protected void onSearchBusLineChanged(CharSequence constraint, int start, int before, int count) {
        String search = constraint.toString().trim();
        if (!TextUtils.isEmpty(search)) {
            eventBus.post(new PleaseLoadBusLinesSuggestionForPoiEvent(search));
        }
    }

    private static final String TAG_REF = "ref";

    private List<RelationDisplay> currentBusLines;
    private List<RelationDisplay> busLinesNearby;
    private List<RelationDisplay> suggestionList;
    private AddBusLineListener addBusLineListener;
    private BusLineSuggestionAdapter suggestionAdapter;

    public interface AddBusLineListener {
        void onBusLineClick(RelationDisplay busLine);
    }

    public void setAddBusLineListener(AddBusLineListener addBusLineListener) {
        this.addBusLineListener = addBusLineListener;
    }

    public static AddPoiBusLineDialogFragment
    newInstance(List<RelationDisplay> currentBusLines, List<RelationDisplay> busLinesNearby) {
        AddPoiBusLineDialogFragment dialog = new AddPoiBusLineDialogFragment();
        dialog.init(currentBusLines, busLinesNearby);
        return dialog;
    }

    private void init(List<RelationDisplay> currentBusLines, List<RelationDisplay> busLinesNearby) {
        this.currentBusLines = currentBusLines;
        this.busLinesNearby = busLinesNearby;
        this.suggestionList = new ArrayList<>();
    }

    private void addBusLine(RelationDisplay busLine) {
        addBusLineListener.onBusLineClick(busLine);
        dismiss();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        EventBus.getDefault().register(this);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View rootView = LayoutInflater.from(getActivity().getBaseContext())
                .inflate(R.layout.dialog_add_bus_line_poi, null);
        ButterKnife.bind(this, rootView);

        ((OsmTemplateApplication) getActivity().getApplication()).getOsmTemplateComponent().inject(this);

        AlertDialog alertDialog = new AlertDialog.Builder(getActivity(), getTheme())
                .setNegativeButton(R.string.cancel, (dialog, which) -> dismiss())
                .create();

        alertDialog.setView(rootView);
        alertDialog.setTitle(R.string.adding_poi_bus_line_title);

        clearButton.setOnClickListener(view -> searchBusLineTextView.setText(""));

        suggestionAdapter = new BusLineSuggestionAdapter(getActivity().getBaseContext(), busLineParser);
        searchBusLineTextView.setAdapter(suggestionAdapter);
        searchBusLineTextView.setOnItemClickListener((parent, view, position, id) -> addBusLine(suggestionAdapter.getItem(position)));

        PoiBusLineAddingAdapter adapter = new PoiBusLineAddingAdapter(getActivity().getBaseContext(), busLinesNearby, busLineParser);
        adapter.setAddBusLineListener((position, busLine) -> addBusLine(busLine));

        recyclerView.setLayoutManager(new LinearLayoutManager(this.getActivity()));
        recyclerView.setAdapter(adapter);
        recyclerView.addItemDecoration(new DividerItemDecoration(recyclerView.getContext(), R.drawable.bus_line_divider));

        return alertDialog;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onBusLinesSuggestionForPoiLoadedEvent(BusLinesSuggestionForPoiLoadedEvent event) {
        for (RelationDisplay relationDisplay : event.getRelationDisplays()) {
            if (!RelationDisplayUtils.isBusLineOrTagEqual(currentBusLines, relationDisplay, TAG_REF)) {
                suggestionList.add(relationDisplay);
            }
        }
        suggestionAdapter.setItems(suggestionList);
    }
}
