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
package io.mapsquare.osmcontributor.ui.utils;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.view.ScaleGestureDetector;
import android.view.animation.DecelerateInterpolator;

import java.util.Queue;

import io.mapsquare.osmcontributor.utils.LimitedQueue;

/**
 * @author Tommy Buonomo on 16/06/16.
 */
public abstract class ZoomAnimationGestureDetector extends ScaleGestureDetector.SimpleOnScaleGestureListener {
    private Queue<Float> previousSpeedQueue = new LimitedQueue<>(5);

    @Override
    public boolean onScaleBegin(ScaleGestureDetector scaleGestureDetector) {
        previousSpeedQueue.clear();
        return true;
    }

    @Override
    public boolean onScale(ScaleGestureDetector scaleGestureDetector) {
        float currentSpeed = scaleGestureDetector.getPreviousSpan() - scaleGestureDetector.getCurrentSpan();
        previousSpeedQueue.add(currentSpeed);
        return true;
    }

    @Override
    public void onScaleEnd(ScaleGestureDetector scaleGestureDetector) {
        float sum = 0;
        for (Float speed : previousSpeedQueue) {
            sum += speed;
        }
        float moy = sum / previousSpeedQueue.size();
        if (Math.abs(moy) > 50) {
            moy = moy > 0 ? 50 : -50;
        }

        ValueAnimator valueAnimator = ObjectAnimator.ofFloat(-moy / 1000, 0);
        int duration = (int) (Math.abs(moy) * 12);
        valueAnimator.setDuration(duration);
        valueAnimator.setInterpolator(new DecelerateInterpolator());
        onZoomAnimationEnd(valueAnimator);

        if (Math.abs(moy) > 2) {
            onZoomAnimationEnd(valueAnimator);
        }
    }


    public abstract void onZoomAnimationEnd(ValueAnimator animator);
}
