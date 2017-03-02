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
package io.jawg.osmcontributor.service;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.geometry.LatLngBounds;
import com.mapbox.mapboxsdk.offline.OfflineRegion;
import com.mapbox.mapboxsdk.offline.OfflineRegionError;
import com.mapbox.mapboxsdk.offline.OfflineRegionStatus;
import com.mapbox.mapboxsdk.offline.OfflineTilePyramidRegionDefinition;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import io.jawg.osmcontributor.BuildConfig;
import io.jawg.osmcontributor.OsmTemplateApplication;
import io.jawg.osmcontributor.R;
import io.jawg.osmcontributor.offline.OfflineRegionManager;
import io.jawg.osmcontributor.offline.events.CancelOfflineRegionDownloadEvent;
import io.jawg.osmcontributor.offline.events.OfflineRegionCreatedEvent;

/**
 * @author Tommy Buonomo on 03/08/16.
 */
public class OfflineRegionDownloadService extends IntentService {
    private static final String TAG = "MapOfflineManager";
    public static final String LIST_PARAM = "LIST";
    public static final String SIZE_PARAM = "SIZE_PARAM";
    public static final String REGION_NAME_PARAM = "REGION_NAME";
    private static final String CANCEL_DOWNLOAD = "CANCEL_DOWNLOAD";

    public static final int MIN_ZOOM = 13;
    private static final int MAX_ZOOM = 20;

    private List<OfflineRegion> waitingOfflineRegions;

    private OfflineRegion currentDownloadRegion;

    private boolean deliverStatusUpdate;
    private Intent intent;
    private Map<String, NotificationCompat.Builder> notifications;

    @Inject
    OfflineRegionManager offlineRegionManager;

    @Inject
    EventBus eventBus;

    @Inject
    NotificationManager notificationManager;

    public OfflineRegionDownloadService() {
        super(OfflineRegionDownloadService.class.getSimpleName());
    }

    @Override
    public void onCreate() {
        super.onCreate();
        ((OsmTemplateApplication) getApplication()).getOsmTemplateComponent().inject(this);
        eventBus.register(this);
        waitingOfflineRegions = new ArrayList<>();
        notifications = new HashMap<>();
    }

    @Override
    protected void onHandleIntent(final Intent intent) {
        this.intent = intent;
        offlineRegionManager.listOfflineRegions(new OfflineRegionManager.OnOfflineRegionsListedListener() {
            @Override
            public void onOfflineRegionsListed(List<OfflineRegion> offlineRegions) {
                startDownloadIfNeeded(intent, offlineRegions);
            }
        });
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
                OfflineRegion presentOfflineRegion = containsInOfflineRegion(presentOfflineRegions, bounds);
                if (presentOfflineRegion == null) {
                    // The region has never been downloaded
                    String regionName = intent.getStringExtra(REGION_NAME_PARAM);
                    regionName = regionName == null ? "Region " + (presentOfflineRegions.size() + c)
                            : regionName;
                    c++;
                    downloadOfflineRegion(bounds, regionName);
                } else {
                    //The region is already downloaded, we check if it was completed
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
                    resumeDownloadOfflineRegion(offlineRegion);
                }
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, error);
            }
        });
    }

    public void downloadOfflineRegion(LatLngBounds latLngBounds, final String regionName) {
        final OfflineTilePyramidRegionDefinition definition = new OfflineTilePyramidRegionDefinition(
                BuildConfig.MAP_STYLE_URL,
                latLngBounds,
                MIN_ZOOM,
                MAX_ZOOM,
                this.getResources().getDisplayMetrics().density);

        // Build the notification
        buildNotification(regionName);
        offlineRegionManager.createOfflineRegion(definition, regionName, new OfflineRegionManager.OnOfflineRegionCreatedListener() {
            @Override
            public void onOfflineRegionCreated(OfflineRegion offlineRegion, String regionName) {
                // Monitor the download progress using setObserver
                offlineRegion.setObserver(getOfflineRegionObserver(regionName));
                startDownloadOfflineRegion(offlineRegion);
                eventBus.post(new OfflineRegionCreatedEvent());
            }
        });
    }

    private void resumeDownloadOfflineRegion(OfflineRegion offlineRegion) {
        Log.d(TAG, "resumeDownloadOfflineRegion: " + offlineRegion);
        String regionName = OfflineRegionManager.decodeRegionName(offlineRegion.getMetadata());
        buildNotification(regionName);
        offlineRegion.setObserver(getOfflineRegionObserver(regionName));
        startDownloadOfflineRegion(offlineRegion);
    }

    private void startDownloadOfflineRegion(OfflineRegion offlineRegion) {
        // An area is already downloading, we put the area in waiting
        if (currentDownloadRegion != null) {
            waitingOfflineRegions.add(offlineRegion);
        } else {
            currentDownloadRegion = offlineRegion;
            offlineRegion.setDownloadState(OfflineRegion.STATE_ACTIVE);
            deliverStatusUpdate = true;
        }
    }

    private synchronized void checkNextDownload() {
        if (!waitingOfflineRegions.isEmpty()) {
            startDownloadOfflineRegion(waitingOfflineRegions.get(0));
            waitingOfflineRegions.remove(0);
        }
    }

    private void cancelDownloadOfflineRegion() {
        if (currentDownloadRegion != null) {
            currentDownloadRegion.setDownloadState(OfflineRegion.STATE_INACTIVE);
            deliverStatusUpdate = false;
            currentDownloadRegion = null;
        }
    }

    private OfflineRegion.OfflineRegionObserver getOfflineRegionObserver(final String regionName) {
        final NotificationCompat.Builder builder = notifications.get(regionName);
        return new OfflineRegion.OfflineRegionObserver() {
            int percentage;

            @Override
            public void onStatusChanged(OfflineRegionStatus status) {
                if (deliverStatusUpdate) {
                    // Calculate the download percentage and update the progress bar
                    int newPercent = (int) Math.round(status.getRequiredResourceCount() >= 0 ?
                            (100.0 * status.getCompletedResourceCount() / status.getRequiredResourceCount()) :
                            0.0);
                    if (newPercent != percentage) {
                        percentage = newPercent;
                        builder.setContentText(percentage + "%");
                        builder.setProgress(100, percentage, false);
                        notificationManager.notify(regionName.hashCode(), builder.build());
                    }
                }
                if (status.isComplete()) {
                    // Download complete
                    // When the loop is finished, updates the notification
                    builder.setContentText("Region downloaded successfully")
                            .setProgress(0, 0, false)
                            .mActions
                            .clear();
                    notificationManager.notify(regionName.hashCode(), builder.build());
                    currentDownloadRegion = null;
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

    /**
     * Build the download notification to display
     *
     * @param regionName
     * @return
     */
    public void buildNotification(String regionName) {
        Intent cancelButtonIntent = new Intent(getApplicationContext(), CancelButtonReceiver.class);
        cancelButtonIntent.putExtra(CancelButtonReceiver.MAP_TAG_PARAM, regionName);
        cancelButtonIntent.setAction(CANCEL_DOWNLOAD + regionName.hashCode());

        PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, cancelButtonIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_file_download_white)
                .setContentTitle(this.getString(R.string.notification_download_title) + " " + regionName)
                .addAction(new NotificationCompat.Action(R.drawable.ic_clear_white, getString(R.string.cancel), pendingIntent))
                .setDeleteIntent(pendingIntent);
        notifications.put(regionName, builder);
    }

    /**
     * Check if a region is present in the list with the bounds parameter.
     * @param regions
     * @param bounds
     * @return the OfflineRegion if it's present or null.
     */
    private OfflineRegion containsInOfflineRegion(List<OfflineRegion> regions, LatLngBounds bounds) {
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
                    .include(new LatLng(Double.parseDouble(latLngBoundsStrings.get(2)), Double.parseDouble(latLngBoundsStrings.get(3))))
                    .build();
        }
        return null;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    @SuppressWarnings("unused")
    public void onCancelOfflineRegionEvent(final CancelOfflineRegionDownloadEvent event) {
        if (currentDownloadRegion != null && OfflineRegionManager
                .decodeRegionName(currentDownloadRegion.getMetadata())
                .equals(event.getRegionName())) {
            cancelDownloadOfflineRegion();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
