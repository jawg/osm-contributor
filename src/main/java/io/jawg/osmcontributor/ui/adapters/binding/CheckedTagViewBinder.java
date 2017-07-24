package io.jawg.osmcontributor.ui.adapters.binding;

import android.app.Activity;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.lang.ref.WeakReference;

import io.jawg.osmcontributor.R;
import io.jawg.osmcontributor.ui.adapters.item.TagItem;

/**
 * Created by mouaad on 17/07/17.
 */

public abstract class CheckedTagViewBinder<T> implements TagViewBinder<T> {

    public WeakReference<Activity> activity;
    public LinearLayout content;
    public TagItem tagItem;

    /**
     * Check if the tag contains correct values
     * @param activity Current activity (EditionPOI)
     * @param content Current LinearLayout containing each tag
     * @param tagItem tag currently checked
     */
    public void showInvalidityMessage(WeakReference<Activity> activity, LinearLayout content, TagItem tagItem) {
        // If the tag is not conform we show the error message, if not we remove it exists
        if (!tagItem.isConform() && content.getChildAt(1).getId() != R.id.malformated_layout) {
            content.addView(LayoutInflater.from(activity.get()).inflate(
                    R.layout.malformated_layout, content, false), 1);
            String currentValue = activity.get().getString(R.string.malformated_value) + " " + tagItem.getValue();
            ((TextView) ((LinearLayout) content.getChildAt(1)).getChildAt(1)).setText(currentValue);
        } else if (content.getChildAt(1).getId() == R.id.malformated_layout) {
            content.removeViewAt(1);
        }
    }

    public abstract void showValidation();
}