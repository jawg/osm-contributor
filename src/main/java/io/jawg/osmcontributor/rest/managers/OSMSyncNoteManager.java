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
package io.jawg.osmcontributor.rest.managers;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

import io.jawg.osmcontributor.model.entities.Comment;
import io.jawg.osmcontributor.model.entities.Note;
import io.jawg.osmcontributor.rest.mappers.NoteMapper;
import io.jawg.osmcontributor.rest.dtos.osm.NoteDto;
import io.jawg.osmcontributor.rest.dtos.osm.OsmDto;
import io.jawg.osmcontributor.rest.events.error.SyncConflictingNoteErrorEvent;
import io.jawg.osmcontributor.rest.events.error.SyncDownloadRetrofitErrorEvent;
import io.jawg.osmcontributor.rest.events.error.SyncUploadNoteRetrofitErrorEvent;
import io.jawg.osmcontributor.rest.clients.OsmRestClient;
import io.jawg.osmcontributor.rest.OSMProxy;
import io.jawg.osmcontributor.utils.Box;
import retrofit.RetrofitError;
import timber.log.Timber;

/**
 * Implementation of {@link SyncNoteManager} for an OpenStreetMap backend.
 */
public class OSMSyncNoteManager implements SyncNoteManager {

    OSMProxy osmProxy;
    OsmRestClient osmRestClient;
    EventBus bus;
    NoteMapper noteMapper;

    public OSMSyncNoteManager(OSMProxy osmProxy, OsmRestClient osmRestClient, EventBus bus, NoteMapper noteMapper) {
        this.osmProxy = osmProxy;
        this.osmRestClient = osmRestClient;
        this.bus = bus;
        this.noteMapper = noteMapper;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Note> syncDownloadNotesInBox(final Box box) {
        Timber.d("Requesting osm for notes download");
        OSMProxy.Result<List<Note>> result = osmProxy.proceed(new OSMProxy.NetworkAction<List<Note>>() {
            @Override
            public List<Note> proceed() {
                String strBox = box.getWest() + "," + box.getSouth() + "," + box.getEast() + "," + box.getNorth();

                OsmDto osmDto = osmRestClient.getNotes(strBox);

                if (osmDto != null && osmDto.getNoteDtoList() != null && osmDto.getNoteDtoList().size() > 0) {
                    Timber.d("Updating %d note(s)", osmDto.getNoteDtoList().size());
                    return noteMapper.convertNoteDtosToNotes(osmDto.getNoteDtoList());
                } else {
                    Timber.d("No new note found in the area");
                    return null;
                }
            }
        });

        if (!result.isSuccess()) {
            if (result.getRetrofitError() != null) {
                Timber.e(result.getRetrofitError(), "Retrofit error, couldn't download from overpass");
            }
            bus.post(new SyncDownloadRetrofitErrorEvent());
        }
        return result.getResult();
    }

    /**
     * {@inheritDoc}
     */
    public Note remoteAddComment(final Comment comment) {
        OSMProxy.Result<OsmDto> result = osmProxy.proceed(new OSMProxy.NetworkAction<OsmDto>() {
            @Override
            public OsmDto proceed() {
                OsmDto osmDto;
                switch (comment.getAction()) {
                    case Comment.ACTION_CLOSE:
                        osmDto = osmRestClient.closeNote(comment.getNote().getBackendId(), comment.getText(), "");
                        break;

                    case Comment.ACTION_REOPEN:
                        osmDto = osmRestClient.reopenNote(comment.getNote().getBackendId(), comment.getText(), "");
                        break;

                    case Comment.ACTION_OPEN:
                        osmDto = osmRestClient.addNote(comment.getNote().getLatitude(), comment.getNote().getLongitude(), comment.getText(), "");
                        break;

                    default:
                        osmDto = osmRestClient.addComment(comment.getNote().getBackendId(), comment.getText(), "");
                        break;
                }
                return osmDto;
            }
        });

        if (result.isSuccess()) {
            OsmDto osmDto = result.getResult();
            NoteDto noteDto = osmDto.getNoteDtoList().get(0);
            Note note = noteMapper.convertNoteDtoToNote(noteDto);
            note.setUpdated(false);
            note.setId(comment.getNote().getId());

            return note;
        } else {
            if (result.getRetrofitError() != null) {
                RetrofitError e = result.getRetrofitError();
                if (e.getResponse() != null && e.getResponse().getStatus() == 409) {
                    Timber.e(e, "Couldn't create note, note already closed");
                    bus.post(new SyncConflictingNoteErrorEvent(comment.getNote()));
                } else {
                    Timber.e(e, "Retrofit error, couldn't create comment !");
                    bus.post(new SyncUploadNoteRetrofitErrorEvent(comment.getNote().getId()));
                }
            }
            return null;
        }
    }
}
