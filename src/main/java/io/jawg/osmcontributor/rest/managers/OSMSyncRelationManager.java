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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import io.jawg.osmcontributor.model.entities.Poi;
import io.jawg.osmcontributor.model.entities.relation.FullOSMRelation;
import io.jawg.osmcontributor.model.entities.relation.RelationMember;
import io.jawg.osmcontributor.model.entities.relation_save.RelationEdition;
import io.jawg.osmcontributor.rest.clients.OsmRestClient;
import io.jawg.osmcontributor.rest.dtos.osm.OsmDto;
import io.jawg.osmcontributor.rest.mappers.RelationMapper;
import io.jawg.osmcontributor.utils.CollectionUtils;
import retrofit2.Call;
import retrofit2.Response;
import timber.log.Timber;

/**
 * Implementation of a {@link SyncWayManager} using an OpenStreetMap database as a backend.
 */
public class OSMSyncRelationManager implements SyncRelationManager {

    public static String TAG = "OSMSyncRelationManager";

    RelationMapper relationMapper;
    OsmRestClient osmRestClient;

    public OSMSyncRelationManager(RelationMapper relationMapper, OsmRestClient osmRestClient) {
        this.relationMapper = relationMapper;
        this.osmRestClient = osmRestClient;
    }

    @Override
    public List<FullOSMRelation> downloadRelationsForEdition(List<Long> ids) {
        Call<OsmDto> callOsm = osmRestClient.getRelations(CollectionUtils.formatIdList(ids));
        List<FullOSMRelation> fullOSMRelations = new ArrayList<>();

        if (ids.size() > 0) {
            try {
                Response<OsmDto> response = callOsm.execute();
                if (response.isSuccessful()) {
                    OsmDto osmDto = response.body();
                    if (osmDto != null) {
                        fullOSMRelations = relationMapper.convertDTOstoRelations(osmDto.getRelationDtoList());
                    }
                    return fullOSMRelations;
                }
            } catch (IOException e) {
                Timber.e(e, e.getMessage());
            }
        }


        return fullOSMRelations;
    }

    @Override
    public List<FullOSMRelation> applyChangesToRelations(List<FullOSMRelation> fullOSMRelations, List<RelationEdition> relationEditions) {
        List<FullOSMRelation> editedOsmRelations = new ArrayList<>();

        for (FullOSMRelation fullOSMRelation : fullOSMRelations) {
            for (RelationEdition editedRelation : relationEditions) {

                if (fullOSMRelation.getBackendId().equals(editedRelation.getBackendId())) {

                    //todo change workflow, extract var and add to list at the end
                    editedOsmRelations.add(fullOSMRelation);
                    switch (editedRelation.getChange()) {
                        case ADD_MEMBER:
                            if (!isPoiInRelationMemberList(fullOSMRelation.getMembers(), editedRelation.getPoi())) {
                                editedOsmRelations.get(editedOsmRelations.size() - 1)
                                        .getMembers()
                                        .add(RelationMember.createBusStop(Long.valueOf(editedRelation.getPoi().getBackendId())));
                            } else {
                                //this case should not happen
                                Timber.e(TAG, "While editig relation, the poi with id %s was already in the relation", editedRelation.getPoi().getBackendId());
                            }
                            break;
                        case REMOVE_MEMBER:
                            removeMemberFromList(
                                    editedOsmRelations.get(editedOsmRelations.size() - 1).getMembers(),
                                    editedRelation.getPoi());
                            break;
                    }
                }

            }

        }
        return editedOsmRelations;
    }

    private boolean isPoiInRelationMemberList(Collection<RelationMember> members, Poi poi) {
        for (RelationMember m : members) {
            if (m.getRef().equals(Long.valueOf(poi.getBackendId())))
                return true;

        }
        return false;
    }

    private void removeMemberFromList(Collection<RelationMember> members, Poi poi) {
        Iterator<RelationMember> iterator = members.iterator();
        while (iterator.hasNext()) {
            if (iterator.next().getRef().equals(Long.valueOf(poi.getBackendId()))) {
                iterator.remove();
                break;
            }
        }
    }

}
