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
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.DialogFragment;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;

import org.joda.time.LocalTime;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.mapsquare.osmcontributor.R;

/**
 * @author Tommy Buonomo on 04/07/16.
 */
public class EditDaysTagDialogFragment extends DialogFragment {
    private boolean from = true;
    private int rightViewX;
    private boolean viewPositionFixed;

    private LocalTime fromTime, toTime;

    @BindView(R.id.dialog_edit_day_days_layout)
    LinearLayout daysLayout;

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

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.dialog_edit_days, container, false);
        ButterKnife.bind(this, rootView);

        fromTime = new LocalTime(8, 0);
        toTime = new LocalTime(18, 0);

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

        fromTimePicker.setIs24HourView(true);
        fromTimePicker.setCurrentMinute(fromTime.getMinuteOfHour());
        fromTimePicker.setCurrentHour(fromTime.getHourOfDay());

        toTimePicker.setIs24HourView(true);
        toTimePicker.setCurrentMinute(toTime.getMinuteOfHour());
        toTimePicker.setCurrentHour(toTime.getHourOfDay());
        toTimePicker.setVisibility(View.GONE);

        fromTimePicker.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
            @Override
            public void onTimeChanged(TimePicker timePicker, int h, int m) {
                if (from) {
                    if (fromTime.getMinuteOfHour() != m) {
                        fromTime = new LocalTime(h, m);
                        toggleFromTo();
                    } else {
                        fromTime = new LocalTime(h, m);
                    }
                }
            }
        });

        toTimePicker.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
            @Override
            public void onTimeChanged(TimePicker timePicker, int h, int m) {
                if (!from) {
                    toTime = new LocalTime(h, m);
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

        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
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

    private void toggleFromTo() {
        from = !from;
        createToggleAnimator(from).start();
        fromTextView.setTextColor(from ? Color.WHITE : Color.BLACK);
        toTextView.setTextColor(from ? Color.BLACK : Color.WHITE);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    private void setUpViewPositions() {
        int[] rightLocation = new int[2];
        rightView.getLocationOnScreen(rightLocation);
        rightViewX = rightLocation[0];
    }

    private static final String TAG = "EditDays";

    /**
     * Create the tab transition animation between 'from' tab and 'to' tab
     * @param from
     * @return
     */
    private Animator createToggleAnimator(boolean from) {
        int toX = from ? 0 : rightViewX;

        ObjectAnimator tabAnimatorTranslationX = ObjectAnimator.ofFloat(leftView, View.TRANSLATION_X, toX);

        final TimePicker outTimePicker = from ? toTimePicker : fromTimePicker;
        final TimePicker inTimePicker = from ? fromTimePicker : toTimePicker;

        ObjectAnimator fadeOut = ObjectAnimator.ofFloat(outTimePicker, View.ALPHA, 1, 0);
        ObjectAnimator fadeIn = ObjectAnimator.ofFloat(inTimePicker, View.ALPHA, 0, 1);

        fadeIn.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                inTimePicker.setVisibility(View.VISIBLE);
            }
        });

        fadeOut.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                outTimePicker.setVisibility(View.GONE);
            }
        });

        Interpolator interpolator = new DecelerateInterpolator();
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(tabAnimatorTranslationX, fadeIn, fadeOut);

        for (Animator animator : animatorSet.getChildAnimations()) {
            animator.setDuration(300);
            animator.setInterpolator(interpolator);
        }
        return animatorSet;
    }

    public interface OnEditDaysTagListener {
        void onDaysResponse();
    }
}
