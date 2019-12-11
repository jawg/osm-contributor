/**
 * Copyright (C) 2019 Takima
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
package io.jawg.osmcontributor.ui.dialogs;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;

import org.greenrobot.eventbus.EventBus;

import javax.inject.Inject;

import io.jawg.osmcontributor.OsmTemplateApplication;
import io.jawg.osmcontributor.R;
import io.jawg.osmcontributor.model.entities.PoiTypeTag;
import io.jawg.osmcontributor.ui.events.type.PleaseSavePoiTag;
import io.jawg.osmcontributor.utils.StringUtils;

public class EditPoiTagDialogFragment extends BaseOkCancelDialogFragment {

    private static final String TAG = "PoiTagDialog";
    private static final int NO_ID = -1;

    private static final String BUNDLE_TITLE = "dialog:title";
    private static final String BUNDLE_ID = "tag:id";
    private static final String BUNDLE_KEY = "tag:key";
    private static final String BUNDLE_VALUE = "tag:value";
    private static final String BUNDLE_MANDATORY = "tag:mandatory";

    @Inject
    EventBus bus;

    private EditText keyText;
    private EditText valueText;
    private CheckBox mandatoryBox;
    private long id;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        ((OsmTemplateApplication) activity.getApplication()).getOsmTemplateComponent().inject(this);
    }

    @Override
    public boolean onDialogCreated(Bundle savedInstanceState, AlertDialog dialog) {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_poi_tag, null);
        dialog.setView(view);

        keyText = (EditText) view.findViewById(R.id.poi_tag_key);
        valueText = (EditText) view.findViewById(R.id.poi_tag_value);
        mandatoryBox = (CheckBox) view.findViewById(R.id.poi_tag_mandatory);

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
        keyText.addTextChangedListener(watcher);
        valueText.addTextChangedListener(watcher);

        return isDataValid();
    }

    @Override
    protected void onOkClicked() {
        String key = keyText.getText().toString();
        if (StringUtils.isEmpty(key)) {
            key = null;
        }

        String value = valueText.getText().toString();
        if (StringUtils.isEmpty(value)) {
            value = null;
        }

        boolean mandatory = mandatoryBox.isChecked();

        if (id != NO_ID) {
            bus.post(PleaseSavePoiTag.editTag(id, key, value, mandatory));
        } else {
            bus.post(PleaseSavePoiTag.newTag(key, value, mandatory));
        }
    }

    private void populateData(Bundle savedInstanceState, AlertDialog dialog) {
        Bundle arguments = getArguments();
        if (arguments != null) {
            if (savedInstanceState == null) {
                keyText.setText(arguments.getString(BUNDLE_KEY));
                valueText.setText(arguments.getString(BUNDLE_VALUE));
                mandatoryBox.setChecked(arguments.getBoolean(BUNDLE_MANDATORY));
            }
            id = arguments.getLong(BUNDLE_ID, NO_ID);
            dialog.setTitle(arguments.getInt(BUNDLE_TITLE));
        } else {
            throw new IllegalStateException("null arguments");
        }
    }

    private boolean isDataValid() {
        return keyText.getText().length() > 0;
    }

    public static void display(FragmentManager manager) {
        display(manager, null);
    }

    public static void display(FragmentManager manager, PoiTypeTag poiTypeTag) {
        Bundle bundle = new Bundle();
        if (poiTypeTag != null) {
            bundle.putLong(BUNDLE_ID, poiTypeTag.getId());
            bundle.putString(BUNDLE_KEY, poiTypeTag.getKey());
            bundle.putString(BUNDLE_VALUE, poiTypeTag.getValue());
            bundle.putBoolean(BUNDLE_MANDATORY, poiTypeTag.getMandatory());
            bundle.putInt(BUNDLE_TITLE, R.string.edit_poi_tag);
        } else {
            bundle.putInt(BUNDLE_TITLE, R.string.new_poi_tag);
        }

        EditPoiTagDialogFragment dialog = new EditPoiTagDialogFragment();
        dialog.setArguments(bundle);
        dialog.show(manager, TAG);
    }
}
