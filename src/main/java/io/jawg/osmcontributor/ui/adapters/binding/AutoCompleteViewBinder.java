package io.jawg.osmcontributor.ui.adapters.binding;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.greenrobot.eventbus.EventBus;

import java.lang.ref.WeakReference;
import java.util.List;

import io.jawg.osmcontributor.OsmTemplateApplication;
import io.jawg.osmcontributor.R;
import io.jawg.osmcontributor.ui.adapters.item.TagItem;
import io.jawg.osmcontributor.ui.adapters.parser.ParserManager;
import io.jawg.osmcontributor.ui.events.edition.PleaseApplyTagChange;
import io.jawg.osmcontributor.ui.utils.views.holders.TagItemAutoCompleteViewHolder;

/**
 * Created by capaldi on 05/07/17.
 */

public class AutoCompleteViewBinder implements TagViewBinder<TagItemAutoCompleteViewHolder> {

    private WeakReference<Activity> activity;
    private WeakReference<EventBus> eventBus;

    public AutoCompleteViewBinder(Activity activity, EventBus eventBus) {
        ((OsmTemplateApplication) activity.getApplication()).getOsmTemplateComponent().inject(this);
        this.activity = new WeakReference<>(activity);
        this.eventBus = new WeakReference<>(eventBus);
    }

    @Override
    public boolean supports(TagItem.Type type) {
        return (TagItem.Type.NUMBER.equals(type) || TagItem.Type.TEXT.equals(type));
    }

    @Override
    public void onBindViewHolder(final TagItemAutoCompleteViewHolder holder, TagItem tagItem) {
        // Set values input
        holder.getTextViewKey().setText(ParserManager.parseTagName(tagItem.getKey()));
        holder.getTextViewValue().setText(tagItem.getValue());
        holder.getTextInputLayout().setHint(tagItem.getKey());

        // If phone type if phone, set input type to number
        if (tagItem.getTagType() == TagItem.Type.NUMBER) {
            holder.getTextViewValue().setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        }

        // Get possible values
        final List<String> values = tagItem.getValues();

        holder.getTextViewValue().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (i1 != i2) {
                    eventBus.get().post(new PleaseApplyTagChange(holder.getTextViewKey().getText().toString(), charSequence.toString()));
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        if (!tagItem.isConform() && holder.getContent().getChildAt(1).getId() != R.id.malformated_layout) {
            holder.getContent().addView(LayoutInflater.from(activity.get()).inflate(
                    R.layout.malformated_layout, holder.getContent(), false), 1);
            String currentValue = activity.get().getString(R.string.malformated_value) + " " + tagItem.getValue();
            ((TextView) ((LinearLayout) holder.getContent().getChildAt(1)).getChildAt(1)).setText(currentValue);
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent) {
        View autoCompleteLayout = LayoutInflater.from(parent.getContext()).inflate(R.layout.tag_item_multi_choice, parent, false);
        return new TagItemAutoCompleteViewHolder(autoCompleteLayout);
    }
}
