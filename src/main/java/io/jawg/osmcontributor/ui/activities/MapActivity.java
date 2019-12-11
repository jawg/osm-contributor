/**
 * Copyright (C) 2019 Takima
 * <p>
 * This file is part of OSM Contributor.
 * <p>
 * OSM Contributor is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * OSM Contributor is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with OSM Contributor.  If not, see <http://www.gnu.org/licenses/>.
 */
package io.jawg.osmcontributor.ui.activities;

import android.annotation.TargetApi;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.jawg.osmcontributor.BuildConfig;
import io.jawg.osmcontributor.OsmTemplateApplication;
import io.jawg.osmcontributor.R;
import io.jawg.osmcontributor.model.entities.PoiType;
import io.jawg.osmcontributor.model.events.PleaseLoadPoiTypes;
import io.jawg.osmcontributor.ui.events.map.ChangesInDB;
import io.jawg.osmcontributor.ui.events.map.OnBackPressedMapEvent;
import io.jawg.osmcontributor.ui.events.map.PleaseApplyNoteFilterEvent;
import io.jawg.osmcontributor.ui.events.map.PleaseApplyPoiFilter;
import io.jawg.osmcontributor.ui.events.map.PleaseChangeToolbarColor;
import io.jawg.osmcontributor.ui.events.map.PleaseDisplayTutorialEvent;
import io.jawg.osmcontributor.ui.events.map.PleaseInitializeDrawer;
import io.jawg.osmcontributor.ui.events.map.PleaseInitializeNoteDrawerEvent;
import io.jawg.osmcontributor.ui.events.map.PleaseSwitchMapStyleEvent;
import io.jawg.osmcontributor.ui.events.map.PleaseSwitchWayEditionModeEvent;
import io.jawg.osmcontributor.ui.events.map.PleaseTellIfDbChanges;
import io.jawg.osmcontributor.ui.events.map.PleaseToggleDrawer;
import io.jawg.osmcontributor.ui.events.map.PleaseToggleDrawerLock;
import io.jawg.osmcontributor.ui.utils.BitmapHandler;
import io.jawg.osmcontributor.ui.utils.PoiTypeFilter;
import io.jawg.osmcontributor.utils.ConfigManager;
import io.jawg.osmcontributor.utils.FlavorUtils;
import timber.log.Timber;

public class MapActivity extends AppCompatActivity {

    @Inject
    EventBus eventBus;

    @BindView(R.id.drawer_layout)
    DrawerLayout drawerLayout;

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.navigation)
    NavigationView navigationView;

    @BindView(R.id.filter)
    NavigationView filterView;

    @Inject
    BitmapHandler bitmapHandler;

    @Inject
    ConfigManager configManager;

    @Inject
    SharedPreferences sharedPreferences;

    private List<PoiTypeFilter> filters = new ArrayList<>();

    private List<Long> poiTypesHidden;

    private MenuItem selectAllMenuItem;

    private List<MenuItem> filtersItemList;

    private boolean displayOpenNotes;
    private boolean displayClosedNotes;

    private boolean isSatelliteMode;

    private boolean isMapnikMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        ((OsmTemplateApplication) getApplication()).getOsmTemplateComponent().inject(this);
        ButterKnife.bind(this);

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

        initFilterDrawer();

        if (configManager.hasPoiAddition()) {
            navigationView.getMenu().findItem(R.id.replay_tuto_menu).setVisible(true);
        }

        navigationView.getMenu().findItem(R.id.save_changes).setEnabled(false);

        drawerLayout.setDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {

            }

            @Override
            public void onDrawerOpened(View drawerView) {
                eventBus.post(new PleaseTellIfDbChanges());
            }

            @Override
            public void onDrawerClosed(View drawerView) {

            }

            @Override
            public void onDrawerStateChanged(int newState) {

            }
        });

        navigationView.getMenu().findItem(R.id.manage_poi_types).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                startPoiTypeEditionActivity();
                return true;
            }
        });
    }

    private void initFilterDrawer() {
        if (FlavorUtils.isBus()) {
            eventBus.post(new PleaseApplyNoteFilterEvent(false, false));
        }
        if (FlavorUtils.hasFilter()) {
            filterView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(MenuItem menuItem) {
                    if (menuItem.getItemId() == R.id.select_all_item) {
                        selectAllMenuItem.setChecked(!selectAllMenuItem.isChecked());
                        onSelectAllClick();
                    } else if (menuItem.getItemId() != R.id.display_open_notes_item && menuItem.getItemId() != R.id.display_closed_notes_item) {
                        menuItem.setChecked(!menuItem.isChecked());
                        onFilterItemClick(menuItem);
                    } else {
                        menuItem.setChecked(!menuItem.isChecked());
                        onNoteItemClick(menuItem);
                    }
                    return true;
                }
            });

            selectAllMenuItem = filterView.getMenu().findItem(R.id.select_all_item);
        } else {
            drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED, filterView);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        eventBus.register(this);

        if (!sharedPreferences.getBoolean(getString(R.string.shared_prefs_preset_default), false) || !BuildConfig.FLAVOR.equals("store")) {
            navigationView.getMenu().findItem(R.id.edit_way).setVisible(false);
            navigationView.getMenu().findItem(R.id.manage_poi_types).setVisible(false);
            eventBus.post(new PleaseLoadPoiTypes());
        } else {
            navigationView.getMenu().findItem(R.id.edit_way).setVisible(true);
            navigationView.getMenu().findItem(R.id.manage_poi_types).setVisible(sharedPreferences.getBoolean(getString(R.string.shared_prefs_expert_mode), false));
        }
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

    List<PoiType> poiTypesSave;

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onPleaseInitializeDrawer(PleaseInitializeDrawer event) {
        Timber.d("initializing drawer with poiType");

        if (filtersItemList == null) {
            filtersItemList = new ArrayList<>();
        }
        List<PoiType> poiTypes = event.getPoiTypes();

        poiTypesHidden = event.getPoiTypeHidden();

        filters.clear();
        for (PoiType poiType : poiTypes) {
            Long id = poiType.getId();
            filters.add(new PoiTypeFilter(poiType.getName(), id, poiType.getIcon(), !poiTypesHidden.contains(id)));
        }
        Collections.sort(filters);

        Menu menu = filterView.getMenu();

        int order = 1;
        for (PoiTypeFilter poiTypeFilter : filters) {
            PoiTypeFilter nextPoiTypeFilter = null;
            if (order < filters.size()) {
                nextPoiTypeFilter = filters.get(order);
            }
            // Add only menu if not present
            if (menu.findItem(poiTypeFilter.getPoiTypeId().intValue()) == null) {
                filtersItemList.add(menu
                        .add(Menu.NONE, poiTypeFilter.getPoiTypeId().intValue(), (nextPoiTypeFilter != null ?
                                (poiTypeFilter.compareTo(nextPoiTypeFilter) > 0 ? order - 1 : order) : order), poiTypeFilter.getPoiTypeName())
                        .setChecked(poiTypeFilter.isActive())
                        .setIcon(bitmapHandler.getDrawable(poiTypeFilter.getPoiTypeIconName())));
            }
            order++;
        }

        // Remove only filter removed by user
        if (filters.size() < filtersItemList.size()) {
            List<MenuItem> temp = new ArrayList<>(filters.size());
            for (MenuItem menuItem : filtersItemList) {
                PoiTypeFilter poiTypeFilter = new PoiTypeFilter("", (long) menuItem.getItemId(), "", true);
                if (!filters.contains(poiTypeFilter)) {
                    menu.removeItem(poiTypeFilter.getPoiTypeId().intValue());
                } else {
                    temp.add(menuItem);
                }
            }

            filtersItemList.clear();
            filtersItemList.addAll(temp);
            temp.clear();
        }

        selectAllMenuItem.setChecked(poiTypesHidden.isEmpty());
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onPleaseInitializeNoteDrawerEvent(PleaseInitializeNoteDrawerEvent event) {
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

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onPleaseToggleDrawer(PleaseToggleDrawer event) {
        if (drawerLayout.isDrawerOpen(navigationView)) {
            Timber.d("Closing Drawer");
            drawerLayout.closeDrawer(navigationView);
        } else {
            Timber.d("Opening Drawer");
            drawerLayout.openDrawer(navigationView);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onPleaseChangeToolbarColor(PleaseChangeToolbarColor event) {
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

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onPleaseToggleDrawerLock(PleaseToggleDrawerLock event) {
        if (event.isLock()) {
            drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED, navigationView);
            drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED, filterView);
        } else {
            drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED, navigationView);
            if (FlavorUtils.hasFilter()) {
                drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED, filterView);
            }
        }
    }

    private void onSelectAllClick() {
        if (selectAllMenuItem.isChecked()) {
            poiTypesHidden.clear();
            for (MenuItem filter : filtersItemList) {
                filter.setChecked(true);
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
            poiTypesHidden.remove(id);
        } else {
            poiTypesHidden.add(id);
        }
        selectAllMenuItem.setChecked(poiTypesHidden.isEmpty());
        eventBus.post(new PleaseApplyPoiFilter(poiTypesHidden));
    }


    private void onOptionsClick(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.profile_load:
                startProfileLoadingActivity();
                break;
            case R.id.replay_tuto_menu:
                replayTutorial();
                break;
            case R.id.edit_way:
                eventBus.post(new PleaseSwitchWayEditionModeEvent());
                break;
            case R.id.switch_style:
                isSatelliteMode = !isSatelliteMode;
                eventBus.post(new PleaseSwitchMapStyleEvent(isSatelliteMode));
                drawerLayout.closeDrawer(navigationView);
                menuItem.setTitle(isSatelliteMode ? R.string.switch_style_mapnik : R.string.switch_style_satellite);
                break;
            case R.id.offline_regions:
                startOfflineRegionsActivity();
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
            case R.id.save_changes:
                drawerLayout.closeDrawer(navigationView);
                Intent intent = new Intent(this, UploadActivity.class);
                startActivity(intent);
                break;
        }
    }

    private void startPoiTypeEditionActivity() {
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

    private void startProfileLoadingActivity() {
        drawerLayout.closeDrawer(navigationView);
        Intent intent = new Intent(this, LoadProfileActivity.class);
        startActivity(intent);
    }

    private void startOfflineRegionsActivity() {
        drawerLayout.closeDrawer(navigationView);
        Intent intent = new Intent(this, OfflineRegionsActivity.class);
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

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onChangesInDB(ChangesInDB event) {
        navigationView.getMenu().findItem(R.id.save_changes).setEnabled(event.hasChanges()).setChecked(event.hasChanges());
    }

    public NavigationView getNavigationView() {
        return navigationView;
    }
}
