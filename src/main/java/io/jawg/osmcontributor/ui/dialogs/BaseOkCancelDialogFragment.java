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

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

import io.jawg.osmcontributor.R;

/**
 * Abstract {@link DialogFragment} that displays a dialog window with a positive and a negative button.
 */
public abstract class BaseOkCancelDialogFragment extends DialogFragment {

    private static final String BUNDLE_OK_ENABLED = "dialog:okEnabled";

    private boolean okEnabled;

    @NonNull
    @Override
    public final AlertDialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog dialog = new AlertDialog.Builder(getActivity(), getTheme())
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dismiss();
                    }
                })
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        onOkClicked();
                    }
                })
                .create();

        boolean isOkButtonInitiallyEnabled = onDialogCreated(savedInstanceState, dialog);

        if (savedInstanceState == null) {
            okEnabled = isOkButtonInitiallyEnabled;
        } else {
            okEnabled = savedInstanceState.getBoolean(BUNDLE_OK_ENABLED, isOkButtonInitiallyEnabled);
        }

        return dialog;
    }

    /**
     * Called by {@link #onCreateDialog(Bundle)} once the dialog is created.<br/>
     * Here you can set the title or use a custom view for the dialog.
     * <p>
     * The return result of this function allows you to set the initial enabled state of the "ok"
     * button.
     * </p>
     *
     * @param savedInstanceState The last saved instance state of the Fragment,
     *                           or null if this is a freshly created Fragment.
     * @param dialog             The freshly created alert dialog.
     * @return true if the "ok" button is enabled initially, false otherwise
     */
    protected abstract boolean onDialogCreated(Bundle savedInstanceState, AlertDialog dialog);

    @Override
    public final AlertDialog getDialog() {
        return (AlertDialog) super.getDialog();
    }

    @Override
    public void onStart() {
        super.onStart();
        setOkButtonEnabled(okEnabled);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        AlertDialog dialog = getDialog();
        if (dialog != null) {
            boolean enabled = dialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled();
            outState.putBoolean(BUNDLE_OK_ENABLED, enabled);
        }
    }

    /**
     * Called when the "ok" button is clicked.
     */
    protected void onOkClicked() {
    }

    /**
     * Change the enabled state of the "ok" button.
     *
     * @param enabled The new enabled state of the "ok" button
     */
    protected final void setOkButtonEnabled(boolean enabled) {
        AlertDialog dialog = getDialog();
        if (dialog != null) {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(enabled);
        }
    }
}
