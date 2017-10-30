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
package io.jawg.osmcontributor.rest;

import java.util.List;

import io.jawg.osmcontributor.model.entities.Poi;
import io.jawg.osmcontributor.model.entities.PoiType;
import io.jawg.osmcontributor.utils.Box;

/**
 * Interface representing the application's backend.
 */
public interface Backend {
    /**
     * Represent the status of the request.
     */
    enum ModificationStatus {
        SUCCESS, FAILURE_CONFLICT, FAILURE_NOT_EXISTING, FAILURE_UNKNOWN
    }

    /**
     * Class representing the result of a creation request.
     */
    class CreationResult {
        /**
         * Status of the request.
         */
        private final ModificationStatus status;
        /**
         * Backend id of the newly created Poi.
         */
        private final String backendId;

        public CreationResult(ModificationStatus status, String backendId) {
            this.backendId = backendId;
            this.status = status;
        }

        public String getBackendId() {
            return backendId;
        }

        public ModificationStatus getStatus() {
            return status;
        }
    }

    /**
     * Class representing the result of an update request.
     */
    class UpdateResult {
        /**
         * Status of the request.
         */
        private final ModificationStatus status;
        /**
         * New version of the Poi.
         */
        private final String version;

        public UpdateResult(ModificationStatus status, String version) {
            this.version = version;
            this.status = status;
        }

        public String getVersion() {
            return version;
        }

        public ModificationStatus getStatus() {
            return status;
        }
    }

    /**
     * Initialize a transaction with the backend.
     *
     * @param comment the comment to add to the transaction
     * @return a non null id for the transaction, or null is the transaction couldn't be created and we shouldn't proceed
     */
    String initializeTransaction(String comment);

    /**
     * Download all the POIs contained in the area delimited by the box.
     *
     * @param box The bounds of the area.
     * @return The list of downloaded POIs.
     */
    List<Poi> getPoisInBox(final Box box) throws NetworkException;

    /**
     * Download a Poi from the backend by its id.
     *
     * @param backendId The backend id of the Poi to download.
     * @return The downloaded Poi.
     */
    Poi getPoiById(String backendId);

    /**
     * Add a Poi to the backend.
     *
     * @param poi           The Poi to add to the backend.
     * @param transactionId The transaction in which the addition must be done.
     * @return The result of the creation.
     */
    CreationResult addPoi(Poi poi, String transactionId);

    /**
     * Update a Poi in the backend.
     *
     * @param poi           The Poi to update in the backend.
     * @param transactionId The transaction in which the update must be done.
     * @return The result of the update.
     */
    UpdateResult updatePoi(Poi poi, String transactionId);

    /**
     * Delete a Poi of the backend.
     *
     * @param poi           The Poi to delete.
     * @param transactionId The transaction in which the Poi is sent.
     * @return The status of the deletion.
     */
    ModificationStatus deletePoi(Poi poi, String transactionId);

    /**
     * Download the list of PoiType from the backend.
     *
     * @return The list of PoiTypes.
     */
    List<PoiType> getPoiTypes();


}
