package io.jawg.osmcontributor.ui.adapters.binding;

import android.app.Activity;
import android.content.Context;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import io.jawg.osmcontributor.OsmTemplateApplication;
import io.jawg.osmcontributor.R;
import io.jawg.osmcontributor.model.entities.relation_display.RelationDisplay;
import io.jawg.osmcontributor.model.entities.relation_save.RelationEdition;
import io.jawg.osmcontributor.ui.adapters.BusLineAdapter;
import io.jawg.osmcontributor.ui.adapters.item.shelter.TagItem;
import io.jawg.osmcontributor.ui.adapters.parser.BusLineRelationDisplayParser;
import io.jawg.osmcontributor.ui.adapters.parser.BusLineValueParserImpl;
import io.jawg.osmcontributor.ui.adapters.parser.ParserManager;
import io.jawg.osmcontributor.ui.dialogs.AddPoiBusLineDialogFragment;
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

    private static final String TAG = BusLinesViewBinder.class.getName();
    private static final String TAG_REF = "ref";

    private List<RelationDisplay> currentBusLines = new ArrayList<>();
    private List<RelationDisplay> busLinesNearby = new ArrayList<>();

    public BusLinesViewBinder(Activity activity, TagItemChangeListener tagItemChangeListener) {
        super(activity, tagItemChangeListener);
        ((OsmTemplateApplication) activity.getApplication()).getOsmTemplateComponent().inject(this);
    }

    @Override
    public boolean supports(TagItem.Type type) {
        return TagItem.Type.BUS_LINE.equals(type);
    }

    @Override
    public TagItemBusLineViewHolder onCreateViewHolder(ViewGroup parent) {
        View poiTagOpeningHoursLayout = LayoutInflater.from(parent.getContext()).inflate(R.layout.tag_item_bus_line, parent, false);
        return new TagItemBusLineViewHolder(poiTagOpeningHoursLayout);
    }

    @Override
    public void onBindViewHolder(TagItemBusLineViewHolder holder, TagItem tagItem) {
        // Save holder
        this.content = holder.getContent();

        holder.getTextViewKey().setText(ParserManager.parseTagName(tagItem.getKey(), holder.getContent().getContext()));

        BusLineAdapter adapter = new BusLineAdapter(activity.get().getBaseContext(), currentBusLines, busLineRelationDisplayParser);

        adapter.setRemoveBusListener((busLine, position) -> {
            removeBusLine(adapter, busLine, position);
            cancelBusLinesUpdate(
                    activity.get().getBaseContext(), holder.getBusLineRecyclerView(),
                    v -> adapter.addItem(position, busLine),
                    busLine, R.string.item_removed);
        });

        RecyclerView.ItemAnimator itemAnimator = new DefaultItemAnimator();
        itemAnimator.setAddDuration(300);

        RecyclerView recyclerView = holder.getBusLineRecyclerView();
        recyclerView.setAdapter(adapter);
        recyclerView.setItemAnimator(itemAnimator);
        recyclerView.setHasFixedSize(false);
        recyclerView.setLayoutManager(new LinearLayoutManager(activity.get()));

        DividerItemDecoration dividerItemDecoration =
                new DividerItemDecoration(recyclerView.getContext(), R.drawable.bus_line_divider);
        recyclerView.addItemDecoration(dividerItemDecoration);

        onSwipeBusLine(recyclerView, adapter, currentBusLines);

        holder.getEditAddLayout().setOnClickListener(
                view -> {
                    List<RelationDisplay> filteredBusLinesNearby = new ArrayList<>();
                    boolean addItem;

                    for (RelationDisplay busLineNearby : busLinesNearby) {
                        final String tagRefValNearby = new RelationDisplayDto(busLineNearby).getTagValue(TAG_REF);
                        addItem = true;

                        for (RelationDisplay currentBusLine : currentBusLines) {
                            final String tagRefValCurrent = new RelationDisplayDto(currentBusLine).getTagValue(TAG_REF);

                            // If the list "currentBusLines" already contains the bus line "busLineNearby"
                            // or a bus line whose tag value "REF" is equal,
                            // busLineNearby is not added to the list "filteredBusLinesNearby"
                            if (currentBusLine.equals(busLineNearby) || tagRefValCurrent.equals(tagRefValNearby)) {
                                addItem = false;
                                break;
                            }
                        }
                        if (addItem) {
                            filteredBusLinesNearby.add(busLineNearby);
                        }
                    }

                    AddPoiBusLineDialogFragment frag =
                            AddPoiBusLineDialogFragment.newInstance(currentBusLines, filteredBusLinesNearby);

                    frag.setAddBusLineListener(busLine -> {
                        addBusLine(adapter, busLine, currentBusLines.size());
                        cancelBusLinesUpdate(
                                activity.get().getBaseContext(), holder.getBusLineRecyclerView(),
                                v -> adapter.removeItem(currentBusLines.indexOf(busLine)),
                                busLine, R.string.item_added);
                    });

                    frag.show(activity.get().getFragmentManager(), TAG);
                }
        );
    }

    private void addBusLine(BusLineAdapter adapter, RelationDisplay busLine, int position) {
        adapter.addItem(position, busLine);
        onRelationItemChange(new Pair<>(busLine, RelationEdition.RelationModificationType.ADD_MEMBER));
    }

    private void removeBusLine(BusLineAdapter adapter, RelationDisplay busLine, int position) {
        adapter.removeItem(position);
        onRelationItemChange(new Pair<>(busLine, RelationEdition.RelationModificationType.REMOVE_MEMBER));
    }

    private void onRelationItemChange(Pair<RelationDisplay, RelationEdition.RelationModificationType> change) {
        if (tagItemChangeListener != null) {
            tagItemChangeListener.onRelationForBusUpdated(change);
        }
    }

    /**
     * Action when swipping bus line item
     * @param recyclerView  Bus line recyclerView
     * @param adapter       Bus lines adapter
     * @param busLines      List of bus lines
     */
    private void onSwipeBusLine(RecyclerView recyclerView, BusLineAdapter adapter, List<RelationDisplay> busLines) {
        ItemTouchHelper.SimpleCallback itemTouchHelperCallback = new ItemTouchHelper
                .SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {

            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                final int position = viewHolder.getAdapterPosition();
                final RelationDisplay busLine = busLines.get(position);

                removeBusLine(adapter, busLine, position);
                cancelBusLinesUpdate(
                        activity.get().getBaseContext(), viewHolder.itemView,
                        v -> adapter.addItem(position, busLine),
                        busLine, R.string.item_removed);
            }
        };
        new ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(recyclerView);
    }

    /**
     * Display a snackbar allowing to cancel deleting a bus lines from the list of bus lines
     * @param context       Context
     * @param view          View to find a parent from
     * @param listener      Performed action listener
     * @param busLine       Removed bus line
     * @param actionText    Message of performed action
     */
    private void cancelBusLinesUpdate(Context context, View view, View.OnClickListener listener,
                                      RelationDisplay busLine, int actionText) {
        final String busLineText =
                busLineRelationDisplayParser.getBusLineNetwork(busLine) + " " +
                busLineRelationDisplayParser.getBusLineRef(busLine);

        final CharSequence message = context.getString(actionText, busLineText);

        Snackbar snackbar = Snackbar.make(view, message, Snackbar.LENGTH_LONG);
        snackbar.setAction(context.getString(R.string.undo), listener);
        snackbar.setActionTextColor(context.getResources().getColor(R.color.material_blue_500));
        snackbar.show();
    }

    public void setCurrentBusLines(List<RelationDisplay> currentBusLines) {
        this.currentBusLines = currentBusLines;
    }

    public void setBusLinesNearby(List<RelationDisplay> busLinesNearby) {
        this.busLinesNearby = busLinesNearby;
    }
}
