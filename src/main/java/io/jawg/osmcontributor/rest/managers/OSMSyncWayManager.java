/**
 * Copyright (C) 2016 eBusiness Information
 * <p>
 * This file is part of OSM Contributor.
 * <p>
 * OSM Contributor is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * OSM Contributor is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with OSM Contributor.  If not, see <http://www.gnu.org/licenses/>.
 */
package io.jawg.osmcontributor.rest.managers;

import org.greenrobot.eventbus.EventBus;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.jawg.osmcontributor.database.dao.PoiNodeRefDao;
import io.jawg.osmcontributor.model.entities.Poi;
import io.jawg.osmcontributor.model.entities.PoiNodeRef;
import io.jawg.osmcontributor.rest.OSMProxy;
import io.jawg.osmcontributor.rest.clients.OsmRestClient;
import io.jawg.osmcontributor.rest.clients.OverpassRestClient;
import io.jawg.osmcontributor.rest.dtos.osm.NodeDto;
import io.jawg.osmcontributor.rest.dtos.osm.OsmDto;
import io.jawg.osmcontributor.rest.dtos.osm.WayDto;
import io.jawg.osmcontributor.rest.mappers.PoiMapper;
import io.jawg.osmcontributor.ui.events.map.PleaseLoadEditWaysEvent;
import io.jawg.osmcontributor.ui.managers.PoiManager;
import io.jawg.osmcontributor.utils.Box;
import io.jawg.osmcontributor.utils.CollectionUtils;
import retrofit2.Call;
import retrofit2.Response;
import timber.log.Timber;

/**
 * Implementation of a {@link SyncWayManager} using an OpenStreetMap database as a backend.
 */
public class OSMSyncWayManager implements SyncWayManager {

    OSMProxy osmProxy;
    OverpassRestClient overpassRestClient;
    PoiMapper poiMapper;
    PoiManager poiManager;
    EventBus bus;
    PoiNodeRefDao poiNodeRefDao;
    OsmRestClient osmRestClient;

    public OSMSyncWayManager(OSMProxy osmProxy, OverpassRestClient overpassRestClient, PoiMapper poiMapper, PoiManager poiManager, EventBus bus, PoiNodeRefDao poiNodeRefDao, OsmRestClient osmRestClient) {
        this.osmProxy = osmProxy;
        this.overpassRestClient = overpassRestClient;
        this.poiMapper = poiMapper;
        this.poiManager = poiManager;
        this.bus = bus;
        this.poiNodeRefDao = poiNodeRefDao;
        this.osmRestClient = osmRestClient;
    }

    /**
     * Build the Overpass request to download all the ways included in the bounds.
     *
     * @param box The bounds of the request.
     * @return The Overpass request.
     */
    private String generateOverpassRequestForWay(Box box) {
        StringBuilder cmplReq = new StringBuilder("(way");

        // bounding box
        cmplReq.append(box.osmFormat());

        cmplReq.append(");out meta geom;");
        return cmplReq.toString();
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void syncDownloadWay(final Box box) {
        Timber.d("Requesting overpass for ways");

        OSMProxy.Result<Void> result = osmProxy.proceed(new OSMProxy.NetworkAction<Void>() {
            @Override
            public Void proceed() {
                String request = generateOverpassRequestForWay(box);
                Call<OsmDto> call = overpassRestClient.sendRequest(request);

                try {
                    Response<OsmDto> response = call.execute();
                    if (response.isSuccessful()) {
                        OsmDto osmDto = response.body();
                        if (osmDto != null) {
                            List<WayDto> wayDtoList = osmDto.getWayDtoList();
                            if (wayDtoList != null && wayDtoList.size() > 0) {
                                Timber.d(" %d ways have been downloaded", wayDtoList.size());
                                List<Poi> poisFromOSM = poiMapper.convertDtosToPois(osmDto.getWayDtoList(), false);
                                poiManager.mergeFromOsmPois(poisFromOSM, box);
                                poiManager.deleteAllWaysExcept(poisFromOSM);
                                bus.post(new PleaseLoadEditWaysEvent(true));
                            }
                        }
                    }
                } catch (IOException e) {
                    Timber.e(e, e.getMessage());
                }
                Timber.d("No new ways found in the area");
                return null;
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Poi> downloadPoiForWayEdition(List<Long> ids) {
        //get noderefs to update
        List<PoiNodeRef> poiNodeRefs = poiNodeRefDao.queryByPoiNodeRefIds(ids);

        //get Pois corresponding to noderefs to update
        List<Poi> pois = getPoiWaysToUpdate();

        List<PoiNodeRef> poiNodeRefsToSave = new ArrayList<>();

        Map<String, PoiNodeRef> poiNodeRefMap = new HashMap<>();
        for (PoiNodeRef poiNodeRef : poiNodeRefs) {
            poiNodeRefMap.put(poiNodeRef.getNodeBackendId(), poiNodeRef);
        }

        //apply new lat lng to poi
        for (Poi poi : pois) {
            PoiNodeRef poiNodeRef = poiNodeRefMap.get(poi.getBackendId());
            if (poiNodeRef != null) {
                poi.setLatitude(poiNodeRef.getLatitude());
                poi.setLongitude(poiNodeRef.getLongitude());
                poi.setDetailsUpdated(true);

                poiNodeRef.setUpdated(false);
                Long oldId = poiNodeRef.getOldPoiId();
                if (oldId != null) {
                    poiNodeRefDao.deleteById(oldId);
                }
                poiNodeRef.setOldPoiId(null);
                poiNodeRefsToSave.add(poiNodeRef);
            }
        }

        //save changes
        pois = poiManager.savePois(pois);
        poiManager.savePoiNodeRefs(poiNodeRefsToSave);

        return pois;
    }

    /**
     * Download from backend all the NodeRefs to update as POIs.
     *
     * @return The list of POIs to update.
     */
    private List<Poi> getPoiWaysToUpdate() {
        final List<Long> ids = poiNodeRefDao.queryAllUpdated();
        List<Poi> pois = new ArrayList<>();

        if (ids != null && !ids.isEmpty()) {
            Call<OsmDto> callOsm = osmRestClient.getNode(CollectionUtils.formatIdList(ids));
            try {
                Response<OsmDto> response = callOsm.execute();
                if (response.isSuccessful()) {
                    OsmDto osmDto = response.body();
                    if (osmDto != null) {
                        List<NodeDto> nodeDtoList = osmDto.getNodeDtoList();
                        pois = poiMapper.convertDtosToPois(nodeDtoList, false);
                    }
                    return pois;
                }
            } catch (IOException e) {
                Timber.e(e, e.getMessage());
            }
            Timber.w("The poi with id %s couldn't be found on OSM", 1);
        }
        return pois;
    }

}
