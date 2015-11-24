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
package io.mapsquare.osmcontributor.type;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.TextView;

import org.joda.time.DateTime;

import java.util.ArrayList;

import javax.inject.Inject;

import de.greenrobot.event.EventBus;
import io.mapsquare.osmcontributor.OsmTemplateApplication;
import io.mapsquare.osmcontributor.R;
import io.mapsquare.osmcontributor.core.model.PoiType;
import io.mapsquare.osmcontributor.core.model.PoiTypeTag;
import io.mapsquare.osmcontributor.type.adapter.SuggestionAdapter;
import io.mapsquare.osmcontributor.type.dto.SuggestionsData;
import io.mapsquare.osmcontributor.type.event.PleaseDownloadPoiTypeSuggestionEvent;
import io.mapsquare.osmcontributor.type.event.PoiTypeSuggestedDownloadedEvent;
import io.mapsquare.osmcontributor.utils.StringUtils;

public class EditPoiTypeDialogFragment extends BaseOkCancelDialogFragment {

    private static final String TAG = "PoiTypeDialog";

    @Inject
    EventBus bus;

    @Inject
    TypeManager typeManager;

    private DelayedAutoCompleteTextView modelText;
    private SuggestionAdapter adapter;
    private TextView detailsText;

    private PoiType poiType;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        ((OsmTemplateApplication) activity.getApplication()).getOsmTemplateComponent().inject(this);
        bus.register(this);
    }

    @Override
    public void onDetach() {
        bus.unregister(this);
        super.onDetach();
    }

    @Override
    public boolean onDialogCreated(Bundle savedInstanceState, AlertDialog dialog) {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_poi_type, null);
        dialog.setView(view);

        modelText = (DelayedAutoCompleteTextView) view.findViewById(R.id.poi_type_model);
        detailsText = (TextView) view.findViewById(R.id.poi_type_details);

        adapter = new SuggestionAdapter(getContext(), typeManager);
        modelText.initialize(bus);
        modelText.setAdapter(adapter);
        modelText.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                SuggestionsData data = adapter.getItem(position);
                bus.post(new PleaseDownloadPoiTypeSuggestionEvent(data.getKey()));
            }
        });
        modelText.setDropDownHeight((int) (150 * getContext().getResources().getDisplayMetrics().density));
        modelText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                setOkButtonEnabled(!StringUtils.isEmpty(s.toString().trim()));
            }
        });
        dialog.setTitle(R.string.new_poi_type);

        return false;
    }

    @Override
    protected void onOkClicked() {
        if (poiType == null) {
            poiType = new PoiType();
            poiType.setName(modelText.getText().toString());
            poiType.setTechnicalName(poiType.getName());
            poiType.setLastUse(DateTime.now());
            poiType.setTags(new ArrayList<PoiTypeTag>());
        }
        typeManager.savePoiType(poiType);

    }

    public void onEventMainThread(PoiTypeSuggestedDownloadedEvent event) {
        poiType = event.getPoiType();
        detailsText.setVisibility(View.VISIBLE);
        detailsText.setText(getContext().getString(R.string.tag_number, poiType.getTags().size()));
    }

    public static void display(FragmentManager manager) {
        EditPoiTypeDialogFragment dialog = new EditPoiTypeDialogFragment();
        dialog.show(manager, TAG);
    }
}
