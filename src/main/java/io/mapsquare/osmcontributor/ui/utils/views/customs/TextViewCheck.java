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
package io.mapsquare.osmcontributor.ui.utils.views.customs;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.View;

import io.mapsquare.osmcontributor.R;

/**
 * @author Tommy Buonomo on 04/07/16.
 */
public class TextViewCheck extends TypeFaceTextView {
    private static final String TAG = "CheckBoxDay";
    private static final int COLOR_CHECKED = Color.WHITE;
    private static final int COLOR_UNCHECKED = Color.parseColor("#424242");

    private OnCheckListener listener;

    private boolean checked = false;

    public TextViewCheck(Context context) {
        super(context);
        init(context, null);
    }

    public TextViewCheck(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public TextViewCheck(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        if (attrs != null) {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.TextViewCheck);
            boolean checked = a.getBoolean(R.styleable.TextViewCheck_checked, false);
            setSelected((this.checked = checked));
            setTextColor(checked ? COLOR_CHECKED : COLOR_UNCHECKED);
            a.recycle();
        }

        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                checked = !checked;
                setChecked(checked);
            }
        });
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
        setSelected(checked);
        setTextColor(checked ? COLOR_CHECKED : COLOR_UNCHECKED);
        if (listener != null) {
            listener.onChecked(checked);
        }
    }

    public boolean isChecked() {
        return checked;
    }

    public void setOnCheckListener(OnCheckListener listener) {
        this.listener = listener;
    }

    public interface OnCheckListener {
        void onChecked(boolean checked);
    }
}
