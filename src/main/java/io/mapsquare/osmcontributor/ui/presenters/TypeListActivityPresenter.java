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
package io.mapsquare.osmcontributor.ui.presenters;

import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.view.View;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.Collection;
import java.util.List;

import javax.inject.Inject;

import io.mapsquare.osmcontributor.OsmTemplateApplication;
import io.mapsquare.osmcontributor.ui.managers.PoiManager;
import io.mapsquare.osmcontributor.database.helper.DatabaseHelper;
import io.mapsquare.osmcontributor.model.entities.PoiType;
import io.mapsquare.osmcontributor.model.entities.PoiTypeTag;
import io.mapsquare.osmcontributor.ui.activities.TypeListActivity;
import io.mapsquare.osmcontributor.ui.adapters.PoiTypeAdapter;
import io.mapsquare.osmcontributor.ui.adapters.PoiTypeTagAdapter;
import io.mapsquare.osmcontributor.ui.events.type.PleaseSavePoiTag;
import io.mapsquare.osmcontributor.ui.events.type.PoiTagCreatedEvent;
import io.mapsquare.osmcontributor.ui.events.type.PoiTagDeletedEvent;
import io.mapsquare.osmcontributor.ui.events.type.PoiTagsUpdatedEvent;
import io.mapsquare.osmcontributor.ui.events.type.PoiTypeCreatedEvent;
import io.mapsquare.osmcontributor.ui.events.type.PoiTypeDeletedEvent;
import io.mapsquare.osmcontributor.ui.fragments.EditPoiTagDialogFragment;
import io.mapsquare.osmcontributor.ui.fragments.EditPoiTypeNameDialogFragment;
import io.mapsquare.osmcontributor.ui.managers.TypeManager;
import timber.log.Timber;

public class TypeListActivityPresenter {

    private static final String BUNDLE_CURRENT_POI_TYPE = "poi_type:current:id";

    private final TypeListActivity typeListActivity;
    private PoiType currentPoiType;
    private Long poiTypeIdFromSavedState;

    @Inject
    EventBus bus;

    @Inject
    TypeManager typeManager;

    @Inject
    PoiManager poiManager;

    public TypeListActivityPresenter(TypeListActivity typeListActivity, Bundle savedInstanceState) {
        ((OsmTemplateApplication) typeListActivity.getApplication()).getOsmTemplateComponent().inject(this);
        this.typeListActivity = typeListActivity;

        if (savedInstanceState != null) {
            long id = savedInstanceState.getLong(BUNDLE_CURRENT_POI_TYPE, -1);
            if (id != -1) {
                poiTypeIdFromSavedState = id;
            }
        }
    }

    public void onResume() {
        bus.register(this);
        bus.post(new InternalPleaseLoadEvent(poiTypeIdFromSavedState));
        poiTypeIdFromSavedState = null;
    }

    public void onSaveInstanceState(Bundle outState) {
        if (currentPoiType != null) {
            outState.putLong(BUNDLE_CURRENT_POI_TYPE, currentPoiType.getId());
        }
    }

    public void onPause() {
        typeManager.finishLastDeletionJob();
        bus.unregister(this);
    }

    public boolean onBackPressed() {
        if (currentPoiType != null) {
            typeManager.finishLastDeletionJob();
            bus.post(new InternalPleaseLoadEvent());
            return true;
        } else {
            return false;
        }
    }

    public PoiTypeAdapter.PoiTypeAdapterListener getPoiTypeAdapterListener() {
        return new PoiTypeAdapter.PoiTypeAdapterListener() {
            @Override
            public void onItemClick(final PoiType item) {
                currentPoiType = item;
                typeManager.finishLastDeletionJob();
                bus.post(new InternalPleaseLoadEvent(item.getId()));
            }

            @Override
            public void onItemLongClick(final PoiType item) {
                EditPoiTypeNameDialogFragment.display(typeListActivity.getSupportFragmentManager(), item);
            }

            @Override
            public void onItemRemoved(final PoiType item) {
                Snackbar snackbar = typeListActivity.createSnackbar(item.getName());
                typeManager.deletePoiTypeDelayed(item, snackbar, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        typeListActivity.undoPoiTypeRemoval();
                        Timber.i("Undone poi type %d removal", item.getId());
                    }
                });
            }
        };
    }

    public PoiTypeTagAdapter.PoiTypeTagAdapterListener getPoiTypeTagAdapterListener() {
        return new PoiTypeTagAdapter.PoiTypeTagAdapterListener() {
            @Override
            public void onItemClick(PoiTypeTag item) {
                EditPoiTagDialogFragment.display(typeListActivity.getSupportFragmentManager(), item);
            }

            @Override
            public void onItemLongClick(PoiTypeTag item) {
                EditPoiTagDialogFragment.display(typeListActivity.getSupportFragmentManager(), item);
            }

            @Override
            public void onItemRemoved(final PoiTypeTag item) {
                Snackbar snackbar = typeListActivity.createSnackbar(item.getKey());
                typeManager.deletePoiTagDelayed(item, snackbar, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        typeListActivity.undoPoiTypeTagRemoval();
                        Timber.i("Undone poi tag %d removal", item.getId());
                    }
                });
            }

            @Override
            public void onItemMoved(PoiTypeTag item, int fromPosition, int toPosition) {
                int first, last, offset;
                if (fromPosition < toPosition) {
                    first = fromPosition;
                    last = toPosition;
                    offset = -1;
                } else if (fromPosition > toPosition) {
                    first = toPosition;
                    last = fromPosition;
                    offset = 1;
                } else {
                    return;
                }

                Timber.d("Moving poi tags within [%d ; %d]", first, last);

                for (PoiTypeTag tag : currentPoiType.getTags()) {
                    if (!item.equals(tag)) {
                        int ordinal = tag.getOrdinal();
                        if (ordinal >= first && ordinal <= last) {
                            int newOrdinal = ordinal + offset;
                            tag.setOrdinal(newOrdinal);
                            Timber.d("Moved poi tag %d from %d to %d", tag.getId(), ordinal, newOrdinal);
                        }
                    }
                }

                item.setOrdinal(toPosition);
                Timber.d("Moved poi tag %d from %d to %d", item.getId(), fromPosition, toPosition);

                typeManager.updatePoiTags(currentPoiType);
            }
        };
    }

    /* ========== POI type/tag loading ========== */

    private static class InternalPleaseLoadEvent {
        private final Long currentPoiTypeId;

        public InternalPleaseLoadEvent() {
            this(null);
        }

        public InternalPleaseLoadEvent(Long currentPoiTypeId) {
            this.currentPoiTypeId = currentPoiTypeId;
        }

        public Long getCurrentPoiTypeId() {
            return currentPoiTypeId;
        }
    }

    @Subscribe(threadMode = ThreadMode.ASYNC)
    public void onInternalPleaseLoadEvent(InternalPleaseLoadEvent event) {
        List<PoiType> poiTypesAlphabeticallySorted = poiManager.getPoiTypesSortedByName();
        for (PoiType type : poiTypesAlphabeticallySorted) {
            type.setTags(DatabaseHelper.loadLazyForeignCollection(type.getTags()));
        }

        PoiType currentPoiType = null;
        Long currentPoiTypeId = event.getCurrentPoiTypeId();
        if (currentPoiTypeId != null) {
            for (PoiType type : poiTypesAlphabeticallySorted) {
                if (currentPoiTypeId.equals(type.getId())) {
                    currentPoiType = type;
                    break;
                }
            }
            if (currentPoiType == null) {
                throw new IllegalStateException("Current POI type not found after reloading data");
            }
        }

        bus.post(new InternalTypesLoadedEvent(poiTypesAlphabeticallySorted, currentPoiType));
    }

    private static class InternalTypesLoadedEvent {
        private final List<PoiType> poiTypes;
        private final PoiType currentPoiType;

        public InternalTypesLoadedEvent(List<PoiType> poiTypes, PoiType currentPoiType) {
            this.poiTypes = poiTypes;
            this.currentPoiType = currentPoiType;
        }

        public List<PoiType> getPoiTypes() {
            return poiTypes;
        }

        public PoiType getCurrentPoiType() {
            return currentPoiType;
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onInternalTypesLoadedEvent(InternalTypesLoadedEvent event) {
        List<PoiType> currentPoiTypes = event.getPoiTypes();
        PoiType currentPoiType = event.getCurrentPoiType();

        if (currentPoiType == null) {
            typeListActivity.showTypes(currentPoiTypes);
        } else {
            typeListActivity.showTags(currentPoiType.getTags(), currentPoiType);
        }

        this.currentPoiType = currentPoiType;
    }

    /* ========== POI type/tag edition ========== */

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onPleaseSavePoiTag(PleaseSavePoiTag event) {
        PoiTypeTag poiTypeTag = null;
        PoiType poiType = currentPoiType;
        if (poiType == null) {
            throw new IllegalStateException("Not currently displaying tags of a specific POI type");
        }

        Long id = event.getId();
        if (id != null) {
            // We edit an existing tag, so find it in the list...
            for (PoiTypeTag tag : poiType.getTags()) {
                if (tag.getId().equals(id)) {
                    poiTypeTag = tag;
                    break;
                }
            }
            if (poiTypeTag == null) {
                throw new IllegalStateException("Edited tag not found in current POI type");
            }
        } else {
            // Create a new tag
            poiTypeTag = new PoiTypeTag();
            poiTypeTag.setPoiType(poiType);
            Collection<PoiTypeTag> tags = poiType.getTags();
            poiTypeTag.setOrdinal(tags.size());
            tags.add(poiTypeTag);
        }

        poiTypeTag.setKey(event.getKey());
        poiTypeTag.setValue(event.getValue());
        poiTypeTag.setMandatory(event.isMandatory());
        typeManager.savePoiTag(poiTypeTag);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onPoiTypeCreatedEvent(PoiTypeCreatedEvent event) {
        typeManager.finishLastDeletionJob();
        typeListActivity.addNewPoiType(event.getPoiType());
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onPoiTagCreatedEvent(PoiTagCreatedEvent event) {
        typeManager.finishLastDeletionJob();
        typeListActivity.addNewPoiTag(event.getPoiTypeTag());
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onPoiTagsUpdatedEvent(PoiTagsUpdatedEvent event) {
        typeManager.finishLastDeletionJob();
        typeListActivity.refreshPoiTag(event.getPoiType());
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onPoiTypeDeletedEvent(PoiTypeDeletedEvent event) {
        typeListActivity.notifyPoiTypeDefinitivelyRemoved(event.getPoiType());
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onPoiTagDeletedEvent(PoiTagDeletedEvent event) {
        typeListActivity.notifyPoiTagDefinitivelyRemoved(event.getPoiTypeTag());
    }
}
