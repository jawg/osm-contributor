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

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import io.mapsquare.osmcontributor.R;

public class ItemDividerDecoration extends RecyclerView.ItemDecoration {

    private static final int LINE_HEIGHT_PX = 1;

    private Paint paint;

    public ItemDividerDecoration(Context context) {
        paint = new Paint();
        paint.setColor(context.getResources().getColor(R.color.list_divider));
    }

    @Override
    public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
        float yPositionThreshold = LINE_HEIGHT_PX + 1.0f; // [px]
        float zPositionThreshold = 1.0f; // [px]
        int left = parent.getPaddingLeft();
        int right = parent.getWidth() - parent.getPaddingRight();

        for (int i = 0, count = parent.getChildCount() - 1; i < count; i++) {
            View child = parent.getChildAt(i);
            View nextChild = parent.getChildAt(i + 1);
            if (child.getVisibility() != View.VISIBLE || nextChild.getVisibility() != View.VISIBLE) {
                continue;
            }

            // check if the next item is placed at the bottom
            float childBottom = child.getBottom() + ViewCompat.getTranslationY(child);
            float nextChildTop = nextChild.getTop() + ViewCompat.getTranslationY(nextChild);
            if (Math.abs(nextChildTop - childBottom) >= yPositionThreshold) {
                continue;
            }

            // check if the next item is placed on the same plane
            float childZ = ViewCompat.getTranslationZ(child) + ViewCompat.getElevation(child);
            float nextChildZ = ViewCompat.getTranslationZ(nextChild) + ViewCompat.getElevation(nextChild);
            if (Math.abs(nextChildZ - childZ) >= zPositionThreshold) {
                continue;
            }

            float childAlpha = ViewCompat.getAlpha(child);
            float nextChildAlpha = ViewCompat.getAlpha(nextChild);
            paint.setAlpha((int) (255 * Math.min(childAlpha, nextChildAlpha) + 0.5f));

            int top = child.getBottom();
            int ty = (int) (ViewCompat.getTranslationY(child) + 0.5f);
            c.drawLine(left, top + ty, right, top + ty, paint);
        }
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        outRect.set(0, 0, 0, LINE_HEIGHT_PX);
    }
}