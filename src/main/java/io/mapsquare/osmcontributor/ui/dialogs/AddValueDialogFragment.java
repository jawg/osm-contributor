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
package io.mapsquare.osmcontributor.ui.dialogs;


import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import javax.inject.Inject;

import org.greenrobot.eventbus.EventBus;
import io.mapsquare.osmcontributor.OsmTemplateApplication;
import io.mapsquare.osmcontributor.R;
import io.mapsquare.osmcontributor.ui.events.edition.PleaseApplyTagChangeView;

public class AddValueDialogFragment extends DialogFragment {
    private String key;
    private String value;
    private View cardView;
    private static final String KEY = "KEY";
    private static final String VALUE = "VALUE";

    @Inject
    EventBus eventBus;

    public static AddValueDialogFragment newInstance(String key, String value, View cardView) {
        AddValueDialogFragment dialog = new AddValueDialogFragment();
        Bundle args = new Bundle();
        args.putString(KEY, key);
        args.putString(VALUE, value);
        dialog.setArguments(args);
        dialog.cardView = cardView;
        return dialog;
    }


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        ((OsmTemplateApplication) getActivity().getApplication()).getOsmTemplateComponent().inject(this);


        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        key = getArguments().getString(KEY);
        value = getArguments().getString(VALUE);
        final EditText input = new EditText(this.getActivity());
        input.setText(value);
        input.setSelectAllOnFocus(true);

        openKeyboard();

        String title = String.format(getResources().getString(R.string.addValueDialogTitle), key);

        builder.setTitle(title)
                .setView(input)
                .setPositiveButton(R.string.addValueDialogAdd, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        eventBus.postSticky(new PleaseApplyTagChangeView(key, input.getText().toString()));
                        cardView.performClick();
                        closeKeyboard();
                    }
                })
                .setNegativeButton(R.string.addValueDialogCancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        // Create the AlertDialog object and return it
        return builder.create();
    }

    private void openKeyboard() {
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.RESULT_SHOWN, 0);
    }

    private void closeKeyboard() {
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.RESULT_UNCHANGED_HIDDEN, 0);
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        closeKeyboard();
        super.onCancel(dialog);
    }
}