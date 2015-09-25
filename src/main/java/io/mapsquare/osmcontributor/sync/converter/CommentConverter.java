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
package io.mapsquare.osmcontributor.sync.converter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.inject.Inject;

import io.mapsquare.osmcontributor.core.model.Comment;
import io.mapsquare.osmcontributor.sync.dto.osm.CommentDto;

public class CommentConverter {

    @Inject
    public CommentConverter() {
    }

    public List<CommentDto> convertFromComment(Collection<Comment> comments) {
        List<CommentDto> result = new ArrayList<>();
        for (Comment comment : comments) {
            CommentDto commentDto = new CommentDto();

            commentDto.setText(comment.getText());
            commentDto.setAction(comment.getAction());

            result.add(commentDto);
        }
        return result;
    }
}