/**
 * Copyright (C) 2019 Takima
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
package io.jawg.osmcontributor.ui.utils.views.customs;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.support.design.widget.TextInputEditText;
import android.util.AttributeSet;
import android.util.Log;

import io.jawg.osmcontributor.R;

public class TypeFaceTextInputEditText extends TextInputEditText {
    private static final String TAG = "TextInputEditText";

    public TypeFaceTextInputEditText(Context context) {
        super(context);
    }

    public TypeFaceTextInputEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.TypeFaceView, 0, 0);
        setCustomFont(context, a.getString(R.styleable.TypeFaceView_typeface));
        a.recycle();
    }

    public TypeFaceTextInputEditText(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.TypeFaceView, 0, 0);
        setCustomFont(context, a.getString(R.styleable.TypeFaceView_typeface));
        a.recycle();
    }

    public boolean setCustomFont(Context ctx, String typeface) {
        Typeface tf = null;
        try {
            tf = Typeface.createFromAsset(ctx.getAssets(), typeface);
        } catch (Exception e) {
            Log.e(TAG, "Could not get typeface: " + e.getMessage());
            return false;
        }

        setTypeface(tf);
        return true;
    }

}