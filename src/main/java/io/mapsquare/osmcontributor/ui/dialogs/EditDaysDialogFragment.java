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

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.DialogFragment;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.Button;
import android.widget.TextView;
import android.widget.TimePicker;

import org.joda.time.LocalTime;

import butterknife.BindView;
import butterknife.BindViews;
import butterknife.ButterKnife;
import io.mapsquare.osmcontributor.R;
import io.mapsquare.osmcontributor.model.utils.OpeningHours;
import io.mapsquare.osmcontributor.ui.utils.views.customs.TextViewCheck;

/**
 * @author Tommy Buonomo on 04/07/16.
 */
public class EditDaysDialogFragment extends DialogFragment {
    private boolean from = true;
    private int rightViewX;
    private boolean viewPositionFixed;
    private OnEditDaysListener listener;

    private OpeningHours openingHours;

    @BindView(R.id.dialog_edit_day_left_view)
    View leftView;

    @BindView(R.id.dialog_edit_day_right_view)
    View rightView;

    @BindView(R.id.dialog_edit_day_from_text)
    TextView fromTextView;

    @BindView(R.id.dialog_edit_day_to_text)
    TextView toTextView;

    @BindView(R.id.dialog_edit_day_from_time_picker)
    TimePicker fromTimePicker;

    @BindView(R.id.dialog_edit_day_to_time_picker)
    TimePicker toTimePicker;

    @BindView(R.id.dialog_edit_day_ok_button)
    Button okButton;

    @BindView(R.id.dialog_edit_day_cancel_button)
    Button cancelButton;

    @BindViews(value = {R.id.dialog_edit_day_monday_check,
            R.id.dialog_edit_day_tuesday_check,
            R.id.dialog_edit_day_wednesday_check,
            R.id.dialog_edit_day_thursday_check,
            R.id.dialog_edit_day_friday_check,
            R.id.dialog_edit_day_saturday_check,
            R.id.dialog_edit_day_sunday_check})
    TextViewCheck[] daysTextCheck;

    public EditDaysDialogFragment() {
        openingHours = new OpeningHours();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.dialog_edit_days_tag, container, false);
        ButterKnife.bind(this, rootView);

        rootView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (!viewPositionFixed) {
                    setUpViewPositions();
                    viewPositionFixed = true;
                }
            }
        });

        setUpViews();
        return rootView;
    }

    private void setUpViews() {
        fromTextView.setTextColor(Color.WHITE);
        // TODO: 24h or AM/PM depending on locale
        fromTimePicker.setIs24HourView(true);
        fromTimePicker.bringToFront();
        toTimePicker.setIs24HourView(true);
        toTimePicker.setAlpha(0);


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            fromTimePicker.setMinute(openingHours.getFromTime().getMinuteOfHour());
            fromTimePicker.setHour(openingHours.getFromTime().getHourOfDay());
            toTimePicker.setMinute(openingHours.getToTime().getMinuteOfHour());
            toTimePicker.setHour(openingHours.getToTime().getHourOfDay());
        } else {
            //The two methods are deprecated but this in the only way to change
            //the minute and the hour of the time picker before API 23
            fromTimePicker.setCurrentMinute(openingHours.getFromTime().getMinuteOfHour());
            fromTimePicker.setCurrentHour(openingHours.getFromTime().getHourOfDay());
            toTimePicker.setCurrentMinute(openingHours.getToTime().getMinuteOfHour());
            toTimePicker.setCurrentHour(openingHours.getToTime().getHourOfDay());
        }

        fromTimePicker.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
            @Override
            public void onTimeChanged(TimePicker timePicker, int h, int m) {
                if (from) {
                    if (openingHours.getFromTime().getMinuteOfHour() != m) {
                        openingHours.setFromTime(new LocalTime(h, m));
                    } else {
                        openingHours.setFromTime(new LocalTime(h, m));
                    }
                }
            }
        });

        toTimePicker.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
            @Override
            public void onTimeChanged(TimePicker timePicker, int h, int m) {
                if (!from) {
                    openingHours.setToTime(new LocalTime(h, m));
                }
            }
        });

        toTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (from) {
                    toggleFromTo();
                }
            }
        });

        fromTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!from) {
                    toggleFromTo();
                }
            }
        });

        // If user click on one day letter, set the daysChanged to true
        for (int i = 0; i < daysTextCheck.length; i++) {
            final int finalI = i;
            daysTextCheck[i].setChecked(openingHours.getDays()[i] != null);
            daysTextCheck[i].setOnCheckListener(new TextViewCheck.OnCheckListener() {
                @Override
                public void onChecked(boolean checked) {
                    openingHours.setDayActivated(finalI, checked);
                }
            });
        }

        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (openingHours.isChanged() && listener != null) {
                    // Handle the listener
                    listener.onOpeningTimeChanged(openingHours);
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

    /**
     * This method allows the transition between 'from' and 'to' tabs
     */
    private void toggleFromTo() {
        from = !from;
        createToggleAnimator(from).start();
        fromTextView.setTextColor(from ? Color.WHITE : Color.BLACK);
        toTextView.setTextColor(from ? Color.BLACK : Color.WHITE);
        (from ? fromTimePicker : toTimePicker).bringToFront();
    }

    private void setUpViewPositions() {
        int[] rightLocation = new int[2];
        rightView.getLocationOnScreen(rightLocation);
        rightViewX = rightLocation[0];
    }

    private static final String TAG = "EditDays";

    /**
     * Create the tab transition animation between 'from' tab and 'to' tab and the
     * fade animation for the time pickers
     * @param from
     * @return
     */
    private Animator createToggleAnimator(final boolean from) {
        int toX = from ? 0 : rightViewX;

        //Tab animator
        ObjectAnimator tabAnimatorTranslationX = ObjectAnimator.ofFloat(leftView, View.TRANSLATION_X, toX);

        final TimePicker outTimePicker = from ? toTimePicker : fromTimePicker;
        final TimePicker inTimePicker = from ? fromTimePicker : toTimePicker;

        //Time picker animator
        ObjectAnimator fadeOut = ObjectAnimator.ofFloat(outTimePicker, View.ALPHA, 1, 0);
        ObjectAnimator fadeIn = ObjectAnimator.ofFloat(inTimePicker, View.ALPHA, 0, 1);

        //Final animator set
        Interpolator interpolator = new DecelerateInterpolator();
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(tabAnimatorTranslationX, fadeIn, fadeOut);

        for (Animator animator : animatorSet.getChildAnimations()) {
            animator.setDuration(300);
            animator.setInterpolator(interpolator);
        }
        return animatorSet;
    }

    public void setOpeningHours(OpeningHours openingHours) {
        this.openingHours = openingHours;
    }

    public void setOnEditDaysListener(OnEditDaysListener listener) {
        this.listener = listener;
    }

    public interface OnEditDaysListener {
        void onOpeningTimeChanged(OpeningHours openingTime);
    }
}
