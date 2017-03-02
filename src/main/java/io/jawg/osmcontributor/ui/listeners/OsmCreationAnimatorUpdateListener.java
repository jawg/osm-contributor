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
package io.jawg.osmcontributor.ui.listeners;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ImageView;

import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.maps.MapboxMap;

public class OsmCreationAnimatorUpdateListener implements ValueAnimator.AnimatorUpdateListener, ValueAnimator.AnimatorListener {
    public static final int STEPS_CENTER_ANIMATION = 100;
    public static final float MOVE_COEF = 0.1f; // the animation will be done in 30% of the screen
    int totalDist;
    int screenWidth;
    float previousStep = 0f;

    MapboxMap mapboxMap;
    Context context;
    ImageView handImageView;

    public OsmCreationAnimatorUpdateListener(MapboxMap mapboxMap, ImageView handImageView, Context context) {
        this.mapboxMap = mapboxMap;
        this.context = context;
        this.handImageView = handImageView;

        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        screenWidth = displayMetrics.widthPixels;
        totalDist = (int) (screenWidth * MOVE_COEF) * 2;
    }

    @Override
    public void onAnimationUpdate(ValueAnimator valueAnimator) {
        float animatedValue = (Float) valueAnimator.getAnimatedValue();
        int pixelToMove = (int) ((animatedValue - previousStep) * totalDist / 100);

        if (previousStep == 0) {
            Animation animation = new AlphaAnimation(0.0f, 1.0f);
            animation.setDuration(100);
            animation.setFillAfter(true);
            handImageView.startAnimation(animation);
        }
        if (animatedValue > STEPS_CENTER_ANIMATION / 2) {
            pixelToMove = -pixelToMove;
        }

        mapboxMap.moveCamera(CameraUpdateFactory.scrollBy(pixelToMove, 0));

        float left = handImageView.getX() + pixelToMove;
        handImageView.setX(left);


        previousStep = animatedValue;
    }

    @Override
    public void onAnimationStart(Animator animator) {

    }

    @Override
    public void onAnimationEnd(Animator animator) {
        Animation animation = new AlphaAnimation(1.0f, 0.0f);
        animation.setDuration(3000);
        animation.setFillAfter(true);
        handImageView.startAnimation(animation);
    }

    @Override
    public void onAnimationCancel(Animator animator) {
        handImageView.setVisibility(View.GONE);
    }

    @Override
    public void onAnimationRepeat(Animator animator) {

    }
}
