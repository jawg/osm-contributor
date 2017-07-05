package io.jawg.osmcontributor.ui.adapters.binding;

import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import io.jawg.osmcontributor.ui.adapters.item.TagItem;
import io.jawg.osmcontributor.ui.utils.views.holders.TagItemConstantViewHolder;

/**
 * Created by loicortola on 05/07/2017.
 */

public interface TagViewBinder<T> {
    boolean supports(TagItem.Type tag);
    void onBindViewHolder(T holder, TagItem tag);
    RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent);
}
