package io.jawg.osmcontributor.ui.adapters.binding;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.greenrobot.eventbus.EventBus;

import java.lang.ref.WeakReference;

import io.jawg.osmcontributor.OsmTemplateApplication;
import io.jawg.osmcontributor.R;
import io.jawg.osmcontributor.ui.adapters.item.TagItem;
import io.jawg.osmcontributor.ui.adapters.parser.ParserManager;
import io.jawg.osmcontributor.ui.events.edition.PleaseApplyTagChange;
import io.jawg.osmcontributor.ui.utils.views.holders.TagItemAutoCompleteViewHolder;

/**
 * Created by capaldi on 05/07/17.
 */

public class AutoCompleteViewBinder extends CheckedTagViewBinder<TagItemAutoCompleteViewHolder> {

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
        // Save holder
        this.content = holder.getContent();
        this.tagItem = tagItem;

        // Set values input
        holder.getTextViewKey().setText(ParserManager.parseTagName(tagItem.getKey()));
        holder.getTextViewValue().setText(tagItem.getValue());
        holder.getTextInputLayout().setHint(tagItem.getKey());

        // If phone type if phone, set input type to number
        if (tagItem.getTagType() == TagItem.Type.NUMBER) {
            holder.getTextViewValue().setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        }

        if (!tagItem.isShow()) {
            holder.getContent().setVisibility(View.INVISIBLE);
        }

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

        showValidation();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent) {
        View autoCompleteLayout = LayoutInflater.from(parent.getContext()).inflate(R.layout.tag_item_multi_choice, parent, false);
        return new TagItemAutoCompleteViewHolder(autoCompleteLayout);
    }

    public void showValidation() {
        showInvalidityMessage(activity, content, tagItem);
    }
}
