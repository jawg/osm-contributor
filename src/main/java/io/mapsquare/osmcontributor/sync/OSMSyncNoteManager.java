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

import java.util.List;

import de.greenrobot.event.EventBus;
import io.mapsquare.osmcontributor.core.database.dao.CommentDao;
import io.mapsquare.osmcontributor.core.database.dao.NoteDao;
import io.mapsquare.osmcontributor.core.events.NotesLoadedEvent;
import io.mapsquare.osmcontributor.core.model.Comment;
import io.mapsquare.osmcontributor.core.model.Note;
import io.mapsquare.osmcontributor.note.NoteManager;
import io.mapsquare.osmcontributor.sync.converter.NoteConverter;
import io.mapsquare.osmcontributor.sync.dto.osm.NoteDto;
import io.mapsquare.osmcontributor.sync.dto.osm.OsmDto;
import io.mapsquare.osmcontributor.sync.events.SyncDownloadNoteEvent;
import io.mapsquare.osmcontributor.sync.events.SyncFinishUploadNote;
import io.mapsquare.osmcontributor.sync.events.error.SyncConflictingNoteErrorEvent;
import io.mapsquare.osmcontributor.sync.events.error.SyncDownloadRetrofitErrorEvent;
import io.mapsquare.osmcontributor.sync.events.error.SyncUploadNoteRetrofitErrorEvent;
import io.mapsquare.osmcontributor.sync.rest.OsmRestClient;
import io.mapsquare.osmcontributor.utils.Box;
import retrofit.RetrofitError;
import timber.log.Timber;

/**
 * Implementation of {@link io.mapsquare.osmcontributor.sync.SyncNoteManager} for an OpenStreetMap backend.
 */
public class OSMSyncNoteManager implements SyncNoteManager {

    OSMProxy osmProxy;
    OsmRestClient osmRestClient;
    EventBus bus;
    NoteManager noteManager;
    NoteConverter noteConverter;
    CommentDao commentDao;
    NoteDao noteDao;

    public OSMSyncNoteManager(OSMProxy osmProxy, OsmRestClient osmRestClient, EventBus bus, NoteManager noteManager, NoteConverter noteConverter, CommentDao commentDao, NoteDao noteDao) {
        this.osmProxy = osmProxy;
        this.osmRestClient = osmRestClient;
        this.bus = bus;
        this.noteManager = noteManager;
        this.noteConverter = noteConverter;
        this.commentDao = commentDao;
        this.noteDao = noteDao;
    }

    public void onEventAsync(SyncDownloadNoteEvent event) {
        syncDownloadNotesInBox(event.getBox());
        //to notify the app that notes have been loaded
        bus.post(new NotesLoadedEvent(event.getBox(), noteManager.queryForAllInRect(event.getBox())));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void syncDownloadNotesInBox(final Box box) {
        Timber.d("Requesting osm for notes download");
        OSMProxy.Result<Void> result = osmProxy.proceed(new OSMProxy.NetworkAction<Void>() {
            @Override
            public Void proceed() {
                String strBox = box.getWest() + "," + box.getSouth() + "," + box.getEast() + "," + box.getNorth();

                OsmDto osmDto = osmRestClient.getNotes(strBox);

                if (osmDto != null && osmDto.getNoteDtoList() != null && osmDto.getNoteDtoList().size() > 0) {
                    Timber.d("Updating %d note(s)", osmDto.getNoteDtoList().size());
                    locallyUpdateNotes(osmDto);
                } else {
                    Timber.d("No new note found in the area");
                }
                return null;
            }
        });

        if (!result.isSuccess() && result.getRetrofitError() != null) {
            Timber.e(result.getRetrofitError(), "Retrofit error while trying to download notes in area");
            bus.post(new SyncDownloadRetrofitErrorEvent());
        }
    }

    /**
     * Merge Notes contained in an OsmDto with the database.
     *
     * @param osmDto The Notes to merge.
     */
    private void locallyUpdateNotes(OsmDto osmDto) {
        List<NoteDto> noteDtoList = osmDto.getNoteDtoList();
        List<Note> notes = noteConverter.convertNoteDtosToNotes(noteDtoList);

        noteManager.mergeFromOsmNotes(notes);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void remoteAddComments() {
        List<Comment> newComments = commentDao.queryForAllNew();
        int successfullyAddedComments = 0;

        if (newComments.size() == 0) {
            Timber.i("No new Comments to send to osm");
        } else {
            Timber.i("Found %d new  Comment to send to osm", newComments.size());
            for (final Comment comment : newComments) {

                noteDao.refresh(comment.getNote());

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
                    if (osmDto != null && osmDto.getNoteDtoList() != null && osmDto.getNoteDtoList().size() == 1) {

                        NoteDto noteDto = osmDto.getNoteDtoList().get(0);
                        Note note = noteConverter.convertNoteDtoToNote(noteDto);
                        note.setUpdated(false);
                        note.setId(comment.getNote().getId());

                        //delete comments updated = true
                        commentDao.deleteByNoteId(note.getId(), true);

                        // save note with all comments updated
                        noteManager.saveNote(note);

                        bus.post(new SyncFinishUploadNote(note));
                        successfullyAddedComments++;
                    }
                }

                if (!result.isSuccess() && result.getRetrofitError() != null) {
                    RetrofitError e = result.getRetrofitError();
                    if (e.getResponse() != null && e.getResponse().getStatus() == 409) {
                        Timber.e(e, "Couldn't create note, note already closed");
                        commentDao.delete(comment);
                        bus.post(new SyncConflictingNoteErrorEvent(noteDao.queryForId(comment.getNote().getId())));
                    } else {
                        Timber.e(e, "Retrofit error, couldn't create comment !");
                        bus.post(new SyncUploadNoteRetrofitErrorEvent(comment.getNote().getId()));
                    }
                }
            }
            Timber.i(" %d comment sent to osm", successfullyAddedComments);
        }
    }
}
