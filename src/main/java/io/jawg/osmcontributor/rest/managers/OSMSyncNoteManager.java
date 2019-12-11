/**
 * Copyright (C) 2019 Takima
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
import java.util.List;

import io.jawg.osmcontributor.model.entities.Comment;
import io.jawg.osmcontributor.model.entities.Note;
import io.jawg.osmcontributor.rest.OSMProxy;
import io.jawg.osmcontributor.rest.clients.OsmRestClient;
import io.jawg.osmcontributor.rest.dtos.osm.NoteDto;
import io.jawg.osmcontributor.rest.dtos.osm.OsmDto;
import io.jawg.osmcontributor.rest.events.error.SyncConflictingNoteErrorEvent;
import io.jawg.osmcontributor.rest.events.error.SyncDownloadRetrofitErrorEvent;
import io.jawg.osmcontributor.rest.events.error.SyncUploadNoteRetrofitErrorEvent;
import io.jawg.osmcontributor.rest.mappers.NoteMapper;
import io.jawg.osmcontributor.utils.Box;
import retrofit2.Call;
import retrofit2.Response;
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
        String strBox = box.getWest() + "," + box.getSouth() + "," + box.getEast() + "," + box.getNorth();

        Call<OsmDto> osmDtoCall = osmRestClient.getNotes(strBox);
        try {
            Response<OsmDto> response = osmDtoCall.execute();
            if (response.isSuccessful()) {
                OsmDto osmDto = response.body();
                if (osmDto != null) {
                    List<NoteDto> noteDtos = osmDto.getNoteDtoList();
                    if (noteDtos != null && !noteDtos.isEmpty()) {
                        Timber.d("Updating %d note(s)", noteDtos.size());
                        bus.post(new SyncDownloadRetrofitErrorEvent());
                        return noteMapper.convertNoteDtosToNotes(noteDtos);
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
    public Note remoteAddComment(final Comment comment) {
        Call<OsmDto> osmDtoCall;
        switch (comment.getAction()) {
            case Comment.ACTION_CLOSE:
                osmDtoCall = osmRestClient.closeNote(comment.getNote().getBackendId(), comment.getText(), "");
                break;

            case Comment.ACTION_REOPEN:
                osmDtoCall = osmRestClient.reopenNote(comment.getNote().getBackendId(), comment.getText(), "");
                break;

            case Comment.ACTION_OPEN:
                osmDtoCall = osmRestClient.addNote(comment.getNote().getLatitude(), comment.getNote().getLongitude(), comment.getText(), "");
                break;

            default:
                osmDtoCall = osmRestClient.addComment(comment.getNote().getBackendId(), comment.getText(), "");
                break;
        }

        try {
            Response<OsmDto> response = osmDtoCall.execute();
            if (response.isSuccessful()) {
                OsmDto osmDto = response.body();
                if (osmDto != null) {
                    NoteDto noteDto = osmDto.getNoteDtoList().get(0);
                    Note note = noteMapper.convertNoteDtoToNote(noteDto);
                    note.setUpdated(false);
                    note.setId(comment.getNote().getId());
                    return note;
                }
            } else if (response.code() == 409) {
                bus.post(new SyncConflictingNoteErrorEvent(comment.getNote()));
            }
        } catch (IOException e) {
            Timber.e(e, "Retrofit error, couldn't create comment !");
        }

        bus.post(new SyncUploadNoteRetrofitErrorEvent(comment.getNote().getId()));
        return null;
    }
}
