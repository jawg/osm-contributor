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
package io.jawg.osmcontributor.ui.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.github.clans.fab.FloatingActionButton;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import io.jawg.osmcontributor.OsmTemplateApplication;
import io.jawg.osmcontributor.R;
import io.jawg.osmcontributor.model.entities.Poi;
import io.jawg.osmcontributor.model.entities.relation_display.RelationDisplay;
import io.jawg.osmcontributor.model.events.BusLinesForPoiLoadedEvent;
import io.jawg.osmcontributor.model.events.BusLinesNearbyForPoiLoadedEvent;
import io.jawg.osmcontributor.model.events.PleaseCreatePoiEvent;
import io.jawg.osmcontributor.model.events.PleaseLoadBusLinesForPoiEvent;
import io.jawg.osmcontributor.model.events.PleaseLoadBusLinesNearbyForPoiEvent;
import io.jawg.osmcontributor.model.events.PleaseLoadPoiForCreationEvent;
import io.jawg.osmcontributor.model.events.PleaseLoadPoiForEditionEvent;
import io.jawg.osmcontributor.model.events.PoiForEditionLoadedEvent;
import io.jawg.osmcontributor.ui.activities.EditPoiActivity;
import io.jawg.osmcontributor.ui.adapters.TagsAdapter;
import io.jawg.osmcontributor.ui.adapters.item.shelter.TagItem;
import io.jawg.osmcontributor.ui.dialogs.AddTagDialogFragment;
import io.jawg.osmcontributor.ui.dialogs.LoginDialogFragment;
import io.jawg.osmcontributor.ui.events.edition.NewPoiTagAddedEvent;
import io.jawg.osmcontributor.ui.events.edition.PleaseApplyPoiChanges;
import io.jawg.osmcontributor.ui.events.edition.PoiChangesApplyEvent;
import io.jawg.osmcontributor.ui.events.login.PleaseAskForLoginEvent;
import io.jawg.osmcontributor.ui.managers.tutorial.AddPoiTutoManager;
import io.jawg.osmcontributor.ui.managers.tutorial.TutorialManager;
import io.jawg.osmcontributor.utils.ConfigManager;
import io.jawg.osmcontributor.utils.OsmAnswers;


public class EditPoiFragment extends Fragment {

    /*=========================================*/
    /*----------------CONSTANTS----------------*/
    /*=========================================*/
    public static final String CHANGES_KEY = "CHANGES_KEY";

    private static final String TAG = "EditPoiFragment";

    /*=========================================*/
    /*---------------INJECTIONS----------------*/
    /*=========================================*/
    @Inject
    EventBus eventBus;

    @Inject
    SharedPreferences sharedPreferences;

    @Inject
    ConfigManager configManager;

    @BindView(R.id.fab_add)
    FloatingActionButton fabAdd;

    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;

    /*=========================================*/
    /*---------------ATTRIBUTES----------------*/
    /*=========================================*/

    private TagsAdapter tagsAdapter;

    private List<TagItem> tagItemList;

    private boolean creation = false;

    private Poi poi;

    private Unbinder unbinder;

    /*=========================================*/
    /*----------------OVERRIDE-----------------*/
    /*=========================================*/
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true);

        View rootView = inflater.inflate(R.layout.fragment_edit_poi, container, false);
        ((OsmTemplateApplication) getActivity().getApplication()).getOsmTemplateComponent().inject(this);
        if (!eventBus.isRegistered(this)) {
            eventBus.register(this);
        }

        unbinder = ButterKnife.bind(this, rootView);

        Bundle args = getArguments();
        creation = args.getBoolean(EditPoiActivity.CREATION_MODE);
        Long poiId = args.getLong(EditPoiActivity.POI_ID);
        Double lat = args.getDouble(EditPoiActivity.POI_LAT, 0);
        Double lng = args.getDouble(EditPoiActivity.POI_LNG, 0);
        Long poiType = args.getLong(EditPoiActivity.POI_TYPE, 0);
        Double level = args.getDouble(EditPoiActivity.POI_LEVEL, 0);

        recyclerView.setHasFixedSize(false);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        if (creation) {
            eventBus.post(new PleaseLoadPoiForCreationEvent(lat, lng, poiType, level));
            getActivity().setTitle(getResources().getString(R.string.creation_title));
        } else {
            eventBus.post(new PleaseLoadPoiForEditionEvent(poiId));
            getActivity().setTitle(getResources().getString(R.string.edition_title));
        }

        fabAdd.setVisibility(sharedPreferences.getBoolean(getString(R.string.shared_prefs_expert_mode), false) ? View.VISIBLE : View.GONE);

        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Handler myHandler = new Handler();
        myHandler.postDelayed(() -> {
            // Display tutorial
            AddPoiTutoManager addPoiTutoManager = new AddPoiTutoManager(getActivity(), TutorialManager.forceDisplayAddTuto);
            addPoiTutoManager.addInfoTuto(getActivity().findViewById(R.id.action_confirm_edit));
        }, 500);
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        EventBus.getDefault().register(this);
    }

    @Override
    public void onDetach() {
        EventBus.getDefault().unregister(this);
        if (tagsAdapter != null) {
            EventBus.getDefault().unregister(tagsAdapter);
        }
        super.onDetach();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        MenuItem confirmMenuItem = menu.findItem(R.id.action_confirm_edit);
        if (!configManager.hasPoiModification()) {
            confirmMenuItem.setVisible(false);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (tagsAdapter == null) {
            getActivity().finish();
            return true;
        }

        if (id == R.id.action_confirm_edit) {
            // If we are in expert mode, directly create or update the Poi
            if (sharedPreferences.getBoolean(getString(R.string.shared_prefs_expert_mode), false)) {
                if (creation) {
                    getActivity().setResult(EditPoiActivity.POI_CREATED, null);
                    eventBus.post(new PleaseCreatePoiEvent(poi, tagsAdapter.getPoiChanges()));
                    OsmAnswers.localPoiAction(poi.getType().getTechnicalName(), "add");
                } else if (tagsAdapter.isChange()) {
                    getActivity().setResult(EditPoiActivity.POI_EDITED, null);
                    eventBus.post(new PleaseApplyPoiChanges(tagsAdapter.getPoiChanges(), null));
                    OsmAnswers.localPoiAction(poi.getType().getTechnicalName(), "update");
                } else {
                    Toast.makeText(getActivity(), R.string.not_any_changes, Toast.LENGTH_SHORT).show();
                }
                return true;
            }

            // In creation mode, check if there are mandatory tags. If there aren't any, proceed,
            // else check if all mandatory tags are completed.
            if (creation) {
                if (!poi.getType().hasMandatoryTags() || tagsAdapter.isValidChanges()) {
                    getActivity().setResult(EditPoiActivity.POI_CREATED, null);
                    eventBus.post(new PleaseCreatePoiEvent(poi, tagsAdapter.getPoiChanges()));
                    OsmAnswers.localPoiAction(poi.getType().getTechnicalName(), "add");
                } else {
                    tagsAdapter.refreshErrorStatus();
                    Toast.makeText(getActivity(), R.string.uncompleted_fields, Toast.LENGTH_SHORT).show();
                }
                return true;
            }

            // In edit mode, check if there are changes then check if all the mandatory tags are completed.
            if (tagsAdapter.isChange()) {
                if (tagsAdapter.isValidChanges()) {
                    getActivity().setResult(EditPoiActivity.POI_EDITED, null);
                    eventBus.post(new PleaseApplyPoiChanges(tagsAdapter.getPoiChanges(), tagsAdapter.getChangedRelations()));
                    OsmAnswers.localPoiAction(poi.getType().getTechnicalName(), "update");
                } else {
                    tagsAdapter.refreshErrorStatus();
                    Toast.makeText(getActivity(), R.string.uncompleted_fields, Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(getActivity(), R.string.not_any_changes, Toast.LENGTH_SHORT).show();
            }
            return true;
        }

        if (id == android.R.id.home) {
            if (!tagsAdapter.isChange()) {
                getActivity().setResult(Activity.RESULT_CANCELED, null);
                getActivity().finish();
            } else {
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());

                alertDialog.setTitle(R.string.quit_edition_title);
                alertDialog.setMessage(R.string.quit_edition_message);
                alertDialog.setPositiveButton(R.string.quit_edition_positive_btn, (dialog, which) -> {
                    getActivity().setResult(Activity.RESULT_CANCELED, null);
                    getActivity().finish();
                });

                alertDialog.setNegativeButton(R.string.quit_edition_negative_btn, (dialog, which) -> dialog.cancel());

                alertDialog.show();
            }
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSaveInstanceState(Bundle savedState) {
        super.onSaveInstanceState(savedState);
    }

    /*=========================================*/
    /*------------------CLICK------------------*/
    /*=========================================*/
    @OnClick(R.id.fab_add)
    public void addTag() {
        AddTagDialogFragment.display(((AppCompatActivity) getActivity()).getSupportFragmentManager());
    }

    /*=========================================*/
    /*------------------EVENTS-----------------*/
    /*=========================================*/
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onPoiForEditionLoadedEvent(PoiForEditionLoadedEvent event) {
        poi = event.getPoi();

        //Set the poitype name in the action bar
        ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.setSubtitle(poi.getType().getName());
        }

        Log.i(TAG, "onPoiForEditionLoadedEvent: " + poi);
        tagsAdapter = new TagsAdapter(poi,
                tagItemList,
                getActivity(),
                event.getValuesMap(),
                sharedPreferences.getBoolean(getString(R.string.shared_prefs_expert_mode), false));
        recyclerView.setAdapter(tagsAdapter);

        eventBus.post(new PleaseLoadBusLinesForPoiEvent(poi.getRelationIds()));
        eventBus.post(new PleaseLoadBusLinesNearbyForPoiEvent(poi));
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onNewPoiTagAddedEvent(NewPoiTagAddedEvent event) {
        recyclerView.smoothScrollToPosition(tagsAdapter.addLast(event.getTagKey(), event.getTagValue(), Collections.<String, String>emptyMap()));
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onPoiChangesApplyEvent(PoiChangesApplyEvent event) {
        getActivity().finish();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onAskForLoginEvent(PleaseAskForLoginEvent event) {
        LoginDialogFragment.newInstance()
                .setOnDismiss(dialogInterface -> getActivity().finish())
                .show(getFragmentManager(), LoginDialogFragment.class.getSimpleName());
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onBusLinesForPoiLoadedEvent(BusLinesForPoiLoadedEvent event) {
        List<RelationDisplay> relationDisplays = event.getRelationDisplays();
        tagsAdapter.setRelationDisplays(relationDisplays);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onBusLinesNearbyForPoiLoadedEvent(BusLinesNearbyForPoiLoadedEvent event) {
        List<RelationDisplay> relationDisplays = event.getRelationDisplays();
        tagsAdapter.setRelationDisplaysNearby(relationDisplays);
    }
}
