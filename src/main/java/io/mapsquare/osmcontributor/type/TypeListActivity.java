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
package io.mapsquare.osmcontributor.type;

import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SearchView;
import android.widget.ViewSwitcher;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import de.greenrobot.event.EventBus;
import io.mapsquare.osmcontributor.OsmTemplateApplication;
import io.mapsquare.osmcontributor.R;
import io.mapsquare.osmcontributor.core.events.PleaseLoadPoiTypes;
import io.mapsquare.osmcontributor.core.model.PoiType;
import io.mapsquare.osmcontributor.core.model.PoiTypeTag;
import io.mapsquare.osmcontributor.map.BitmapHandler;
import io.mapsquare.osmcontributor.type.adapter.PoiTypeAdapter;
import io.mapsquare.osmcontributor.type.adapter.PoiTypeTagAdapter;
import io.mapsquare.osmcontributor.type.helper.DragSwipeItemTouchHelperCallback;
import io.mapsquare.osmcontributor.type.helper.SwipeItemTouchHelperCallback;
import io.mapsquare.osmcontributor.utils.StringUtils;
import timber.log.Timber;

public class TypeListActivity extends AppCompatActivity implements PoiTypeTagAdapter.OnStartDragListener {

    @InjectView(R.id.toolbar)
    Toolbar toolbar;
    @InjectView(R.id.progress_content_switcher)
    ViewSwitcher viewSwitcher;

    @InjectView(R.id.content)
    RelativeLayout content;

    @InjectView(R.id.progressbar)
    ProgressBar progressBar;

    @InjectView(R.id.list_switcher)
    HorizontalViewSwitcher listSwitcher;

    @InjectView(R.id.list_poi_types)
    RecyclerView recyclerTypes;

    @InjectView(R.id.list_poi_tags)
    RecyclerView recyclerTags;

    @Inject
    EventBus eventBus;

    @Inject
    BitmapHandler bitmapHandler;

    private static final String FILTER_CONSTRAINT = "FILTER_CONSTRAINT";

    private TypeListActivityPresenter presenter;

    private boolean showingTypes = true;

    private PoiTypeAdapter typesAdapter;

    private PoiTypeTagAdapter tagsAdapter;
    private ItemTouchHelper dragSwipeItemTouchHelper;

    private SearchView searchView;
    private String filterConstraint;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_type_list);
        ButterKnife.inject(this);
        ((OsmTemplateApplication) getApplication()).getOsmTemplateComponent().inject(this);

        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        presenter = new TypeListActivityPresenter(this, savedInstanceState);

        typesAdapter = new PoiTypeAdapter(new ArrayList<PoiType>(), bitmapHandler);
        typesAdapter.setPoiTypeAdapterListener(presenter.getPoiTypeAdapterListener());

        recyclerTypes.setAdapter(typesAdapter);
        recyclerTypes.setLayoutManager(new LinearLayoutManager(this));

        ItemTouchHelper.Callback callback = new SwipeItemTouchHelperCallback(typesAdapter);
        ItemTouchHelper swipeItemTouchHelper = new ItemTouchHelper(callback);
        swipeItemTouchHelper.attachToRecyclerView(recyclerTypes);

        tagsAdapter = new PoiTypeTagAdapter(new ArrayList<PoiTypeTag>());
        tagsAdapter.setPoiTypeTagAdapterListener(presenter.getPoiTypeTagAdapterListener());
        tagsAdapter.setOnStartDragListener(this);

        recyclerTags.setAdapter(tagsAdapter);
        recyclerTags.setLayoutManager(new LinearLayoutManager(this));

        ItemTouchHelper.Callback dragSwipeCallback = new DragSwipeItemTouchHelperCallback(tagsAdapter);
        dragSwipeItemTouchHelper = new ItemTouchHelper(dragSwipeCallback);
        dragSwipeItemTouchHelper.attachToRecyclerView(recyclerTags);

        listSwitcher.prepareViews(showingTypes);

        if (savedInstanceState != null) {
            filterConstraint = savedInstanceState.getString(FILTER_CONSTRAINT);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_manage_poi, menu);

        final MenuItem searchItem = menu.findItem(R.id.action_search);
        searchView = (SearchView) searchItem.getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                onQueryTextChange(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                typesAdapter.getFilter().filter(newText);
                filterConstraint = newText;
                return true;
            }
        });
        if (!showingTypes) {
            searchView.setVisibility(View.GONE);
        }
        if (showingTypes && !StringUtils.isEmpty(filterConstraint)) {
            searchView.setIconified(false);
            searchView.setQuery(filterConstraint, true);
        }
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        presenter.onResume();
    }

    @Override
    protected void onPause() {
        presenter.onPause();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        presenter.onSaveInstanceState(outState);
        if (!StringUtils.isEmpty(filterConstraint)) {
            outState.putString(FILTER_CONSTRAINT, filterConstraint);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        if (!presenter.onBackPressed()) {
            eventBus.post(new PleaseLoadPoiTypes());
            super.onBackPressed();
        }
    }

    @OnClick(R.id.fab_add)
    public void fabClick() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        if (showingTypes) {
            EditPoiTypeDialogFragment.display(fragmentManager);
        } else {
            EditPoiTagDialogFragment.display(fragmentManager);
        }
    }

    void showTypes(List<PoiType> poiTypes) {
        showingTypes = true;

        typesAdapter.setPoiTypes(poiTypes);
        changeTitle(R.string.manage_poi_types, null);
        showContent();
        listSwitcher.showView(recyclerTypes);
        if (searchView != null) {
            searchView.setVisibility(View.VISIBLE);
            // If there was a search, relaunch it
            if (searchView.getQuery() != null && searchView.getQuery().length() > 0) {
                searchView.setQuery(searchView.getQuery(), true);
            }
        }
        Timber.d("just loaded %s types ", poiTypes.size());
    }

    void showTags(Collection<PoiTypeTag> poiTypeTags, PoiType poiType) {
        showingTypes = false;

        tagsAdapter.setPoiTypeTags(poiTypeTags);
        changeTitle(R.string.manage_poi_tags, poiType.getName());
        showContent();
        listSwitcher.showView(recyclerTags);
        searchView.setVisibility(View.GONE);
        Timber.d("just loaded %s tags ", poiTypeTags.size());
    }

    public void showContent() {
        if (viewSwitcher.getNextView() == content) {
            viewSwitcher.showNext();
        }
    }

    public void showProgressBar() {
        if (viewSwitcher.getNextView() == progressBar) {
            viewSwitcher.showNext();
        }
    }

    public Snackbar createSnackbar(CharSequence deletedItem) {
        CharSequence message = getString(R.string.item_removed, deletedItem);
        return Snackbar.make(content, message, Snackbar.LENGTH_LONG);
    }

    public void undoPoiTypeRemoval() {
        typesAdapter.undoLastRemoval();
    }

    public void undoPoiTypeTagRemoval() {
        tagsAdapter.undoLastRemoval();
    }

    public void notifyPoiTypeDefinitivelyRemoved(PoiType poiType) {
        typesAdapter.notifyLastRemovalDone(poiType);
    }

    public void notifyPoiTagDefinitivelyRemoved(PoiTypeTag poiTypeTag) {
        tagsAdapter.notifyLastRemovalDone(poiTypeTag);
        typesAdapter.notifyTagRemoved(poiTypeTag);
    }

    public PoiType getPoiTypeById(Long id) {
        return typesAdapter.getItemById(id);
    }

    public void addNewPoiType(PoiType item) {
        int position = typesAdapter.addItem(item);
        if (position != -1) {
            recyclerTypes.smoothScrollToPosition(position);
        }
    }

    public void addNewPoiTag(PoiTypeTag item) {
        recyclerTags.smoothScrollToPosition(tagsAdapter.addItem(item));
    }

    public void refreshPoiTag(PoiType item) {
        tagsAdapter.setPoiTypeTags(item.getTags());
    }

    private void changeTitle(int title, CharSequence subtitle) {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(title);
            actionBar.setSubtitle(subtitle);
        }
    }

    @Override
    public void onStartDrag(RecyclerView.ViewHolder viewHolder) {
        dragSwipeItemTouchHelper.startDrag(viewHolder);
    }
}

