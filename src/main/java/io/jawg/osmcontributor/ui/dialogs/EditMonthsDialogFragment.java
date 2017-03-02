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

import android.app.DialogFragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import butterknife.BindView;
import butterknife.BindViews;
import butterknife.ButterKnife;
import io.jawg.osmcontributor.R;
import io.jawg.osmcontributor.model.utils.OpeningMonth;
import io.jawg.osmcontributor.ui.utils.views.customs.TextViewCheck;

/**
 * @author Tommy Buonomo on 07/07/16.
 */
public class EditMonthsDialogFragment extends DialogFragment {
    private OpeningMonth openingMonth;
    private OnEditMonthsTagListener listener;

    @BindViews({R.id.dialog_edit_month_jan_check,
    R.id.dialog_edit_month_feb_check,
    R.id.dialog_edit_month_mar_check,
    R.id.dialog_edit_month_apr_check,
    R.id.dialog_edit_month_may_check,
    R.id.dialog_edit_month_jun_check,
    R.id.dialog_edit_month_jul_check,
    R.id.dialog_edit_month_aug_check,
    R.id.dialog_edit_month_sep_check,
    R.id.dialog_edit_month_oct_check,
    R.id.dialog_edit_month_nov_check,
    R.id.dialog_edit_month_dec_check})
    TextViewCheck[] textViewChecks;

    @BindView(R.id.dialog_edit_month_ok_button)
    Button okButton;

    @BindView(R.id.dialog_edit_month_cancel_button)
    Button cancelButton;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.dialog_edit_months_tag, container, false);
        ButterKnife.bind(this, rootView);
        setUpViews();
        return rootView;
    }

    private void setUpViews() {
        for (int i = 0; i < textViewChecks.length; i++) {
            final int finalI = i;
            textViewChecks[i].setChecked(openingMonth.getMonths()[i] != null);
            textViewChecks[i].setOnCheckListener(new TextViewCheck.OnCheckListener() {
                @Override
                public void onChecked(boolean checked) {
                    openingMonth.setMonthActivated(finalI, checked);
                }
            });
        }

        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (listener != null && openingMonth.isChanged()) {
                    listener.onOpeningMonthChanged(openingMonth);
                }
                dismiss();
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });
    }

    public void setOnEditMonthsListener(OnEditMonthsTagListener listener) {
        this.listener = listener;
    }

    public void setOpeningMonth(OpeningMonth openingMonth) {
        this.openingMonth = openingMonth;
    }

    public interface OnEditMonthsTagListener {
        void onOpeningMonthChanged(OpeningMonth openingMonth);
    }
}
