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
package io.jawg.osmcontributor.ui.dialogs;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import org.joda.time.DateTime;

import javax.inject.Inject;

import io.jawg.osmcontributor.OsmTemplateApplication;
import io.jawg.osmcontributor.R;
import io.jawg.osmcontributor.model.entities.PoiType;
import io.jawg.osmcontributor.ui.managers.TypeManager;
import io.jawg.osmcontributor.utils.StringUtils;

public class EditPoiTypeNameDialogFragment extends BaseOkCancelDialogFragment {

    private static final String TAG = "PoiTypeDialog";

    @Inject
    TypeManager typeManager;

    private EditText poiTypeName;
    private PoiType poiType;

    public PoiType getPoiType() {
        return poiType;
    }

    public void setPoiType(PoiType poiType) {
        this.poiType = poiType;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        ((OsmTemplateApplication) activity.getApplication()).getOsmTemplateComponent().inject(this);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public boolean onDialogCreated(Bundle savedInstanceState, AlertDialog dialog) {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_poi_type_name, null);
        dialog.setView(view);

        dialog.setTitle(R.string.edit_poi_name);

        poiTypeName = (EditText) view.findViewById(R.id.poi_type_name);
        poiTypeName.setText(poiType.getName());
        poiTypeName.addTextChangedListener(new TextWatcher() {
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

        return false;
    }

    @Override
    protected void onOkClicked() {
        if (poiType != null) {
            poiType.setName(poiTypeName.getText().toString());
            poiType.setLastUse(DateTime.now());
            typeManager.savePoiType(poiType);
        }
    }

    public static void display(FragmentManager manager, PoiType poiType) {
        EditPoiTypeNameDialogFragment dialog = new EditPoiTypeNameDialogFragment();
        dialog.setPoiType(poiType);
        dialog.show(manager, TAG);
    }
}
