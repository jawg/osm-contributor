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
package io.mapsquare.osmcontributor.sync;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.greenrobot.event.EventBus;
import io.mapsquare.osmcontributor.core.PoiManager;
import io.mapsquare.osmcontributor.core.database.dao.PoiNodeRefDao;
import io.mapsquare.osmcontributor.core.model.Poi;
import io.mapsquare.osmcontributor.core.model.PoiNodeRef;
import io.mapsquare.osmcontributor.map.events.PleaseLoadEditVectorialTileEvent;
import io.mapsquare.osmcontributor.sync.converter.PoiConverter;
import io.mapsquare.osmcontributor.sync.dto.osm.NodeDto;
import io.mapsquare.osmcontributor.sync.dto.osm.OsmDto;
import io.mapsquare.osmcontributor.sync.dto.osm.WayDto;
import io.mapsquare.osmcontributor.sync.rest.OsmRestClient;
import io.mapsquare.osmcontributor.sync.rest.OverpassRestClient;
import io.mapsquare.osmcontributor.utils.Box;
import retrofit.RetrofitError;
import retrofit.mime.TypedString;
import timber.log.Timber;

/**
 * Implementation of a {@link io.mapsquare.osmcontributor.sync.SyncWayManager} using an OpenStreetMap database as a backend.
 */
public class OSMSyncWayManager implements SyncWayManager {

    OSMProxy osmProxy;
    OverpassRestClient overpassRestClient;
    PoiConverter poiConverter;
    PoiManager poiManager;
    EventBus bus;
    PoiNodeRefDao poiNodeRefDao;
    OsmRestClient osmRestClient;

    public OSMSyncWayManager(OSMProxy osmProxy, OverpassRestClient overpassRestClient, PoiConverter poiConverter, PoiManager poiManager, EventBus bus, PoiNodeRefDao poiNodeRefDao, OsmRestClient osmRestClient) {
        this.osmProxy = osmProxy;
        this.overpassRestClient = overpassRestClient;
        this.poiConverter = poiConverter;
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
        cmplReq.append("(")
                .append(box.getSouth()).append(",")
                .append(box.getWest()).append(",")
                .append(box.getNorth()).append(",")
                .append(box.getEast())
                .append(");");

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
                OsmDto osmDto = overpassRestClient.sendRequest(new TypedString(request));

                List<WayDto> wayDtoList = osmDto.getWayDtoList();

                if (wayDtoList != null && wayDtoList.size() > 0) {
                    Timber.d(" %d ways have been downloaded", wayDtoList.size());
                    List<Poi> poisFromOSM = poiConverter.convertDtosToPois(osmDto.getWayDtoList(), false);
                    poiManager.mergeFromOsmPois(poisFromOSM);
                    poiManager.deleteAllWaysExcept(poisFromOSM);
                    bus.post(new PleaseLoadEditVectorialTileEvent(true));
                } else {
                    Timber.d("No new ways found in the area");
                }
                return null;
            }
        });

        if (!result.isSuccess() && result.getRetrofitError() != null) {
            Timber.e(result.getRetrofitError(), "Retrofit error while trying to download area");
        }
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
                poi.setUpdated(true);

                poiNodeRef.setUpdated(false);
                Long oldId = poiNodeRef.getOldPoiId();
                if(oldId != null){
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

        if (ids != null) {
            OSMProxy.Result<List<Poi>> result = osmProxy.proceed(new OSMProxy.NetworkAction<List<Poi>>() {
                @Override
                public List<Poi> proceed() {
                    List<Poi> pois;
                    OsmDto osmDtoRetrievedNode = osmRestClient.getNode(formatIdList(ids));
                    if (osmDtoRetrievedNode != null) {
                        List<NodeDto> nodeDtoList = osmDtoRetrievedNode.getNodeDtoList();
                        pois = poiConverter.convertDtosToPois(nodeDtoList, false);
                    } else {
                        pois = new ArrayList<>();
                    }
                    return pois;
                }
            });

            if (result.isSuccess()) {
                return result.getResult();
            }

            if (result.getRetrofitError() != null) {
                RetrofitError e = result.getRetrofitError();
                if (e.getResponse() != null && (e.getResponse().getStatus() == 404 || e.getResponse().getStatus() == 410)) {
                    Timber.w("The poi with id %s couldn't be found on OSM", 1);
                }
            }
        }
        return pois;
    }

    /**
     * Transform a list of ids into a string where ids are separated from each over by a ",".
     *
     * @param ids The list of ids to transform.
     * @return The formatted list.
     */
    private String formatIdList(List<Long> ids) {
        String idsStr = "";
        int i = 1;
        for (Long id : ids) {
            if (id != null) {
                idsStr += id;
                if (i < ids.size()) {
                    idsStr += ",";
                }
            }
            i++;
        }
        return idsStr;
    }
}
