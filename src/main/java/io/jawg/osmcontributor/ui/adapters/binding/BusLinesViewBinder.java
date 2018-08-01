package io.jawg.osmcontributor.ui.adapters.binding;

import android.app.Activity;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import javax.inject.Inject;

import io.jawg.osmcontributor.OsmTemplateApplication;
import io.jawg.osmcontributor.R;
import io.jawg.osmcontributor.model.entities.Poi;
import io.jawg.osmcontributor.model.entities.relation_display.RelationDisplay;
import io.jawg.osmcontributor.model.entities.relation_save.RelationEdition;
import io.jawg.osmcontributor.ui.adapters.BusLineAdapter;
import io.jawg.osmcontributor.ui.adapters.item.shelter.TagItem;
import io.jawg.osmcontributor.ui.adapters.parser.BusLineRelationDisplayParser;
import io.jawg.osmcontributor.ui.adapters.parser.BusLineValueParserImpl;
import io.jawg.osmcontributor.ui.adapters.parser.ParserManager;
import io.jawg.osmcontributor.ui.dialogs.AddBusLinePoiDialogFragment;
import io.jawg.osmcontributor.ui.managers.RelationManager;
import io.jawg.osmcontributor.ui.utils.views.DividerItemDecoration;
import io.jawg.osmcontributor.ui.utils.views.holders.TagItemBusLineViewHolder;
import io.jawg.osmcontributor.utils.edition.RelationDisplayDto;

public class BusLinesViewBinder extends CheckedTagViewBinder<TagItemBusLineViewHolder, TagItem> {

    @Inject
    BusLineValueParserImpl busLineValueParser;

    @Inject
    BusLineRelationDisplayParser busLineRelationDisplayParser;

    @Inject
    RelationManager relationManager;

    private static final int NB_RELATIONS_TO_DISPLAY = 3;
    private static final String TAG_REF = "ref";

    private Poi poi;

    public BusLinesViewBinder(Activity activity, TagItemChangeListener tagItemChangeListener, Poi poi) {
        super(activity, tagItemChangeListener);
        this.poi = poi;
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
        final List<RelationDisplay> busLines = relationManager.getRelationDisplaysFromRelationsIDs(poi.getRelationIds());

        BusLineAdapter adapter = new BusLineAdapter(busLines, busLineRelationDisplayParser);

        RecyclerView recyclerView = holder.getBusLineRecyclerView();
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(activity.get()));
        recyclerView.setHasFixedSize(false);

        RecyclerView.ItemAnimator itemAnimator = new DefaultItemAnimator();
        itemAnimator.setAddDuration(300);
        recyclerView.setItemAnimator(itemAnimator);
        recyclerView.addItemDecoration(new DividerItemDecoration(activity.get()));

        adapter.setRemoveBusListener((lineRemoved) -> onRelationItemChange(new Pair<>(lineRemoved, RelationEdition.RelationModificationType.REMOVE_MEMBER)));

        // TODO don't call bdd on UI thread
        final List<RelationDisplay> nearestBusLines = relationManager.getBusLinesOrderedByDistanceFromPoiById(poi.getId());

        HashSet<RelationDisplay> relationDisplaySet = new HashSet<>(busLines);

        holder.getEditAddButton().setOnClickListener(
                view -> {
                    List<RelationDisplay> nearestBusLinesFiltered = new ArrayList<>();

                    int nbBusLines = 0;
                    boolean isBusLineToAdd;
                    for (RelationDisplay nearestBusLine : nearestBusLines) {

                        // Check if the busLine is already in relations
                        if (isBusLineToAdd = !relationDisplaySet.contains(nearestBusLine)) {
                            RelationDisplayDto nearestBusLineDto = new RelationDisplayDto(nearestBusLine);

                            for (RelationDisplay busLine : busLines) {
                                RelationDisplayDto busLineDto = new RelationDisplayDto(busLine);

                                // Check if the busLine 'ref' tag corresponding to an existing busline ref in relations
                                if (nearestBusLineDto.getTag(TAG_REF).equals(busLineDto.getTag(TAG_REF))) {
                                    isBusLineToAdd = false;
                                    break;
                                }
                            }
                        }

                        if (isBusLineToAdd) {
                            nearestBusLinesFiltered.add(nearestBusLine);
                            nbBusLines++;

                            if (nbBusLines == NB_RELATIONS_TO_DISPLAY) {
                                break;
                            }
                        }
                    }

                    AddBusLinePoiDialogFragment.display(
                            ((AppCompatActivity) activity.get()).getSupportFragmentManager(),
                            relationManager,
                            busLineRelationDisplayParser,
                            poi,
                            busLines,
                            nearestBusLinesFiltered,
                            (busLine) -> addBusLine(busLines, adapter, new Pair<>(busLine, RelationEdition.RelationModificationType.ADD_MEMBER)));
                }
        );
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
