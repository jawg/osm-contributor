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

/**
 * Rest interface for requests to OpenStreetMap.
 */
public interface OsmRestClient {

    /**
     * Get the permissions granted to the current API connection.
     *
     * @return The list of permissions granted to the current API client. Empty if the client is not authorized.
     */
    @GET("/permissions")
    OsmDto getPermissions();

    /**
     * Get a node from it's OSM id.
     *
     * @param id The id of the node.
     * @return The node.
     */
    @GET("/nodes")
    OsmDto getNode(@Query("nodes") String id);

    /**
     * Get all the notes contained in the box.
     *
     * @param box The limit in space for the request bbox=left,bottom,right,top.
     * @return A list of notes (all status) inside the box.
     */
    @GET("/notes")
    OsmDto getNotes(@Query("bbox") String box);

    /**
     * Get a way from it's OSM id.
     *
     * @param id The id of the way.
     * @return The way.
     */
    @GET("/ways")
    OsmDto getWay(@Query("ways") long id);

    /**
     * Get a changeSet and it's discussion from it's id.
     *
     * @param id The id of the changeSet.
     * @return The changeSet.
     */
    @GET("/changeset/{id}?include_discussion=true")
    OsmDto getChangeSet(@Path("id") String id);

    /**
     * Create a ChangeSet.
     *
     * @param osmDto an OsmDto containing a ChangeSetDto.
     *               It is recommended to add the tags "comment=[description]" and "created-by=[user]"
     *               to the ChangeSetDto.
     * @return The id of the created ChangeSet.
     */
    @PUT("/changeset/create")
    String addChangeSet(@Body OsmDto osmDto);

    /**
     * Close the changeSet.
     *
     * @param id Id of the changeSet to close.
     * @return The response of OSM (nothing, just a HTTP status code).
     */
    @PUT("/changeset/{id}/close")
    Response closeChangeSet(@Path("id") String id);

    /**
     * Create a Node.
     *
     * @param osmDto OsmDto containing a Node.
     * @return The id of the created Node.
     */
    @PUT("/node/create")
    String addNode(@Body OsmDto osmDto);

    /**
     * Create a Note. Do not require to be an authenticated user.
     *
     * @param lat      Latitude of the note.
     * @param lon      Longitude of the note.
     * @param text     Comment of the note all those filed are needed.
     * @param bodyText Just because retrofit need a body for POST methods.
     * @return The note in a OsmDto.
     */
    @POST("/notes")
    OsmDto addNote(@Query("lat") Double lat, @Query("lon") Double lon, @Query("text") String text, @Body String bodyText);

    /**
     * Create a Comment. Do not require to be an authenticated user.
     *
     * @param id       Id of the note that will content this new comment.
     * @param text     Text of the comment, can't be null.
     * @param bodyText Just because retrofit need a body for POST methods.
     * @return The note with the new comment.
     */
    @POST("/notes/{id}/comment")
    OsmDto addComment(@Path("id") String id, @Query("text") String text, @Body String bodyText);

    /**
     * Close a note. Require to be an authenticated user.
     *
     * @param id       Id of the note to close.
     * @param text     The comment, can't be null.
     * @param bodyText Just because retrofit need a body for POST methods.
     * @return The note with the a new status closed.
     */
    @POST("/notes/{id}/close")
    OsmDto closeNote(@Path("id") String id, @Query("text") String text, @Body String bodyText);

    /**
     * Reopen a note. Require to be an authenticated user.
     *
     * @param id       Id of the note to reopen.
     * @param text     The comment, can't be null.
     * @param bodyText Just because retrofit need a body for POST methods.
     * @return The note with the a new status open.
     */
    @POST("/notes/{id}/reopen")
    OsmDto reopenNote(@Path("id") String id, @Query("text") String text, @Body String bodyText);

    /**
     * Create a Way.
     *
     * @param osmDto OsmDto containing a Way.
     * @return The id of the created Way.
     */
    @PUT("/way/create")
    String addWay(@Body OsmDto osmDto);

    /**
     * Update a Node. Returns a 409 if the version doesn't match.
     *
     * @param id     The id of the Node to update.
     * @param osmDto OsmDto containing a Node.
     * @return The version of the Node updated.
     */
    @PUT("/node/{id}")
    String updateNode(@Path("id") String id, @Body OsmDto osmDto);

    /**
     * Update a Way. Returns a 409 if the version doesn't match.
     *
     * @param id     The id of the Way to update.
     * @param osmDto OsmDto containing a Way.
     * @return The version of the Way updated.
     */
    @PUT("/way/{id}")
    String updateWay(@Path("id") String id, @Body OsmDto osmDto);

    /**
     * Delete a Node.
     * Returns a 409 if the version doesn't match.
     * Returns a 410 if the Node was already deleted.
     * Returns a 412 if a precondition failed (node still in a way, a relation...).
     *
     * @param id     The id of the Node to delete.
     * @param osmDto OsmDto containing a Node.
     * @return The version of the Node deleted.
     */
    @BODY_DELETE("/node/{id}")
    String deleteNode(@Path("id") String id, @Body OsmDto osmDto);

    /**
     * Delete a Way.
     * Returns a 409 if the version doesn't match.
     * Returns a 410 if the Way was already deleted.
     * Returns a 412 if a precondition failed (way still in  a relation...).
     *
     * @param id     The id of the Way to delete.
     * @param osmDto OsmDto containing a Way.
     * @return The version of the Way deleted.
     */
    @BODY_DELETE("/way/{id}")
    String deleteWay(@Path("id") String id, @Body OsmDto osmDto);
}


