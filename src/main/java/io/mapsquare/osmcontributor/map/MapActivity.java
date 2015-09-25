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

import android.annotation.TargetApi;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.NavigationView;
import android.support.design.widget.NavigationView2;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import de.greenrobot.event.EventBus;
import io.mapsquare.osmcontributor.OsmTemplateApplication;
import io.mapsquare.osmcontributor.R;
import io.mapsquare.osmcontributor.about.AboutActivity;
import io.mapsquare.osmcontributor.core.ConfigManager;
import io.mapsquare.osmcontributor.core.model.PoiType;
import io.mapsquare.osmcontributor.map.events.OnBackPressedMapEvent;
import io.mapsquare.osmcontributor.map.events.PleaseApplyNoteFilterEvent;
import io.mapsquare.osmcontributor.map.events.PleaseApplyPoiFilter;
import io.mapsquare.osmcontributor.map.events.PleaseChangeToolbarColor;
import io.mapsquare.osmcontributor.map.events.PleaseDisplayTutorialEvent;
import io.mapsquare.osmcontributor.map.events.PleaseInitializeDrawer;
import io.mapsquare.osmcontributor.map.events.PleaseInitializeNoteDrawerEvent;
import io.mapsquare.osmcontributor.map.events.PleaseSwitchModeEvent;
import io.mapsquare.osmcontributor.map.events.PleaseToggleDrawer;
import io.mapsquare.osmcontributor.map.events.PleaseToggleDrawerLock;
import io.mapsquare.osmcontributor.preferences.MyPreferencesActivity;
import io.mapsquare.osmcontributor.sync.upload.SyncUploadService;
import io.mapsquare.osmcontributor.type.TypeListActivity;
import io.mapsquare.osmcontributor.upload.UploadActivity;
import io.mapsquare.osmcontributor.utils.FlavorUtils;
import timber.log.Timber;

public class MapActivity extends AppCompatActivity {

    @Inject
    EventBus eventBus;

    @InjectView(R.id.drawer_layout)
    DrawerLayout drawerLayout;

    @InjectView(R.id.toolbar)
    Toolbar toolbar;

    @InjectView(R.id.navigation)
    NavigationView navigationView;

    @InjectView(R.id.filter)
    NavigationView2 filterView;

    @Inject
    BitmapHandler bitmapHandler;

    @Inject
    ConfigManager configManager;

    @Inject
    SharedPreferences sharedPreferences;

    private Map<Long, PoiType> poiTypes;

    private List<PoiTypeFilter> filters = new ArrayList<>();

    private List<Long> poiTypesHidden;

    private MenuItem selectAllMenuItem;

    private List<MenuItem> filtersItemList;

    private boolean displayOpenNotes;
    private boolean displayClosedNotes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        ((OsmTemplateApplication) getApplication()).getOsmTemplateComponent().inject(this);
        ButterKnife.inject(this);

        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

        setSupportActionBar(toolbar);

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {
                switch (menuItem.getGroupId()) {
                    case R.id.drawer_options_group:
                        onOptionsClick(menuItem);
                        break;
                    case R.id.sync:
                        onOptionsSyncClick(menuItem);
                        break;
                }
                return true;
            }
        });
        filterView.setNavigationItemSelectedListener(new NavigationView2.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {
                switch (menuItem.getGroupId()) {
                    case R.id.drawer_filter_pois_group:
                        if (menuItem.getItemId() == R.id.select_all_item) {
                            onSelectAllClick();
                        } else {
                            onFilterItemClick(menuItem);
                        }
                        break;
                    case R.id.drawer_filter_notes_group:
                        onNoteItemClick(menuItem);
                        break;
                }
                return true;
            }
        });

        selectAllMenuItem = filterView.getMenu().findItem(R.id.select_all_item);

        navigationView.getMenu().findItem(R.id.manage_poi_types).setVisible(!FlavorUtils.isPoiStorage());

        if (!FlavorUtils.isPoiStorage() && configManager.hasPoiModification()) {
            navigationView.getMenu().findItem(R.id.edit_way).setVisible(true);
        }

        if (configManager.hasPoiAddition()) {
            navigationView.getMenu().findItem(R.id.replay_tuto_menu).setVisible(true);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        boolean isManual = sharedPreferences.getBoolean(SyncUploadService.IS_MANUAL_SYNC, false);
        navigationView.getMenu().setGroupVisible(R.id.sync, isManual);

        eventBus.register(this);
    }

    @Override
    protected void onPause() {
        eventBus.unregister(this);

        super.onPause();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_map, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_filter_drawer) {
            if (drawerLayout.isDrawerOpen(filterView)) {
                drawerLayout.closeDrawer(filterView);
            } else {
                drawerLayout.openDrawer(filterView);
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(navigationView)) {
            drawerLayout.closeDrawer(navigationView);
        } else if (drawerLayout.isDrawerOpen(filterView)) {
            drawerLayout.closeDrawer(filterView);
        } else {
            eventBus.post(new OnBackPressedMapEvent());
        }
    }

    public void onEventMainThread(PleaseInitializeDrawer event) {
        Timber.d("initializing drawer with poiType");

        if (filtersItemList == null) {
            filtersItemList = new ArrayList<>();
        }
        poiTypes = event.getPoiTypes();

        poiTypesHidden = event.getPoiTypeHidden();
        boolean allSelected = poiTypesHidden.isEmpty();

        filters.clear();
        for (PoiType poiType : poiTypes.values()) {
            Long id = poiType.getId();
            filters.add(new PoiTypeFilter(poiType.getName(), id, poiType.getIcon(), !poiTypesHidden.contains(id)));
        }
        Collections.sort(filters);

        Menu menu = filterView.getMenu();
        menu.removeGroup(R.id.drawer_filter_pois_group);

        filtersItemList.clear();
        for (PoiTypeFilter poiTypeFilter : filters) {
            filtersItemList.add(menu
                    .add(R.id.drawer_filter_pois_group, poiTypeFilter.getPoiTypeId().intValue(), 0, poiTypeFilter.getPoiTypeName())
                    .setChecked(!allSelected && poiTypeFilter.isActive())
                    .setIcon(bitmapHandler.getDrawable(poiTypeFilter.getPoiTypeIconName())));
        }

        menu.setGroupCheckable(R.id.drawer_filter_pois_group, true, false);

        selectAllMenuItem.setChecked(allSelected);
    }

    public void onEventMainThread(PleaseInitializeNoteDrawerEvent event) {
        Menu menu = filterView.getMenu();
        if (!FlavorUtils.isPoiStorage()) {
            displayOpenNotes = event.isDisplayOpenNotes();
            displayClosedNotes = event.isDisplayClosedNotes();

            menu.findItem(R.id.display_open_notes_item).setChecked(displayOpenNotes);
            menu.findItem(R.id.display_closed_notes_item).setChecked(displayClosedNotes);
        } else {
            menu.removeItem(R.id.drawer_filter_notes_menu);
        }
    }

    public void onEventMainThread(PleaseToggleDrawer event) {
        Timber.d("Opening Drawer");
        drawerLayout.openDrawer(navigationView);
    }

    public void onEventMainThread(PleaseChangeToolbarColor event) {
        if (toolbar != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                changeNotificationToolbarColor(event.isCreation());
            }
            if (event.isCreation()) {
                toolbar.setBackgroundColor(getResources().getColor(R.color.colorCreation));
            } else {
                toolbar.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
            }
        }
    }

    @TargetApi(21)
    private void changeNotificationToolbarColor(boolean isCreation) {
        Window window = getWindow();

        if (window != null) {
            if (isCreation) {
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
                window.setStatusBarColor(getResources().getColor(R.color.colorCreationDark));
            } else {
                window.clearFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            }
        }
    }

    public void onEventMainThread(PleaseToggleDrawerLock event) {
        if (event.isLock()) {
            drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        } else {
            drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
        }
    }

    private void onSelectAllClick() {
        if (selectAllMenuItem.isChecked()) {
            poiTypesHidden.clear();
            for (MenuItem filter : filtersItemList) {
                filter.setChecked(false);
            }
        } else {
            for (MenuItem filter : filtersItemList) {
                poiTypesHidden.add((long) filter.getItemId());
                filter.setChecked(false);
            }
        }
        eventBus.post(new PleaseApplyPoiFilter(poiTypesHidden));
    }

    private void onFilterItemClick(MenuItem item) {
        long id = item.getItemId();
        if (item.isChecked()) {
            if (selectAllMenuItem.isChecked()) {
                selectAllMenuItem.setChecked(false);
                poiTypesHidden.clear(); // Should be already empty
                for (MenuItem filter : filtersItemList) {
                    long itemId = filter.getItemId();
                    if (itemId != id) {
                        poiTypesHidden.add(itemId);
                    }
                }
            } else {
                poiTypesHidden.remove(id);
                if (poiTypesHidden.isEmpty()) {
                    selectAllMenuItem.setChecked(true);
                    for (MenuItem filter : filtersItemList) {
                        filter.setChecked(false);
                    }
                }
            }
        } else {
            poiTypesHidden.add(id);
        }
        eventBus.post(new PleaseApplyPoiFilter(poiTypesHidden));
    }


    private void onOptionsClick(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.replay_tuto_menu:
                replayTutorial();
                break;
            case R.id.edit_way:
                eventBus.post(new PleaseSwitchModeEvent(MapMode.WAY_EDITION));
                break;
            case R.id.manage_poi_types:
                startPoiTypeListActivity();
                break;
            case R.id.preferences_menu:
                startPreferencesActivity();
                break;
            case R.id.about_menu:
                startAboutActivity();
                break;
        }
    }

    private void onOptionsSyncClick(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.auto_sync:
                Intent intent = new Intent(this, UploadActivity.class);
                startActivity(intent);
                break;
        }
    }

    private void startPoiTypeListActivity() {
        drawerLayout.closeDrawer(navigationView);
        Intent intent = new Intent(this, TypeListActivity.class);
        startActivity(intent);
    }

    private void startPreferencesActivity() {
        drawerLayout.closeDrawer(navigationView);
        Intent intent = new Intent(this, MyPreferencesActivity.class);
        startActivity(intent);
    }

    private void startAboutActivity() {
        drawerLayout.closeDrawer(navigationView);
        Intent intent = new Intent(this, AboutActivity.class);
        startActivity(intent);
    }

    private void replayTutorial() {
        drawerLayout.closeDrawer(navigationView);
        eventBus.post(new PleaseDisplayTutorialEvent());
    }

    private void onNoteItemClick(MenuItem menuItem) {
        boolean checked = menuItem.isChecked();

        switch (menuItem.getItemId()) {
            case R.id.display_open_notes_item:
                displayOpenNotes = checked;
                break;
            case R.id.display_closed_notes_item:
                displayClosedNotes = checked;
                break;
        }

        eventBus.post(new PleaseApplyNoteFilterEvent(displayOpenNotes, displayClosedNotes));
    }
}
