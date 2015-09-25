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
import android.widget.EditText;

import javax.inject.Inject;

import de.greenrobot.event.EventBus;
import io.mapsquare.osmcontributor.OsmTemplateApplication;
import io.mapsquare.osmcontributor.R;
import io.mapsquare.osmcontributor.core.model.PoiType;
import io.mapsquare.osmcontributor.type.event.PleaseSavePoiType;

public class EditPoiTypeDialogFragment extends BaseOkCancelDialogFragment {

    private static final String TAG = "PoiTypeDialog";
    private static final int NO_ID = -1;

    private static final String BUNDLE_TITLE = "dialog:title";
    private static final String BUNDLE_ID = "tag:id";
    private static final String BUNDLE_NAME = "tag:name";

    @Inject
    EventBus bus;

    private EditText nameText;
    private long id;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        ((OsmTemplateApplication) activity.getApplication()).getOsmTemplateComponent().inject(this);
    }

    @Override
    public boolean onDialogCreated(Bundle savedInstanceState, AlertDialog dialog) {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_poi_type, null);
        dialog.setView(view);

        nameText = (EditText) view.findViewById(R.id.poi_type_name);

        populateData(savedInstanceState, dialog);

        TextWatcher watcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                setOkButtonEnabled(isDataValid());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        };
        nameText.addTextChangedListener(watcher);

        return isDataValid();
    }

    @Override
    protected void onOkClicked() {
        String name = nameText.getText().toString();

        if (id != NO_ID) {
            bus.post(PleaseSavePoiType.editType(id, name));
        } else {
            bus.post(PleaseSavePoiType.newType(name));
        }
    }

    private void populateData(Bundle savedInstanceState, AlertDialog dialog) {
        Bundle arguments = getArguments();
        if (arguments != null) {
            if (savedInstanceState == null) {
                nameText.setText(arguments.getString(BUNDLE_NAME));
            }
            id = arguments.getLong(BUNDLE_ID, NO_ID);
            dialog.setTitle(arguments.getInt(BUNDLE_TITLE));
        } else {
            throw new IllegalStateException("null arguments");
        }
    }

    private boolean isDataValid() {
        return nameText.getText().length() > 0;
    }

    public static void display(FragmentManager manager) {
        display(manager, null);
    }

    public static void display(FragmentManager manager, PoiType poiType) {
        Bundle bundle = new Bundle();
        if (poiType != null) {
            bundle.putLong(BUNDLE_ID, poiType.getId());
            bundle.putString(BUNDLE_NAME, poiType.getName());
            bundle.putInt(BUNDLE_TITLE, R.string.edit_poi_type);
        } else {
            bundle.putInt(BUNDLE_TITLE, R.string.new_poi_type);
        }

        EditPoiTypeDialogFragment dialog = new EditPoiTypeDialogFragment();
        dialog.setArguments(bundle);
        dialog.show(manager, TAG);
    }
}
