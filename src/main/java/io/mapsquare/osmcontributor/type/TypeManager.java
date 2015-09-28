/**
 * Copyright (C) 2015 eBusiness Information
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
package io.mapsquare.osmcontributor.type;

import android.support.design.widget.Snackbar;
import android.view.View.OnClickListener;

import javax.inject.Inject;
import javax.inject.Singleton;

import de.greenrobot.event.EventBus;
import io.mapsquare.osmcontributor.R;
import io.mapsquare.osmcontributor.core.PoiManager;
import io.mapsquare.osmcontributor.core.model.PoiType;
import io.mapsquare.osmcontributor.core.model.PoiTypeTag;
import io.mapsquare.osmcontributor.type.event.BasePoiTagEvent;
import io.mapsquare.osmcontributor.type.event.BasePoiTypeEvent;
import io.mapsquare.osmcontributor.type.event.PoiTagCreatedEvent;
import io.mapsquare.osmcontributor.type.event.PoiTagDeletedEvent;
import io.mapsquare.osmcontributor.type.event.PoiTypeCreatedEvent;
import io.mapsquare.osmcontributor.type.event.PoiTypeDeletedEvent;
import timber.log.Timber;


@Singleton
public class TypeManager {

    private EventBus bus;
    private PoiManager poiManager;

    private final Object lock = new Object();
    private Snackbar lastSnackBar;

    @Inject
    public TypeManager(EventBus bus, PoiManager poiManager) {
        this.bus = bus;
        this.poiManager = poiManager;
    }

    // ********************************
    // ************ Events ************
    // ********************************

    public void onEventBackgroundThread(InternalSavePoiTypeEvent event) {
        PoiType poiType = event.getPoiType();
        poiManager.savePoiType(poiType);
        bus.post(new PoiTypeCreatedEvent(poiType));
    }

    public void onEventBackgroundThread(InternalSavePoiTagEvent event) {
        PoiTypeTag poiTypeTag = event.getPoiTypeTag();
        PoiType poiType = poiTypeTag.getPoiType();
        poiManager.savePoiType(poiType);
        bus.post(new PoiTagCreatedEvent(poiTypeTag));
    }

    public void onEventBackgroundThread(InternalRemovePoiTypeEvent event) {
        PoiType poiType = event.getPoiType();
        poiManager.deletePoiType(poiType);
        Timber.i("Removed poi type %d", poiType.getId());
        bus.post(new PoiTypeDeletedEvent(poiType));
    }

    public void onEventBackgroundThread(InternalRemovePoiTagEvent event) {
        PoiTypeTag poiTypeTag = event.getPoiTypeTag();
        PoiType poiType = poiTypeTag.getPoiType(); // FIXME clone POI type to avoid border effects?
        poiType.getTags().remove(poiTypeTag);
        poiManager.savePoiType(poiType);
        Timber.i("Removed poi tag %d", poiTypeTag.getId());
        bus.post(new PoiTagDeletedEvent(poiTypeTag));
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

    // *********************************
    // ************ private ************
    // *********************************

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
