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
package io.mapsquare.osmcontributor.note;

import android.app.Application;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import javax.inject.Inject;

import de.greenrobot.event.EventBus;
import io.mapsquare.osmcontributor.core.ConfigManager;
import io.mapsquare.osmcontributor.core.database.DatabaseHelper;
import io.mapsquare.osmcontributor.core.database.dao.CommentDao;
import io.mapsquare.osmcontributor.core.database.dao.NoteDao;
import io.mapsquare.osmcontributor.core.events.NoteLoadedEvent;
import io.mapsquare.osmcontributor.core.events.NoteSavedEvent;
import io.mapsquare.osmcontributor.core.events.NotesLoadedEvent;
import io.mapsquare.osmcontributor.core.events.PleaseLoadNoteEvent;
import io.mapsquare.osmcontributor.core.events.PleaseLoadNotesEvent;
import io.mapsquare.osmcontributor.core.model.Comment;
import io.mapsquare.osmcontributor.core.model.Note;
import io.mapsquare.osmcontributor.map.events.NewNoteCreatedEvent;
import io.mapsquare.osmcontributor.map.events.PleaseApplyNewComment;
import io.mapsquare.osmcontributor.sync.SyncNoteManager;
import io.mapsquare.osmcontributor.sync.events.SyncFinishUploadNote;
import io.mapsquare.osmcontributor.utils.Box;
import timber.log.Timber;

import static io.mapsquare.osmcontributor.core.database.DatabaseHelper.loadLazyForeignCollection;

/**
 * Manager class for Notes and Comments.
 * Provides a number of methods to manipulate the {@link io.mapsquare.osmcontributor.core.model.Note}
 * and {@link io.mapsquare.osmcontributor.core.model.Comment} in the database that should be used instead
 * of calling the {@link io.mapsquare.osmcontributor.core.database.dao.NoteDao}
 * and {@link io.mapsquare.osmcontributor.core.database.dao.NoteDao}.
 */
public class NoteManager {

    CommentDao commentDao;
    NoteDao noteDao;
    DatabaseHelper databaseHelper;
    ConfigManager configManager;
    EventBus bus;
    Application application;
    SyncNoteManager syncNoteManager;

    @Inject
    public NoteManager(NoteDao noteDao, CommentDao commentDao, DatabaseHelper databaseHelper, ConfigManager configManager, EventBus bus, Application application, SyncNoteManager syncNoteManager) {
        this.noteDao = noteDao;
        this.commentDao = commentDao;
        this.databaseHelper = databaseHelper;
        this.configManager = configManager;
        this.application = application;
        this.bus = bus;
        this.syncNoteManager = syncNoteManager;
    }

    // ********************************
    // ************ Events ************
    // ********************************

    public void onEventAsync(PleaseLoadNoteEvent event) {
        bus.post(new NoteLoadedEvent(queryForId(event.getNoteId())));
    }

    public void onEventAsync(PleaseLoadNotesEvent event) {
        loadNotes(event);
    }


    public void onEventAsync(PleaseApplyNewComment event) {
        Timber.d("please apply new comment");

        Note note = syncNoteManager.remoteAddComment(createComment(event.getNote(), event.getAction(), event.getText()));

        if (note != null) {
            mergeBackendNote(note);
            bus.post(new NewNoteCreatedEvent(note.getId()));
            bus.post(new SyncFinishUploadNote(note));
        }
    }

    // ********************************
    // ************ public ************
    // ********************************

    /**
     * Method saving a list Notes and the associated foreign collections (comments).
     * <p/>
     * Do not call the DAO directly to save a List of Notes, use this method.
     *
     * @param notes The Notes to save.
     * @return The saved Notes.
     */
    public List<Note> saveNotes(final List<Note> notes) {
        return databaseHelper.callInTransaction(new Callable<List<Note>>() {
            @Override
            public List<Note> call() throws Exception {
                List<Note> result = new ArrayList<>(notes.size());
                for (Note note : notes) {
                    result.add(saveNoteNoTransactionMgmt(note));
                }
                return result;
            }
        });
    }

    /**
     * Method saving a note and the associated foreign collection (comments) without transaction management.
     * <p/>
     * Do not call the DAO directly to save a note, use this method.
     *
     * @param note The note to save.
     * @return The saved note.
     * @see #saveNote(Note)
     */
    private Note saveNoteNoTransactionMgmt(Note note) {
        commentDao.deleteByNoteIdAndUpdated(note.getId(), false);
        noteDao.createOrUpdate(note);

        if (note.getComments() != null) {
            for (Comment comment : note.getComments()) {
                commentDao.createOrUpdate(comment);
            }
        }

        bus.post(new NoteSavedEvent(note));
        return note;
    }

    /**
     * Method saving a note and the associated foreign collection (comments).
     * <p/>
     * Do not call the DAO directly to save a note, use this method.
     *
     * @param note The note to save.
     * @return The saved note.
     */
    public Note saveNote(final Note note) {
        return databaseHelper.callInTransaction(new Callable<Note>() {
            @Override
            public Note call() throws Exception {
                return saveNoteNoTransactionMgmt(note);
            }
        });
    }

    /**
     * Query for a Note with a given id eagerly.
     *
     * @param id The id of the Note to load.
     * @return The queried Note.
     */
    public Note queryForId(Long id) {
        Note note = noteDao.queryForId(id);
        if (note == null) {
            return null;
        }
        note.setComments(loadLazyForeignCollection(note.getComments()));
        return note;
    }

    /**
     * Merge Note in parameters to the already in the database.
     *
     * @param remoteNote The Note to merge.
     */
    public void mergeBackendNote(Note remoteNote) {
        Note localNote = noteDao.queryByBackendId(remoteNote.getBackendId());

        if (localNote != null) {
            remoteNote.setId(localNote.getId());
        }
        saveNote(remoteNote);
    }

    /**
     * Merge Notes in parameters to those already in the database.
     *
     * @param remoteNotes The Notes to merge.
     */
    public void mergeBackendNotes(List<Note> remoteNotes) {
        List<Note> toMergeNotes = new ArrayList<>();

        Map<String, Note> remoteNotesMap = new HashMap<>();

        // Map remote Note backend Ids
        for (Note note : remoteNotes) {
            remoteNotesMap.put(note.getBackendId(), note);
        }

        // List matching Notes
        List<Note> localNotes = noteDao.queryByBackendIds(remoteNotesMap.keySet());


        Map<String, Note> localNotesMap = new HashMap<>();
        // Map matching local Notes
        for (Note localNote : localNotes) {
            localNotesMap.put(localNote.getBackendId(), localNote);
        }

        // Browse remote notes
        for (Note remoteNote : remoteNotes) {
            Note localNote = localNotesMap.get(remoteNote.getBackendId());

            if (localNote != null) {
                remoteNote.setId(localNote.getId());
            }
            // This Note should be updated
            toMergeNotes.add(remoteNote);

        }

        // saveNotes of either new or existing Notes
        saveNotes(toMergeNotes);
    }

    /**
     * Query for all the Notes contained in the bounds defined by the box.
     *
     * @param box Bounds of the search in latitude and longitude coordinates.
     * @return The Notes contained in the box.
     */
    public List<Note> queryForAllInRect(Box box) {
        return noteDao.queryForAllInRect(box);
    }

    /**
     * Create a Comment with the given parameters without modifying the Note.
     *
     * @param note    The Note associated with the comment.
     * @param action  The action of the comment.
     * @param comment The comment of the comment.
     * @return The created comment.
     */
    public Comment createComment(Note note, String action, String comment) {
        Comment newComment = new Comment();
        newComment.setText(comment);

        switch (action) {
            case NoteActivity.CLOSE:
                newComment.setAction(Comment.ACTION_CLOSE);
                break;

            case NoteActivity.COMMENT:
                newComment.setAction(Comment.ACTION_COMMENT);
                break;

            case NoteActivity.REOPEN:
                newComment.setAction(Comment.ACTION_REOPEN);
                break;

            default:
                newComment.setAction(Comment.ACTION_OPEN);
                break;
        }

        newComment.setNote(note);

        return newComment;
    }

    // *********************************
    // ************ private ************
    // *********************************

    /**
     * Send a {@link io.mapsquare.osmcontributor.core.events.NotesLoadedEvent} containing all the Notes
     * in the Box of the {@link io.mapsquare.osmcontributor.core.events.PleaseLoadNotesEvent}.
     *
     * @param event Event containing the box to load.
     */
    private void loadNotes(PleaseLoadNotesEvent event) {
        bus.post(new NotesLoadedEvent(event.getBox(), queryForAllInRect(event.getBox())));
    }
}
