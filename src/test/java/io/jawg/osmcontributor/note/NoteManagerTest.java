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
package io.jawg.osmcontributor.note;


import com.j256.ormlite.android.apptools.OpenHelperManager;

import org.joda.time.DateTime;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;

import java.util.ArrayList;
import java.util.List;

import io.jawg.osmcontributor.modules.DaggerOsmTemplateComponent;
import io.jawg.osmcontributor.modules.OsmTemplateComponent;
import io.jawg.osmcontributor.modules.OsmTemplateModule;
import io.jawg.osmcontributor.model.entities.Comment;
import io.jawg.osmcontributor.model.entities.Note;
import io.jawg.osmcontributor.ui.managers.NoteManager;

import static org.fest.assertions.api.Assertions.assertThat;


@RunWith(RobolectricTestRunner.class)
public class NoteManagerTest {

    OsmTemplateComponent component;

    @Before
    public void before() {
        component = DaggerOsmTemplateComponent.builder()
                .osmTemplateModule(new OsmTemplateModule(Robolectric.application)).build();
    }

    @After
    public void after() {
        OpenHelperManager.releaseHelper();
    }

    @Test
    public void testSaveAndQuery() {
        NoteManager noteManager = component.getNoteManager();
        Note note = getNote(1);
        Note saved = noteManager.saveNote(note);
        Note queried = noteManager.queryForId(saved.getId());
        Comment queriedComment = ((ArrayList<Comment>) queried.getComments()).get(0);

        assertThat(queried.getLatitude()).isEqualTo(42.0);
        assertThat(queried.getLongitude()).isEqualTo(73.0);
        assertThat(queried.getUpdated()).isTrue();
        assertThat(queriedComment.getText()).isEqualTo("firstComment1");
    }

    @Test
    public void testBulkSaveAndBulkUpdate() {
        NoteManager noteManager = component.getNoteManager();

        // try to save and then update 1000 notes.
        // 1000 because it can happen in real life and pose problems if we try to do an "IN" sql clause
        List<Note> notes = new ArrayList<>(1000);
        for (int i = 0; i < 1000; i++) {
            notes.add(getNote(i));
        }
        noteManager.saveNotes(notes);
        for (Note note : notes) {
            assertThat(note.getId()).isNotNull();
        }
        for (Note note : notes) {
            Comment newComment = new Comment();
            newComment.setText("secondComment");
            newComment.setAction(Comment.ACTION_OPEN);
            newComment.setNote(note);
            newComment.setUpdated(true);
            newComment.setCreatedDate(new DateTime());

            note.getComments().clear();
            note.getComments().add(newComment);
        }

        List<Note> savedNotes = noteManager.saveNotes(notes);

        for (Note note : savedNotes) {
            assertThat(note.getComments()).hasSize(1);
            Comment com = ((ArrayList<Comment>) note.getComments()).get(0);
            assertThat(com.getText()).isEqualTo("secondComment");
        }
    }

    private Note getNote(int i) {
        Note note = new Note();
        note.setLatitude(42.0);
        note.setLongitude(73.0);
        note.setUpdated(true);

        Comment newComment = new Comment();
        newComment.setText("firstComment" + i);
        newComment.setAction(Comment.ACTION_OPEN);
        newComment.setNote(note);
        newComment.setUpdated(true);
        newComment.setCreatedDate(new DateTime());

        note.getComments().add(newComment);
        note.setStatus(Note.STATUS_SYNC);

        return note;
    }

}