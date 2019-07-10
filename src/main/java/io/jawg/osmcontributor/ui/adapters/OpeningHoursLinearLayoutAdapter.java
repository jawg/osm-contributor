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
package io.jawg.osmcontributor.ui.adapters;

import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;

import org.greenrobot.eventbus.EventBus;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.inject.Inject;

import io.jawg.osmcontributor.OsmTemplateApplication;
import io.jawg.osmcontributor.R;
import io.jawg.osmcontributor.ui.adapters.item.opening.OpeningHours;
import io.jawg.osmcontributor.ui.adapters.item.opening.OpeningTime;
import io.jawg.osmcontributor.ui.adapters.parser.OpeningHoursValueParser;
import io.jawg.osmcontributor.ui.adapters.parser.OpeningTimeValueParser;
import io.jawg.osmcontributor.ui.dialogs.EditDaysDialogFragment;
import io.jawg.osmcontributor.ui.events.edition.PleaseApplyOpeningTimeChange;

/**
 * This class allows listing opening hours items without use a recycler view or a list view
 *
 * @author Tommy Buonomo on 11/07/16.
 */
public class OpeningHoursLinearLayoutAdapter {
    private static final String TAG = "OpeningHoursAdapter";

    private final EventBus eventBus;
    private FragmentActivity activity;
    private OpeningTime openingTime;
    private CopyOnWriteArrayList<OpeningHours> openingHoursList;
    private LinearLayout linearLayout;
    private boolean hasToHide;

    @Inject
    OpeningHoursValueParser openingHoursValueParser;

    @Inject
    OpeningTimeValueParser openingTimeValueParser;

    public OpeningHoursLinearLayoutAdapter(OpeningTime openingTime,
                                           List<OpeningHours> openingHoursList,
                                           LinearLayout openingHoursLayout,
                                           FragmentActivity activity, boolean hasToHide) {
        this.openingTime = openingTime;
        this.activity = activity;
        this.openingHoursList = new CopyOnWriteArrayList<>();
        this.openingHoursList.addAll(openingHoursList);
        this.linearLayout = openingHoursLayout;
        this.hasToHide = hasToHide;
        eventBus = EventBus.getDefault();
        ((OsmTemplateApplication) activity.getApplication()).getOsmTemplateComponent().inject(this);
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
                fragment.show(activity.getSupportFragmentManager(), EditDaysDialogFragment.class.getSimpleName());
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

        String[] openingHoursPart = openingHoursValueParser.toValue(Collections.singletonList(openingHours)).split(" ");

        if (openingHoursPart.length > 1) {
            String[] days = openingHoursPart[0].split("-");
            if (days.length == 7) {
                textViewDaysValue.setText("7/7");
            } else {
                textViewDaysValue.setText(openingHoursPart[0]);
            }

            String[] hours = openingHoursPart[1].split("-");
            if (hours[0].equals(hours[1])) {
                textViewHoursValue.setText("24/24");
            } else if (hasToHide) {
                textViewHoursValue.setText(openingHoursPart[1].substring(0, 5));
            } else {
                textViewHoursValue.setText(openingHoursPart[1]);
            }
        } else {
            if (openingHoursPart[0].equals("24/7")) {
                textViewDaysValue.setText("7/7");
                textViewHoursValue.setText("24/24");
            }
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

                        String[] openingHoursPart = openingHoursValueParser.toValue(Collections.singletonList(openingHours)).split(" ");
                        if (openingHoursPart.length > 1) {
                            textViewDaysValue.setText(openingHoursPart[0]);
                            if (hasToHide) {
                                textViewHoursValue.setText(openingHoursPart[1].substring(0, 5));
                            } else {
                                textViewHoursValue.setText(openingHoursPart[1]);
                            }
                        } else if (openingHoursPart[0].equals("24/7")) {
                            textViewDaysValue.setText("7/7");
                            textViewHoursValue.setText("24/24");
                        }
                    }
                });
                fragment.show(activity.getSupportFragmentManager(), EditDaysDialogFragment.class.getSimpleName());
            }
        };

        textViewDaysValue.setOnClickListener(onClickListener);
        textViewHoursValue.setOnClickListener(onClickListener);

        linearLayout.addView(openingHoursItem, linearLayout.getChildCount() - 1);
    }
}
