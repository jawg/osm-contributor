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
package io.jawg.osmcontributor.rest;

import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.github.scribejava.core.model.Verb;

import org.greenrobot.eventbus.EventBus;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.jawg.osmcontributor.BuildConfig;
import io.jawg.osmcontributor.database.PoiAssetLoader;
import io.jawg.osmcontributor.database.preferences.LoginPreferences;
import io.jawg.osmcontributor.model.entities.Poi;
import io.jawg.osmcontributor.model.entities.PoiType;
import io.jawg.osmcontributor.model.entities.PoiTypeTag;
import io.jawg.osmcontributor.model.entities.relation.FullOSMRelation;
import io.jawg.osmcontributor.rest.clients.OsmRestClient;
import io.jawg.osmcontributor.rest.clients.OverpassRestClient;
import io.jawg.osmcontributor.rest.dtos.osm.ChangeSetDto;
import io.jawg.osmcontributor.rest.dtos.osm.NodeDto;
import io.jawg.osmcontributor.rest.dtos.osm.OsmDto;
import io.jawg.osmcontributor.rest.dtos.osm.OsmDtoInterface;
import io.jawg.osmcontributor.rest.dtos.osm.TagDto;
import io.jawg.osmcontributor.rest.dtos.osm.WayDto;
import io.jawg.osmcontributor.rest.events.error.SyncUploadRetrofitErrorEvent;
import io.jawg.osmcontributor.rest.mappers.PoiMapper;
import io.jawg.osmcontributor.rest.mappers.RelationMapper;
import io.jawg.osmcontributor.rest.utils.AuthenticationRequestInterceptor;
import io.jawg.osmcontributor.ui.managers.PoiManager;
import io.jawg.osmcontributor.utils.Box;
import io.jawg.osmcontributor.utils.FlavorUtils;
import io.jawg.osmcontributor.utils.upload.PoiLoadWrapper;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;
import timber.log.Timber;

import static java.util.Collections.singletonList;

/**
 * Implementation of a {@link Backend} for an OpenStreetMap backend.
 */
public class OsmBackend implements Backend {

    public static final String[] OBJECT_TYPES = new String[]{"node", "way"};
    private static final String BBOX = "({{bbox}});";
    private PoiManager poiManager;
    private PoiAssetLoader poiAssetLoader;
    private OSMProxy osmProxy;
    private OverpassRestClient overpassRestClient;
    private OsmRestClient osmRestClient;
    private PoiMapper poiMapper;
    private RelationMapper relationMapper;
    private EventBus bus;
    private LoginPreferences loginPreferences;
    private Map<Long, PoiType> poiTypes = null;

    public OsmBackend(LoginPreferences loginPreferences, EventBus bus, OSMProxy osmProxy, OverpassRestClient overpassRestClient,
                      OsmRestClient osmRestClient, PoiMapper poiMapper, RelationMapper relationMapper, PoiManager poiManager, PoiAssetLoader poiAssetLoader) {
        this.loginPreferences = loginPreferences;
        this.bus = bus;
        this.osmProxy = osmProxy;
        this.overpassRestClient = overpassRestClient;
        this.osmRestClient = osmRestClient;
        this.poiMapper = poiMapper;
        this.relationMapper = relationMapper;
        this.poiManager = poiManager;
        this.poiAssetLoader = poiAssetLoader;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String initializeTransaction(String comment) {
        final OsmDto osmDto = new OsmDto();
        ChangeSetDto changeSetDto = new ChangeSetDto();
        List<TagDto> tagDtos = new ArrayList<>();

        osmDto.setChangeSetDto(changeSetDto);
        tagDtos.add(new TagDto(comment, "comment"));
        tagDtos.add(new TagDto(BuildConfig.APP_NAME + " " + BuildConfig.VERSION_NAME, "created_by"));
        changeSetDto.setTagDtoList(tagDtos);

        Call<ResponseBody> callChangeSet;
        if (loginPreferences.retrieveOAuthParams() != null) {
            callChangeSet = osmRestClient.addChangeSet(
                    AuthenticationRequestInterceptor.getOAuthRequest(loginPreferences, BuildConfig.BASE_OSM_URL + "changeset/create", Verb.PUT)
                            .getOAuthHeader(), osmDto);
        } else {
            callChangeSet = osmRestClient.addChangeSet(osmDto);
        }

        try {
            Response<ResponseBody> response = callChangeSet.execute();
            if (response.isSuccessful() && response.body() != null) {
                return response.body().string();
            }
        } catch (IOException e) {
            Timber.e("Retrofit error, couldn't create Changeset!");
        }
        bus.post(new SyncUploadRetrofitErrorEvent(-1L));
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NonNull
    public List<Poi> getPoisInBox(final Box box) throws NetworkException {
        return poiMapper.convertPois(getPoisDtosInBox(box));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NonNull
    public List<PoiLoadWrapper> getPoisDtosInBox(final Box box) throws NetworkException {
        return requestPoisDtosInBox(box);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NonNull
    public List<PoiLoadWrapper> requestPoisDtosInBox(final Box box) throws NetworkException {
        Timber.d("Requesting overpass for download");

        poiTypes = poiManager.loadPoiTypes();

        final Map<Long, PoiType> poiTypeDtoCopy = new HashMap<>(poiTypes);
        final Map<Long, PoiType> poiTypeDtoList = new HashMap<>();
        final List<PoiLoadWrapper> poiLoad = new ArrayList<>();
        final Long idx = 0L;

        for (Map.Entry<Long, PoiType> entry : poiTypeDtoCopy.entrySet()) {
            final PoiType poiTypeDto = entry.getValue();
            poiTypeDtoList.clear();
            poiTypeDtoList.put(idx, poiTypeDto);

            StringBuilder sbRequest = new StringBuilder();
            if (poiTypeDto.getQuery() != null) {
                sbRequest.append(poiTypeDto.getQuery().contains(BBOX) ?
                        poiTypeDto.getQuery().replace(BBOX, box.osmFormat()) :
                        poiTypeDto.getQuery());
            } else if (poiTypeDto.getQuery() == null && FlavorUtils.isBus(poiTypeDto)) {
                sbRequest.append(generateOverpassRequest(box, poiTypeDtoList));
            }

            if (!TextUtils.isEmpty(sbRequest.toString())) {
                final String request = sbRequest.toString();

                OSMProxy.Result<OsmDtoInterface> result = osmProxy.proceed(() -> {
                    try {
                        return FlavorUtils.isBus(poiTypeDto) ?
                                overpassRestClient.sendRequestBlock(request).execute().body() :
                                overpassRestClient.sendRequest(request).execute().body();
                    } catch (IOException e) {
                        return null;
                    }
                });

                if (result != null) {
                    OsmDtoInterface osmDto = result.getResult();
                    if (osmDto != null) {
                        poiLoad.add(new PoiLoadWrapper(osmDto, poiTypeDto));
                    } else {
                        throw new NetworkException();
                    }
                    poiTypes.remove(entry.getKey());
                } else {
                    throw new NetworkException();
                }
            }
        }

        if (!poiTypes.isEmpty()) {
            OSMProxy.Result<OsmDtoInterface> result = osmProxy.proceed(() -> {
                final String request = generateOverpassRequest(box, poiTypes);
                try {
                    return overpassRestClient.sendRequest(request).execute().body();
                } catch (IOException e) {
                    e.printStackTrace();
                    return null;
                }
            });

            if (result != null) {
                OsmDtoInterface osmDto = result.getResult();
                if (osmDto != null) {
                    poiLoad.add(new PoiLoadWrapper(osmDto, null));
                } else {
                    throw new NetworkException();
                }
            } else {
                throw new NetworkException();
            }
        }
        return poiLoad;
    }

    @Override
    public List<OsmDtoInterface> getBusRelationForDisplayInArea(Box box) {
        List<OsmDtoInterface> osmDtos = new ArrayList<>();
        Timber.d("Requesting overpass for downloading bus relations");

        OSMProxy.Result<OsmDtoInterface> result = osmProxy.proceed(() -> {
            try {
                return overpassRestClient.sendRequest(getBusRelationRequest(box)).execute().body();
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        });
        if (result != null) {
            OsmDtoInterface dto = result.getResult();
            if (dto != null) {
                osmDtos.add(dto);
            }
        }
        return osmDtos;
    }

    private String getBusRelationRequest(Box box) {
        String request = "relation[\"route\"=\"bus\"]({{bbox}});out tags;";
        return request.replace(BBOX, box.osmFormat());
    }

    /**
     * Generate a String corresponding to an Overpass request
     * which will call all the POI given by the map.
     *
     * @param box      The coordinates of each direction of the map
     * @param poiTypes Map of poiTypes depending the preset loaded
     * @return A String of the request.
     */
    private String generateOverpassRequest(Box box, Map<Long, PoiType> poiTypes) {
        StringBuilder cmplReq = new StringBuilder("(");

        if (poiTypes.size() > 15) {
            List<String> types = poiManager.loadPoiTypeKeysWithDefaultValues();
            // we've got lots of pois, overpath will struggle with the finer request, download all the pois in the box
            for (String type : OBJECT_TYPES) {
                for (String key : types) {
                    cmplReq.append(type).append("[\"").append(key).append("\"]").append(box.osmFormat());
                }
            }
        } else {
            // Download all the pois in the box who are of one of the PoiType contained in the database
            for (String type : OBJECT_TYPES) {
                // For each poiTypes, add the corresponding part to the request
                for (PoiType poiTypeDto : poiTypes.values()) {
                    // Check for tags who have a value and add a ["key"~"value"] string to the request
                    boolean valid = false;
                    for (PoiTypeTag poiTypeTag : poiTypeDto.getTags()) {
                        if (poiTypeTag.getValue() != null) {
                            if (!valid) {
                                cmplReq.append(type);
                            }
                            valid = true;
                            cmplReq.append("[\"").append(poiTypeTag.getKey()).append("\"~\"").append(poiTypeTag.getValue()).append("\"]");
                        }
                    }
                    // If there was at least one tag with a value, add the box coordinates to the request
                    if (valid) {
                        cmplReq.append(box.osmFormat());
                    }
                }
            }
        }

        cmplReq.append(");out meta center;");
        return cmplReq.toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CreationResult addPoi(final Poi poi, String transactionId) {
        final OsmDto osmDto = new OsmDto();

        if (poi.getWay()) {
            WayDto nodeDto = poiMapper.convertPoiToWayDto(poi, transactionId);
            osmDto.setWayDtoList(singletonList(nodeDto));
        } else {
            NodeDto nodeDto = poiMapper.convertPoiToNodeDto(poi, transactionId);
            osmDto.setNodeDtoList(singletonList(nodeDto));
        }

        Call<ResponseBody> getCall;
        if (poi.getWay()) {
            getCall = osmRestClient.addWay(osmDto);
        } else {
            getCall = osmRestClient.addNode(osmDto);
        }

        try {
            Response<ResponseBody> response = getCall.execute();
            if (response.isSuccessful()) {
                return new CreationResult(ModificationStatus.SUCCESS, response.body().string());
            }
        } catch (IOException e) {
            Timber.e(e, e.getMessage());
        }
        return new CreationResult(ModificationStatus.FAILURE_UNKNOWN, null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UpdateResult updatePoi(final Poi poi, String transactionId) {
        final OsmDto osmDto = new OsmDto();

        if (poi.getWay()) {
            WayDto nodeDto = poiMapper.convertPoiToWayDto(poi, transactionId);
            osmDto.setWayDtoList(singletonList(nodeDto));
        } else {
            NodeDto nodeDto = poiMapper.convertPoiToNodeDto(poi, transactionId);
            osmDto.setNodeDtoList(singletonList(nodeDto));
        }

        Call<ResponseBody> versionCall;
        if (poi.getWay()) {
            versionCall = osmRestClient.updateWay(poi.getBackendId(), osmDto);
        } else {
            versionCall = osmRestClient.updateNode(poi.getBackendId(), osmDto);
        }

        try {
            Response<ResponseBody> response = versionCall.execute();
            if (response.isSuccessful()) {
                return new UpdateResult(ModificationStatus.SUCCESS, response.body().string());
            } else {
                if (response.code() == 400) {
                    Timber.e("Couldn't update node, conflicting version");
                    return new UpdateResult(ModificationStatus.FAILURE_CONFLICT, null);
                } else if (response.code() == 404) {
                    Timber.e("Couldn't update node, no existing node found with the id " + poi.getId());
                    return new UpdateResult(ModificationStatus.FAILURE_NOT_EXISTING, null);
                } else {
                    Timber.e("Couldn't update node");
                    return new UpdateResult(ModificationStatus.FAILURE_UNKNOWN, null);
                }
            }
        } catch (IOException e) {
            Timber.e(e, e.getMessage());
        }
        return new UpdateResult(ModificationStatus.FAILURE_UNKNOWN, null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ModificationStatus deletePoi(final Poi poi, String transactionId) {
        final OsmDto osmDto = new OsmDto();

        if (poi.getWay()) {
            WayDto nodeDto = poiMapper.convertPoiToWayDto(poi, transactionId);
            osmDto.setWayDtoList(singletonList(nodeDto));
        } else {
            NodeDto nodeDto = poiMapper.convertPoiToNodeDto(poi, transactionId);
            osmDto.setNodeDtoList(singletonList(nodeDto));
        }

        Call<ResponseBody> deleteCall;
        if (poi.getWay()) {
            deleteCall = osmRestClient.deleteWay(poi.getBackendId(), osmDto);
            Timber.d("Deleted way %s", poi.getBackendId());
        } else {
            deleteCall = osmRestClient.deleteNode(poi.getBackendId(), osmDto);
            Timber.d("Deleted node %s", poi.getBackendId());
        }
        poiManager.deletePoi(poi);

        try {
            Response<ResponseBody> response = deleteCall.execute();
            if (response.isSuccessful()) {
                return ModificationStatus.SUCCESS;
            } else {
                if (response.code() == 400) {
                    return ModificationStatus.FAILURE_CONFLICT;
                } else if (response.code() == 404) {
                    //the point doesn't exist
                    return ModificationStatus.FAILURE_NOT_EXISTING;
                } else if (response.code() == 410) {
                    //the poi was already deleted
                    return ModificationStatus.FAILURE_NOT_EXISTING;
                }
            }
        } catch (IOException e) {
            Timber.e(e, e.getMessage());
        }
        return ModificationStatus.FAILURE_UNKNOWN;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Poi getPoiById(final String backendId) {
        Call<OsmDto> osmDtoRetrievedNode = osmRestClient.getNode(backendId);
        try {
            Response<OsmDto> response = osmDtoRetrievedNode.execute();
            if (response.isSuccessful()) {
                OsmDto osmDto = response.body();
                if (osmDto != null) {
                    List<NodeDto> nodeDtoList = osmDto.getNodeDtoList();
                    if (nodeDtoList != null && nodeDtoList.size() > 0) {
                        List<Poi> pois = poiMapper.convertDtosToPois(nodeDtoList);
                        if (pois.size() > 0) {
                            return pois.get(0);
                        }
                    }
                }
            }
        } catch (IOException e) {
            Timber.e(e, e.getMessage());
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<PoiType> getPoiTypes() {
        return poiAssetLoader.loadPoiTypesByDefault();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<FullOSMRelation> getRelations(String backendIds) {
        Call<OsmDto> relationCall = osmRestClient.getRelations(backendIds);
        try {
            Response<OsmDto> response = relationCall.execute();
            if (response.isSuccessful()) {
                OsmDto osmDto = response.body();
                if (osmDto != null && osmDto.getRelationDtoList() != null) {
                    List<FullOSMRelation> fullOSMRelations = relationMapper.convertDTOstoRelations(osmDto.getRelationDtoList());
                    if (fullOSMRelations.size() > 0)
                        return fullOSMRelations;
                }
            }
        } catch (IOException e) {
            Timber.e(e, e.getMessage());
        }
        return Collections.emptyList();
    }

    @Override
    public UpdateResult updateRelation(FullOSMRelation fullOSMRelation, String transactionId) {
        final OsmDto osmDto = new OsmDto();
        osmDto.setRelationDtoList(singletonList(relationMapper.convertRelationToDTO(fullOSMRelation, transactionId)));

        Call<ResponseBody> versionCall= osmRestClient.updateRelation(fullOSMRelation.getBackendId(), osmDto);

        try {
            Response<ResponseBody> response = versionCall.execute();
            if (response.isSuccessful()) {
                return new UpdateResult(ModificationStatus.SUCCESS, response.body().string());
            } else {
                if (response.code() == 400) {
                    Timber.e("Couldn't update node, conflicting version");
                    return new UpdateResult(ModificationStatus.FAILURE_CONFLICT, null);
                } else if (response.code() == 404) {
                    Timber.e("Couldn't update node, no existing node found with the id " + fullOSMRelation.getId());
                    return new UpdateResult(ModificationStatus.FAILURE_NOT_EXISTING, null);
                } else {
                    Timber.e("Couldn't update node");
                    return new UpdateResult(ModificationStatus.FAILURE_UNKNOWN, null);
                }
            }
        } catch (IOException e) {
            Timber.e(e, e.getMessage());
        }
        return new UpdateResult(ModificationStatus.FAILURE_UNKNOWN, null);
    }
}
