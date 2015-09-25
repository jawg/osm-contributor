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
package io.mapsquare.osmcontributor.sync.rest;

import io.mapsquare.osmcontributor.sync.dto.osm.OsmDto;
import retrofit.client.Response;
import retrofit.http.Body;
import retrofit.http.GET;
import retrofit.http.POST;
import retrofit.http.PUT;
import retrofit.http.Path;
import retrofit.http.Query;

public interface OsmRestClient {

    @GET("/permissions")
    OsmDto getPermissions();

    @GET("/nodes")
    OsmDto getNode(@Query("nodes") String id);

    /**
     * @param box the limit in space for the request bbox=left,bottom,right,top
     * @return a list of notes (all status) inside the box
     */
    @GET("/notes")
    OsmDto getNotes(@Query("bbox") String box);

    @GET("/ways")
    OsmDto getWay(@Query("ways") long id);

    @GET("/changeset/{id}?include_discussion=true")
    OsmDto getChangeSet(@Path("id") String id);

    /**
     * Create a ChangeSet
     *
     * @param osmDto an OsmDto containing a ChangeSetDto.
     *               It is recommended to add the tags "comment=[description]" and "created-by=[user]"
     *               to the ChangeSetDto.
     * @return the id of the created ChangeSet
     */
    @PUT("/changeset/create")
    String addChangeSet(@Body OsmDto osmDto);

    @PUT("/changeset/{id}/close")
    Response closeChangeSet(@Path("id") String id);

    /**
     * Create a Node
     *
     * @param osmDto OsmDto containing a Node
     * @return the id of the created Node
     */
    @PUT("/node/create")
    String addNode(@Body OsmDto osmDto);

    /**
     * Create a Note
     *
     * @param lat      latitude of the note
     * @param lon      longitude of the note
     * @param text     comment of the note all those filed are needed
     * @param bodyText just because retrofit need a body for POST methods
     * @return the note in a OsmDto
     */
    @POST("/notes")
    OsmDto addNote(@Query("lat") Double lat, @Query("lon") Double lon, @Query("text") String text, @Body String bodyText);

    /**
     * Create a Comment
     *
     * @param id       id of the note that will content this new comment
     * @param text     text of the comment, can't be null
     * @param bodyText just because retrofit need a body for POST methods
     * @return the note with the new comment
     */
    @POST("/notes/{id}/comment")
    OsmDto addComment(@Path("id") String id, @Query("text") String text, @Body String bodyText);

    /**
     * close a note
     *
     * @param id       id of the note to close
     * @param text     the comment, can't be null
     * @param bodyText just because retrofit need a  body for POST methods
     * @return the note with the a new status closed
     */
    @POST("/notes/{id}/close")
    OsmDto closeNote(@Path("id") String id, @Query("text") String text, @Body String bodyText);

    /**
     * reopen a note
     *
     * @param id       id of the note to reopen
     * @param text     the comment, can't be null
     * @param bodyText just because retrofit need a  body for POST methods
     * @return the note with the a new status open
     */
    @POST("/notes/{id}/reopen")
    OsmDto reopenNote(@Path("id") String id, @Query("text") String text, @Body String bodyText);

    /**
     * Create a Way
     *
     * @param osmDto OsmDto containing a Way
     * @return the id of the created Way
     */
    @PUT("/way/create")
    String addWay(@Body OsmDto osmDto);

    /**
     * Update a Node. Returns a 409 if the version doesn't match
     *
     * @param id     the id of the Node to update
     * @param osmDto OsmDto containing a Node
     * @return the version of the Node updated
     */
    @PUT("/node/{id}")
    String updateNode(@Path("id") String id, @Body OsmDto osmDto);

    /**
     * Update a Way. Returns a 409 if the version doesn't match
     *
     * @param id     the id of the Way to update
     * @param osmDto OsmDto containing a Way
     * @return the version of the Way updated
     */
    @PUT("/way/{id}")
    String updateWay(@Path("id") String id, @Body OsmDto osmDto);

    /**
     * Delete a Node.
     * Returns a 409 if the version doesn't match
     * Returns a 410 if the Node was already deleted
     * Returns a 412 if a precondition failed (node still in a way, a relation...)
     *
     * @param id     the id of the Node to delete
     * @param osmDto OsmDto containing a Node
     * @return the version of the Node deleted
     */
    @BODY_DELETE("/node/{id}")
    String deleteNode(@Path("id") String id, @Body OsmDto osmDto);

    /**
     * Delete a Way.
     * Returns a 409 if the version doesn't match
     * Returns a 410 if the Way was already deleted
     * Returns a 412 if a precondition failed (way still in  a relation...)
     *
     * @param id     the id of the Way to delete
     * @param osmDto OsmDto containing a Way
     * @return the version of the Way deleted
     */
    @BODY_DELETE("/way/{id}")
    String deleteWay(@Path("id") String id, @Body OsmDto osmDto);
}


