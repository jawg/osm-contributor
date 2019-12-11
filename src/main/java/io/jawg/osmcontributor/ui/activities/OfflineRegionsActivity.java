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
package io.jawg.osmcontributor.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.geometry.LatLngBounds;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.offline.OfflineRegion;
import com.mapbox.mapboxsdk.offline.OfflineRegionStatus;
import com.mapbox.mapboxsdk.offline.OfflineTilePyramidRegionDefinition;
import com.yarolegovich.lovelydialog.LovelyTextInputDialog;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.jawg.osmcontributor.BuildConfig;
import io.jawg.osmcontributor.OsmTemplateApplication;
import io.jawg.osmcontributor.R;
import io.jawg.osmcontributor.offline.OfflineRegionManager;
import io.jawg.osmcontributor.offline.events.OfflineRegionCreatedEvent;
import io.jawg.osmcontributor.service.OfflineRegionDownloadService;
import io.jawg.osmcontributor.ui.adapters.OfflineRegionItem;
import io.jawg.osmcontributor.ui.adapters.OfflineRegionsAdapter;
import io.jawg.osmcontributor.ui.listeners.RecyclerItemClickListener;
import io.jawg.osmcontributor.ui.managers.tutorial.OfflineTutoManager;
import io.jawg.osmcontributor.ui.managers.tutorial.TutorialManager;
import io.jawg.osmcontributor.utils.OsmAnswers;

/**
 * @author Tommy Buonomo on 08/08/16.
 */
public class OfflineRegionsActivity extends AppCompatActivity {
    private static final String TAG = "OfflineRegionsActivity";

    private static final int PADDING_TOP_BOUNDS = 100;
    private static final int PADDING_OTHER_BOUNDS = 20;
    private static final int MIN_DOWNLOAD_ZOOM = 11;

    private enum Mode {
        STATUS_COMPLETE(false, View.VISIBLE, View.VISIBLE, View.GONE, View.GONE, View.GONE, View.GONE),
        STATUS_INCOMPLETE(false, View.VISIBLE, View.VISIBLE, View.GONE, View.VISIBLE, View.GONE, View.GONE),
        NO_REGIONS(false, View.GONE, View.VISIBLE, View.VISIBLE, View.GONE, View.GONE, View.GONE),
        ADD_REGION(true, View.GONE, View.GONE, View.GONE, View.GONE, View.VISIBLE, View.GONE);

        private final int downloadButtonVisibility;

        private final int downloadNewButtonVisibility;
        private final int editMenuVisibility;
        private final int addMenuVisibility;
        private final int messageTextVisibility;
        private final int renameButtonVisibility;
        private final boolean mapGesturesEnabled;
        Mode(boolean mapGesturesEnabled,
             int editMenuVisibility,
             int addButtonVisibility,
             int messageTextVisibility,
             int downloadButtonVisibility,
             int downloadNewButtonVisibility,
             int renameButtonVisibility) {
            this.mapGesturesEnabled = mapGesturesEnabled;
            this.editMenuVisibility = editMenuVisibility;
            this.addMenuVisibility = addButtonVisibility;
            this.messageTextVisibility = messageTextVisibility;
            this.downloadButtonVisibility = downloadButtonVisibility;
            this.downloadNewButtonVisibility = downloadNewButtonVisibility;
            this.renameButtonVisibility = renameButtonVisibility;
        }

    }
    @Inject
    OfflineRegionManager offlineRegionManager;

    @Inject
    EventBus eventBus;

    @BindView(R.id.offline_regions_view)
    View activityView;

    @BindView(R.id.offline_regions_list)
    RecyclerView offlineRegionsRecyclerView;

    @BindView(R.id.mapview)
    MapView mapView;

    @BindView(R.id.offline_regions_empty_text)
    TextView messageTextView;

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.edit_region_floating_action_menu)
    FloatingActionMenu editRegionFloatingActionMenu;

    @BindView(R.id.add_offline_region_floating_button)
    FloatingActionButton addRegionFloatingActionButton;

    @BindView(R.id.rename_region_floating_button)
    FloatingActionButton renameRegionFloatingActionButton;

    @BindView(R.id.delete_region_floating_button)
    FloatingActionButton deleteRegionFloatingActionButton;

    @BindView(R.id.download_region_floating_button)
    FloatingActionButton downloadRegionFloatingActionButton;

    @BindView(R.id.download_new_region_floating_button)
    FloatingActionButton downloadNewRegionFloatingActionButton;

    private OfflineRegionItem selectedRegionItem;
    private MapboxMap mapboxMap;
    private OfflineRegionsAdapter adapter;
    private Mode currentMode;

    private OfflineTutoManager offlineTutoManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_offline_regions);
        ((OsmTemplateApplication) getApplication()).getOsmTemplateComponent().inject(this);
        ButterKnife.bind(this);

        OsmAnswers.visitedActivity("Page des cartes hors ligne");

        offlineTutoManager = new OfflineTutoManager(this, TutorialManager.forceDisplayOfflineTuto);

        switchMode(Mode.NO_REGIONS);

        initToolbar();
        initMapView(savedInstanceState);
        initRecyclerView();
        initOfflineRegions();

        offlineTutoManager.startTuto();
        initFloatingActionButtons();
    }

    private void initToolbar() {
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    private void initMapView(Bundle savedInstanceState) {
        mapView.onCreate(savedInstanceState);

        mapView.setStyleUrl(BuildConfig.MAP_STYLE);
        mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(MapboxMap mapboxMap) {
                OfflineRegionsActivity.this.mapboxMap = mapboxMap;
                mapboxMap.getUiSettings().setCompassEnabled(false);
                mapboxMap.getUiSettings().setRotateGesturesEnabled(false);
                mapboxMap.getUiSettings().setTiltGesturesEnabled(false);
                enableMapGestures(false);
            }
        });
    }

    private void initRecyclerView() {
        adapter = new OfflineRegionsAdapter(new ArrayList<OfflineRegionItem>());
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        offlineRegionsRecyclerView.setLayoutManager(linearLayoutManager);
        offlineRegionsRecyclerView.setAdapter(adapter);
        offlineRegionsRecyclerView.addOnItemTouchListener(new RecyclerItemClickListener(this, offlineRegionsRecyclerView, new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                selectOfflineRegion(position);
            }

            @Override
            public void onLongItemClick(View view, int position) {

            }
        }));
    }

    private void initOfflineRegions() {
        refreshOfflineRegions();
    }

    private void initFloatingActionButtons() {
        deleteRegionFloatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (selectedRegionItem != null && selectedRegionItem.getOfflineRegion() != null) {
                    int position = adapter.getOfflineRegionItems().indexOf(selectedRegionItem);
                    offlineRegionManager.deleteOfflineRegion(selectedRegionItem.getOfflineRegion(),
                            getOfflineRegionDeletedListener());
                    adapter.removeOfflineRegion(selectedRegionItem);
                    if (adapter.getItemCount() == 0) {
                        switchMode(Mode.NO_REGIONS);
                    } else {
                        int newPosition = position == 0 ?
                                0 : position == adapter.getItemCount() ?
                                adapter.getItemCount() - 1 : position - 1;
                        selectOfflineRegion(newPosition);
                    }
                    closeEditMenu();
                }
            }
        });

        downloadRegionFloatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (selectedRegionItem != null && selectedRegionItem.getOfflineRegion() != null) {
                    Intent intent = new Intent(OfflineRegionsActivity.this, OfflineRegionDownloadService.class);
                    intent.putStringArrayListExtra(OfflineRegionDownloadService.LIST_PARAM + 0, convertDoubleList(selectedRegionItem.getOfflineRegion(), true));
                    intent.putExtra(OfflineRegionDownloadService.SIZE_PARAM, 1);
                    startService(intent);
                    closeEditMenu();
                }
            }
        });

        addRegionFloatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switchMode(Mode.ADD_REGION);
            }
        });

        downloadNewRegionFloatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mapboxMap.getCameraPosition().zoom < MIN_DOWNLOAD_ZOOM) {
                    Snackbar snackbar = Snackbar.make(activityView, R.string.region_to_large, Snackbar.LENGTH_LONG);
                    TextView textView = (TextView) snackbar.getView().findViewById(android.support.design.R.id.snackbar_text);
                    textView.setTextColor(ContextCompat.getColor(OfflineRegionsActivity.this, R.color.error_color));
                    snackbar.show();
                    return;
                }

                final Intent intent = new Intent(OfflineRegionsActivity.this, OfflineRegionDownloadService.class);

                new LovelyTextInputDialog(OfflineRegionsActivity.this)
                        .setTopColorRes(R.color.colorPrimary)
                        .setTitle(R.string.enter_region_name)
                        .setIcon(R.drawable.ic_rename)
                        .setInputFilter(R.string.region_name_error, new LovelyTextInputDialog.TextFilter() {
                            @Override
                            public boolean check(String text) {
                                return text.length() < 20;
                            }
                        })
                        .setConfirmButton(R.string.ok, new LovelyTextInputDialog.OnTextInputConfirmListener() {
                            @Override
                            public void onTextInputConfirmed(String text) {
                                intent.putExtra(OfflineRegionDownloadService.REGION_NAME_PARAM, text);
                                LatLngBounds bounds = mapboxMap.getProjection().getVisibleRegion().latLngBounds;
                                intent.putStringArrayListExtra(OfflineRegionDownloadService.LIST_PARAM + 0, convertDoubleList(bounds, false));
                                intent.putExtra(OfflineRegionDownloadService.SIZE_PARAM, 1);
                                startService(intent);
                            }
                        })
                        .show();
            }
        });
    }

    public ArrayList<String> convertDoubleList(OfflineRegion region, boolean inversed) {
        OfflineTilePyramidRegionDefinition definition = (OfflineTilePyramidRegionDefinition) region.getDefinition();
        return convertDoubleList(definition.getBounds(), inversed);
    }

    public ArrayList<String> convertDoubleList(LatLngBounds bounds, boolean inversed) {
        ArrayList<String> latLngBounds = new ArrayList<>();
        if (inversed) {
            latLngBounds.add(String.valueOf(bounds.getLatSouth()));
            latLngBounds.add(String.valueOf(bounds.getLonWest()));
            latLngBounds.add(String.valueOf(bounds.getLatNorth()));
            latLngBounds.add(String.valueOf(bounds.getLonEast()));
        } else {
            latLngBounds.add(String.valueOf(bounds.getLonWest()));
            latLngBounds.add(String.valueOf(bounds.getLatSouth()));
            latLngBounds.add(String.valueOf(bounds.getLonEast()));
            latLngBounds.add(String.valueOf(bounds.getLatNorth()));
        }
        return latLngBounds;
    }

    public OfflineRegionManager.OnOfflineRegionDeletedListener getOfflineRegionDeletedListener() {
        return new OfflineRegionManager.OnOfflineRegionDeletedListener() {
            @Override
            public void onOfflineRegionDeleted() {
                Snackbar.make(activityView, R.string.offline_regions_deleted, Snackbar.LENGTH_LONG).show();
            }
        };
    }

    private void refreshOfflineRegions() {
        offlineRegionManager.listOfflineRegions(new OfflineRegionManager.OnOfflineRegionsListedListener() {
            @Override
            public void onOfflineRegionsListed(List<OfflineRegion> offlineRegions) {
                for (final OfflineRegion region : offlineRegions) {
                    region.getStatus(new OfflineRegion.OfflineRegionStatusCallback() {
                        @Override
                        public void onStatus(OfflineRegionStatus status) {
                            OfflineRegionItem regionItem = new OfflineRegionItem(region, status);
                            if (!adapter.getOfflineRegionItems().contains(regionItem)) {
                                adapter.addOfflineRegion(regionItem);
                                if (adapter.getItemCount() == 1) {
                                    selectOfflineRegion(0);
                                }
                            }
                        }

                        @Override
                        public void onError(String error) {
                            Log.e(TAG, "Refresh Offline Regions error: " + error);
                        }
                    });
                }
            }
        });
    }

    private void selectOfflineRegion(int position) {
        if (selectedRegionItem != null) {
            selectedRegionItem.setSelected(false);
        }
        selectedRegionItem = adapter.getOfflineRegion(position);
        selectedRegionItem.setSelected(true);
        OfflineTilePyramidRegionDefinition definition =
                (OfflineTilePyramidRegionDefinition) selectedRegionItem
                        .getOfflineRegion()
                        .getDefinition();
        animateCameraToBounds(definition.getBounds());
        if (selectedRegionItem.getStatus().isComplete()) {
            switchMode(Mode.STATUS_COMPLETE);
        } else {
            switchMode(Mode.STATUS_INCOMPLETE);
        }
        closeEditMenu();
        adapter.notifyDataSetChanged();
    }

    private void switchMode(final Mode mode) {
        if (mode.equals(currentMode)) {
            return;
        }
        enableMapGestures(mode.mapGesturesEnabled);
        editRegionFloatingActionMenu.setVisibility(mode.editMenuVisibility);
        addRegionFloatingActionButton.setVisibility(mode.addMenuVisibility);
        messageTextView.setVisibility(mode.messageTextVisibility);
        downloadNewRegionFloatingActionButton.setVisibility(mode.downloadNewButtonVisibility);
        downloadRegionFloatingActionButton.setVisibility(mode.downloadButtonVisibility);
        renameRegionFloatingActionButton.setVisibility(mode.renameButtonVisibility);

        currentMode = mode;
    }

    private void enableMapGestures(boolean enable) {
        if (mapboxMap != null) {
            mapboxMap.getUiSettings().setScrollGesturesEnabled(enable);
            mapboxMap.getUiSettings().setZoomGesturesEnabled(enable);
        }
    }

    private void closeEditMenu() {
        if (editRegionFloatingActionMenu.isOpened()) {
            editRegionFloatingActionMenu.toggle(true);
        }
    }

    private void animateCameraToBounds(final LatLngBounds bounds) {
        mapboxMap.easeCamera(CameraUpdateFactory
                .newLatLngBounds(inverseLatLngBounds(bounds),
                        PADDING_OTHER_BOUNDS,
                        PADDING_TOP_BOUNDS,
                        PADDING_OTHER_BOUNDS,
                        PADDING_OTHER_BOUNDS), 500);
    }

    private LatLngBounds inverseLatLngBounds(LatLngBounds latLngBounds) {
        return new LatLngBounds.Builder()
                .include(new LatLng(latLngBounds.getLonEast(), latLngBounds.getLatSouth()))
                .include(new LatLng(latLngBounds.getLonWest(), latLngBounds.getLatNorth()))
                .build();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    @SuppressWarnings("unused")
    public void onOfflineRegionCreatedEvent(final OfflineRegionCreatedEvent event) {
        if (adapter != null) {
            refreshOfflineRegions();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
        eventBus.unregister(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
        eventBus.register(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

}
