/**
 * Copyright (C) 2019 Takima
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
package io.jawg.osmcontributor.rest.mappers;


import org.joda.time.DateTime;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;

import io.jawg.osmcontributor.model.entities.Comment;
import io.jawg.osmcontributor.model.entities.Note;
import io.jawg.osmcontributor.rest.dtos.osm.CommentDto;
import io.jawg.osmcontributor.rest.dtos.osm.NoteDto;

public class NoteMapper {

    CommentMapper commentMapper;

    @Inject
    public NoteMapper(CommentMapper commentMapper) {
        this.commentMapper = commentMapper;
    }

    public Note convertNoteDtoToNote(NoteDto dto) {
        if (dto != null) {
            return convertNoteDtosToNotes(Collections.singletonList(dto)).get(0);
        } else {
            return null;
        }
    }

    public List<Note> convertNoteDtosToNotes(List<NoteDto> dtos) {
        List<Note> result = new ArrayList<>();
        if (dtos != null) {
            for (NoteDto dto : dtos) {
                Note note = new Note();
                note.setLatitude(dto.getLat());
                note.setLongitude(dto.getLon());
                note.setBackendId(dto.getId());
                note.setUpdated(false);
                note.setStatus(dto.getStatus());

                DateTime dt = null;
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z", Locale.US);

                try {
                    Date date = simpleDateFormat.parse(dto.getDate_created());
                    dt = new DateTime(date);

                } catch (ParseException ex) {
                    System.out.println("Exception " + ex);
                }
                note.setCreatedDate(dt);

                ArrayList<Comment> comments = new ArrayList<>(dto.getCommentDtoList().size());
                for (CommentDto commentDto : dto.getCommentDtoList()) {
                    Comment comment = new Comment();
                    comment.setNote(note);
                    comment.setUpdated(false);
                    comment.setAction(commentDto.getAction());
                    comment.setText(commentDto.getText());

                    try {
                        Date date = simpleDateFormat.parse(commentDto.getDate());
                        dt = new DateTime(date);
                    } catch (ParseException ex) {
                        System.out.println("Exception " + ex);
                    }
                    comment.setCreatedDate(dt);
                    comments.add(comment);

                }
                note.setComments(comments);
                result.add(note);
            }
        }
        return result;
    }

    public List<NoteDto> convertNotesToNoteDtos(List<Note> notes) {
        List<NoteDto> result = new ArrayList<>();
        for (Note note : notes) {
            result.add(convertNoteToNoteDto(note));
        }

        return result;
    }

    /**
     * @param note the note to convert
     * @return the converted NoteDto
     */
    public NoteDto convertNoteToNoteDto(Note note) {
        NoteDto noteDto = new NoteDto();

        noteDto.setLat(note.getLatitude());
        noteDto.setLon(note.getLongitude());
        noteDto.setId(note.getBackendId());
        noteDto.setStatus(note.getStatus());

        noteDto.setCommentDtoList(commentMapper.convertFromComment(note.getComments()));

        return noteDto;
    }

}
