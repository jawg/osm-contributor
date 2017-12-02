package io.jawg.osmcontributor.ui.adapters.binding;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import io.jawg.osmcontributor.OsmTemplateApplication;
import io.jawg.osmcontributor.R;
import io.jawg.osmcontributor.ui.adapters.item.TagItem;
import io.jawg.osmcontributor.ui.adapters.parser.ParserManager;
import io.jawg.osmcontributor.ui.utils.views.holders.TagItemConstantViewHolder;

/**
 * Created by capaldi on 05/07/17.
 */

public class ConstantViewBinder implements TagViewBinder<TagItemConstantViewHolder> {

    public ConstantViewBinder(Activity activity) {
        ((OsmTemplateApplication) activity.getApplication()).getOsmTemplateComponent().inject(this);
    }

    @Override
    public boolean supports(TagItem.Type type) {
        return TagItem.Type.CONSTANT.equals(type);
    }

    @Override
    public void onBindViewHolder(TagItemConstantViewHolder holder, TagItem tagItem) {
        holder.getTextViewKey().setText(ParserManager.parseTagName(tagItem.getKey()));
        holder.getTextViewValue().setText(tagItem.getValue());
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent) {
        View poiTagImposedLayout = LayoutInflater.from(parent.getContext()).inflate(R.layout.tag_item_constant, parent, false);
        return new TagItemConstantViewHolder(poiTagImposedLayout);
    }
}
