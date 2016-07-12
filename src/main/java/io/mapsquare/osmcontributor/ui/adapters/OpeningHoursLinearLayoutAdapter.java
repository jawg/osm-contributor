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
package io.mapsquare.osmcontributor.ui.adapters;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;

import org.greenrobot.eventbus.EventBus;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import io.mapsquare.osmcontributor.R;
import io.mapsquare.osmcontributor.model.utils.OpeningHours;
import io.mapsquare.osmcontributor.model.utils.OpeningTime;
import io.mapsquare.osmcontributor.ui.adapters.parser.OpeningTimeParser;
import io.mapsquare.osmcontributor.ui.dialogs.EditDaysDialogFragment;
import io.mapsquare.osmcontributor.ui.events.edition.PleaseApplyOpeningTimeChange;

/**
 * This class allows listing opening hours items without use a recycler view or a list view
 *
 * @author Tommy Buonomo on 11/07/16.
 */
public class OpeningHoursLinearLayoutAdapter {
    private static final String TAG = "OpeningHoursAdapter";

    private final OpeningTimeParser openingTimeParser;
    private final EventBus eventBus;
    private Activity activity;
    private OpeningTime openingTime;
    private CopyOnWriteArrayList<OpeningHours> openingHoursList;
    private LinearLayout linearLayout;

    public OpeningHoursLinearLayoutAdapter(OpeningTime openingTime,
                                           List<OpeningHours> openingHoursList,
                                           OpeningTimeParser openingTimeParser,
                                           LinearLayout openingHoursLayout,
                                           Activity activity) {
        this.openingTime = openingTime;
        this.openingTimeParser = openingTimeParser;
        this.activity = activity;
        this.openingHoursList = new CopyOnWriteArrayList<>();
        this.openingHoursList.addAll(openingHoursList);
        this.linearLayout = openingHoursLayout;
        eventBus = EventBus.getDefault();
        initEditOpeningHours(openingHoursList);
        initOpeningHoursList();
    }

    private void initOpeningHoursList() {
        for (OpeningHours openingHours : openingHoursList) {
            addOpeningHours(openingHours);
        }
    }

    private void initEditOpeningHours(final List<OpeningHours> originalHoursList) {
        View openingHoursItem = LayoutInflater.from(activity)
                .inflate(R.layout.item_opening_hours, linearLayout, false);

        final EditText textViewDaysValue = (EditText) openingHoursItem.findViewById(R.id.opening_hours_days_value);
        final EditText textViewHoursValue = (EditText) openingHoursItem.findViewById(R.id.opening_hours_hours_value);

        textViewDaysValue.setText("");
        textViewHoursValue.setText("");

        // When the days input text is clicked, we start the dialog to pick
        // the opening days and hours
        View.OnClickListener onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditDaysDialogFragment fragment = new EditDaysDialogFragment();
                fragment.setOnEditDaysListener(new EditDaysDialogFragment.OnEditDaysListener() {
                    @Override
                    public void onOpeningTimeChanged(OpeningHours openingHours) {
                        addOpeningHours(openingHours);
                        originalHoursList.add(openingHours);
                        eventBus.post(new PleaseApplyOpeningTimeChange(openingTime));
                    }
                });
                fragment.show(activity.getFragmentManager(), EditDaysDialogFragment.class.getSimpleName());
            }
        };

        textViewDaysValue.setOnClickListener(onClickListener);
        textViewHoursValue.setOnClickListener(onClickListener);
        if (this.openingHoursList.size() == linearLayout.getChildCount()) {
            linearLayout.addView(openingHoursItem);
        }
    }

    public void addOpeningHours(final OpeningHours openingHours) {
        View openingHoursItem = LayoutInflater.from(activity)
                .inflate(R.layout.item_opening_hours, linearLayout, false);

        openingHoursList.add(openingHours);

        final EditText textViewDaysValue = (EditText) openingHoursItem.findViewById(R.id.opening_hours_days_value);
        final EditText textViewHoursValue = (EditText) openingHoursItem.findViewById(R.id.opening_hours_hours_value);

        String[] openingHoursPart = openingTimeParser.buildHoursPart(openingHours).toString().split(" ");

        if (openingHoursPart.length > 1) {
            textViewDaysValue.setText(openingHoursPart[0]);
            textViewHoursValue.setText(openingHoursPart[1]);
        } else {
            textViewDaysValue.setText("");
            textViewHoursValue.setText("");
        }

        // When the days input text is clicked, we start the dialog to pick
        // the opening days and hours
        View.OnClickListener onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditDaysDialogFragment fragment = new EditDaysDialogFragment();
                fragment.setOpeningHours(openingHours);
                fragment.setOnEditDaysListener(new EditDaysDialogFragment.OnEditDaysListener() {
                    @Override
                    public void onOpeningTimeChanged(OpeningHours openingHours) {
                        eventBus.post(new PleaseApplyOpeningTimeChange(openingTime));

                        String[] openingHoursPart = openingTimeParser.buildHoursPart(openingHours).toString().split(" ");
                        if (openingHoursPart.length > 1) {
                            textViewDaysValue.setText(openingHoursPart[0]);
                            textViewHoursValue.setText(openingHoursPart[1]);
                        }
                    }
                });
                fragment.show(activity.getFragmentManager(), EditDaysDialogFragment.class.getSimpleName());
            }
        };

        textViewDaysValue.setOnClickListener(onClickListener);
        textViewHoursValue.setOnClickListener(onClickListener);

        linearLayout.addView(openingHoursItem, linearLayout.getChildCount() - 1);
    }
}
