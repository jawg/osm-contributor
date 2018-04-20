package io.jawg.osmcontributor.ui.adapters.binding;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RelativeLayout;

import java.util.ArrayList;
import java.util.Map;

import io.jawg.osmcontributor.OsmTemplateApplication;
import io.jawg.osmcontributor.R;
import io.jawg.osmcontributor.ui.adapters.item.TagItem;
import io.jawg.osmcontributor.ui.adapters.parser.ParserManager;
import io.jawg.osmcontributor.ui.utils.views.holders.TagRadioChoiceHolder;

public class RadioChoiceViewBinder extends CheckedTagViewBinder<TagRadioChoiceHolder, TagItem> {

    public RadioChoiceViewBinder(Activity activity, OnTagItemChange onTagItemChange) {
        super(activity, onTagItemChange);
        ((OsmTemplateApplication) activity.getApplication()).getOsmTemplateComponent().inject(this);
    }

    @Override
    public boolean supports(TagItem.Type type) {
        return TagItem.Type.SINGLE_CHOICE.equals(type);
    }

    @Override
    public void onBindViewHolder(TagRadioChoiceHolder holder, final TagItem tagItem) {
        // Save holder
        this.content = holder.getContent();

        holder.setCheckBoxListener(new TagRadioChoiceHolder.CheckBoxListener() {
            @Override
            public void onCheckBoxSelected(String value) {
                tagItem.setValue(value);
                if (onTagItemChange != null) {
                    onTagItemChange.onTagItemUpdated(tagItem);
                }
            }
        });

        // Set key text view
        holder.getTextViewKey().setText(ParserManager.parseTagName(tagItem.getKey(), holder.getContent().getContext()));

        // if Tag is show=false, hide it
        if (!tagItem.isShow()) {
            ((RelativeLayout) holder.getContent().getParent()).setVisibility(View.GONE);
        }

        // Check if size of possible values are 3, means special action to organize layout
        Map<String, String> values = tagItem.getValues();
        boolean isFourElements = values.size() == 3;

        // List of radio buttons without undefined. Undefined is always showing
        RadioButton[] radioButtons = holder.getRadioButtons();
        RadioButton undefinedRadioButton = holder.getUndefinedRadioButton();

        // If the tag is mandatory, the undefined button is disabled and unchecked
        if (tagItem.isMandatory()) {
            undefinedRadioButton.setEnabled(false);
            undefinedRadioButton.setChecked(false);
        } else {
            undefinedRadioButton.setChecked(true);
        }

        // Access element for values
        int pos = 0;
        for (int i = 0; i < radioButtons.length; i++) {
            if (!values.isEmpty()) {
                // If values is not empty...
                if (isFourElements && i == 1) {
                    // ... and list contains four values, skip one radio to have a 2/2 side by side printing
                    radioButtons[i].setVisibility(View.INVISIBLE);
                    i++;
                    isFourElements = false;
                }

                if (pos < values.size()) {
                    // Set value of radio button and show it
                    String value = (new ArrayList<>(values.values())).get(pos);
                    radioButtons[i].setText(value);
                    radioButtons[i].setVisibility(View.VISIBLE);

                    // Select radio if value is not undefined
                    String key = (new ArrayList<>(values.keySet())).get(pos);
                    if (tagItem.getValue() != null && tagItem.getValue().equals(key)) {
                        holder.getUndefinedRadioButton().setChecked(false);
                        radioButtons[i].setChecked(true);
                    }
                    pos++;
                } else {
                    // If all values are set, hide radio button not used
                    radioButtons[i].setVisibility(View.INVISIBLE);
                }
            }
        }

        // run validation process
        showInvalidityMessage(tagItem);
    }

    @Override
    public TagRadioChoiceHolder onCreateViewHolder(ViewGroup parent) {
        View booleanChoiceLayout = LayoutInflater.from(parent.getContext()).inflate(R.layout.tag_item_radio, parent, false);
        return new TagRadioChoiceHolder(booleanChoiceLayout);
    }
}
