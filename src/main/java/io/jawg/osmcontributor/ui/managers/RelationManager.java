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
package io.jawg.osmcontributor.ui.managers;

import android.util.LongSparseArray;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import javax.inject.Inject;

import io.jawg.osmcontributor.database.dao.RelationDisplayDao;
import io.jawg.osmcontributor.database.dao.RelationDisplayTagDao;
import io.jawg.osmcontributor.database.dao.RelationEditionDao;
import io.jawg.osmcontributor.model.entities.Poi;
import io.jawg.osmcontributor.model.entities.RelationId;
import io.jawg.osmcontributor.model.entities.relation_display.RelationDisplay;
import io.jawg.osmcontributor.model.entities.relation_save.RelationEdition;
import io.jawg.osmcontributor.model.events.BusLinesNearbyForPoiLoadedEvent;
import io.jawg.osmcontributor.model.events.BusLinesSuggestionForPoiLoadedEvent;
import io.jawg.osmcontributor.model.events.PleaseLoadBusLinesNearbyForPoiEvent;
import io.jawg.osmcontributor.model.events.PleaseLoadBusLinesForPoiEvent;
import io.jawg.osmcontributor.model.events.BusLinesForPoiLoadedEvent;
import io.jawg.osmcontributor.ui.events.type.PleaseLoadBusLinesSuggestionForPoiEvent;

/**
 * Manager class for Relations.
 * Provides a number of methods to manipulate the Relations in the database.
 */
public class RelationManager {

    private RelationDisplayDao relationDisplayDao;
    private RelationDisplayTagDao relationDisplayTagDao;
    private RelationEditionDao relationEditionDao;
    private EventBus eventBus;

    @Inject
    public RelationManager(RelationDisplayDao relationDisplayDao, RelationDisplayTagDao relationDisplayTagDao,
                           RelationEditionDao relationEditionDao, EventBus eventBus) {
        this.relationDisplayDao = relationDisplayDao;
        this.relationEditionDao = relationEditionDao;
        this.relationDisplayTagDao = relationDisplayTagDao;
        this.eventBus = eventBus;
    }

    @Subscribe(threadMode = ThreadMode.ASYNC)
    public void onPleaseLoadBusLinesForPoiEvent(PleaseLoadBusLinesForPoiEvent event) {
        List<RelationDisplay> relationDisplays = relationDisplayDao
                .queryByBackendRelationIds(getBackendIdsFromRelation(event.getRelationIds()));
        eventBus.post(new BusLinesForPoiLoadedEvent(relationDisplays));
    }

    @Subscribe(threadMode = ThreadMode.ASYNC)
    public void onPleaseLoadBusLinesNearbyForPoiEvent(PleaseLoadBusLinesNearbyForPoiEvent event) {
        List<RelationDisplay> relationDisplays = getBusLinesOrderedByDistanceFromPoiById(event.getPoi());
        eventBus.post(new BusLinesNearbyForPoiLoadedEvent(relationDisplays));
    }

    @Subscribe(threadMode = ThreadMode.ASYNC)
    public void onPleaseLoadBusLinesSuggestionForPoiEvent(PleaseLoadBusLinesSuggestionForPoiEvent event) {
        List<RelationDisplay> relationDisplays = getValuesForBusLinesAutocompletion(event.getSearch());
        eventBus.post(new BusLinesSuggestionForPoiLoadedEvent(relationDisplays));
    }

    private List<Long> getBackendIdsFromRelation(Collection<RelationId> relationIds) {
        List<Long> ids = new ArrayList<>();
        for (RelationId re : relationIds) {
            if (re.getBackendRelationId() != null && !re.getBackendRelationId().isEmpty()) {
                ids.add(Long.valueOf(re.getBackendRelationId()));
            }
        }
        return ids;
    }

    /**
     * Save the changes made to the relations associated to a poi
     *
     * @param relationEditions the list of changes
     * @param poi              the poi
     */
    public void saveRelationEditions(List<RelationEdition> relationEditions, Poi poi) {
        for (RelationEdition relationEdition : relationEditions) {
            relationEdition.setPoi(poi);
            relationEditionDao.create(relationEdition);
        }
    }

    /**
     * get values for autocompletion of bus field
     *
     * @return
     */
    public List<RelationDisplay> getValuesForBusLinesAutocompletion(String search) {
        return relationDisplayDao.queryByDatabaseIds(
                relationDisplayTagDao.queryForRelationIDsByTag(search));
    }

    /**
     * get bus lines ordered by distance ASC from a POI
     *
     * @param poi a Poi
     * @return a list of RelationDisplay
     */
    public List<RelationDisplay> getBusLinesOrderedByDistanceFromPoiById(Poi poi) {
        List<Long> relationIds = relationDisplayDao.queryForRelationIdsOrderedByDistance(poi);
        List<RelationDisplay> relationDisplays = relationDisplayDao.queryByDatabaseIds(relationIds);

        // The request which converts List<Long> to List<RelationDisplay> sorts the result by id ASC
        // But we want to keep the same relations order after the Long to RelationDisplay conversion
        // So we create a HashMap to set an index for each id
        LongSparseArray<Integer> array = new LongSparseArray<>();
        for (int i = 0; i < relationIds.size(); i++) {
            array.put(relationIds.get(i), i);
        }
        // Then we create an array and fill it with the list of RelationDisplay in the right order
        RelationDisplay[] relationsArray = new RelationDisplay[relationIds.size()];
        for (RelationDisplay relationDisplay : relationDisplays) {
            relationsArray[array.get(relationDisplay.getId())] = relationDisplay;
        }

        return Arrays.asList(relationsArray);
    }

    /**
     * delete the relation edition objects
     * @param relationEditions list of editions
     */
    public void deleteFinishedEditions(RelationEdition relationEditions) {
        relationEditionDao.delete(relationEditions);
    }
}
