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
package io.mapsquare.osmcontributor.ui.activities;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.geometry.LatLngBounds;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.offline.OfflineManager;
import com.mapbox.mapboxsdk.offline.OfflineRegion;
import com.mapbox.mapboxsdk.offline.OfflineRegionStatus;
import com.mapbox.mapboxsdk.offline.OfflineTilePyramidRegionDefinition;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.mapsquare.osmcontributor.OsmTemplateApplication;
import io.mapsquare.osmcontributor.R;
import io.mapsquare.osmcontributor.ui.adapters.OfflineRegionsAdapter;
import io.mapsquare.osmcontributor.ui.listeners.RecyclerItemClickListener;

/**
 * @author Tommy Buonomo on 08/08/16.
 */
public class OfflineRegionsActivity extends AppCompatActivity {
    private static final String TAG = "OfflineRegionsActivity";

    private static final int PADDING_TOP_BOUNDS = 100;
    private static final int PADDING_OTHER_BOUNDS = 20;

    @Inject
    OfflineManager offlineManager;

    @Inject
    EventBus eventBus;

    @BindView(R.id.offline_regions_list)
    RecyclerView offlineRegionsRecyclerView;

    @BindView(R.id.mapview)
    MapView mapView;

    @BindView(R.id.offline_regions_empty_text)
    TextView emptyListTextView;

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    private MapboxMap mapboxMap;
    private OfflineRegionsAdapter adapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_offline_regions);
        ((OsmTemplateApplication) getApplication()).getOsmTemplateComponent().inject(this);
        ButterKnife.bind(this);

        initToolbar();
        initMapView(savedInstanceState);
        initRecyclerView();
        initOfflineRegions();
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
        mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(MapboxMap mapboxMap) {
                OfflineRegionsActivity.this.mapboxMap = mapboxMap;
            }
        });
    }

    private void initRecyclerView() {
        adapter = new OfflineRegionsAdapter(new ArrayList<OfflineRegion>());
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

    private void selectOfflineRegion(int position) {
        OfflineTilePyramidRegionDefinition definition =
                (OfflineTilePyramidRegionDefinition) adapter.getOfflineRegion(position).getDefinition();
        animateCameraToBounds(definition.getBounds());

        adapter.setSelectedPosition(position);
    }

    private void initOfflineRegions() {
        offlineManager.listOfflineRegions(new OfflineManager.ListOfflineRegionsCallback() {
            @Override
            public void onList(OfflineRegion[] offlineRegions) {
                for (final OfflineRegion region : offlineRegions) {
                    region.getStatus(new OfflineRegion.OfflineRegionStatusCallback() {
                        @Override
                        public void onStatus(OfflineRegionStatus status) {
                            if (status.isComplete()) {
                                adapter.addOfflineRegion(region);
                                if (adapter.getItemCount() == 1) {
                                    selectOfflineRegion(0);
                                }
                            }
                        }

                        @Override
                        public void onError(String error) {
                            Log.e(TAG, "onError: " + error);
                        }
                    });
                }
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "listOfflineRegions: " + error);
            }
        });
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
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
