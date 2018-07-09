package io.jawg.osmcontributor.ui.adapters.binding;

import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import io.jawg.osmcontributor.ui.adapters.item.shelter.TagItem;

public interface TagViewBinder<H extends RecyclerView.ViewHolder, T extends TagItem> {
    boolean supports(TagItem.Type type);
    void onBindViewHolder(H holder, T tagItem);
    H onCreateViewHolder(ViewGroup parent);
}
