/**
 * Copyright (C) 2016 eBusiness Information
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

import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.jawg.osmcontributor.OsmTemplateApplication;
import io.jawg.osmcontributor.R;
import io.jawg.osmcontributor.model.events.PleaseLoadPoisToUpdateEvent;
import io.jawg.osmcontributor.model.events.PleaseRevertPoiEvent;
import io.jawg.osmcontributor.model.events.PleaseRevertPoiNodeRefEvent;
import io.jawg.osmcontributor.model.events.PoisToUpdateLoadedEvent;
import io.jawg.osmcontributor.rest.events.PleaseUploadPoiChangesByIdsEvent;
import io.jawg.osmcontributor.rest.events.SyncFinishUploadPoiEvent;
import io.jawg.osmcontributor.rest.events.error.SyncConflictingNodeErrorEvent;
import io.jawg.osmcontributor.rest.events.error.SyncConnectionLostErrorEvent;
import io.jawg.osmcontributor.rest.events.error.SyncNewNodeErrorEvent;
import io.jawg.osmcontributor.rest.events.error.SyncUnauthorizedEvent;
import io.jawg.osmcontributor.rest.events.error.SyncUploadNoteRetrofitErrorEvent;
import io.jawg.osmcontributor.rest.events.error.SyncUploadRetrofitErrorEvent;
import io.jawg.osmcontributor.ui.adapters.PoisAdapter;
import io.jawg.osmcontributor.ui.managers.tutorial.SyncTutoManager;
import io.jawg.osmcontributor.ui.managers.tutorial.TutorialManager;
import io.jawg.osmcontributor.utils.OsmAnswers;
import io.jawg.osmcontributor.utils.helper.SwipeItemTouchHelperCallback;
import io.jawg.osmcontributor.utils.upload.PoiUpdateWrapper;

public class UploadActivity extends AppCompatActivity implements PoisAdapter.OnItemRemovedListener {

    /*=========================================*/
    /*---------------INJECTIONS----------------*/
    /*=========================================*/
    @BindView(R.id.comment_edit_text)
    EditText editTextComment;

    @BindView(R.id.no_value_text)
    TextView noValues;

    @BindView(R.id.poi_list)
    RecyclerView poisListView;

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.coordinatorLayout)
    CoordinatorLayout coordinatorLayout;

    @Inject
    EventBus eventBus;

    /*=========================================*/
    /*---------------ATTRIBUTES----------------*/
    /*=========================================*/
    private List<PoiUpdateWrapper> poisWrapper = new ArrayList<>();

    private PoisAdapter adapter;

    private ProgressDialog ringProgressDialog;

    private boolean isCanceled;

    private SyncTutoManager syncTutoManager;

    /*=========================================*/
    /*----------------OVERRIDE-----------------*/
    /*=========================================*/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);
        ((OsmTemplateApplication) getApplication()).getOsmTemplateComponent().inject(this);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        OsmAnswers.visitedActivity("Page de synchronisation");

        adapter = new PoisAdapter(this, poisWrapper);
        adapter.setOnStartSwipeListener(this);
        poisListView.setAdapter(adapter);
        poisListView.setLayoutManager(new LinearLayoutManager(this));

        syncTutoManager = new SyncTutoManager(this, TutorialManager.forceDisplaySyncTuto);

        ItemTouchHelper.Callback callback = new SwipeItemTouchHelperCallback(adapter);
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(callback);
        itemTouchHelper.attachToRecyclerView(poisListView);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                View childAt = poisListView.getChildAt(0);
                if (childAt != null) {
                    syncTutoManager.launchTuto(childAt.findViewById(R.id.revert), findViewById(R.id.comment_edit_text), findViewById(R.id.action_confirm));
                }
            }
        }, 500);
    }

    @Override
    protected void onResume() {
        super.onResume();
        eventBus.register(this);
        eventBus.post(new PleaseLoadPoisToUpdateEvent());
    }

    @Override
    protected void onPause() {
        eventBus.unregister(this);
        super.onPause();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_upload, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            finish();
            return true;
        }

        if (id == R.id.action_confirm) {
            if (poisWrapper.size() == 0) {
                Toast.makeText(this, R.string.nothing_to_update, Toast.LENGTH_SHORT).show();
                return true;
            }

            if (!adapter.changedSelected()) {
                Toast.makeText(this, R.string.nothing_selected, Toast.LENGTH_SHORT).show();
                return true;
            }

            String comment = editTextComment.getText().toString();

            if (!comment.isEmpty()) {
                eventBus.post(new PleaseUploadPoiChangesByIdsEvent(comment, adapter.getPoiToUpload(), adapter.getPoiNodeRefToUpload()));
                ringProgressDialog = ProgressDialog.show(this, null, getString(R.string.saving), true);
                ringProgressDialog.setCancelable(true);
            } else {
                Toast.makeText(this, R.string.need_a_comment, Toast.LENGTH_SHORT).show();
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemRemoved(final PoiUpdateWrapper removedItem, final int position) {
        final Snackbar snackbar = Snackbar.make(coordinatorLayout, getString(R.string.revert_snack_bar), Snackbar.LENGTH_SHORT);
        snackbar.setAction(getString(R.string.undo_snack_bar), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isCanceled = true;
                snackbar.dismiss();
                adapter.insert(position, removedItem);
                poisListView.smoothScrollToPosition(position);
            }
        });
        snackbar.setCallback(new Snackbar.Callback() {
            @Override
            public void onDismissed(Snackbar snackbar, int action) {
                super.onDismissed(snackbar, action);
                if (!isCanceled && (action == DISMISS_EVENT_TIMEOUT || action == DISMISS_EVENT_CONSECUTIVE || action == DISMISS_EVENT_SWIPE || action == DISMISS_EVENT_MANUAL)) {
                    if (removedItem.getIsPoi()) {
                        eventBus.post(new PleaseRevertPoiEvent(removedItem.getId()));
                    } else {
                        eventBus.post(new PleaseRevertPoiNodeRefEvent(removedItem.getId()));
                    }
                }
            }
        });
        snackbar.show();
    }

    /*=========================================*/
    /*-----------------EVENTS------------------*/
    /*=========================================*/
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onPoisToUpdateLoadedEvent(PoisToUpdateLoadedEvent event) {
        poisWrapper.clear();
        poisWrapper.addAll(event.getPoiUpdateWrappers());
        adapter.notifyDataSetChanged();

        if (event.getPoiUpdateWrappers().size() == 0) {
            poisListView.setVisibility(View.GONE);
            noValues.setVisibility(View.VISIBLE);
            finish();
        } else {
            poisListView.setVisibility(View.VISIBLE);
            noValues.setVisibility(View.GONE);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onSyncFinishUploadPoiEvent(SyncFinishUploadPoiEvent event) {
        String result;

        if (event.getSuccessfullyAddedPoisCount() > 0) {
            result = getResources().getQuantityString(R.plurals.add_done,
                    event.getSuccessfullyAddedPoisCount(), event.getSuccessfullyAddedPoisCount());
            Toast.makeText(this, result, Toast.LENGTH_SHORT).show();
            resultReceived();
        }
        if (event.getSuccessfullyUpdatedPoisCount() > 0) {
            result = getResources().getQuantityString(R.plurals.update_done,
                    event.getSuccessfullyUpdatedPoisCount(), event.getSuccessfullyUpdatedPoisCount());
            Toast.makeText(this, result, Toast.LENGTH_SHORT).show();
            resultReceived();
        }
        if (event.getSuccessfullyDeletedPoisCount() > 0) {
            result = getResources().getQuantityString(R.plurals.delete_done,
                    event.getSuccessfullyDeletedPoisCount(), event.getSuccessfullyDeletedPoisCount());
            Toast.makeText(this, result, Toast.LENGTH_SHORT).show();
            resultReceived();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onSyncUnauthorizedEvent(SyncUnauthorizedEvent event) {
        Toast.makeText(this, R.string.couldnt_connect_retrofit, Toast.LENGTH_LONG).show();
        resultReceived();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onSyncConflictingNodeErrorEvent(SyncConflictingNodeErrorEvent event) {
        Toast.makeText(this, R.string.couldnt_update_node, Toast.LENGTH_LONG).show();
        resultReceived();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onSyncNewNodeErrorEvent(SyncNewNodeErrorEvent event) {
        Toast.makeText(this, R.string.couldnt_create_node, Toast.LENGTH_LONG).show();
        resultReceived();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onSyncUploadRetrofitErrorEvent(SyncUploadRetrofitErrorEvent event) {
        Toast.makeText(this, R.string.couldnt_upload_retrofit, Toast.LENGTH_SHORT).show();
        resultReceived();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onSyncUploadNoteRetrofitErrorEvent(SyncUploadNoteRetrofitErrorEvent event) {
        Toast.makeText(this, R.string.couldnt_upload_retrofit, Toast.LENGTH_SHORT).show();
        resultReceived();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onSyncConnectionLostErrorEvent(SyncConnectionLostErrorEvent event) {
        Toast.makeText(this, R.string.couldnt_save_connectivity, Toast.LENGTH_SHORT).show();
        resultReceived();
    }

    /*=========================================*/
    /*--------------PRIVATE CODE---------------*/
    /*=========================================*/
    private void resultReceived() {
        ringProgressDialog.cancel();
        //get all pois not updated
        eventBus.post(new PleaseLoadPoisToUpdateEvent());
    }
}