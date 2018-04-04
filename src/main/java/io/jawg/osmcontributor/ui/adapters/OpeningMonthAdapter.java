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
package io.jawg.osmcontributor.ui.adapters;

import android.app.Activity;
import android.support.design.widget.TextInputLayout;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;

import org.greenrobot.eventbus.EventBus;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.jawg.osmcontributor.OsmTemplateApplication;
import io.jawg.osmcontributor.R;
import io.jawg.osmcontributor.model.utils.OpeningMonth;
import io.jawg.osmcontributor.model.utils.OpeningTime;
import io.jawg.osmcontributor.ui.adapters.parser.OpeningMonthValueParser;
import io.jawg.osmcontributor.ui.adapters.parser.OpeningTimeValueParser;
import io.jawg.osmcontributor.ui.dialogs.EditDaysDialogFragment;
import io.jawg.osmcontributor.ui.dialogs.EditMonthsDialogFragment;
import io.jawg.osmcontributor.ui.events.edition.PleaseApplyOpeningTimeChange;

/**
 * @author Tommy Buonomo on 11/07/16.
 */
public class OpeningMonthAdapter extends RecyclerView.Adapter<OpeningMonthAdapter.OpeningTimeHolder> {
    private OpeningTime openingTime;
    private Activity activity;
    private EventBus eventBus;
    private String time;
    private boolean hasToHide;

    @Inject
    OpeningMonthValueParser openingMonthValueParser;

    @Inject
    OpeningTimeValueParser openingTimeValueParser;

    public OpeningMonthAdapter(OpeningTime openingTime, Activity activity) {
        this.openingTime = openingTime;
        this.activity = activity;
        eventBus = EventBus.getDefault();
        ((OsmTemplateApplication) activity.getApplication()).getOsmTemplateComponent().inject(this);
    }

    @Override
    public OpeningTimeHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View viewRoot = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_opening_time, parent, false);
        return new OpeningTimeHolder(viewRoot);
    }

    @Override
    public void onBindViewHolder(final OpeningTimeHolder holder, int position) {
        final OpeningMonth openingMonth = openingTime.getOpeningMonths().get(position);

        holder.getTextInputLayout().setVisibility(hasToHide ? View.INVISIBLE : View.VISIBLE);

        String[] split = openingMonthValueParser.toValue(openingMonth).split(OpeningMonthValueParser.MONTH_SEP);
        if (split.length > 1) {
            holder.getTextViewMonthValue().setText(openingMonthValueParser.toValue(openingMonth)
                    .split(OpeningMonthValueParser.MONTH_SEP)[0]);
        }

        // When the months input text is clicked, we start the dialog to pick
        // the opening months
        holder.getTextViewMonthValue().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditMonthsDialogFragment fragment = new EditMonthsDialogFragment();
                fragment.setOpeningMonth(openingMonth);
                fragment.setOnEditMonthsListener(new EditMonthsDialogFragment.OnEditMonthsTagListener() {
                    @Override
                    public void onOpeningMonthChanged(OpeningMonth o) {
                        openingMonth.setMonths(o.getMonths());
                        String[] split = openingMonthValueParser.toValue(openingMonth).split(OpeningMonthValueParser.MONTH_SEP);
                        if (split.length > 1) {
                            holder.getTextViewMonthValue().setText(openingMonthValueParser.toValue(openingMonth)
                                    .split(OpeningMonthValueParser.MONTH_SEP)[0]);
                        }
                        eventBus.post(new PleaseApplyOpeningTimeChange(openingTime));
                    }
                });
                fragment.show(activity.getFragmentManager(), EditDaysDialogFragment.class.getSimpleName());
            }
        });

        holder.getDeleteButton().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openingTime.getOpeningMonths().remove(openingMonth);
                eventBus.post(new PleaseApplyOpeningTimeChange(openingTime));
                notifyDataSetChanged();
            }
        });


        holder.getOpeningHoursLayout().removeAllViews();

        new OpeningHoursLinearLayoutAdapter(openingTime,
                openingMonth.getOpeningHours(),
                holder.getOpeningHoursLayout(),
                activity, hasToHide);
    }

    @Override
    public int getItemCount() {
        return openingTime.getOpeningMonths().size();
    }


    public static class OpeningTimeHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.poi_month_value)
        EditText textViewMonthValue;

        @BindView(R.id.item_opening_time_hours_layout)
        LinearLayout openingHoursLayout;

        @BindView(R.id.item_opening_time_delete_button)
        View deleteButton;

        @BindView(R.id.poi_month)
        TextInputLayout textInputLayout;

        public OpeningTimeHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        public EditText getTextViewMonthValue() {
            return textViewMonthValue;
        }

        public View getDeleteButton() {
            return deleteButton;
        }

        public LinearLayout getOpeningHoursLayout() {
            return openingHoursLayout;
        }

        public TextInputLayout getTextInputLayout() {
            return textInputLayout;
        }
    }

    public void hideMonth(boolean hasToHide) {
        this.hasToHide = hasToHide;
    }

    public void setTime(String time) {
        this.time = time;
    }
}
