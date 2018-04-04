package io.jawg.osmcontributor.ui.adapters.binding;

import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import io.jawg.osmcontributor.ui.adapters.item.TagItem;

public interface TagViewBinder<T> {
    boolean supports(TagItem.Type type);
    void onBindViewHolder(T holder, TagItem tagItem);
    RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent);
}
