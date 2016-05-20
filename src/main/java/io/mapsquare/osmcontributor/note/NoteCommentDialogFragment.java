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
package io.mapsquare.osmcontributor.note;


import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import javax.inject.Inject;

import org.greenrobot.eventbus.EventBus;
import io.mapsquare.osmcontributor.OsmTemplateApplication;
import io.mapsquare.osmcontributor.R;
import io.mapsquare.osmcontributor.core.model.Comment;
import io.mapsquare.osmcontributor.core.model.Note;
import io.mapsquare.osmcontributor.map.events.PleaseApplyNewComment;

public class NoteCommentDialogFragment extends DialogFragment {

    @Inject
    EventBus eventBus;
    private static final String LAT = "LAT";
    private static final String LNG = "LNG";

    private static double lat;
    private static double lng;

    // ANALYTICS ATTRIBUTES
    private Tracker tracker;

    public static NoteCommentDialogFragment newInstance(double lat, double lng) {
        NoteCommentDialogFragment dialog = new NoteCommentDialogFragment();
        Bundle args = new Bundle();
        args.putDouble(LAT, lat);
        args.putDouble(LNG, lng);
        dialog.setArguments(args);
        return dialog;
    }


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        ((OsmTemplateApplication) getActivity().getApplication()).getOsmTemplateComponent().inject(this);
        lat = getArguments().getDouble(LAT);
        lng = getArguments().getDouble(LNG);

        tracker = ((OsmTemplateApplication) getActivity().getApplication()).getTracker(OsmTemplateApplication.TrackerName.APP_TRACKER);
        tracker.setScreenName("NoteActivity");

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        final EditText input = new EditText(this.getActivity());
        input.setMaxLines(5);
        openKeyboard();

        String title = getResources().getString(R.string.title_creation_note);

        builder.setTitle(title)
                .setView(input)
                .setPositiveButton(R.string.create_note, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        String comment = input.getText().toString();
                        if (!comment.isEmpty()) {
                            Note note = new Note();
                            note.setStatus(Note.STATUS_SYNC);
                            note.setLatitude(lat);
                            note.setLongitude(lng);
                            note.setUpdated(true);
                            eventBus.post(new PleaseApplyNewComment(note, Comment.ACTION_OPEN, comment));
                            closeKeyboard();

                            tracker.send(new HitBuilders.EventBuilder()
                                    .setCategory("Note")
                                    .setAction("Create a note")
                                    .build());
                        }
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });


        final AlertDialog dialog = builder.create();
        dialog.show();

        // Initially disable the button
        dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                .setEnabled(false);

        input.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before,
                                      int count) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Check if edittext is empty
                if (TextUtils.isEmpty(s)) {
                    // Disable ok button
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
                } else {
                    // Something into edit text. Enable the button.
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                }

            }
        });

        return dialog;
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