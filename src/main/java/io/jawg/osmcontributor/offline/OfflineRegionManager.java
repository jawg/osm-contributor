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
package io.jawg.osmcontributor.offline;

import android.util.Log;

import com.mapbox.mapboxsdk.offline.OfflineManager;
import com.mapbox.mapboxsdk.offline.OfflineRegion;
import com.mapbox.mapboxsdk.offline.OfflineRegionDefinition;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * @author Tommy Buonomo on 11/08/16.
 */
@Singleton
public class OfflineRegionManager {
    private static final String TAG = "OfflineRegionManager";
    public static final String JSON_CHARSET = "UTF-8";
    public static final String JSON_FIELD_TAG = "FIELD_TAG";

    private OfflineManager offlineManager;

    @Inject
    public OfflineRegionManager(OfflineManager offlineManager) {
        this.offlineManager = offlineManager;
    }


    public void listOfflineRegions(final OnOfflineRegionsListedListener listener) {
        offlineManager.listOfflineRegions(new OfflineManager.ListOfflineRegionsCallback() {
            @Override
            public void onList(OfflineRegion[] offlineRegions) {
                listener.onOfflineRegionsListed(Arrays.asList(offlineRegions));
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "List offline regions error: " + error);
            }
        });
    }

    public void createOfflineRegion(OfflineRegionDefinition definition,
                                    final String regionName,
                                    final OnOfflineRegionCreatedListener listener) {
        offlineManager.createOfflineRegion(definition,
                encodeRegionName(regionName),
                new OfflineManager.CreateOfflineRegionCallback() {
            @Override
            public void onCreate(OfflineRegion offlineRegion) {
                listener.onOfflineRegionCreated(offlineRegion, regionName);
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "Create offline region error: " + error);
            }
        });
    }


    public void deleteOfflineRegion(final OfflineRegion offlineRegion, final OnOfflineRegionDeletedListener listener) {
        offlineRegion.delete(new OfflineRegion.OfflineRegionDeleteCallback() {
            @Override
            public void onDelete() {
                listener.onOfflineRegionDeleted();
                Log.d(TAG, "deleteOfflineRegion: Region successfully deleted!");
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "Delete offline region error: " + error);
            }
        });
    }

    /**
     * Build the metadata array byte
     * @param regionName
     * @return the byte array of the metadata
     */
    public static byte[] encodeRegionName(String regionName) {
        // Set the metadata
        byte[] metadata;
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put(JSON_FIELD_TAG, regionName);
            String json = jsonObject.toString();
            metadata = json.getBytes(JSON_CHARSET);
        } catch (Exception e) {
            Log.e(TAG, "Failed to encode metadata: " + e.getMessage());
            metadata = null;
        }
        return metadata;
    }

    /**
     * Decode the metadata and return the map tag field
     * @param metadata
     * @return mapTag field
     */
    public static String decodeRegionName(byte[] metadata) {
        String jsonString = new String(metadata);
        try {
            JSONObject json = new JSONObject(jsonString);
            return json.getString(JSON_FIELD_TAG);

        } catch (JSONException e) {
            e.printStackTrace();
            return "";
        }
    }

    public interface OnOfflineRegionsListedListener {
        void onOfflineRegionsListed(List<OfflineRegion> offlineRegions);
    }

    public interface OnOfflineRegionDeletedListener {
        void onOfflineRegionDeleted();
    }

    public interface OnOfflineRegionCreatedListener {
        void onOfflineRegionCreated(OfflineRegion offlineRegion, String regionName);
    }
}
