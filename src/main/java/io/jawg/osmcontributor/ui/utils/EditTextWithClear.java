package io.jawg.osmcontributor.ui.utils;


import android.content.Context;
import android.text.TextWatcher;
import android.util.AttributeSet;

import java.util.ArrayList;

public class EditTextWithClear extends android.support.v7.widget.AppCompatEditText {
    private ArrayList<TextWatcher> listeners = null;

    public EditTextWithClear(Context ctx) {
        super(ctx);
    }

    public EditTextWithClear(Context ctx, AttributeSet attrs) {
        super(ctx, attrs);
    }

    public EditTextWithClear(Context ctx, AttributeSet attrs, int defStyle) {
        super(ctx, attrs, defStyle);
    }

    @Override
    public void addTextChangedListener(TextWatcher watcher) {
        if (listeners == null) {
            listeners = new ArrayList<>();
        }
        listeners.add(watcher);

        super.addTextChangedListener(watcher);
    }

    @Override
    public void removeTextChangedListener(TextWatcher watcher) {
        if (listeners != null) {
            int i = listeners.indexOf(watcher);
            if (i >= 0) {
                listeners.remove(i);
            }
        }

        super.removeTextChangedListener(watcher);
    }

    public void clearTextChangedListeners() {
        if (listeners != null) {
            for (TextWatcher watcher : listeners) {
                super.removeTextChangedListener(watcher);
            }

            listeners.clear();
            listeners = null;
        }
    }

}
