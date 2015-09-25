/**
 * Copyright (C) 2015 eBusiness Information
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
package io.mapsquare.osmcontributor.type;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

/**
 * Custom {@link FrameLayout} allowing to show only one of two of its children at a time.
 */
public class HorizontalViewSwitcher extends FrameLayout {

    private AnimatorSet animatorSet;
    private ObjectAnimator firstAnimator;
    private ObjectAnimator lastAnimator;
    private SimpleAnimatorListener lastAnimatorListener;
    private SimpleAnimatorListener firstAnimatorListener;
    private View firstView;
    private View lastView;
    private boolean lastVisible;

    public HorizontalViewSwitcher(Context context) {
        super(context);
        init(context);
    }

    public HorizontalViewSwitcher(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public HorizontalViewSwitcher(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        int animationDuration = context.getResources().getInteger(android.R.integer.config_mediumAnimTime);

        firstAnimator = new ObjectAnimator();
        firstAnimator.setPropertyName("translationX");
        firstAnimator.setDuration(animationDuration);

        firstAnimatorListener = new SimpleAnimatorListener() {
            @Override
            public void onAnimationEnd(Animator animation) {
                firstView.setVisibility(GONE);
            }
        };

        lastAnimator = new ObjectAnimator();
        lastAnimator.setPropertyName("translationX");
        lastAnimator.setDuration(animationDuration);

        lastAnimatorListener = new SimpleAnimatorListener() {
            @Override
            public void onAnimationEnd(Animator animation) {
                lastView.setVisibility(GONE);
            }
        };
    }

    @Override
    public void addView(@NonNull View child, int index, ViewGroup.LayoutParams params) {
        int childNumber = getChildCount();
        if (childNumber >= 2) {
            throw new IllegalStateException("You cannot add more than 2 children to this view");
        } else if (childNumber == 0) {
            child.setVisibility(View.VISIBLE);
        } else {
            child.setVisibility(View.GONE);
        }
        super.addView(child, index, params);
    }

    /**
     * Prepare the view switcher.<br/>
     * The ViewSwitcher must have exactly two children, only one of them will be visible initially
     * (which one stays visible depends on the given parameter).<br/><br/>
     * <b>You must call this method before calling any other one.</b>
     *
     * @param firstVisible Initial visibility state: true for the first child, false for the last child.
     */
    public void prepareViews(boolean firstVisible) {
        if (getChildCount() != 2) {
            throw new IllegalStateException("This view must have exactly two children");
        }

        firstView = getChildAt(0);
        firstAnimator.setTarget(firstView);
        lastView = getChildAt(1);
        lastAnimator.setTarget(lastView);

        View visibleView = firstVisible ? firstView : lastView;
        visibleView.setVisibility(VISIBLE);

        View hiddenView = firstVisible ? lastView : firstView;
        hiddenView.setVisibility(GONE);

        lastVisible = !firstVisible;
    }

    public void showFirstView() {
        enforceViewPrepared();
        if (!lastVisible) {
            return;
        }
        lastVisible = false;

        if (animatorSet != null) {
            animatorSet.cancel();
        }

        int width = getWidth();

        // Restore views to a known state
        firstView.setTranslationX(-width);
        firstView.setVisibility(VISIBLE);

        lastView.setTranslationX(0);

        // Update animators
        firstAnimator.setFloatValues(0);
        lastAnimator.setFloatValues(width);

        // Start animation
        animatorSet = new AnimatorSet();
        animatorSet.playTogether(lastAnimator, firstAnimator);
        animatorSet.addListener(lastAnimatorListener);
        animatorSet.start();
    }

    public void showLastView() {
        enforceViewPrepared();
        if (lastVisible) {
            return;
        }
        lastVisible = true;

        if (animatorSet != null) {
            animatorSet.cancel();
        }

        int width = getWidth();

        // Restore views to a known state
        firstView.setTranslationX(0);

        lastView.setTranslationX(width);
        lastView.setVisibility(VISIBLE);

        // Update animators
        firstAnimator.setFloatValues(-width);
        lastAnimator.setFloatValues(0);

        // Start animation
        animatorSet = new AnimatorSet();
        animatorSet.playTogether(firstAnimator, lastAnimator);
        animatorSet.addListener(firstAnimatorListener);
        animatorSet.start();
    }

    public void showView(View view) {
        if (view == firstView) {
            showFirstView();
        } else if (view == lastView) {
            showLastView();
        } else {
            throw new IllegalArgumentException("Provided view does not belong to this view switcher");
        }
    }

    public boolean isLastViewShown() {
        enforceViewPrepared();
        return lastVisible;
    }

    public boolean isFirstViewShown() {
        enforceViewPrepared();
        return !lastVisible;
    }

    private void enforceViewPrepared() {
        if (firstView == null) {
            throw new IllegalStateException("You must call prepareViews() before using this function");
        }
    }

    private static class SimpleAnimatorListener implements Animator.AnimatorListener {
        @Override
        public void onAnimationStart(Animator animation) {
        }

        @Override
        public void onAnimationEnd(Animator animation) {
        }

        @Override
        public void onAnimationCancel(Animator animation) {
        }

        @Override
        public void onAnimationRepeat(Animator animation) {
        }
    }
}
