/**
 * Copyright (C) 2016 eBusiness Information
 *
 * This file is part of OSM Contributor.
 *
 * OSM Contributor is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * OSM Contributor is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OSM Contributor.  If not, see <http://www.gnu.org/licenses/>.
 */
package io.jawg.osmcontributor.ui.managers;

import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.view.View.OnClickListener;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.jawg.osmcontributor.R;
import io.jawg.osmcontributor.model.events.PoiTypesLoaded;
import io.jawg.osmcontributor.model.entities.PoiType;
import io.jawg.osmcontributor.model.entities.PoiTypeTag;
import io.jawg.osmcontributor.rest.dtos.osm.CombinationsDto;
import io.jawg.osmcontributor.rest.dtos.osm.CombinationsDataDto;
import io.jawg.osmcontributor.rest.dtos.osm.SuggestionsDto;
import io.jawg.osmcontributor.rest.dtos.osm.Wiki;
import io.jawg.osmcontributor.rest.dtos.osm.WikiDataDto;
import io.jawg.osmcontributor.ui.events.type.BasePoiTagEvent;
import io.jawg.osmcontributor.ui.events.type.BasePoiTypeEvent;
import io.jawg.osmcontributor.ui.events.type.PleaseDownloadPoiTypeSuggestionEvent;
import io.jawg.osmcontributor.ui.events.type.PoiTagCreatedEvent;
import io.jawg.osmcontributor.ui.events.type.PoiTagDeletedEvent;
import io.jawg.osmcontributor.ui.events.type.PoiTagsUpdatedEvent;
import io.jawg.osmcontributor.ui.events.type.PoiTypeCreatedEvent;
import io.jawg.osmcontributor.ui.events.type.PoiTypeDeletedEvent;
import io.jawg.osmcontributor.ui.events.type.PoiTypeSuggestedDownloadedEvent;
import io.jawg.osmcontributor.rest.clients.OsmTagInfoRestClient;
import io.jawg.osmcontributor.utils.StringUtils;
import timber.log.Timber;


@Singleton
public class TypeManager {

    private EventBus bus;
    private PoiManager poiManager;
    private OsmTagInfoRestClient tagInfoRestClient;

    private final Object lock = new Object();
    private Snackbar lastSnackBar;

    private static List<String> tagsGroup = Arrays.asList("aerialway", "aeroway", "amenity", "barrier", "craft", "emergency", "geological",
            "highway", "cycleway", "busway", "historic", "landuse", "leisure", "man_made", "military",
            "natural", "office", "place", "power", "public_transport", "railway", "service", "route",
            "shop", "sport", "tourism", "waterway");

    @Inject
    public TypeManager(EventBus bus, PoiManager poiManager, OsmTagInfoRestClient tagInfoRestClient) {
        this.bus = bus;
        this.poiManager = poiManager;
        this.tagInfoRestClient = tagInfoRestClient;
    }

    // ********************************
    // ************ Events ************
    // ********************************

    @Subscribe(threadMode = ThreadMode.ASYNC)
    public void onInternalSavePoiTypeEvent(InternalSavePoiTypeEvent event) {
        PoiType poiType = event.getPoiType();
        poiManager.savePoiType(poiType);
        bus.post(new PoiTypeCreatedEvent(poiType));
    }

    @Subscribe(threadMode = ThreadMode.ASYNC)
    public void onInternalSavePoiTagEvent(InternalSavePoiTagEvent event) {
        PoiTypeTag poiTypeTag = event.getPoiTypeTag();
        PoiType poiType = poiTypeTag.getPoiType();
        poiManager.savePoiType(poiType);
        bus.post(new PoiTagCreatedEvent(poiTypeTag));
    }

    @Subscribe(threadMode = ThreadMode.ASYNC)
    public void onInternalUpdatePoiTagsEvent(InternalUpdatePoiTagsEvent event) {
        PoiType poiType = event.getPoiType();
        poiManager.savePoiType(poiType);
        bus.post(new PoiTagsUpdatedEvent(poiType));
    }

    @Subscribe(threadMode = ThreadMode.ASYNC)
    public void onInternalRemovePoiTypeEvent(InternalRemovePoiTypeEvent event) {
        PoiType poiType = event.getPoiType();
        poiManager.deletePoiType(poiType);
        Timber.i("Removed poi type %d", poiType.getId());
        bus.post(new PoiTypeDeletedEvent(poiType));
        bus.post(new PoiTypesLoaded(poiManager.getPoiTypesSortedByName()));
    }

    @Subscribe(threadMode = ThreadMode.ASYNC)
    public void onInternalRemovePoiTagEvent(InternalRemovePoiTagEvent event) {
        PoiTypeTag poiTypeTag = event.getPoiTypeTag();
        PoiType poiType = poiTypeTag.getPoiType(); // FIXME clone POI type to avoid border effects?;
        poiType.getTags().remove(poiTypeTag);
        poiManager.savePoiType(poiType);
        Timber.i("Removed poi tag %d", poiTypeTag.getId());
        bus.post(new PoiTagDeletedEvent(poiTypeTag));
    }

    @Subscribe(threadMode = ThreadMode.ASYNC)
    public void onPleaseDownloadPoiTypeSuggestionEvent(PleaseDownloadPoiTypeSuggestionEvent event) {
        bus.post(new PoiTypeSuggestedDownloadedEvent(getPoiTypeSuggested(event.getPoiTypeName())));
    }

    // ********************************
    // ************ public ************
    // ********************************

    /**
     * Create or edit a poi type.
     *
     * @param poiType The poi type to persist
     */
    public void savePoiType(PoiType poiType) {
        bus.post(new InternalSavePoiTypeEvent(poiType));
    }

    /**
     * Create or edit a poi type tag.
     *
     * @param poiTypeTag The poi type tag to persist
     */
    public void savePoiTag(PoiTypeTag poiTypeTag) {
        bus.post(new InternalSavePoiTagEvent(poiTypeTag));
    }


    /**
     * Update the poi tags of a poiType.
     * @param poiType Update the tags of this PoiType.
     */
    public void updatePoiTags(PoiType poiType) {
        bus.post(new InternalUpdatePoiTagsEvent(poiType));
    }

    /**
     * Delete a poi type after a short time.<br/>
     * A snackbar is shown in order to allow the user to cancel the deletion. If a snackbar was already
     * showing, it will be dismissed.
     * <p>
     * The deletion does not take place until the snackbar is dismissed or hidden by another snackbar.<br/>
     * The deletion does not take place if the undo action is clicked.
     * </p>
     *
     * @param item       The poi type to delete
     * @param snackbar   A new snackbar instance
     * @param undoAction The callback used for the undo action
     */
    public void deletePoiTypeDelayed(PoiType item, Snackbar snackbar, OnClickListener undoAction) {
        Timber.d("Delaying poi type %d removal", item.getId());
        displayNewUndoSnackbar(snackbar, undoAction, new DeleteTaskCallback(new InternalRemovePoiTypeEvent(item)));
    }

    /**
     * Delete a poi type tag after a short time.<br/>
     * A snackbar is shown in order to allow the user to cancel the deletion. If a snackbar was already
     * showing, it will be dismissed.
     * <p>
     * The deletion does not take place until the snackbar is dismissed or hidden by another snackbar.<br/>
     * The deletion does not take place if the undo action is clicked.
     * </p>
     *
     * @param item       The poi type tag to delete
     * @param snackbar   A new snackbar instance
     * @param undoAction The callback used for the undo action
     */
    public void deletePoiTagDelayed(PoiTypeTag item, Snackbar snackbar, OnClickListener undoAction) {
        Timber.d("Delaying poi tag %d removal", item.getId());
        displayNewUndoSnackbar(snackbar, undoAction, new DeleteTaskCallback(new InternalRemovePoiTagEvent(item)));
    }

    /**
     * Force the last deletion job to be done: if a snackbar was still visible, it will be dismissed
     * and its task will be executed.
     */
    public void finishLastDeletionJob() {
        synchronized (lock) {
            Snackbar snackbar = lastSnackBar;
            if (snackbar != null && snackbar.isShown()) {
                snackbar.dismiss();
                lastSnackBar = null;
            }
        }
    }

    /**
     * Return poiTypes SuggestionsDto for a given query.
     *
     * @param query          The query.
     * @param page           The page number.
     * @param resultsPerPage The number of results per page.
     * @return The suggestions.
     */
    @Nullable
    public SuggestionsDto getSuggestionsBlocking(String query, Integer page, Integer resultsPerPage) {
        if (!StringUtils.isEmpty(query)) {
            return tagInfoRestClient.getSuggestions(query, page, resultsPerPage);
        }
        return null;
    }

    // *********************************
    // ************ private ************
    // *********************************

    /**
     * Return the PoiType suggested for a given key.
     *
     * @param key The name of the wished PoiType.
     * @return The suggested PoiType.
     */
    private PoiType getPoiTypeSuggested(String key) {
        if (StringUtils.isEmpty(key)) {
            return null;
        }

        Wiki wiki = tagInfoRestClient.getWikiPages(key);
        PoiType poiType = new PoiType();
        poiType.setName(key);
        poiType.setIcon(key);
        poiType.setLastUse(DateTime.now());
        int ordinal = 0;
        List<PoiTypeTag> poiTypeTags = new ArrayList<>();

        // Request for the English wiki and keep the tags of the tags_combination field.
        for (WikiDataDto data : wiki.getDatas()) {
            if ("en".equals(data.getLang())) {
                for (String tagCombination : data.getTagsCombination()) {
                    String[] splitResult = tagCombination.split("=");

                    if (splitResult.length > 1) {
                        poiTypeTags.add(PoiTypeTag.builder()
                                .key(splitResult[0])
                                .value(splitResult[1])
                                .mandatory(true)
                                .poiType(poiType)
                                .ordinal(ordinal++)
                                .build());
                    } else {
                        poiTypeTags.add(PoiTypeTag.builder()
                                .key(tagCombination)
                                .mandatory(false)
                                .poiType(poiType)
                                .ordinal(ordinal++)
                                .build());
                    }
                }
                break;
            }
        }

        // If there was no relevant information in the English wiki, query for tags combinations.
        if (poiTypeTags.size() == 0) {
            CombinationsDto combinationsDto = tagInfoRestClient.getCombinations(key, 1, 5);
            for (CombinationsDataDto data : combinationsDto.getData()) {
                if (tagsGroup.contains(data.getOtherKey())) {
                    poiTypeTags.add(PoiTypeTag.builder()
                            .key(data.getOtherKey())
                            .value(key)
                            .mandatory(true)
                            .poiType(poiType)
                            .ordinal(ordinal++)
                            .build());
                } else {
                    poiTypeTags.add(PoiTypeTag.builder()
                            .key(data.getOtherKey())
                            .mandatory(false)
                            .poiType(poiType)
                            .ordinal(ordinal++)
                            .build());
                }
            }
        }

        poiType.setTags(poiTypeTags);
        return poiType;
    }

    private void displayNewUndoSnackbar(Snackbar snackbar, OnClickListener undoAction, Snackbar.Callback callback) {
        if (snackbar.isShown()) {
            throw new IllegalStateException("Snackbar is already shown");
        }
        snackbar.setAction(R.string.undo, undoAction);
        snackbar.setCallback(callback);

        synchronized (lock) {
            lastSnackBar = snackbar;
            snackbar.show();
        }
    }

    private void onSnackBarDismissed(Snackbar snackbar) {
        synchronized (lock) {
            if (lastSnackBar == snackbar) {
                lastSnackBar = null;
            }
        }
    }

    private class DeleteTaskCallback extends Snackbar.Callback {

        private final DeletionTask task;
        private boolean done;

        public DeleteTaskCallback(DeletionTask task) {
            this.task = task;
        }

        @Override
        public final void onDismissed(Snackbar snackbar, int event) {
            if (!done) {
                done = true;
                if (event != DISMISS_EVENT_ACTION) {
                    bus.post(task);
                }
                onSnackBarDismissed(snackbar);
            }
        }
    }

    private interface DeletionTask {
    }

    private class InternalSavePoiTypeEvent extends BasePoiTypeEvent {
        public InternalSavePoiTypeEvent(PoiType poiType) {
            super(poiType);
        }
    }

    private class InternalSavePoiTagEvent extends BasePoiTagEvent {
        public InternalSavePoiTagEvent(PoiTypeTag poiTypeTag) {
            super(poiTypeTag);
        }
    }

    private class InternalUpdatePoiTagsEvent extends BasePoiTypeEvent {
        public InternalUpdatePoiTagsEvent(PoiType poiType) {
            super(poiType);
        }
    }

    private class InternalRemovePoiTypeEvent extends BasePoiTypeEvent implements DeletionTask {
        public InternalRemovePoiTypeEvent(PoiType poiType) {
            super(poiType);
        }
    }

    private class InternalRemovePoiTagEvent extends BasePoiTagEvent implements DeletionTask {
        public InternalRemovePoiTagEvent(PoiTypeTag poiTypeTag) {
            super(poiTypeTag);
        }
    }
}
