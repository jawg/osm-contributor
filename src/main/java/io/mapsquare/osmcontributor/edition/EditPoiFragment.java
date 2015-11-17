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
package io.mapsquare.osmcontributor.edition;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.github.amlcurran.showcaseview.ShowcaseView;
import com.github.amlcurran.showcaseview.targets.ViewTarget;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import de.greenrobot.event.EventBus;
import io.mapsquare.osmcontributor.OsmTemplateApplication;
import io.mapsquare.osmcontributor.R;
import io.mapsquare.osmcontributor.core.ConfigManager;
import io.mapsquare.osmcontributor.core.events.PleaseCreatePoiEvent;
import io.mapsquare.osmcontributor.core.events.PleaseLoadPoiForCreationEvent;
import io.mapsquare.osmcontributor.core.events.PleaseLoadPoiForEditionEvent;
import io.mapsquare.osmcontributor.core.events.PoiForEditionLoadedEvent;
import io.mapsquare.osmcontributor.core.model.Poi;
import io.mapsquare.osmcontributor.edition.events.PleaseApplyPoiChanges;
import io.mapsquare.osmcontributor.edition.events.PoiChangesApplyEvent;


public class EditPoiFragment extends Fragment {

    private TagsAdapter tagsAdapter;
    public static final String CHANGES_KEY = "CHANGES_KEY";

    private List<CardModel> cardModelList;
    private boolean creation = false;
    private Poi poi;
    private boolean menuReady = false;
    private boolean viewReady = false;

    // ANALYTICS ATTRIBUTES
    private Tracker tracker;

    private enum Category {
        Creation("Creation POI"),
        Edition("Edition POI");

        private final String value;

        Category(String value) {
            this.value = value;
        }

        String getValue() {
            return value;
        }
    }

    @InjectView(R.id.recycler_view)
    android.support.v7.widget.RecyclerView recyclerView;

    @Inject
    EventBus eventBus;

    @Inject
    SharedPreferences sharedPreferences;

    @Inject
    ConfigManager configManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            cardModelList = savedInstanceState.getParcelableArrayList(CHANGES_KEY);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        setHasOptionsMenu(true);

        View rootView = inflater.inflate(R.layout.fragment_edit_poi, container, false);
        ((OsmTemplateApplication) getActivity().getApplication()).getOsmTemplateComponent().inject(this);
        ButterKnife.inject(this, rootView);

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

        tracker = ((OsmTemplateApplication) this.getActivity().getApplication()).getTracker(OsmTemplateApplication.TrackerName.APP_TRACKER);
        if (creation) {
            eventBus.post(new PleaseLoadPoiForCreationEvent(lat, lng, poiType, level));
            getActivity().setTitle(getResources().getString(R.string.creation_title));

            tracker.setScreenName("CreationView");
            tracker.send(new HitBuilders.ScreenViewBuilder().build());

        } else {
            eventBus.post(new PleaseLoadPoiForEditionEvent(poiId));
            getActivity().setTitle(getResources().getString(R.string.edition_title));

            tracker.setScreenName("EditView");
            tracker.send(new HitBuilders.ScreenViewBuilder().build());
        }

        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (menuReady) {
            Handler myHandler = new Handler();
            myHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    displayTutorial();
                }
            }, 400);
        } else {
            viewReady = true;
        }
    }


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
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
        ButterKnife.reset(this);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        MenuItem confirmMenuItem = menu.findItem(R.id.action_confirm_edit);
        if (!configManager.hasPoiModification()) {
            confirmMenuItem.setVisible(false);
        }
        if (viewReady) {
            Handler myHandler = new Handler();
            myHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    displayTutorial();
                }
            }, 400);
        } else {
            menuReady = true;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (tagsAdapter == null) {
            finishTuto();
            getActivity().finish();
            return true;
        }


        if (id == R.id.action_confirm_edit) {
            finishTuto();
            if (tagsAdapter.isChange()) {
                if (tagsAdapter.isValidChanges()) {
                    if (creation) {
                        getActivity().setResult(EditPoiActivity.POI_CREATED, null);
                        eventBus.post(new PleaseCreatePoiEvent(poi, tagsAdapter.getPoiChanges()));
                    } else {
                        getActivity().setResult(EditPoiActivity.POI_EDITED, null);
                        eventBus.post(new PleaseApplyPoiChanges(tagsAdapter.getPoiChanges()));
                    }
                } else {
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
                if (creation) {
                    tracker.send(new HitBuilders.EventBuilder()
                            .setCategory(Category.Creation.getValue())
                            .setAction("Canceled creation")
                            .build());
                } else {
                    tracker.send(new HitBuilders.EventBuilder()
                            .setCategory(Category.Edition.getValue())
                            .setAction("Canceled Edition")
                            .build());
                }
            } else {
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());

                alertDialog.setTitle(R.string.quit_edition_title);
                alertDialog.setMessage(R.string.quit_edition_message);
                alertDialog.setPositiveButton(R.string.quit_edition_positive_btn, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        if (creation) {
                            tracker.send(new HitBuilders.EventBuilder()
                                    .setCategory(Category.Creation.getValue())
                                    .setAction("Canceled creation")
                                    .build());
                        } else {
                            tracker.send(new HitBuilders.EventBuilder()
                                    .setCategory(Category.Edition.getValue())
                                    .setAction("Canceled Edition")
                                    .build());
                        }

                        getActivity().setResult(Activity.RESULT_CANCELED, null);
                        getActivity().finish();
                    }
                });

                alertDialog.setNegativeButton(R.string.quit_edition_negative_btn, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                alertDialog.show();
            }
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSaveInstanceState(Bundle savedState) {
        super.onSaveInstanceState(savedState);
        savedState.putParcelableArrayList(CHANGES_KEY, new ArrayList<>(tagsAdapter.getCardModelList()));
    }

    public void onEventMainThread(PoiForEditionLoadedEvent event) {
        poi = event.getPoi();

        //Set the poitype name in the action bar
        ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.setSubtitle(poi.getType().getName());
        }

        tagsAdapter = new TagsAdapter(poi, cardModelList, getActivity(), event.getValuesMap(), configManager);
        recyclerView.setAdapter(tagsAdapter);
    }

    public void onEventMainThread(PoiChangesApplyEvent event) {
        if (creation) {
            tracker.send(new HitBuilders.EventBuilder()
                    .setCategory(Category.Creation.getValue())
                    .setAction("POI Created")
                    .build());
        } else {
            tracker.send(new HitBuilders.EventBuilder()
                    .setCategory(Category.Edition.getValue())
                    .setAction("POI Edited")
                    .build());
        }

        getActivity().finish();
    }



    /*-----------------------------------------------------------
    * TUTORIAL
    *---------------------------------------------------------*/

    public static final String TUTORIAL_EDIT_TAG_FINISH = "TUTORIAL_EDIT_TAG_FINISH";
    private ShowcaseView showcaseView;
    private int showcaseCounter = 0;
    private boolean isTuto = false;

    private void displayTutorial() {
        isTuto = !sharedPreferences.getBoolean(TUTORIAL_EDIT_TAG_FINISH, false);

        if (!configManager.hasPoiModification()) {
            isTuto = false;
            sharedPreferences.edit().putBoolean(TUTORIAL_EDIT_TAG_FINISH, true).apply();
        }


        if (isTuto) {

            RelativeLayout.LayoutParams params =
                    new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
                            RelativeLayout.LayoutParams.WRAP_CONTENT);
            params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);
            params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
            params.setMargins(0, 0, 60, 200);

            showcaseView = new ShowcaseView.Builder(getActivity(), true)
                    .setStyle(R.style.CustomShowcaseTheme)
                    .setContentTitle(getString(R.string.tuto_title_confirm))
                    .setContentText(getString(R.string.tuto_text_confirm_creation))
                    .setTarget(new ViewTarget(R.id.action_confirm_edit, getActivity()))
                    .setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                nextTutoStep();
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
                showcaseView.setShowcaseX(0);
                showcaseView.setShowcaseY(40);
                showcaseView.setContentText(getString(R.string.tuto_text_cancel_creation));
                showcaseView.setContentTitle(getString(R.string.tuto_title_cancel));
                break;

            case 1:
                finishTuto();
                break;
        }

        showcaseCounter++;
    }

    private void finishTuto() {
        if (showcaseView != null) {
            isTuto = false;
            showcaseView.hide();
            sharedPreferences.edit().putBoolean(TUTORIAL_EDIT_TAG_FINISH, true).apply();
        }
    }
}
