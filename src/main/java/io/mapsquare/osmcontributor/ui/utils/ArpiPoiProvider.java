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
package io.mapsquare.osmcontributor.ui.utils;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import io.mapsquare.osmcontributor.model.events.NotesArpiLoadedEvent;
import io.mapsquare.osmcontributor.model.events.PleaseLoadNoteForArpiEvent;
import io.mapsquare.osmcontributor.model.events.PleaseLoadPoiForArpiEvent;
import io.mapsquare.osmcontributor.model.events.PleaseRemoveArpiMarkerEvent;
import io.mapsquare.osmcontributor.model.events.PoisArpiLoadedEvent;
import io.mapsquare.osmcontributor.model.entities.Note;
import io.mapsquare.osmcontributor.utils.Box;
import mobi.designmyapp.arpigl.engine.Engine;
import mobi.designmyapp.arpigl.event.PoiEvent;
import mobi.designmyapp.arpigl.mapper.PoiMapper;
import mobi.designmyapp.arpigl.model.Poi;
import mobi.designmyapp.arpigl.model.Tile;
import mobi.designmyapp.arpigl.provider.PoiProvider;
import mobi.designmyapp.arpigl.util.ProjectionUtils;

public class ArpiPoiProvider extends PoiProvider<List<io.mapsquare.osmcontributor.model.entities.Poi>> {
    EventBus eventBus;
    private Engine engine;
    private ArpiPoiMapper poiMapper;
    private ArpiNoteMapper noteMapper;
    public static final int POI_COLOR = 0xFFAAAAAA;
    public static final int NOTE_COLOR = 0xFFCC0052;
    public static final int SELECTED_COLOR = 0xFF2196F3;

    @Inject
    public ArpiPoiProvider(EventBus eventBus) {
        super(ArpiPoiMapper.class);
        this.eventBus = eventBus;
        this.poiMapper = new ArpiPoiMapper();
        this.noteMapper = new ArpiNoteMapper();
    }

    public void register() {
        eventBus.register(this);
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
        eventBus.post(new PleaseLoadPoiForArpiEvent(b));
        eventBus.post(new PleaseLoadNoteForArpiEvent(b));
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.ASYNC)
    public void onPleaseRemoveArpiMarkerEvent(PleaseRemoveArpiMarkerEvent event) {
        if (engine != null) {
            Object poi = event.getPoi();
            if (poi instanceof io.mapsquare.osmcontributor.model.entities.Poi) {
                engine.removePoi(poiMapper.convert((io.mapsquare.osmcontributor.model.entities.Poi) poi));
            } else if (poi instanceof Note) {
                engine.removePoi(noteMapper.convert((Note) poi));
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.ASYNC)
    public void onPoisArpiLoadedEvent(PoisArpiLoadedEvent event) {
        ArrayList<Poi> toRemove = new ArrayList<>();
        postEvent(new PoiEvent(poiMapper.convert(event.getPois(), toRemove)));
        for (Poi poi : toRemove) {
            engine.removePoi(poi);
        }
    }

    @Subscribe(threadMode = ThreadMode.ASYNC)
    public void onNotesArpiLoadedEvent(NotesArpiLoadedEvent event) {
        postEvent(new PoiEvent(noteMapper.convert(event.getNotes())));
    }


    public static class ArpiPoiMapper implements PoiMapper<List<io.mapsquare.osmcontributor.model.entities.Poi>> {

        public Poi convert(io.mapsquare.osmcontributor.model.entities.Poi source) {
            return Poi.builder()
                    .id("POI:" + source.getId().toString())
                    .latitude(source.getLatitude())
                    .longitude(source.getLongitude())
                    .color(POI_COLOR)
                    .icon(source.getType().getIcon())
                    .altitude(2)
                    .shape("POI_balloon")
                    .build();
        }

        @Override
        public List<Poi> convert(List<io.mapsquare.osmcontributor.model.entities.Poi> sources) {
            if (sources == null) {
                return null;
            }
            List<Poi> result = new ArrayList<>(sources.size());
            for (io.mapsquare.osmcontributor.model.entities.Poi poi : sources) {
                if (!poi.getToDelete() && !poi.getOld()) {
                    result.add(convert(poi));
                }
            }
            return result;
        }

        public List<Poi> convert(List<io.mapsquare.osmcontributor.model.entities.Poi> sources, List<Poi> toRemove) {
            if (sources == null) {
                return null;
            }
            List<Poi> result = new ArrayList<>(sources.size());
            Poi poi;
            for (io.mapsquare.osmcontributor.model.entities.Poi source : sources) {
                poi = convert(source);
                if (!source.getToDelete() && !source.getOld()) {
                    result.add(poi);
                } else {
                    toRemove.add(poi);
                }
            }
            return result;
        }
    }

    public static class ArpiNoteMapper implements PoiMapper<List<Note>> {

        public Poi convert(io.mapsquare.osmcontributor.model.entities.Note source) {
            return Poi.builder()
                    .id("NOTE:" + source.getBackendId())
                    .latitude(source.getLatitude())
                    .longitude(source.getLongitude())
                    .color(NOTE_COLOR)
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