package io.jawg.osmcontributor.ui.adapters.binding;

import android.app.Activity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;

import java.util.Collection;
import java.util.List;

import javax.inject.Inject;

import io.jawg.osmcontributor.OsmTemplateApplication;
import io.jawg.osmcontributor.R;
import io.jawg.osmcontributor.model.entities.RelationId;
import io.jawg.osmcontributor.model.entities.relation_display.RelationDisplay;
import io.jawg.osmcontributor.model.entities.relation_save.RelationEdition;
import io.jawg.osmcontributor.ui.adapters.BusLineAdapter;
import io.jawg.osmcontributor.ui.adapters.BusLineSuggestionAdapter;
import io.jawg.osmcontributor.ui.adapters.item.shelter.TagItem;
import io.jawg.osmcontributor.ui.adapters.parser.BusLineRelationDisplayParser;
import io.jawg.osmcontributor.ui.adapters.parser.BusLineValueParserImpl;
import io.jawg.osmcontributor.ui.adapters.parser.ParserManager;
import io.jawg.osmcontributor.ui.managers.RelationManager;
import io.jawg.osmcontributor.ui.utils.views.DividerItemDecoration;
import io.jawg.osmcontributor.ui.utils.views.holders.TagItemBusLineViewHolder;

public class BusLinesViewBinder extends CheckedTagViewBinder<TagItemBusLineViewHolder, TagItem> {

    @Inject
    BusLineValueParserImpl busLineValueParser;

    @Inject
    BusLineRelationDisplayParser busLineRelationDisplayParser;

    @Inject
    RelationManager relationManager;

    private AutoCompleteTextView modelText;
    private Collection<RelationId> backendRelationIds;

    public BusLinesViewBinder(Activity activity, TagItemChangeListener tagItemChangeListener, Collection<RelationId> relationIds) {
        super(activity, tagItemChangeListener);
        this.backendRelationIds = relationIds;
        ((OsmTemplateApplication) activity.getApplication()).getOsmTemplateComponent().inject(this);
    }

    @Override
    public boolean supports(TagItem.Type type) {
        return TagItem.Type.BUS_LINE.equals(type);
    }

    @Override
    public void onBindViewHolder(TagItemBusLineViewHolder holder, TagItem tagItem) {
        // Save holder
        this.content = holder.getContent();

        holder.getTextViewKey().setText(ParserManager.parseTagName(tagItem.getKey(), holder.getContent().getContext()));

        //todo don't call bdd on UI thread
        final List<RelationDisplay> busLines = relationManager.getRelationDisplaysFromRelationsIDs(backendRelationIds);

        BusLineAdapter adapter = new BusLineAdapter(busLines, busLineRelationDisplayParser);

        RecyclerView recyclerView = holder.getBusLineRecyclerView();
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(activity.get()));
        recyclerView.setHasFixedSize(false);
        RecyclerView.ItemAnimator itemAnimator = new DefaultItemAnimator();
        itemAnimator.setAddDuration(300);
        recyclerView.setItemAnimator(itemAnimator);
        recyclerView.addItemDecoration(new DividerItemDecoration(activity.get()));
        adapter.setRemoveBusListener((lineRemoved) -> {
            onRelationItemChange(new Pair<>(lineRemoved, RelationEdition.RelationModificationType.REMOVE_MEMBER));
        });

      /*  holder.getEditAddButton().setOnClickListener(
                view -> {
                    String lineValue = holder.getTextViewValue().getText().toString();
                    if (!lineValue.trim().equals("")) {
                        addBusLine(tagItem, busLines, adapter, lineValue);
                    }
                    holder.getTextViewValue().getText().clear();
                });*/


        BusLineSuggestionAdapter suggestionAdapter = new BusLineSuggestionAdapter(activity.get().getApplicationContext(), relationManager, busLineRelationDisplayParser, busLines);
        modelText = holder.getTextViewValue();
        modelText.setAdapter(suggestionAdapter);
        modelText.setOnItemClickListener((parent, view, position, id) -> {
            addBusLine(busLines, adapter, new Pair<>(suggestionAdapter.getItem(position), RelationEdition.RelationModificationType.ADD_MEMBER));
            holder.getTextViewValue().getText().clear();
        });
        //  modelText.setDropDownHeight((int) (120 * activity.get().getApplication().getResources().getDisplayMetrics().density));
    }

    private void addBusLine(List<RelationDisplay> busLines, BusLineAdapter adapter, Pair<RelationDisplay, RelationEdition.RelationModificationType> change) {
        busLines.add(change.first);
        adapter.notifyItemInserted(busLines.size() - 1);
        onRelationItemChange(change);
    }

    private void onRelationItemChange(Pair<RelationDisplay, RelationEdition.RelationModificationType> change) {
        if (tagItemChangeListener != null) {
            tagItemChangeListener.onRelationForBusUpdated(change);
        }
    }

    @Override
    public TagItemBusLineViewHolder onCreateViewHolder(ViewGroup parent) {
        View poiTagOpeningHoursLayout = LayoutInflater.from(parent.getContext()).inflate(R.layout.tag_item_bus_line, parent, false);
        return new TagItemBusLineViewHolder(poiTagOpeningHoursLayout);
    }

}
