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
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.widget.AutoCompleteTextView;

import de.greenrobot.event.EventBus;
import io.mapsquare.osmcontributor.utils.EventCountDownTimer;
import timber.log.Timber;

public class DelayedAutoCompleteTextView extends AutoCompleteTextView {

    private static final int DELAY = 1000;

    private EventBus bus;
    private EventCountDownTimer timer;
    private boolean initialized = false;

    public DelayedAutoCompleteTextView(Context context) {
        super(context);
    }

    public DelayedAutoCompleteTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public DelayedAutoCompleteTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void performFiltering(CharSequence text, int keyCode) {
        if (timer == null) {
            super.performFiltering(text, keyCode);
        } else {
            Timber.d("Delaying \"perform filtering\"");
            timer.cancel();
            timer.setEvent(new PleasePerformFiltering(text, keyCode));
            timer.start();
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        if (bus != null) {
            bus.unregister(this);
            bus = null;
        }
        super.onDetachedFromWindow();
    }

    /**
     * Call this from your activity/fragment to allow the AutoCompleteTextView to delay calls to
     * {@link #performFiltering(CharSequence, int)}.
     *
     * @param eventBus The event bus used for our timer
     */
    public void initialize(@NonNull EventBus eventBus) {
        if (initialized) {
            throw new IllegalStateException("Already initialized");
        }
        initialized = true;
        bus = eventBus;
        timer = new EventCountDownTimer(DELAY, DELAY, bus);

        bus.register(this);
    }

    public void onEventMainThread(PleasePerformFiltering event) {
        super.performFiltering(event.getText(), event.getKeyCode());
    }

    private static class PleasePerformFiltering {

        private final CharSequence text;
        private final int keyCode;

        public PleasePerformFiltering(CharSequence text, int keyCode) {
            this.text = text;
            this.keyCode = keyCode;
        }

        public CharSequence getText() {
            return text;
        }

        public int getKeyCode() {
            return keyCode;
        }
    }
}
