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
package io.mapsquare.osmcontributor.service;

import android.app.Activity;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.geometry.LatLngBounds;
import com.mapbox.mapboxsdk.offline.OfflineManager;
import com.mapbox.mapboxsdk.offline.OfflineRegion;
import com.mapbox.mapboxsdk.offline.OfflineRegionError;
import com.mapbox.mapboxsdk.offline.OfflineRegionStatus;
import com.mapbox.mapboxsdk.offline.OfflineTilePyramidRegionDefinition;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.mapsquare.osmcontributor.R;

/**
 * @author Tommy Buonomo on 03/08/16.
 */
public class OfflineAreaDownloadService extends Service {
    public static final String LIST_PARAM = "LIST";
    public static final String SIZE_PARAM = "SIZE_PARAM";

    private static final String TAG = "MapOfflineManager";
    private static final int MIN_ZOOM = 17;

    private static final int MAX_ZOOM = 21;
    public static final String JSON_CHARSET = "UTF-8";
    public static final String JSON_FIELD_TAG = "FIELD_TAG";

    private OfflineManager offlineManager;

    private NotificationManager notificationManager;

    private List<WaitingAreaDownload> waitingAreaDownloads;
    private WaitingAreaDownload currentDownloadArea;

    @Override
    public void onCreate() {
        super.onCreate();
        offlineManager = OfflineManager.getInstance(this);
        notificationManager = (NotificationManager) getSystemService(Activity.NOTIFICATION_SERVICE);
        waitingAreaDownloads = new ArrayList<>();
    }

    @Override
    public int onStartCommand(final Intent intent, int flags, int startId) {
        offlineManager.listOfflineRegions(new OfflineManager.ListOfflineRegionsCallback() {
            @Override
            public void onList(OfflineRegion[] offlineRegions) {
                Log.i(TAG, "onList: " + Arrays.toString(offlineRegions));
                startDownloadIfNeeded(intent, Arrays.asList(offlineRegions));
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, error);
            }
        });

        return START_NOT_STICKY;
    }

    private void startDownloadIfNeeded(Intent intent, final List<OfflineRegion> presentOfflineRegions) {
        if (intent == null) {
            return;
        }
        final int size = intent.getIntExtra(SIZE_PARAM, -1);
        if (size != -1) {
            int c = 0;
            // There is some regions to download
            for (int i = 0; i < size; i++) {
                ArrayList<String> areasString = intent.getStringArrayListExtra(LIST_PARAM + i);
                LatLngBounds bounds = convertToLatLngBounds(areasString);
                OfflineRegion presentOfflineRegion = containsBoundsInOfflineRegion(presentOfflineRegions, bounds);
                if (presentOfflineRegion == null) {
                    // The region has never been downloaded
                    c++;
                    downloadOfflineRegion(bounds, "Region " + (presentOfflineRegions.size() + c));
                } else {
                    //The region is already downloaded, we check is it is completed
                    checkIfRegionDownloadIsCompleted(presentOfflineRegion);
                }
            }
        }
    }

    private void checkIfRegionDownloadIsCompleted(final OfflineRegion offlineRegion) {
        offlineRegion.getStatus(new OfflineRegion.OfflineRegionStatusCallback() {
            @Override
            public void onStatus(OfflineRegionStatus status) {
                if (!status.isComplete() && status.getDownloadState() != OfflineRegion.STATE_ACTIVE) {
                    resumeDownloadOfflineRegion(offlineRegion, status);
                }
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, error);
            }
        });
    }

    /**
     * Check if a region is present in the list with the bounds parameter.
     * @param regions
     * @param bounds
     * @return the OfflineRegion if it's present or null.
     */
    private OfflineRegion containsBoundsInOfflineRegion(List<OfflineRegion> regions, LatLngBounds bounds) {
        for (OfflineRegion offlineRegion : regions) {
            if (((OfflineTilePyramidRegionDefinition) offlineRegion.getDefinition()).getBounds().equals(bounds)) {
                return offlineRegion;
            }
        }
        return null;
    }


    private LatLngBounds convertToLatLngBounds(List<String> latLngBoundsStrings) {
        if (latLngBoundsStrings.size() == 4) {
            return new LatLngBounds.Builder()
                    .include(new LatLng(Double.parseDouble(latLngBoundsStrings.get(0)), Double.parseDouble(latLngBoundsStrings.get(1))))
                    .include(new LatLng(Double.parseDouble(latLngBoundsStrings.get(1)), Double.parseDouble(latLngBoundsStrings.get(2))))
                    .build();
        }
        return null;
    }

    public void downloadOfflineRegion(LatLngBounds latLngBounds, final String mapTag) {
        OfflineTilePyramidRegionDefinition definition = new OfflineTilePyramidRegionDefinition(
                getString(R.string.map_style_url),
                latLngBounds,
                MIN_ZOOM,
                MAX_ZOOM,
                this.getResources().getDisplayMetrics().density);

        byte[] metadata = encodeMetadata(mapTag);

        final NotificationCompat.Builder builder = buildNotification(mapTag);
        WaitingAreaDownload area = new WaitingAreaDownload(mapTag, builder, metadata, definition);

        downloadWaitingDownloadArea(area);
    }

    private void resumeDownloadOfflineRegion(OfflineRegion offlineRegion, OfflineRegionStatus status) {
        Log.i(TAG, "resumeDownloadOfflineRegion: " + offlineRegion);
        String mapTag = decodeMetadata(offlineRegion.getMetadata());
        WaitingAreaDownload areaDownload = new WaitingAreaDownload(
                mapTag,
                buildNotification(mapTag),
                offlineRegion.getMetadata(),
                (OfflineTilePyramidRegionDefinition) offlineRegion.getDefinition());

        offlineRegion.setObserver(getOfflineRegionObserver(areaDownload));
        offlineRegion.setDownloadState(OfflineRegion.STATE_ACTIVE);
    }

    private void downloadWaitingDownloadArea(final WaitingAreaDownload area) {
        // An area is already downloading, we put the area in waiting
        if (currentDownloadArea != null) {
            waitingAreaDownloads.add(area);
            return;
        }
        currentDownloadArea = area;
        // Create the region asynchronously
        offlineManager.createOfflineRegion(
                area.getDefinition(),
                area.getMetadata(),
                new OfflineManager.CreateOfflineRegionCallback() {
                    @Override
                    public void onCreate(OfflineRegion offlineRegion) {
                        // Monitor the download progress using setObserver
                        offlineRegion.setDownloadState(OfflineRegion.STATE_ACTIVE);
                        offlineRegion.setObserver(getOfflineRegionObserver(area));
                    }

                    @Override
                    public void onError(String error) {
                        Log.e(TAG, "Error: " + error);
                    }
                });
    }

    private OfflineRegion.OfflineRegionObserver getOfflineRegionObserver(final WaitingAreaDownload area) {
        final NotificationCompat.Builder builder = area.getBuilder();
        return new OfflineRegion.OfflineRegionObserver() {
            @Override
            public void onStatusChanged(OfflineRegionStatus status) {
                // Calculate the download percentage and update the progress bar
                int percentage = (int) Math.round(status.getRequiredResourceCount() >= 0 ?
                        (100.0 * status.getCompletedResourceCount() / status.getRequiredResourceCount()) :
                        0.0);
                builder.setContentText(percentage + "%");
                builder.setProgress(100, percentage, false);
                notificationManager.notify(area.getMapTag().hashCode(), builder.build());
                if (status.isComplete()) {
                    // Download complete
                    // When the loop is finished, updates the notification
                    builder.setContentText("Region downloaded successfully")
                            .setProgress(0, 0, false);
                    notificationManager.notify(area.getMapTag().hashCode(), builder.build());
                    currentDownloadArea = null;
                    checkNextDownload();
                }
            }

            @Override
            public void onError(OfflineRegionError error) {
                // If an error occurs, print to logcat
                Log.e(TAG, "onError reason: " + error.getReason());
                Log.e(TAG, "onError message: " + error.getMessage());
            }

            @Override
            public void mapboxTileCountLimitExceeded(long limit) {
                // Notify if offline region exceeds maximum tile count
                Log.e(TAG, "Mapbox tile count limit exceeded: " + limit);
            }
        };
    }

    private void checkNextDownload() {
        if (!waitingAreaDownloads.isEmpty()) {
            downloadWaitingDownloadArea(waitingAreaDownloads.get(0));
            waitingAreaDownloads.remove(0);
        }
    }

    /**
     * Build the metadata array byte
     * @param mapTag
     * @return the byte array of the metadata
     */
    private byte[] encodeMetadata(String mapTag) {
        // Set the metadata
        byte[] metadata;
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put(JSON_FIELD_TAG, mapTag);
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
    private String decodeMetadata(byte[] metadata) {
        String jsonString = new String(metadata);
        try {
            JSONObject json = new JSONObject(jsonString);
            return json.getString(JSON_FIELD_TAG);

        } catch (JSONException e) {
            e.printStackTrace();
            return "";
        }
    }

    /**
     * Build the download notification to display
     *
     * @param mapTag
     * @return
     */
    public NotificationCompat.Builder buildNotification(String mapTag) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_file_download_white)
                .setContentTitle(this.getString(R.string.notification_download_title) + " " + mapTag);
        return builder;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
