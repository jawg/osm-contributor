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
package io.mapsquare.osmcontributor.map;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;
import com.github.amlcurran.showcaseview.ShowcaseView;
import com.github.amlcurran.showcaseview.targets.ViewTarget;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.mapbox.mapboxsdk.api.ILatLng;
import com.mapbox.mapboxsdk.events.MapListener;
import com.mapbox.mapboxsdk.events.RotateEvent;
import com.mapbox.mapboxsdk.events.ScrollEvent;
import com.mapbox.mapboxsdk.events.ZoomEvent;
import com.mapbox.mapboxsdk.geometry.BoundingBox;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.overlay.Icon;
import com.mapbox.mapboxsdk.overlay.Marker;
import com.mapbox.mapboxsdk.overlay.Overlay;
import com.mapbox.mapboxsdk.tileprovider.tilesource.TileLayer;
import com.mapbox.mapboxsdk.views.MapView;
import com.mapbox.mapboxsdk.views.MapViewListener;
import com.mapbox.mapboxsdk.views.util.TilesLoadedListener;

import java.io.IOException;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import de.greenrobot.event.EventBus;
import io.mapsquare.osmcontributor.OsmTemplateApplication;
import io.mapsquare.osmcontributor.R;
import io.mapsquare.osmcontributor.core.ConfigManager;
import io.mapsquare.osmcontributor.core.events.NodeRefAroundLoadedEvent;
import io.mapsquare.osmcontributor.core.events.PleaseDeletePoiEvent;
import io.mapsquare.osmcontributor.core.events.PleaseLoadNodeRefAround;
import io.mapsquare.osmcontributor.core.events.PleaseRemoveArpiMarkerEvent;
import io.mapsquare.osmcontributor.core.model.Note;
import io.mapsquare.osmcontributor.core.model.Poi;
import io.mapsquare.osmcontributor.core.model.PoiNodeRef;
import io.mapsquare.osmcontributor.core.model.PoiType;
import io.mapsquare.osmcontributor.core.model.PoiTypeTag;
import io.mapsquare.osmcontributor.edition.EditPoiActivity;
import io.mapsquare.osmcontributor.edition.events.PleaseApplyNodeRefPositionChange;
import io.mapsquare.osmcontributor.edition.events.PleaseApplyPoiPositionChange;
import io.mapsquare.osmcontributor.map.events.AddressFoundEvent;
import io.mapsquare.osmcontributor.map.events.ChangeMapModeEvent;
import io.mapsquare.osmcontributor.map.events.EditionVectorialTilesLoadedEvent;
import io.mapsquare.osmcontributor.map.events.LastUsePoiTypeLoaded;
import io.mapsquare.osmcontributor.map.events.MapCenterValueEvent;
import io.mapsquare.osmcontributor.map.events.NewNoteCreatedEvent;
import io.mapsquare.osmcontributor.map.events.NewPoiTypeSelected;
import io.mapsquare.osmcontributor.map.events.OnBackPressedMapEvent;
import io.mapsquare.osmcontributor.map.events.PleaseApplyNoteFilterEvent;
import io.mapsquare.osmcontributor.map.events.PleaseApplyPoiFilter;
import io.mapsquare.osmcontributor.map.events.PleaseChangePoiPosition;
import io.mapsquare.osmcontributor.map.events.PleaseChangeToolbarColor;
import io.mapsquare.osmcontributor.map.events.PleaseChangeValuesDetailNoteFragmentEvent;
import io.mapsquare.osmcontributor.map.events.PleaseChangeValuesDetailPoiFragmentEvent;
import io.mapsquare.osmcontributor.map.events.PleaseCreateNoTagPoiEvent;
import io.mapsquare.osmcontributor.map.events.PleaseDeletePoiFromMapEvent;
import io.mapsquare.osmcontributor.map.events.PleaseDisplayTutorialEvent;
import io.mapsquare.osmcontributor.map.events.PleaseGiveMeMapCenterEvent;
import io.mapsquare.osmcontributor.map.events.PleaseInitializeArpiEvent;
import io.mapsquare.osmcontributor.map.events.PleaseInitializeNoteDrawerEvent;
import io.mapsquare.osmcontributor.map.events.PleaseLoadEditVectorialTileEvent;
import io.mapsquare.osmcontributor.map.events.PleaseLoadLastUsedPoiType;
import io.mapsquare.osmcontributor.map.events.PleaseOpenEditionEvent;
import io.mapsquare.osmcontributor.map.events.PleaseSelectNodeRefByID;
import io.mapsquare.osmcontributor.map.events.PleaseSwitchMapStyleEvent;
import io.mapsquare.osmcontributor.map.events.PleaseSwitchWayEditionModeEvent;
import io.mapsquare.osmcontributor.map.events.PleaseToggleArpiEvent;
import io.mapsquare.osmcontributor.map.events.PleaseToggleDrawer;
import io.mapsquare.osmcontributor.map.events.PleaseToggleDrawerLock;
import io.mapsquare.osmcontributor.map.events.PoiNoTypeCreated;
import io.mapsquare.osmcontributor.map.vectorial.BoxOverlay;
import io.mapsquare.osmcontributor.map.vectorial.Geocoder;
import io.mapsquare.osmcontributor.map.vectorial.LevelBar;
import io.mapsquare.osmcontributor.map.vectorial.VectorialObject;
import io.mapsquare.osmcontributor.map.vectorial.VectorialOverlay;
import io.mapsquare.osmcontributor.note.NoteCommentDialogFragment;
import io.mapsquare.osmcontributor.note.events.ApplyNewCommentFailedEvent;
import io.mapsquare.osmcontributor.sync.events.SyncDownloadWayEvent;
import io.mapsquare.osmcontributor.sync.events.SyncFinishUploadPoiEvent;
import io.mapsquare.osmcontributor.sync.events.error.SyncConflictingNodeErrorEvent;
import io.mapsquare.osmcontributor.sync.events.error.SyncDownloadRetrofitErrorEvent;
import io.mapsquare.osmcontributor.sync.events.error.SyncNewNodeErrorEvent;
import io.mapsquare.osmcontributor.sync.events.error.SyncUnauthorizedEvent;
import io.mapsquare.osmcontributor.sync.events.error.SyncUploadNoteRetrofitErrorEvent;
import io.mapsquare.osmcontributor.sync.events.error.SyncUploadRetrofitErrorEvent;
import io.mapsquare.osmcontributor.sync.events.error.TooManyRequestsEvent;
import io.mapsquare.osmcontributor.tileslayer.BingTileLayer;
import io.mapsquare.osmcontributor.tileslayer.MBTilesLayer;
import io.mapsquare.osmcontributor.tileslayer.WebSourceTileLayer;
import io.mapsquare.osmcontributor.utils.Box;
import io.mapsquare.osmcontributor.utils.FlavorUtils;
import io.mapsquare.osmcontributor.utils.StringUtils;
import timber.log.Timber;


public class MapFragment extends Fragment {

    public static final String MAP_FRAGMENT_TAG = "MAP_FRAGMENT_TAG";
    private static final String LOCATION = "location";
    private static final String ZOOM_LEVEL = "zoom level";
    private static final String LEVEL = "level";
    private static final String MARKER_TYPE = "MARKER_TYPE";
    public static final String CREATION_MODE = "CREATION_MODE";
    public static final String POI_TYPE_ID = "POI_TYPE_ID";
    public static final String SELECTED_MARKER_ID = "SELECTED_MARKER_ID";
    public static final String HIDDEN_POI_TYPE = "HIDDEN_POI_TYPE";
    private static final String DISPLAY_OPEN_NOTES = "DISPLAY_OPEN_NOTES";
    private static final String DISPLAY_CLOSED_NOTES = "DISPLAY_CLOSED_NOTES";

    private LocationMarker markerSelected = null;

    // when resuming app we use this id to re-select the good marker
    private Long markerSelectedId = -1L;
    private MapMode mapMode = MapMode.DEFAULT;

    private boolean isMenuLoaded = false;
    private boolean pleaseSwitchToPoiSelected = false;

    private Map<Long, LocationMarker> markersPoi;
    private Map<Long, LocationMarker> markersNotes;
    private Map<Long, LocationMarker> markersNodeRef;

    private int maxPoiType;
    private PoiType poiTypeSelected;
    private ButteryProgressBar progressBar;
    MapFragmentPresenter presenter;

    @Inject
    BitmapHandler bitmapHandler;

    @InjectView(R.id.mapview)
    MapView mapView;

    @Inject
    EventBus eventBus;

    @InjectView(R.id.poi_detail_wrapper)
    RelativeLayout poiDetailWrapper;

    @InjectView(R.id.progressbar)
    RelativeLayout progressbarWrapper;

    @InjectView(R.id.note_detail_wrapper)
    RelativeLayout noteDetailWrapper;

    @Inject
    SharedPreferences sharedPreferences;

    @Inject
    ConfigManager configManager;

    //olduv
    //For testing purpose
    @InjectView(R.id.zoom_level)
    TextView zoomLevelText;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        tracker = ((OsmTemplateApplication) this.getActivity().getApplication()).getTracker(OsmTemplateApplication.TrackerName.APP_TRACKER);
        tracker.setScreenName("MapView");
        tracker.send(new HitBuilders.ScreenViewBuilder().build());

        measureMaxPoiType();

        presenter = new MapFragmentPresenter(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_map, container, false);

        markersPoi = new HashMap<>();
        markersNotes = new HashMap<>();
        markersNodeRef = new HashMap<>();

        ((OsmTemplateApplication) getActivity().getApplication()).getOsmTemplateComponent().inject(this);
        ButterKnife.inject(this, rootView);
        setHasOptionsMenu(true);

        zoomVectorial = configManager.getZoomVectorial();

        if (savedInstanceState != null) {
            currentLevel = savedInstanceState.getDouble(LEVEL);
            selectedMarkerType = LocationMarker.MarkerType.values()[savedInstanceState.getInt(MARKER_TYPE)];
        }

        instantiateProgressBar();
        instantiateMapView(savedInstanceState);
        instantiateLevelBar();
        instantiatePoiTypePicker();

        Timber.d("bounding box : %s", getViewBoundingBox());
        Timber.d("bounding box internal : %s", mapView.getBoundingBoxInternal());

        return rootView;
    }

    private void instantiateProgressBar() {
        progressBar = new ButteryProgressBar(getActivity());
        progressBar.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, 24));
        RelativeLayout.LayoutParams p = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);

        p.addRule(RelativeLayout.BELOW, R.id.poi_type_value_wrapper);
        progressBar.setVisibility(View.GONE);
        progressBar.setLayoutParams(p);
        progressbarWrapper.addView(progressBar);
    }

    private void instantiateLevelBar() {
        Drawable d = getResources().getDrawable(R.drawable.level_thumb);
        levelBar.setThumb(d);
        levelBar.setDrawableHeight(d.getIntrinsicHeight());
        levelBar.setLevel(currentLevel);
        levelBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                Timber.v("onProgressChanged");
                if (vectorialOverlay != null) {
                    LevelBar lvl = (LevelBar) seekBar;
                    currentLevel = lvl.getLevel();
                    vectorialOverlay.setLevel(currentLevel);
                    invalidateMap();
                    applyPoiFilter();
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
    }

    private void instantiateMapView(Bundle savedInstanceState) {
        // Instantiate the different tiles sources
        instantiateTileSources();
        // Set the tile source has the Osm tile source
        switchToTileSource(OSM_TILE_SOURCE);

        // disable rotation of the map
        mapView.setMapRotationEnabled(false);

        // Enable disk cache
        mapView.setDiskCacheEnabled(true);

        mapView.setUserLocationEnabled(true);
        // Set the map center and zoom to the saved values or use the default values
        if (savedInstanceState == null) {
            mapView.setCenter((FlavorUtils.isStore() && mapView.getUserLocation() != null) ? mapView.getUserLocation() : configManager.getDefaultCenter());
            mapView.setZoom(configManager.getDefaultZoom());
        } else {
            mapView.setCenter((LatLng) savedInstanceState.getParcelable(LOCATION));
            mapView.setZoom(savedInstanceState.getFloat(ZOOM_LEVEL));
        }

        mapView.setOnTilesLoadedListener(new TilesLoadedListener() {
            @Override
            public boolean onTilesLoaded() {
                return false;
            }

            @Override
            public boolean onTilesLoadStarted() {
                return false;
            }
        });

        final DecimalFormat df = new DecimalFormat("#.##");
        df.setRoundingMode(RoundingMode.DOWN);
        zoomLevelText.setText(df.format(getZoomLevel()));

        mapView.addListener(new MapListener() {
            int initialX;
            int initialY;
            int deltaX;
            int deltaY;

            @Override
            public void onScroll(ScrollEvent scrollEvent) {
                deltaX = initialX - scrollEvent.getX();
                deltaY = initialY - scrollEvent.getY();

                // 20 px delta before it's worth checking
                int minPixelsDeltaBeforeCheck = 100;

                if (Math.abs(deltaX) > minPixelsDeltaBeforeCheck || Math.abs(deltaY) > minPixelsDeltaBeforeCheck) {
                    initialX = scrollEvent.getX();
                    initialY = scrollEvent.getY();
                    presenter.loadPoisIfNeeded();

                    if (getZoomLevel() > zoomVectorial) {
                        LatLng center = mapView.getCenter();
                        geocoder.delayedReverseGeocoding(center.getLatitude(), center.getLongitude());
                    }
                }
            }

            @Override
            public void onZoom(ZoomEvent zoomEvent) {
                // olduv
                // For testing purpose
                zoomLevelText.setText(df.format(zoomEvent.getZoomLevel()));

                presenter.loadPoisIfNeeded();
                Timber.v("new zoom : %s", zoomEvent.getZoomLevel());

                if (zoomEvent.getZoomLevel() < zoomVectorial) {
                    levelBar.setVisibility(View.INVISIBLE);
                    addressView.setVisibility(View.INVISIBLE);
                    if (isVectorial) {
                        isVectorial = false;
                        applyPoiFilter();
                        applyNoteFilter();
                    }
                } else {
                    LatLng center = mapView.getCenter();
                    geocoder.delayedReverseGeocoding(center.getLatitude(), center.getLongitude());
                    if (levelBar.getLevels().length > 1) {
                        levelBar.setVisibility(View.VISIBLE);
                    }
                    if (!isVectorial) {
                        isVectorial = true;
                        applyPoiFilter();
                        applyNoteFilter();
                    }
                }
            }

            @Override
            public void onRotate(RotateEvent rotateEvent) {
                presenter.loadPoisIfNeeded();
            }

        });

        mapView.setMapViewListener(new MapViewListener() {
            @Override
            public void onShowMarker(MapView mapView, Marker marker) {
            }

            @Override
            public void onHideMarker(MapView mapView, Marker marker) {
            }

            @Override
            public void onTapMarker(MapView mapView, Marker marker) {
                if (marker instanceof LocationMarker) {
                    LocationMarker locationMarker = (LocationMarker) marker;
                    if (mapMode != MapMode.POI_POSITION_EDITION && mapMode != MapMode.POI_CREATION && mapMode != MapMode.WAY_EDITION && !isTuto) {
                        switch (locationMarker.getType()) {
                            case POI:
                                onPoiMarkerClick(locationMarker);
                                break;
                            case NOTE:
                                onNoteMarkerClick(locationMarker);
                                break;
                            default:
                                break;
                        }
                    }
                }

            }

            @Override
            public void onLongPressMarker(MapView mapView, Marker marker) {

            }

            @Override
            public void onTapMap(MapView mapView, ILatLng iLatLng) {
                if (mapMode == MapMode.DETAIL_POI || mapMode == MapMode.DETAIL_NOTE) {
                    // it prevents to reselect the marker
                    markerSelectedId = -1L;
                    switchMode(MapMode.DEFAULT);
                }
                if (mapMode == MapMode.WAY_EDITION) {
                    eventBus.post(new PleaseLoadNodeRefAround(iLatLng.getLatitude(), iLatLng.getLongitude()));
                }
                if (mapMode == MapMode.DEFAULT && floatingMenuAddFewValues.isExpanded()) {
                    floatingMenuAddFewValues.collapse();
                }

            }

            @Override
            public void onLongPressMap(MapView mapView, ILatLng iLatLng) {

            }
        });
    }

    private void drawBounds() {
        BoxOverlay boxOverlay = new BoxOverlay(configManager.getBoundingBox());
        mapView.addOverlay(boxOverlay);
    }

    private void measureMaxPoiType() {
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();

        float toolbarSize = getResources().getDimension(R.dimen.abc_action_bar_default_height_material) / displayMetrics.density;
        float dpHeight = displayMetrics.heightPixels / displayMetrics.density;

        // 80 size of one floating btn in dp
        // 180 size of the menu btn plus location btn
        // -1 because we always have note

        maxPoiType = (int) ((dpHeight - toolbarSize - 160) / 80) - 1;
    }

    void onPoiMarkerClick(LocationMarker marker) {
        unselectIcon();
        markerSelected = (marker);
        Bitmap bitmap = bitmapHandler.getMarkerBitmap(markerSelected.getPoi().getType(), Poi.computeState(true, false, false));
        if (bitmap != null) {
            markerSelected.setIcon(new Icon(new BitmapDrawable(getResources(), bitmap)));
        }
        selectedMarkerType = LocationMarker.MarkerType.POI;
        switchMode(MapMode.DETAIL_POI);
        ValueAnimator valueAnimator = ValueAnimator.ofFloat(0, OsmAnimatorUpdateListener.STEPS_CENTER_ANIMATION);
        valueAnimator.setDuration(500);
        valueAnimator.addUpdateListener(new OsmAnimatorUpdateListener(mapView.getCenter(), markerSelected.getPoint(), mapView));
        valueAnimator.start();
        markerSelectedId = -1L;
    }


    private void onNodeRefClick(LocationMarker marker) {
        editNodeRefPosition.setVisibility(View.VISIBLE);
        ValueAnimator valueAnimator = ValueAnimator.ofFloat(0, OsmAnimatorUpdateListener.STEPS_CENTER_ANIMATION);
        valueAnimator.setDuration(500);
        valueAnimator.addUpdateListener(new OsmAnimatorUpdateListener(mapView.getCenter(), marker.getPoint(), mapView));
        valueAnimator.start();
    }

    private void onNoteMarkerClick(LocationMarker marker) {
        unselectIcon();
        markerSelected = (marker);
        selectedMarkerType = LocationMarker.MarkerType.NOTE;

        Bitmap bitmap = bitmapHandler.getNoteBitmap(Note.computeState(markerSelected.getNote(), true, false));
        if (bitmap != null) {
            markerSelected.setIcon(new Icon(new BitmapDrawable(getResources(), bitmap)));
        }

        switchMode(MapMode.DETAIL_NOTE);
        ValueAnimator valueAnimator = ValueAnimator.ofFloat(0, OsmAnimatorUpdateListener.STEPS_CENTER_ANIMATION);
        valueAnimator.setDuration(500);
        valueAnimator.addUpdateListener(new OsmAnimatorUpdateListener(mapView.getCenter(), markerSelected.getPoint(), mapView));
        valueAnimator.start();
        markerSelectedId = -1L;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (savedInstanceState != null) {
            Integer creationModeInt = savedInstanceState.getInt(CREATION_MODE);
            savePoiTypeId = savedInstanceState.getLong(POI_TYPE_ID);

            markerSelectedId = savedInstanceState.getLong(SELECTED_MARKER_ID, -1);

            mapMode = MapMode.values()[creationModeInt];
            if (mapMode == MapMode.DETAIL_NOTE || mapMode == MapMode.DETAIL_POI || mapMode == MapMode.POI_POSITION_EDITION) {
                mapMode = MapMode.DEFAULT;
            } else if (mapMode == MapMode.NODE_REF_POSITION_EDITION) {
                mapMode = MapMode.WAY_EDITION;
            }

            long[] hidden = savedInstanceState.getLongArray(HIDDEN_POI_TYPE);
            if (hidden != null) {
                for (long l : hidden) {
                    poiTypeHidden.add(l);
                }
            }

            displayOpenNotes = savedInstanceState.getBoolean(DISPLAY_OPEN_NOTES);
            displayClosedNotes = savedInstanceState.getBoolean(DISPLAY_CLOSED_NOTES);
            switchToTileSource(savedInstanceState.getString(TILE_SOURCE));
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        presenter.register();

        //enable geolocation of user
        mapView.setUserLocationEnabled(true);

        eventBus.register(this);
        eventBus.post(new PleaseInitializeArpiEvent());
        presenter.setForceRefreshPoi();
        presenter.setForceRefreshNotes();
        presenter.loadPoisIfNeeded();
        eventBus.post(new PleaseInitializeNoteDrawerEvent(displayOpenNotes, displayClosedNotes));
        if (poiTypePickerAdapter != null) {
            poiTypePickerAdapter.setExpertMode(sharedPreferences.getBoolean(getString(R.string.shared_prefs_expert_mode), false));
        }
    }

    @Override
    public void onPause() {
        eventBus.unregister(this);
        presenter.unregister();
        mapView.setUserLocationEnabled(false);
        if (valueAnimator != null) {
            valueAnimator.cancel();
            valueAnimator.removeAllListeners();
            valueAnimator.removeAllUpdateListeners();
        }
        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Clear bitmapHandler even if activity leaks.
        bitmapHandler = null;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        progressBar.removeListeners();
        ButterKnife.reset(this);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(LOCATION, mapView.getCenter());
        outState.putFloat(ZOOM_LEVEL, getZoomLevel());
        outState.putInt(CREATION_MODE, mapMode.ordinal());
        outState.putLong(POI_TYPE_ID, poiTypeSelected == null ? -1 : poiTypeSelected.getId());
        outState.putDouble(LEVEL, currentLevel);
        outState.putBoolean(DISPLAY_OPEN_NOTES, displayOpenNotes);
        outState.putBoolean(DISPLAY_CLOSED_NOTES, displayClosedNotes);
        outState.putString(TILE_SOURCE, currentTileLayer);

        int markerType = markerSelected == null ? LocationMarker.MarkerType.NONE.ordinal() : markerSelected.getType().ordinal();
        outState.putInt(MARKER_TYPE, markerType);

        if (markerSelected != null) {
            switch (markerSelected.getType()) {
                case POI:
                    markerSelectedId = markerSelected.getPoi().getId();
                    break;
                case NODE_REF:
                    markerSelectedId = markerSelected.getNodeRef().getId();
                    break;
                case NOTE:
                    markerSelectedId = markerSelected.getNote().getId();
                    break;
            }
        } else {
            markerSelectedId = -1L;
        }
        outState.putLong(SELECTED_MARKER_ID, markerSelectedId);


        long[] hidden = new long[poiTypeHidden.size()];
        int index = 0;

        for (Long value : poiTypeHidden) {
            hidden[index] = value;
            index++;
        }

        outState.putLongArray(HIDDEN_POI_TYPE, hidden);
    }

    /*-----------------------------------------------------------
    * ANALYTICS ATTRIBUTES
    *---------------------------------------------------------*/

    private Tracker tracker;

    private enum Category {
        MapMode("Map Mode"),
        GeoLocation("Geolocation"),
        Edition("Edition POI");

        private final String value;

        Category(String value) {
            this.value = value;
        }

        String getValue() {
            return value;
        }
    }

    /*-----------------------------------------------------------
    * ACTIONBAR
    *---------------------------------------------------------*/

    private MenuItem filter;
    private MenuItem confirm;
    private MenuItem downloadArea;
    private boolean homeActionBtnMode = true;

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        displayHomeButton(true); //show the home button
        confirm = menu.findItem(R.id.action_confirm_position);
        downloadArea = menu.findItem(R.id.action_download_area);
        filter = menu.findItem(R.id.action_filter_drawer);
        // the menu has too be created because we modify it depending on mode
        isMenuLoaded = true;
        if (pleaseSwitchToPoiSelected) {
            pleaseSwitchToPoiSelected = false;
            onMarkerClick(markerSelected);
        } else {
            switchMode(mapMode);
        }
    }

    public void onMarkerClick(LocationMarker marker) {
        if (marker.isPoi()) {
            onPoiMarkerClick(marker);
        } else {
            onNoteMarkerClick(marker);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.action_confirm_position:
                confirmPosition();
                break;

            case R.id.action_download_area:
                onDownloadZoneClick();
                break;

            case android.R.id.home:
                if (homeActionBtnMode) {
                    eventBus.post(new PleaseToggleDrawer());
                } else {
                    eventBus.post(new OnBackPressedMapEvent());
                }
                break;

            default:
                super.onOptionsItemSelected(item);
                break;
        }


        return true;
    }

    private void confirmPosition() {
        LatLng newPoiPosition;
        LatLng pos;

        switch (mapMode) {
            case POI_CREATION:
                createPoi();
                break;

            case NOTE_CREATION:
                pos = mapView.getCenter();
                NoteCommentDialogFragment dialog = NoteCommentDialogFragment.newInstance(pos.getLatitude(), pos.getLongitude());
                dialog.show(getActivity().getFragmentManager(), "dialog");
                break;

            case POI_POSITION_EDITION:
                newPoiPosition = mapView.getCenter();
                eventBus.post(new PleaseApplyPoiPositionChange(newPoiPosition, markerSelected.getPoi().getId()));
                markerSelected.setPoint(newPoiPosition);
                markerSelected.getPoi().setUpdated(true);
                mapView.addMarker(markerSelected);
                switchMode(MapMode.DETAIL_POI);
                tracker.send(new HitBuilders.EventBuilder()
                        .setCategory(Category.Edition.getValue())
                        .setAction("POI position edited")
                        .build());
                break;

            case NODE_REF_POSITION_EDITION:
                newPoiPosition = mapView.getCenter();
                eventBus.post(new PleaseApplyNodeRefPositionChange(newPoiPosition, markerSelected.getNodeRef().getId()));
                markerSelected.setPoint(newPoiPosition);
                switchMode(MapMode.WAY_EDITION);
                tracker.send(new HitBuilders.EventBuilder()
                        .setCategory(Category.Edition.getValue())
                        .setAction("way point position edited")
                        .build());
                break;

            case DETAIL_POI:
                getActivity().finish();
                break;

            default:
                break;
        }
    }

    //if is back will show the back arrow, else will display the menu icon
    private void toggleBackButton(boolean homeActionBtnMode) {
        ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (actionBar != null) {
            this.homeActionBtnMode = homeActionBtnMode;
            if (homeActionBtnMode) {
                actionBar.setHomeAsUpIndicator(R.drawable.menu);
            } else {
                actionBar.setHomeAsUpIndicator(R.drawable.back);
            }
        }
    }

    private void displayHomeButton(boolean show) {
        AppCompatActivity appCompatActivity = (AppCompatActivity) getActivity();
        if (appCompatActivity != null) {
            ActionBar actionBar = appCompatActivity.getSupportActionBar();
            if (actionBar != null) {
                actionBar.setDisplayHomeAsUpEnabled(show);
            }
        }
    }

    public void onEventMainThread(OnBackPressedMapEvent event) {
        Timber.d("Received event OnBackPressedMap");
        if (isTuto) {
            closeTuto();
        } else {
            switch (mapMode) {
                case POI_POSITION_EDITION:
                    mapView.addMarker(markerSelected);
                    switchMode(MapMode.DEFAULT);
                    break;

                case NODE_REF_POSITION_EDITION:
                    switchMode(MapMode.WAY_EDITION);
                    break;

                case WAY_EDITION:
                    clearVectorialEdition();
                    switchMode(MapMode.DEFAULT);
                    break;

                case DETAIL_POI:
                case DETAIL_NOTE:
                case POI_CREATION:
                case NOTE_CREATION:
                case TYPE_PICKER:
                    switchMode(MapMode.DEFAULT);
                    InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(mapView.getWindowToken(), 0);
                    break;

                case ARPIGL:
                    switchMode(MapMode.DEFAULT);
                    eventBus.post(new PleaseToggleArpiEvent());
                    break;
                default:
                    getActivity().finish();
                    break;
            }
        }
    }

    public void onEventMainThread(PleaseGiveMeMapCenterEvent event) {
        eventBus.post(new MapCenterValueEvent(mapView.getCenter()));
    }


    public MapMode getMapMode() {
        return mapMode;
    }

    public LocationMarker getMarkerSelected() {
        return markerSelected;
    }

    public void setMarkerSelected(LocationMarker markerSelected) {
        this.markerSelected = markerSelected;
    }

    public Long getMarkerSelectedId() {
        return markerSelectedId;
    }

    public void setMarkerSelectedId(Long markerSelectedId) {
        this.markerSelectedId = markerSelectedId;
    }

    public LocationMarker.MarkerType getSelectedMarkerType() {
        return selectedMarkerType;
    }

    public BitmapHandler getBitmapHandler() {
        return bitmapHandler;
    }

    private void switchMode(MapMode mode) {

        mapMode = mode;
        Bitmap bitmap;

        switchToolbarMode(mapMode);
        editNodeRefPosition.setVisibility(View.GONE);
        progressBar.setVisibility(View.GONE);

        final MapMode.MapModeProperties properties = mode.getProperties();

        if (properties.isUnSelectIcon()) {
            unselectIcon();
        }

        showFloatingButtonAddPoi(properties.isShowAddPoiFab());
        displayPoiTypePicker();
        displayPoiDetailBanner(properties.isShowPoiBanner());
        displayNoteDetailBanner(properties.isShowNodeBanner());

        // If there must be a min zoom, take zoom vectorial
        // If there is no need for a zoom min, take the zoom min of the current TileProvider.
        mapView.setMinZoomLevel(properties.isZoomOutLimited() ? zoomVectorial : mapView.getTileProvider().getMinimumZoomLevel());

        tracker.send(new HitBuilders.EventBuilder()
                .setCategory(Category.MapMode.getValue())
                .setAction(properties.getAnalyticsAction())
                .build());

        switch (mode) {

            case DETAIL_POI:
            case DETAIL_NOTE:
                break;

            case TYPE_PICKER:
                eventBus.post(new PleaseLoadLastUsedPoiType());
                break;

            case POI_CREATION:
                animationPoiCreation();
                break;

            case NOTE_CREATION:
                noteSelected();
                animationPoiCreation();
                break;

            case POI_POSITION_EDITION:
                // This marker is being moved
                bitmap = bitmapHandler.getMarkerBitmap(markerSelected.getPoi().getType(), Poi.computeState(false, true, false));
                creationPin.setImageBitmap(bitmap);
                break;

            case NODE_REF_POSITION_EDITION:
                break;

            case WAY_EDITION:
                loadAreaForEdition();
                if (vectorialOverlay != null) {
                    vectorialOverlay.setMovingObjectId(null);
                    vectorialOverlay.setSelectedObjectId(null);
                } else if (markerSelected == null && markerSelectedId != -1) {
                    eventBus.post(new PleaseSelectNodeRefByID(markerSelectedId));
                }
                break;

            default:
                poiTypeSelected = null;
                poiTypeEditText.setText("");
                floatingMenuAddFewValues.collapse();
                break;
        }

        //the marker is displayed at the end of the animation
        creationPin.setVisibility(properties.isShowCreationPin() ? View.VISIBLE : View.GONE);

    }

    private void switchToolbarMode(MapMode mode) {
        MapMode.MapModeProperties properties = mode.getProperties();

        confirm.setVisible(properties.isShowConfirmBtn());
        downloadArea.setVisible(properties.isShowDownloadArea());
        filter.setVisible(!properties.isLockDrawer());
        toggleBackButton(properties.isMenuBtn());
        getActivity().setTitle(properties.getTitle(getActivity()));
        eventBus.post(new PleaseChangeToolbarColor(properties.isEditColor()));
        eventBus.post(new PleaseToggleDrawerLock(properties.isLockDrawer()));
    }

    /**
     * Show or hide the {@link ButteryProgressBar}.
     *
     * @param show Whether we should show the progressBar.
     */
    public void showProgressBar(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    public void onEventMainThread(PleaseSwitchWayEditionModeEvent event) {
        if (getZoomLevel() < zoomVectorial) {
            mapView.setZoom(zoomVectorial);
        }
        switchMode(MapMode.WAY_EDITION);
        downloadAreaForEdition();
    }

    /**
     * Download data from backend. If we are in {@link MapMode#WAY_EDITION}, download ways
     * and if not, download Pois.
     */
    public void onDownloadZoneClick() {
        if (mapMode == MapMode.WAY_EDITION) {
            downloadAreaForEdition();
        } else {
            // If flavor Store, allow the download only if the zoom > 18
            if (!FlavorUtils.isStore() || getZoomLevel() >= 18) {
                presenter.downloadAreaPoisAndNotes();
                Toast.makeText(getActivity(), R.string.download_in_progress, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getActivity(), R.string.zoom_more, Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void defaultMap() {
        presenter.setForceRefreshPoi();
        presenter.setForceRefreshNotes();
        presenter.loadPoisIfNeeded();
        switchMode(MapMode.DEFAULT);
    }

    private void unselectIcon() {
        if (markerSelected != null) {
            Bitmap bitmap = null;

            switch (markerSelected.getType()) {
                case POI:
                    bitmap = bitmapHandler.getMarkerBitmap(markerSelected.getPoi().getType(), Poi.computeState(false, false, markerSelected.getPoi().getUpdated()));
                    break;

                case NOTE:
                    bitmap = bitmapHandler.getNoteBitmap(Note.computeState(markerSelected.getNote(), false, false));
                    break;

                case NODE_REF:
                    mapView.removeMarker(markerSelected);
                    break;

                default:
                    break;
            }
            if (bitmap != null) {
                markerSelected.setIcon(new Icon(new BitmapDrawable(getResources(), bitmap)));
            }
            markerSelected = null;
        }
    }

    public BoundingBox getViewBoundingBox() {
        return mapView.getBoundingBox();
    }

    public float getZoomLevel() {
        return mapView.getZoomLevel();
    }

    public boolean hasMarkers() {
        return !markersPoi.isEmpty();
    }

    public void removeAllMarkers() {
        for (Long markerId : markersNotes.keySet()) {
            removeMarker(markersNotes.get(markerId));
        }
        for (Long markerId : markersNodeRef.keySet()) {
            removeMarker(markersNodeRef.get(markerId));
        }
        markersNotes.clear();
        markersNodeRef.clear();
        removeAllPoiMarkers();
    }

    public void removeAllPoiMarkers() {
        for (Long markerId : markersPoi.keySet()) {
            removeMarker(markersPoi.get(markerId));
        }
        markersPoi.clear();
    }

    public void removePoiMarkersNotIn(List<Long> poiIds) {
        Set<Long> idsToRemove = new HashSet<>(markersPoi.keySet());
        idsToRemove.removeAll(poiIds);
        for (Long id : idsToRemove) {
            removeMarker(markersPoi.get(id));
        }
        markersPoi.keySet().removeAll(idsToRemove);
    }

    public void removeNoteMarkersNotIn(List<Long> noteIds) {
        Set<Long> idsToRemove = new HashSet<>(markersNotes.keySet());
        idsToRemove.removeAll(noteIds);
        for (Long id : idsToRemove) {
            removeMarker(markersNotes.get(id));
        }
        markersNotes.keySet().removeAll(idsToRemove);
    }

    public Map<Long, LocationMarker> getMarkersPoi() {
        return markersPoi;
    }

    private void removeMarker(LocationMarker marker) {
        if (marker != null) {
            mapView.removeMarker(marker);
            Object poi = marker.getRelatedObject();
            eventBus.post(new PleaseRemoveArpiMarkerEvent(poi));
        }
    }

    public void reselectMarker() {
        // if I found the marker selected I click on it
        if (markerSelected != null) {
            if (isMenuLoaded) {
                onMarkerClick(markerSelected);
            } else {
                pleaseSwitchToPoiSelected = true;
            }
        }
    }

    public void addMarker(LocationMarker marker) {
        markersPoi.put(marker.getPoi().getId(), marker);
        addPoiMarkerDependingOnFilters(marker);
    }

    public void invalidateMap() {
        mapView.invalidate();
    }

    public void addNote(LocationMarker marker) {
        markersNotes.put(marker.getNote().getId(), marker);
        // add the note to the map
        addNoteMarkerDependingOnFilters(marker);
    }

    public LocationMarker getNote(Long id) {
        return markersNotes.get(id);
    }

    /*-----------------------------------------------------------
    * WAY EDITION
    *---------------------------------------------------------*/

    @InjectView(R.id.edit_way_elemnt_position)
    FloatingActionButton editNodeRefPosition;


    @OnClick(R.id.edit_way_elemnt_position)
    public void setEditNodeRefPosition() {
        vectorialOverlay.setMovingObjectId(markerSelected.getNodeRef().getNodeBackendId());
        switchMode(MapMode.NODE_REF_POSITION_EDITION);
    }

    private List<Overlay> overlays = new ArrayList<>();
    Set<VectorialObject> vectorialObjectsEdition = new HashSet<>();

    //get data from overpass
    private void downloadAreaForEdition() {
        if (getZoomLevel() >= zoomVectorial) {
            progressBar.setVisibility(View.VISIBLE);
            BoundingBox viewBoundingBox = getViewBoundingBox();
            eventBus.post(new SyncDownloadWayEvent(viewBoundingBox));
        } else {
            Toast.makeText(getActivity(), getString(R.string.zoom_to_edit), Toast.LENGTH_SHORT).show();
        }
    }

    //get data from the bd
    private void loadAreaForEdition() {
        if (getZoomLevel() >= zoomVectorial) {
            eventBus.post(new PleaseLoadEditVectorialTileEvent(false));
        } else {
            Toast.makeText(getActivity(), getString(R.string.zoom_to_edit), Toast.LENGTH_SHORT).show();
        }
    }

    private void clearAllNodeRef() {
        for (LocationMarker locationMarker : markersNodeRef.values()) {
            removeMarker(locationMarker);
        }
        markersNodeRef.clear();
        for (Overlay overlay : overlays) {
            mapView.removeOverlay(overlay);
        }
    }

    public void onEventMainThread(NodeRefAroundLoadedEvent event) {
        List<PoiNodeRef> poiNodeRefsSelected = event.getPoiNodeRefs();
        if (poiNodeRefsSelected != null && poiNodeRefsSelected.size() > 0) {
            //todo let the user precise his choice
            //lets says it the first one
            if (markerSelected != null) {
                removeMarker(markerSelected);
            }
            unselectIcon();
            PoiNodeRef poiNodeRef = poiNodeRefsSelected.get(0);
            LocationMarker locationMarker = new LocationMarker(poiNodeRef);
            markerSelected = locationMarker;
            vectorialOverlay.setSelectedObjectId(poiNodeRef.getNodeBackendId());
            onNodeRefClick(locationMarker);
            mapView.invalidate();

        } else {
            unselectNoderef();
        }
    }

    private void unselectNoderef() {
        vectorialOverlay.setSelectedObjectId(null);
        markerSelected = null;
        markerSelectedId = -1L;
        editNodeRefPosition.setVisibility(View.GONE);
        mapView.invalidate();
    }

    /*-----------------------------------------------------------
    * POI CREATION
    *---------------------------------------------------------*/

    @InjectView(R.id.hand)
    ImageView handImageView;

    @InjectView(R.id.add_poi_few_values)
    FloatingActionsMenu floatingMenuAddFewValues;

    @InjectView(R.id.floating_btn_wrapper)
    RelativeLayout floatingBtnWrapper;

    @InjectView(R.id.pin)
    ImageButton creationPin;

    ValueAnimator valueAnimator;

    private void createPoi() {
        LatLng pos = mapView.getCenter();
        boolean needTagEdition = false;

        for (PoiTypeTag poiTypeTag : poiTypeSelected.getTags()) {
            if (StringUtils.isEmpty(poiTypeTag.getValue())) {
                needTagEdition = true;
                break;
            }
        }

        if (needTagEdition) {
            Intent intent = new Intent(getActivity(), EditPoiActivity.class);
            intent.putExtra(EditPoiActivity.CREATION_MODE, true);
            intent.putExtra(EditPoiActivity.POI_LAT, pos.getLatitude());
            intent.putExtra(EditPoiActivity.POI_LNG, pos.getLongitude());
            intent.putExtra(EditPoiActivity.POI_LEVEL, isVectorial ? currentLevel : 0d);
            intent.putExtra(EditPoiActivity.POI_TYPE, poiTypeSelected.getId());

            switchMode(MapMode.DEFAULT);
            getActivity().startActivityForResult(intent, EditPoiActivity.EDIT_POI_ACTIVITY_CODE);
        } else {
            eventBus.post(new PleaseCreateNoTagPoiEvent(poiTypeSelected, pos, isVectorial ? currentLevel : 0d));
            switchMode(MapMode.DEFAULT);
        }
    }

    public void onEventMainThread(PoiNoTypeCreated event) {
        presenter.setForceRefreshPoi();
        presenter.loadPoisIfNeeded();
    }

    private void animationPoiCreation() {
        handImageView.setVisibility(View.VISIBLE);
        valueAnimator = ValueAnimator.ofFloat(0, OsmCreationAnimatorUpdateListener.STEPS_CENTER_ANIMATION);
        valueAnimator.setDuration(1000);
        valueAnimator.addListener(new OsmCreationAnimatorUpdateListener(mapView, handImageView, getActivity()));
        valueAnimator.addUpdateListener(new OsmCreationAnimatorUpdateListener(mapView, handImageView, getActivity()));
        valueAnimator.start();
    }

    public void onEventMainThread(NewPoiTypeSelected event) {
        Timber.d("Received event NewPoiTypeSelected");
        if (isTuto) {
            nextTutoStep();
        }
        poiTypeSelected(event.getPoiType());
        switchMode(MapMode.POI_CREATION);
    }

    private void poiTypeSelected(PoiType poiType) {
        poiTypeTextView.setText(poiType.getName());
        poiTypeSelected = poiType;
        savePoiTypeId = 0;
        Bitmap bitmap;

        bitmap = bitmapHandler.getMarkerBitmap(poiType, Poi.computeState(false, true, false));
        if (poiTypeHidden.contains(poiType.getId())) {
            poiTypeHidden.remove(poiType.getId());
            applyPoiFilter();
        }

        if (presenter.getNumberOfPoiTypes() > maxPoiType) {
            editPoiTypeBtn.setVisibility(View.VISIBLE);
        }

        creationPin.setImageBitmap(bitmap);
        creationPin.setVisibility(View.VISIBLE);
    }

    private void noteSelected() {
        Bitmap bitmap = bitmapHandler.getNoteBitmap(Note.computeState(null, false, true));
        creationPin.setImageBitmap(bitmap);
        creationPin.setVisibility(View.VISIBLE);
        poiTypeSelected = null;
    }

    private void showFloatingButtonAddPoi(boolean show) {
        //animation show and hide
        if (show) {
            if (floatingBtnWrapper.getVisibility() == View.GONE) {
                Animation bottomUp = AnimationUtils.loadAnimation(getActivity(),
                        R.anim.anim_up_detail);

                floatingBtnWrapper.startAnimation(bottomUp);
                floatingBtnWrapper.setVisibility(View.VISIBLE);
            }
        } else {
            if (floatingBtnWrapper.getVisibility() == View.VISIBLE) {
                Animation bottomUp = AnimationUtils.loadAnimation(getActivity(),
                        R.anim.anim_down_detail);

                floatingBtnWrapper.startAnimation(bottomUp);
                floatingBtnWrapper.setVisibility(View.GONE);
            }
        }

        //which kind of add to display depending on screen size
        // if in store flavor show the spinner
        floatingMenuAddFewValues.setVisibility(View.VISIBLE);
        floatingMenuAddFewValues.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isTuto) {
                    nextTutoStep();
                }
            }
        });
    }

    protected void loadPoiTypeFloatingBtn() {
        floatingMenuAddFewValues.removeAllButtons();
        FloatingActionButton floatingActionButton;

        //add note
        if (!FlavorUtils.isPoiStorage()) {
            floatingActionButton = new FloatingActionButton(getActivity());
            floatingActionButton.setTitle(getString(R.string.note));
            floatingActionButton.setColorPressed(getResources().getColor(R.color.material_green_700));
            floatingActionButton.setColorNormal(getResources().getColor(R.color.material_green_500));
            floatingActionButton.setSize(FloatingActionButton.SIZE_MINI);
            floatingActionButton.setIconDrawable(bitmapHandler.getIconWhite(null));
            floatingActionButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    switchMode(MapMode.NOTE_CREATION);
                    floatingMenuAddFewValues.collapse();
                    if (isTuto) {
                        nextTutoStep();
                        nextTutoStep();
                    }
                }
            });
            floatingMenuAddFewValues.addButton(floatingActionButton);
        }

        if (presenter.getNumberOfPoiTypes() <= maxPoiType) {
            //add a btn per poiType
            for (final PoiType poiType : presenter.getPoiTypes()) {
                floatingActionButton = new FloatingActionButton(getActivity());
                floatingActionButton.setTitle(poiType.getName());
                floatingActionButton.setColorPressed(getResources().getColor(R.color.material_blue_grey_800));
                floatingActionButton.setColorNormal(getResources().getColor(R.color.material_blue_500));
                floatingActionButton.setSize(FloatingActionButton.SIZE_MINI);
                floatingActionButton.setIconDrawable(bitmapHandler.getIconWhite(poiType));
                floatingActionButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (configManager.hasPoiAddition()) {
                            switchMode(MapMode.POI_CREATION);
                            poiTypeSelected(poiType);
                            floatingMenuAddFewValues.collapse();
                            if (isTuto) {
                                nextTutoStep();
                            }
                        } else {
                            Toast.makeText(getActivity(), getResources().getString(R.string.point_modification_forbidden), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                floatingMenuAddFewValues.addButton(floatingActionButton);
            }
        } else {
            // add a btn for all poiTypes
            floatingActionButton = new FloatingActionButton(getActivity());
            floatingActionButton.setTitle(getResources().getString(R.string.add_poi));
            floatingActionButton.setColorPressed(getResources().getColor(R.color.material_blue_grey_800));
            floatingActionButton.setColorNormal(getResources().getColor(R.color.material_blue_500));
            floatingActionButton.setSize(FloatingActionButton.SIZE_MINI);
            floatingActionButton.setIconDrawable(getResources().getDrawable(R.drawable.fab_poi));
            floatingActionButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (configManager.hasPoiAddition()) {
                        switchMode(MapMode.TYPE_PICKER);
                        if (isTuto) {
                            nextTutoStep();
                        }
                    } else {
                        Toast.makeText(getActivity(), getResources().getString(R.string.point_modification_forbidden), Toast.LENGTH_SHORT).show();
                    }
                }
            });
            floatingMenuAddFewValues.addButton(floatingActionButton);
        }

        // the floating btn and tthe poitypes are loaded we can now start the tuto
        displayTutorial(false);
    }

    protected void loadPoiTypeSpinner() {
        poiTypePickerAdapter.addAll(presenter.getPoiTypes());
        if (savePoiTypeId != 0) {
            for (PoiType poiType : presenter.getPoiTypes()) {
                if (poiType.getId().equals(savePoiTypeId)) {
                    poiTypeSelected(poiType);
                }
            }
        }
    }

    /*-----------------------------------------------------------
    * POI EDITION
    *---------------------------------------------------------*/

    public void onEventMainThread(PleaseOpenEditionEvent event) {
        Timber.d("Received event PleaseOpenEdition");
        Intent intent = new Intent(getActivity(), EditPoiActivity.class);
        intent.putExtra(EditPoiActivity.CREATION_MODE, false);
        intent.putExtra(EditPoiActivity.POI_ID, markerSelected.getPoi().getId());
        startActivity(intent);
    }

    public void onEventMainThread(PleaseChangePoiPosition event) {
        Timber.d("Received event PleaseChangePoiPosition");
        if (configManager.hasPoiModification()) {
            switchMode(MapMode.POI_POSITION_EDITION);
            creationPin.setVisibility(View.GONE);

            ValueAnimator valueAnimator = ValueAnimator.ofFloat(0, OsmAnimatorUpdateListener.STEPS_CENTER_ANIMATION);
            valueAnimator.setDuration(900);
            valueAnimator.addUpdateListener(new OsmAnimatorUpdateListener(mapView.getCenter(), markerSelected.getPoint(), mapView));

            valueAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    creationPin.setVisibility(View.VISIBLE);
                    removeMarker(markerSelected);
                }
            });

            valueAnimator.start();
        } else {
            Toast.makeText(getActivity(), getResources().getString(R.string.point_modification_forbidden), Toast.LENGTH_SHORT).show();
        }
    }

    /*-----------------------------------------------------------
    * POI DELETION
    *---------------------------------------------------------*/

    public void onEventMainThread(PleaseDeletePoiFromMapEvent event) {
        Poi poi = markerSelected.getPoi();
        poi.setToDelete(true);
        markersPoi.remove(poi.getId());
        removeMarker(markerSelected);
        eventBus.post(new PleaseDeletePoiEvent(poi));
        switchMode(MapMode.DEFAULT);
    }

    /*-----------------------------------------------------------
    * POI DETAIL
    *---------------------------------------------------------*/
    private void displayPoiDetailBanner(boolean display) {
        if (display) {

            if (markerSelected != null) {
                eventBus.post(new PleaseChangeValuesDetailPoiFragmentEvent(markerSelected.getPoi().getType().getName(), markerSelected.getPoi().getName(), markerSelected.getPoi().getWay()));
            }

            if (poiDetailWrapper.getVisibility() != View.VISIBLE) {
                Animation bottomUp = AnimationUtils.loadAnimation(getActivity(),
                        R.anim.anim_up_detail);

                poiDetailWrapper.startAnimation(bottomUp);
                poiDetailWrapper.setVisibility(View.VISIBLE);
            }

        } else {


            if (poiDetailWrapper.getVisibility() == View.VISIBLE) {
                Animation bottomUp = AnimationUtils.loadAnimation(getActivity(),
                        R.anim.anim_down_detail);

                poiDetailWrapper.startAnimation(bottomUp);
                poiDetailWrapper.setVisibility(View.GONE);
            }
        }
    }

    /*-----------------------------------------------------------
    * NOTE DETAIL
    *---------------------------------------------------------*/
    private void displayNoteDetailBanner(boolean display) {
        if (display) {
            if (markerSelected != null && !markerSelected.isPoi()) {
                eventBus.post(new PleaseChangeValuesDetailNoteFragmentEvent(markerSelected.getNote()));
            }

            if (noteDetailWrapper.getVisibility() != View.VISIBLE) {
                Animation bottomUp = AnimationUtils.loadAnimation(getActivity(),
                        R.anim.anim_up_detail);

                noteDetailWrapper.startAnimation(bottomUp);
                noteDetailWrapper.setVisibility(View.VISIBLE);
            }

        } else {


            if (noteDetailWrapper.getVisibility() == View.VISIBLE) {
                Animation bottomUp = AnimationUtils.loadAnimation(getActivity(),
                        R.anim.anim_down_detail);

                noteDetailWrapper.startAnimation(bottomUp);
                noteDetailWrapper.setVisibility(View.GONE);
            }
        }
    }

    /*-----------------------------------------------------------
    * MAP UTILS
    *---------------------------------------------------------*/
    @InjectView(R.id.localisation)
    FloatingActionButton floatingButtonLocalisation;

    @OnClick(R.id.localisation)
    public void setOnPosition() {
        Timber.d("Center position on user location");
        LatLng pos = mapView.getUserLocation();
        if (pos != null) {
            mapView.setCenter(pos);
        } else {
            Toast.makeText(getActivity(), getResources().getString(R.string.location_not_found), Toast.LENGTH_SHORT).show();
        }
        tracker.send(new HitBuilders.EventBuilder()
                .setCategory(Category.GeoLocation.getValue())
                .setAction("Center map on user geolocation")
                .build());
    }

    public void onEventMainThread(ChangeMapModeEvent event) {
        switchMode(event.getMapMode());
    }


    /*-----------------------------------------------------------
    * SYNC
    *---------------------------------------------------------*/

    public void onEventMainThread(SyncFinishUploadPoiEvent event) {
        boolean forceRefresh = false;

        if (event.getSuccessfullyAddedPoisCount() > 0) {
            Toast.makeText(getActivity(), String.format(getResources().getString(R.string.add_done), event.getSuccessfullyAddedPoisCount()), Toast.LENGTH_SHORT).show();
            forceRefresh = true;
        }
        if (event.getSuccessfullyUpdatedPoisCount() > 0) {
            Toast.makeText(getActivity(), String.format(getResources().getString(mapMode == MapMode.WAY_EDITION ? R.string.noderef_moved : R.string.update_done), event.getSuccessfullyUpdatedPoisCount()), Toast.LENGTH_SHORT).show();
            forceRefresh = true;
        }
        if (event.getSuccessfullyDeletedPoisCount() > 0) {
            Toast.makeText(getActivity(), String.format(getResources().getString(R.string.delete_done), event.getSuccessfullyDeletedPoisCount()), Toast.LENGTH_SHORT).show();
            forceRefresh = true;
        }

        if (forceRefresh) {
            presenter.setForceRefreshPoi();
            presenter.loadPoisIfNeeded();
        }
    }

    public void onEventMainThread(NewNoteCreatedEvent event) {
        // a note has been created, select it
        markerSelectedId = event.getNoteId();
        markerSelected = null;
        selectedMarkerType = LocationMarker.MarkerType.NOTE;
        presenter.setForceRefreshNotes();
        presenter.loadPoisIfNeeded();
    }

    public void onEventMainThread(ApplyNewCommentFailedEvent event) {
        Toast.makeText(getActivity(), getString(R.string.failed_apply_comment), Toast.LENGTH_SHORT).show();
        markerSelectedId = null;
        markerSelected = null;
        selectedMarkerType = LocationMarker.MarkerType.NONE;
        switchMode(MapMode.DEFAULT);
    }

    public void onEventMainThread(SyncUnauthorizedEvent event) {
        Toast.makeText(getActivity(), R.string.couldnt_connect_retrofit, Toast.LENGTH_LONG).show();
    }

    public void onEventMainThread(SyncDownloadRetrofitErrorEvent event) {
        Toast.makeText(getActivity(), R.string.couldnt_download_retrofit, Toast.LENGTH_SHORT).show();
        showProgressBar(false);
    }

    public void onEventMainThread(SyncConflictingNodeErrorEvent event) {
        Toast.makeText(getActivity(), R.string.couldnt_update_node, Toast.LENGTH_LONG).show();
        removePoiMarkerInError(event.getPoiIdInError());
    }

    public void onEventMainThread(SyncNewNodeErrorEvent event) {
        Toast.makeText(getActivity(), R.string.couldnt_create_node, Toast.LENGTH_LONG).show();
        removePoiMarkerInError(event.getPoiIdInError());
    }

    public void onEventMainThread(SyncUploadRetrofitErrorEvent event) {
        Toast.makeText(getActivity(), R.string.couldnt_upload_retrofit, Toast.LENGTH_SHORT).show();
        removePoiMarkerInError(event.getPoiIdInError());
    }

    public void onEventMainThread(SyncUploadNoteRetrofitErrorEvent event) {
        Toast.makeText(getActivity(), R.string.couldnt_upload_retrofit, Toast.LENGTH_SHORT).show();
        removeNoteMarkerInError(event.getNoteIdInError());
    }

    // markers were added on the map the sync failed we remove them
    private void removePoiMarkerInError(Long id) {
        Marker m = markersPoi.remove(id);
        if (m != null) {
            mapView.removeMarker(m);
        }
        defaultMap();
    }

    // note was added on the map the sync failed we remove it
    private void removeNoteMarkerInError(Long id) {
        Marker m = markersNotes.remove(id);
        if (m != null) {
            mapView.removeMarker(m);
        }
        defaultMap();
    }

    /*-----------------------------------------------------------
    * VECTORIAL
    *---------------------------------------------------------*/

    @InjectView(R.id.level_bar)
    LevelBar levelBar;

    private VectorialOverlay vectorialOverlay;
    private boolean isVectorial = false;
    private double currentLevel = 0;
    Set<VectorialObject> vectorialObjectsBackground = new HashSet<>();
    private LocationMarker.MarkerType selectedMarkerType = LocationMarker.MarkerType.NONE;
    private int zoomVectorial;

    public void onEventMainThread(EditionVectorialTilesLoadedEvent event) {
        if (event.isRefreshFromOverpass()) {
            progressBar.setVisibility(View.GONE);
        }
        if (mapMode == MapMode.WAY_EDITION) {
            Timber.d("Showing nodesRefs : " + event.getVectorialObjects().size());
            clearAllNodeRef();
            vectorialObjectsEdition.clear();
            vectorialObjectsEdition.addAll(event.getVectorialObjects());
            updateVectorial(event.getLevels());

            if (getMarkerSelected() != null && getMarkerSelected().isNodeRef()) {
                boolean reselectMaker = false;
                for (VectorialObject v : vectorialObjectsEdition) {
                    if (v.getId().equals(getMarkerSelected().getNodeRef().getNodeBackendId())) {
                        reselectMaker = true;
                    }
                }
                if (!reselectMaker) {
                    unselectNoderef();
                }
            }
        }
    }

    private void updateVectorial(TreeSet<Double> levels) {

        if (vectorialOverlay == null) {
            Set<VectorialObject> vectorialObjects = new HashSet<>();
            vectorialObjects.addAll(vectorialObjectsBackground);
            vectorialObjects.addAll(vectorialObjectsEdition);

            vectorialOverlay = new VectorialOverlay(zoomVectorial, vectorialObjects, levels, getResources().getDisplayMetrics().scaledDensity);
            mapView.addOverlay(vectorialOverlay);
        } else {

            Set<VectorialObject> vectorialObjects = new HashSet<>();
            vectorialObjects.addAll(vectorialObjectsBackground);
            vectorialObjects.addAll(vectorialObjectsEdition);

            vectorialOverlay.setVectorialObjects(vectorialObjects);
            vectorialOverlay.setLevels(levels);
        }
        invalidateMap();

        levelBar.setLevels(vectorialOverlay.getLevels(), currentLevel);
        if (levelBar.getLevels().length < 2) {
            levelBar.setVisibility(View.INVISIBLE);
        } else {
            levelBar.setVisibility(View.VISIBLE);
        }
    }

    private void clearVectorialEdition() {
        if (vectorialOverlay == null) {
            return;
        } else {
            vectorialObjectsEdition.clear();
            Set<VectorialObject> vectorialObjects = new HashSet<>();
            vectorialObjects.addAll(vectorialObjectsBackground);
            vectorialOverlay.setVectorialObjects(vectorialObjects);
            invalidateMap();
        }
        levelBar.setLevels(vectorialOverlay.getLevels(), currentLevel);
        if (levelBar.getLevels().length < 2) {
            levelBar.setVisibility(View.INVISIBLE);
        } else {
            levelBar.setVisibility(View.VISIBLE);
        }
    }

    public void onEventMainThread(TooManyRequestsEvent event) {
        progressBar.setVisibility(View.GONE);
    }


    /*-----------------------------------------------------------
    * PoiType picker
    *---------------------------------------------------------*/

    @InjectView(R.id.poi_type_value)
    EditText poiTypeEditText;

    @InjectView(R.id.poi_type)
    TextView poiTypeTextView;

    @InjectView(R.id.edit_poi_type)
    ImageButton editPoiTypeBtn;

    @InjectView(R.id.autocomplete_list)
    ListView poiTypeListView;

    @InjectView(R.id.poi_type_value_wrapper)
    RelativeLayout poiTypeHeaderWrapper;

    @OnClick(R.id.edit_poi_type)
    public void onEditPoiTypeWrapperClick() {
        editPoiType();
    }

    @OnClick(R.id.poi_type_value_wrapper)
    public void onEditPoiTypeClick() {
        editPoiType();
    }

    public void editPoiType() {
        if (mapMode == MapMode.POI_CREATION) {
            switchMode(MapMode.TYPE_PICKER);
        }
    }

    private long savePoiTypeId = 0;
    private PoiTypePickerAdapter poiTypePickerAdapter;
    private List<PoiType> autocompletePoiTypeValues = new ArrayList<>();

    private void instantiatePoiTypePicker() {
        poiTypePickerAdapter = new PoiTypePickerAdapter(getActivity(), autocompletePoiTypeValues, poiTypeEditText, eventBus, bitmapHandler, sharedPreferences.getBoolean(getString(R.string.shared_prefs_expert_mode), false));
        poiTypeListView.setAdapter(poiTypePickerAdapter);

        // Add Text Change Listener to EditText
        poiTypeEditText.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Call back the Adapter with current character to Filter
                poiTypePickerAdapter.getFilter().filter(s.toString());
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void displayPoiTypePicker() {
        if (mapMode == MapMode.TYPE_PICKER) {
            if (poiTypeListView.getVisibility() != View.VISIBLE) {
                Animation bottomUp = AnimationUtils.loadAnimation(getActivity(),
                        R.anim.anim_up_poi_type);

                if (android.os.Build.VERSION.SDK_INT >= 21) {
                    bottomUp.setInterpolator(getActivity(), android.R.interpolator.linear_out_slow_in);
                }

                //the text view will be changed to edit text at the end of the animation
                bottomUp.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        poiTypeEditText.setVisibility(View.VISIBLE);
                        poiTypeTextView.setVisibility(View.GONE);
                        editPoiTypeBtn.setVisibility(View.GONE);
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });

                poiTypeListView.startAnimation(bottomUp);
                poiTypeListView.setVisibility(View.VISIBLE);

                // animation of the edit text from the top
                if (poiTypeTextView.getVisibility() == View.GONE) {
                    Animation slideTop = AnimationUtils.loadAnimation(getActivity(),
                            R.anim.slide_top_annimation);
                    poiTypeHeaderWrapper.startAnimation(slideTop);
                    poiTypeHeaderWrapper.setVisibility(View.VISIBLE);
                    poiTypeEditText.setVisibility(View.VISIBLE);
                }
            }
        } else if (mapMode == MapMode.POI_CREATION) {
            if (poiTypeListView.getVisibility() == View.VISIBLE) {
                Animation upBottom = AnimationUtils.loadAnimation(getActivity(),
                        R.anim.anim_down_poi_type);
                if (android.os.Build.VERSION.SDK_INT >= 21) {
                    upBottom.setInterpolator(getActivity(), android.R.interpolator.linear_out_slow_in);
                }
                poiTypeListView.startAnimation(upBottom);
            }
            poiTypeListView.setVisibility(View.GONE);
            poiTypeEditText.setVisibility(View.GONE);
            // if we have few value we hide the editPoiType btn
            if (presenter.getNumberOfPoiTypes() > maxPoiType) {
                editPoiTypeBtn.setVisibility(View.VISIBLE);
            }
            poiTypeTextView.setVisibility(View.VISIBLE);
        } else if (mapMode == MapMode.NOTE_CREATION) {
            poiTypeHeaderWrapper.setVisibility(View.VISIBLE);
            poiTypeTextView.setVisibility(View.VISIBLE);
            poiTypeListView.setVisibility(View.GONE);
            poiTypeEditText.setVisibility(View.GONE);
            editPoiTypeBtn.setVisibility(View.GONE);
            poiTypeTextView.setText(getResources().getString(R.string.note));
        } else {
            //For default behavior hide all poitype picker components
            poiTypeListView.setVisibility(View.GONE);
            poiTypeEditText.setVisibility(View.GONE);
            poiTypeTextView.setVisibility(View.GONE);
            editPoiTypeBtn.setVisibility(View.GONE);
            poiTypeHeaderWrapper.setVisibility(View.GONE);
        }
    }

    public void onEventMainThread(LastUsePoiTypeLoaded event) {
        poiTypePickerAdapter.addAllLastUse(event.getAutocompleteLastUsePoiTypeValues());
        poiTypeListView.setSelectionAfterHeaderView();
    }

    /*-----------------------------------------------------------
    * ADDRESS
    *---------------------------------------------------------*/
    @InjectView(R.id.addressView)
    TextView addressView;

    @Inject
    Geocoder geocoder;

    public void onEventMainThread(AddressFoundEvent event) {
        Timber.d("Received event AddressFound");
        if (getZoomLevel() >= zoomVectorial) {
            addressView.setVisibility(View.VISIBLE);
        }
        addressView.setText(event.getAddress());
    }

    /*-----------------------------------------------------------
    * FILTERS
    *---------------------------------------------------------*/

    private List<Long> poiTypeHidden = new ArrayList<>();

    private boolean displayOpenNotes = true;
    private boolean displayClosedNotes = true;

    public List<Long> getPoiTypeHidden() {
        return poiTypeHidden;
    }

    public void onEventMainThread(PleaseApplyPoiFilter event) {
        Timber.d("filtering Pois by type");
        poiTypeHidden = event.getPoiTypeIdsToHide();
        applyPoiFilter();
    }

    public void onEventMainThread(PleaseApplyNoteFilterEvent event) {
        Timber.d("filtering Notes");
        displayOpenNotes = event.isDisplayOpenNotes();
        displayClosedNotes = event.isDisplayClosedNotes();
        applyNoteFilter();
    }

    private void applyNoteFilter() {
        for (LocationMarker marker : markersNotes.values()) {
            removeMarker(marker);
            addNoteMarkerDependingOnFilters(marker);
        }
    }

    private void applyPoiFilter() {
        for (LocationMarker marker : markersPoi.values()) {
            removeMarker(marker);
            addPoiMarkerDependingOnFilters(marker);
        }
    }

    private void addNoteMarkerDependingOnFilters(LocationMarker marker) {
        Note note = marker.getNote();

        if ((displayOpenNotes && Note.STATUS_OPEN.equals(note.getStatus())) || Note.STATUS_SYNC.equals(note.getStatus()) || (displayClosedNotes && Note.STATUS_CLOSE.equals(note.getStatus()))) {
            mapView.addMarker(marker);
        } else if (mapMode.equals(MapMode.DETAIL_NOTE) && markerSelected.getNote().getId().equals(note.getId())) {
            switchMode(MapMode.DEFAULT);
        }
    }

    private void addPoiMarkerDependingOnFilters(LocationMarker marker) {
        Poi poi = marker.getPoi();
        //if we are in vectorial mode we hide all poi not at the current level
        if (poi.getType() != null && !poiTypeHidden.contains(poi.getType().getId()) && (!isVectorial || poi.isAtLevel(currentLevel) || !poi.isOnLevels(levelBar.getLevels()))) {
            mapView.addMarker(marker);
        } else if (mapMode.equals(MapMode.DETAIL_POI) && markerSelected.getPoi().getId().equals(poi.getId())) {
            //if the poi selected is hidden close the detail mode
            switchMode(MapMode.DEFAULT);
        }
    }

    /*-----------------------------------------------------------
    * TUTORIAL
    *---------------------------------------------------------*/
    public static final String TUTORIAL_CREATION_FINISH = "TUTORIAL_CREATION_FINISH";
    private ShowcaseView showcaseView;
    private int showcaseCounter = 0;
    private boolean isTuto = false;

    public void onEventMainThread(PleaseDisplayTutorialEvent event) {
        switchMode(MapMode.DEFAULT);
        displayTutorial(true);
    }

    protected void displayTutorial(boolean forceDisplay) {
        showcaseCounter = 0;

        if (presenter.getNumberOfPoiTypes() < 1) {
            return;
        }

        if (!configManager.hasPoiAddition()) {
            sharedPreferences.edit().putBoolean(TUTORIAL_CREATION_FINISH, true).apply();
            return;
        }

        boolean showTuto = forceDisplay || !sharedPreferences.getBoolean(TUTORIAL_CREATION_FINISH, false);

        if (showTuto && !isTuto) {
            isTuto = true;
            //position OK button on the left
            RelativeLayout.LayoutParams params =
                    new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
                            RelativeLayout.LayoutParams.WRAP_CONTENT);
            params.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
            params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
            params.setMargins(60, 0, 0, 200);

            showcaseView = new ShowcaseView.Builder(getActivity(), true)
                    .setStyle(R.style.CustomShowcaseTheme)
                    .setContentTitle(getString(R.string.tuto_title_press_create))
                    .setContentText(getString(R.string.tuto_text_press_create))
                    .setTarget(new ViewTarget(floatingMenuAddFewValues.getmAddButton()))
                    .setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                switch (showcaseCounter) {
                                                    case 0:
                                                        floatingMenuAddFewValues.expand();
                                                        nextTutoStep();
                                                        break;

                                                    case 1:
                                                        floatingMenuAddFewValues.getChildAt(1).performClick();
                                                        break;

                                                    case 2:
                                                        //chose the first type
                                                        poiTypeSelected(presenter.getPoiTypes().iterator().next());
                                                        switchMode(MapMode.POI_CREATION);
                                                        nextTutoStep();
                                                        break;

                                                    case 3:
                                                        nextTutoStep();
                                                        break;

                                                    case 4:
                                                        nextTutoStep();
                                                        switchMode(MapMode.DEFAULT);
                                                        break;
                                                }
                                            }
                                        }
                    )
                    .build();

            showcaseView.setButtonPosition(params);
        }
    }

    private void nextTutoStep() {
        switch (showcaseCounter) {
            case 0:
                if (presenter.getNumberOfPoiTypes() <= maxPoiType) {
                    showcaseView.setContentText(getString(R.string.tuto_text_choose_type));
                    showcaseCounter++;
                } else {
                    //compact view
                    showcaseView.setContentText(getString(R.string.tuto_text_poi_or_note));
                }
                showcaseView.setTarget(new ViewTarget(floatingMenuAddFewValues.getChildAt(0)));
                break;

            case 1:
                showcaseView.setContentText(getString(R.string.tuto_text_choose_type));
                showcaseView.setTarget(new ViewTarget(floatingMenuAddFewValues.getChildAt(0)));
                break;

            case 2:
                showcaseView.setContentText(getString(R.string.tuto_text_swipe_for_position));
                showcaseView.setTarget(new ViewTarget(mapView));
                break;

            case 3:
                showcaseView.setContentText(getString(R.string.tuto_text_confirm_position_creation));
                showcaseView.setTarget(new ViewTarget(R.id.action_confirm_position, getActivity()));
                break;

            case 4:
                closeTuto();
                break;
        }

        showcaseCounter++;
    }

    private void closeTuto() {
        showcaseView.hide();
        isTuto = false;
        sharedPreferences.edit().putBoolean(TUTORIAL_CREATION_FINISH, true).apply();
    }

    /*-----------------------------------------------------------
    * TILE SOURCES
    *---------------------------------------------------------*/
    private static final String TILE_SOURCE = "TILE_SOURCE";
    private static final String OSM_MBTILES_FILE = "osm.mbtiles";
    private static final String BING_MBTILES_FILE = "satellite.mbtiles";

    private TileLayer osmTileLayer;
    private TileLayer bingTileLayer;

    // Ids of tile layers
    public static final String OSM_TILE_SOURCE = "OSM_TILE_SOURCE";
    public static final String BING_TILE_SOURCE = "BING_TILE_SOURCE";
    private String currentTileLayer;

    private static final float MIN_ZOOM_LEVEL = 5;

    private BoundingBox scrollableAreaLimit = null;

    /**
     * Switch the tile source between the osm tile source and the bing aerial vue tile source.
     */
    private void switchTileSource() {
        switchToTileSource(BING_TILE_SOURCE.equals(currentTileLayer) ? OSM_TILE_SOURCE : BING_TILE_SOURCE);
    }

    /**
     * Change the tile source of the mapView.
     *
     * @param tileSourceId The id of the new tile source.
     */
    private void switchToTileSource(String tileSourceId) {
        Timber.d("Switch tileSource to %s", tileSourceId);
        switch (tileSourceId) {
            case BING_TILE_SOURCE:
                currentTileLayer = BING_TILE_SOURCE;
                mapView.setTileSource(bingTileLayer);
                break;
            case OSM_TILE_SOURCE:
            default:
                currentTileLayer = OSM_TILE_SOURCE;
                mapView.setTileSource(osmTileLayer);
                break;
        }
        // Reset the scroll limit of the map
        mapView.setScrollableAreaLimit(scrollableAreaLimit);
    }

    /**
     * Instantiate the different tiles sources used by the map.
     */
    private void instantiateTileSources() {
        if (FlavorUtils.isTemplate()) {
            try {
                List<String> assetsList = Arrays.asList(getActivity().getResources().getAssets().list(""));
                if (assetsList.contains(OSM_MBTILES_FILE)) {
                    // Create a TileSource with OpenstreetMap's Tiles from a MBTiles file
                    osmTileLayer = new MBTilesLayer(getActivity(), OSM_MBTILES_FILE, configManager.getZoomMaxProvider())
                            .setName("OpenStreetMap")
                            .setAttribution("© OpenStreetMap Contributors")
                            .setMaximumZoomLevel(configManager.getZoomMax());
                }

                if (assetsList.contains(BING_MBTILES_FILE)) {
                    // Create a TileSource with Bing Maps' Tiles from a MBTiles file
                    bingTileLayer = new MBTilesLayer(getActivity(), BING_MBTILES_FILE, configManager.getZoomMaxProvider())
                            .setName("Bing aerial view")
                            .setMaximumZoomLevel(configManager.getZoomMax());
                }
            } catch (IOException e) {
                Timber.e(e, "Couldn't get assets list");
            }
        }

        if (osmTileLayer == null) {
            // Create a TileSource from OpenStreetMap server
            osmTileLayer = new WebSourceTileLayer("openstreetmap", configManager.getMapUrl(), configManager.getZoomMaxProvider())
                    .setName("OpenStreetMap")
                    .setAttribution("© OpenStreetMap Contributors")
                    .setMinimumZoomLevel(MIN_ZOOM_LEVEL)
                    .setMaximumZoomLevel(configManager.getZoomMax());
        }
        if (bingTileLayer == null) {
            // Create a TileSource from Bing map with aerial with label style
            bingTileLayer = new BingTileLayer(configManager.getBingApiKey(), BingTileLayer.IMAGERYSET_AERIALWITHLABELS, configManager.getZoomMaxProvider())
                    .setName("Bing aerial view")
                    .setMinimumZoomLevel(MIN_ZOOM_LEVEL)
                    .setMaximumZoomLevel(configManager.getZoomMax());
        }

        // Set the map bounds
        if (configManager.hasBounds()) {
            scrollableAreaLimit = configManager.getBoundingBox();
        }

        // Set the map bounds for the map
        if (FlavorUtils.isTemplate()) {
            scrollableAreaLimit = Box.enlarge(configManager.getBoundingBox(), 2);
            drawBounds();
        }
    }

    public void onEventMainThread(PleaseSwitchMapStyleEvent event) {
        switchTileSource();
    }
}