package io.jawg.osmcontributor.ui.adapters.binding;

import android.app.Activity;
import android.content.Context;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.lang.ref.WeakReference;

import javax.inject.Inject;

import io.jawg.osmcontributor.OsmTemplateApplication;
import io.jawg.osmcontributor.R;
import io.jawg.osmcontributor.model.utils.OpeningMonth;
import io.jawg.osmcontributor.model.utils.OpeningTime;
import io.jawg.osmcontributor.ui.adapters.OpeningMonthAdapter;
import io.jawg.osmcontributor.ui.adapters.item.TagItem;
import io.jawg.osmcontributor.ui.adapters.parser.OpeningTimeValueParser;
import io.jawg.osmcontributor.ui.adapters.parser.ParserManager;
import io.jawg.osmcontributor.ui.utils.views.DividerItemDecoration;
import io.jawg.osmcontributor.ui.utils.views.holders.TagItemConstantViewHolder;
import io.jawg.osmcontributor.ui.utils.views.holders.TagItemOpeningTimeViewHolder;

/**
 * Created by loicortola on 05/07/2017.
 */

public class OpeningHoursViewBinder implements TagViewBinder<TagItemOpeningTimeViewHolder> {

    @Inject
    private OpeningTimeValueParser openingTimeValueParser;

    private WeakReference<Activity> activity;

    public OpeningHoursViewBinder(Activity activity) {
        ((OsmTemplateApplication) activity.getApplication()).getOsmTemplateComponent().inject(this);
        this.activity = new WeakReference<>(activity);
    }

    @Override
    public boolean supports(TagItem.Type type) {
        return TagItem.Type.OPENING_HOURS.equals(type);
    }

    @Override
    public void onBindViewHolder(TagItemOpeningTimeViewHolder holder, TagItem tag) {
        holder.getTextViewKey().setText(ParserManager.parseTagName(tag.getKey()));

        OpeningTime openingTime = null;
        try {
            openingTime = openingTimeValueParser.fromValue(tag.getValue());
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            tag.setConform(false);
        }

        if (openingTime == null) {
            openingTime = new OpeningTime();
        }

        final OpeningMonthAdapter adapter = new OpeningMonthAdapter(openingTime, activity.get());
        adapter.setTime(tag.getValue());
        if (tag.getTagType() == TagItem.Type.TIME) {
            adapter.hideMonth(true);
        }
        holder.getOpeningTimeRecyclerView().setAdapter(adapter);
        holder.getOpeningTimeRecyclerView().setLayoutManager(new LinearLayoutManager(activity.get()));
        holder.getOpeningTimeRecyclerView().setHasFixedSize(false);
        RecyclerView.ItemAnimator itemAnimator = new DefaultItemAnimator();
        itemAnimator.setAddDuration(300);
        holder.getOpeningTimeRecyclerView().setItemAnimator(itemAnimator);
        holder.getOpeningTimeRecyclerView().addItemDecoration(new DividerItemDecoration(activity.get()));

        final OpeningTime finalOpeningTime = openingTime;
        holder.getAddButton().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finalOpeningTime.addOpeningMonth(new OpeningMonth());
                adapter.notifyItemInserted(adapter.getItemCount() - 1);
            }
        });

        if (!tag.isConform() && holder.getContent().getChildAt(1).getId() != R.id.malformated_layout) {
            holder.getContent().addView(LayoutInflater.from(activity.get()).inflate(
                    R.layout.malformated_layout, holder.getContent(), false), 1);
            String currentValue = activity.get().getString(R.string.malformated_value) + " " + tag.getValue();
            ((TextView) ((LinearLayout) holder.getContent().getChildAt(1)).getChildAt(1)).setText(currentValue);
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent) {
        View poiTagImposedLayout = LayoutInflater.from(parent.getContext()).inflate(R.layout.tag_item_constant, parent, false);
        return new TagItemConstantViewHolder(poiTagImposedLayout);
    }
}
