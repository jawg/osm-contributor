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
package io.mapsquare.osmcontributor.map;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import de.greenrobot.event.EventBus;
import io.mapsquare.osmcontributor.core.events.NotesLoadedEvent;
import io.mapsquare.osmcontributor.core.events.PleaseLoadNoteForArpiEvent;
import io.mapsquare.osmcontributor.core.events.PleaseLoadPoiForArpiEvent;
import io.mapsquare.osmcontributor.core.events.PleaseRemoveArpiMarkerEvent;
import io.mapsquare.osmcontributor.core.events.PoisLoadedEvent;
import io.mapsquare.osmcontributor.core.model.Note;
import io.mapsquare.osmcontributor.utils.Box;
import mobi.designmyapp.arpigl.engine.Engine;
import mobi.designmyapp.arpigl.event.PoiEvent;
import mobi.designmyapp.arpigl.mapper.PoiMapper;
import mobi.designmyapp.arpigl.model.Poi;
import mobi.designmyapp.arpigl.model.Shape;
import mobi.designmyapp.arpigl.model.Tile;
import mobi.designmyapp.arpigl.provider.PoiProvider;
import mobi.designmyapp.arpigl.util.ProjectionUtils;

public class ArpiPoiProvider extends PoiProvider<List<io.mapsquare.osmcontributor.core.model.Poi>> {

    EventBus eventBus;
    private Engine engine;

    @Inject
    public ArpiPoiProvider(EventBus eventBus) {
        super(ArpiPoiMapper.class);
        this.eventBus = eventBus;
    }

    public void register() {
        eventBus.registerSticky(this);
    }

    public void unregister() {
        eventBus.unregister(this);
    }

    public void setEngine(Engine engine) {
        this.engine = engine;
    }

    @Override
    public void fetch(Tile.Id tile) {

        double[] coords1 = ProjectionUtils.tile2latLon(tile.x - 2, tile.y - 2, tile.z);
        double[] coords2 = ProjectionUtils.tile2latLon(tile.x + 3, tile.y + 3, tile.z);

        Box b = new Box(coords1[0], coords2[1], coords2[0], coords1[1]);
        eventBus.postSticky(new PleaseLoadPoiForArpiEvent(b));
        eventBus.postSticky(new PleaseLoadNoteForArpiEvent(b));
    }

    public void onEventBackgroundThread(PleaseRemoveArpiMarkerEvent event) {
        if (engine != null) {
            Object poi = event.getPoi();
            if (poi instanceof io.mapsquare.osmcontributor.core.model.Poi) {
                engine.removePoi(new ArpiPoiMapper().convert((io.mapsquare.osmcontributor.core.model.Poi) poi));
            } else if (poi instanceof Note) {
                engine.removePoi(new ArpiNoteMapper().convert((Note) poi));
            }
        }
    }

    public void onEventBackgroundThread(PoisLoadedEvent event) {
        PleaseLoadPoiForArpiEvent stickyBox = eventBus.getStickyEvent(PleaseLoadPoiForArpiEvent.class);
        // If sticky box or event box is null, it means the event is not for our system.
        // If event.getBox is not for the latest box request, we simply ignore it.
        if (stickyBox != null && event.getBox() != null && event.getBox().equals(stickyBox.getBox())) {
            postEvent(new PoiEvent(new ArpiPoiMapper().convert(event.getPois())));
            eventBus.removeStickyEvent(PleaseLoadPoiForArpiEvent.class);
        }
    }

    public void onEventBackgroundThread(NotesLoadedEvent event) {
        PleaseLoadNoteForArpiEvent stickyBox = eventBus.getStickyEvent(PleaseLoadNoteForArpiEvent.class);
        // If sticky box or event box is null, it means the event is not for our system.
        // If event.getBox is not for the latest box request, we simply ignore it.
        if (stickyBox != null && event.getBox() != null && event.getBox().equals(stickyBox.getBox())) {
            postEvent(new PoiEvent(new ArpiNoteMapper().convert(event.getNotes())));
            eventBus.removeStickyEvent(PleaseLoadNoteForArpiEvent.class);
        }
    }


    public static class ArpiPoiMapper implements PoiMapper<List<io.mapsquare.osmcontributor.core.model.Poi>> {

        public Poi convert(io.mapsquare.osmcontributor.core.model.Poi source) {
            return Poi.builder()
                    .id(source.getBackendId())
                    .latitude(source.getLatitude())
                    .longitude(source.getLongitude())
                    .color(0xFF00BB55)
                    .icon(source.getType().getIcon())
                    .altitude(1.5)
                    .shape(Shape.Default.BALLOON)
                    .build();
        }

        @Override
        public List<Poi> convert(List<io.mapsquare.osmcontributor.core.model.Poi> sources) {
            if (sources == null) {
                return null;
            }
            List<Poi> result = new ArrayList<>(sources.size());
            for (io.mapsquare.osmcontributor.core.model.Poi poi : sources) {
                result.add(convert(poi));
            }
            return result;
        }

    }

    public static class ArpiNoteMapper implements PoiMapper<List<Note>> {

        public Poi convert(io.mapsquare.osmcontributor.core.model.Note source) {
            return Poi.builder()
                    .id(source.getBackendId())
                    .latitude(source.getLatitude())
                    .longitude(source.getLongitude())
                    .color(0xFFCC0052)
                    .icon("note")
                    .altitude(1.5)
                    .shape("note")
                    .build();
        }

        @Override
        public List<Poi> convert(List<Note> sources) {
            if (sources == null) {
                return null;
            }
            List<Poi> result = new ArrayList<>(sources.size());
            for (Note poi : sources) {
                result.add(convert(poi));
            }
            return result;
        }

    }
}
