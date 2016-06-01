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
package io.mapsquare.osmcontributor.map.ways;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Build;
import android.util.AttributeSet;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.SeekBar;

import java.util.Arrays;
import java.util.TreeSet;

import io.mapsquare.osmcontributor.R;

public class LevelBar extends SeekBar {

    private Double[] levels;
    private Double progressValue;

    private int drawableHeight;
    private int textSize;

    private Paint paint = new Paint();

    {
        this.levels = new Double[]{0d};
        this.setMax(0);
        this.progressValue = 0d;
        textSize = getResources().getDimensionPixelSize(R.dimen.level_text_size);
        this.paint.setTextSize(textSize);
        this.paint.setTextAlign(Paint.Align.CENTER);
    }

    public LevelBar(Context context) {
        super(context);

    }

    public LevelBar(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public LevelBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public LevelBar(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public Double[] getLevels() {
        return levels;
    }

    public void setLevels(Double[] levels, double currentLevel) {
        this.progressValue = currentLevel;
        this.levels = levels;
        refresh();
    }

    public void setLevels(TreeSet<Double> levelsSet, double currentLevel) {
        setLevels(levelsSet.toArray(new Double[levelsSet.size()]), currentLevel);
    }

    public int getDrawableHeight() {
        return drawableHeight;
    }

    public void setDrawableHeight(int drawableHeight) {
        this.drawableHeight = drawableHeight;
    }

    private synchronized void refresh() {
        setMax(levels.length - 1);
        int index = Arrays.binarySearch(levels, progressValue);
        if (index >= 0) {
            setProgress(index);
        } else {
            progressValue = 0d;
            setProgress(Arrays.binarySearch(levels, new Double(0d)));
        }
    }

    public Double getLevel() {
        if (levels.length == 0) {
            return 0d;
        }
        return levels[getProgress()];
    }

    public void setLevel(double level) {
        int index = Arrays.binarySearch(levels, level);
        if (index > -1) {
            setProgress(index);
        }
    }

    @Override
    public void onInitializeAccessibilityEvent(AccessibilityEvent event) {
        super.onInitializeAccessibilityEvent(event);
        event.setClassName(LevelBar.class.getName());
    }

    @Override
    public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo info) {
        super.onInitializeAccessibilityNodeInfo(info);
        info.setClassName(LevelBar.class.getName());
    }

    @Override
    protected synchronized void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (getLevel() != null) {
            canvas.save();
            float percent = ((float) getProgress()) / (float) getMax();
            int width = getWidth() - 2 * getThumbOffset();
            float answer = ((int) (width * percent) + getThumbOffset());
            canvas.rotate(90);
            canvas.drawText(getLevel().toString(), drawableHeight * 3 / 4, -answer + textSize / 2, paint);
            canvas.restore();
        }
    }
}
