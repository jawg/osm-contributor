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
package io.mapsquare.osmcontributor.ui.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import org.greenrobot.eventbus.EventBus;
import io.mapsquare.osmcontributor.R;
import io.mapsquare.osmcontributor.ui.events.edition.NewPoiTagAddedEvent;
import io.mapsquare.osmcontributor.utils.StringUtils;

public class AddTagDialogFragment extends BaseOkCancelDialogFragment {

    private static final String TAG = "PoiTypeDialog";

    private EditText tagkey;
    private EditText tagValue;
    private EventBus eventBus;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public boolean onDialogCreated(Bundle savedInstanceState, AlertDialog dialog) {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_add_tag, null);
        dialog.setView(view);

        dialog.setTitle(R.string.add_tag);

        tagkey = (EditText) view.findViewById(R.id.tag_key);
        tagValue = (EditText) view.findViewById(R.id.tag_value);

        tagkey.addTextChangedListener(new TextWatcher() {
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

        eventBus = EventBus.getDefault();
        return false;
    }

    @Override
    protected void onOkClicked() {
        eventBus.post(new NewPoiTagAddedEvent(tagkey.getText().toString(), tagValue.getText().toString()));
    }

    public static void display(FragmentManager manager) {
        AddTagDialogFragment dialog = new AddTagDialogFragment();
        dialog.show(manager, TAG);
    }
}
