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
package io.mapsquare.osmcontributor.sync;

import java.util.ArrayList;
import java.util.List;

import org.greenrobot.eventbus.EventBus;
import io.mapsquare.osmcontributor.model.entities.Poi;
import io.mapsquare.osmcontributor.model.entities.PoiType;
import io.mapsquare.osmcontributor.rest.mappers.PoiStorageConverter;
import io.mapsquare.osmcontributor.sync.dto.poistorage.CreationOrUpdateResult;
import io.mapsquare.osmcontributor.sync.dto.poistorage.PoiDto;
import io.mapsquare.osmcontributor.sync.dto.poistorage.TypeDto;
import io.mapsquare.osmcontributor.rest.events.error.SyncDownloadRetrofitErrorEvent;
import io.mapsquare.osmcontributor.sync.rest.PoiStorageClient;
import io.mapsquare.osmcontributor.utils.Box;
import io.mapsquare.osmcontributor.utils.CollectionUtils;
import io.mapsquare.osmcontributor.utils.Function;
import retrofit.RetrofitError;
import timber.log.Timber;

/**
 * Implementation of {@link io.mapsquare.osmcontributor.sync.Backend} for a PoiStorage backend.
 */
public class PoiStorageBackend implements Backend {

    PoiStorageClient client;

    PoiStorageConverter converter;

    EventBus bus;

    public PoiStorageBackend(PoiStorageClient client, PoiStorageConverter converter, EventBus bus) {
        this.client = client;
        this.converter = converter;
        this.bus = bus;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String initializeTransaction(String comment) {
        // TODO manage auth ?
        return "NO NEED FOR TRANSACTIONID";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Poi> getPoisInBox(Box box) {
        try {
            return CollectionUtils.map(client.getPois(null, box.getNorth(), box.getWest(), box.getSouth(), box.getEast()), new Function<PoiDto, Poi>() {
                @Override
                public Poi apply(PoiDto poiDto) {
                    return converter.convertPoi(poiDto);
                }
            });
        } catch (RetrofitError e) {
            Timber.e(e, "Retrofit error, connection lost; Couldn't download from backend");
            bus.post(new SyncDownloadRetrofitErrorEvent());
            return new ArrayList<>();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Poi getPoiById(String backendId) {
        try {
            return converter.convertPoi(client.getPoi(backendId));
        } catch (RetrofitError e) {
            return null;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CreationResult addPoi(Poi poi, String transactionId) {
        PoiDto poiDto = converter.convertPoiDto(poi);
        try {
            CreationOrUpdateResult result = client.createPoi(poiDto);
            return new CreationResult(ModificationStatus.SUCCESS, result.getBackendId());
        } catch (RetrofitError e) {
            return new CreationResult(ModificationStatus.FAILURE_UNKNOWN, null);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UpdateResult updatePoi(Poi poi, String transactionId) {
        PoiDto poiDto = converter.convertPoiDto(poi);
        try {
            CreationOrUpdateResult result = client.updatePoi(poiDto.getBackendId(), poiDto);
            return new UpdateResult(ModificationStatus.SUCCESS, result.getRevision().toString());
        } catch (RetrofitError e) {
            if (e.getResponse() != null && e.getResponse().getStatus() == 409) {
                return new UpdateResult(ModificationStatus.FAILURE_CONFLICT, null);
            }
            return new UpdateResult(ModificationStatus.FAILURE_UNKNOWN, null);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ModificationStatus deletePoi(Poi poi, String transactionId) {
        try {
            client.deletePoi(poi.getBackendId());
            return ModificationStatus.SUCCESS;
        } catch (RetrofitError e) {
            if (e.getResponse() != null && e.getResponse().getStatus() == 404) {
                return ModificationStatus.FAILURE_NOT_EXISTING;
            }
            return ModificationStatus.FAILURE_UNKNOWN;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<PoiType> getPoiTypes() {
        try {
            return CollectionUtils.map(client.getTypes(), new Function<TypeDto, PoiType>() {
                @Override
                public PoiType apply(TypeDto typeDto) {
                    return converter.convertPoiType(typeDto);
                }
            });
        } catch (RetrofitError e) {
            return new ArrayList<>();
        }
    }
}
