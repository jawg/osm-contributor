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
package io.mapsquare.osmcontributor.ui.fragments;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.drawee.view.SimpleDraweeView;
import com.flickr4java.flickr.photos.Size;
import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;

import io.mapsquare.osmcontributor.ui.events.map.PleaseDuplicatePoiEvent;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.mapsquare.osmcontributor.OsmTemplateApplication;
import io.mapsquare.osmcontributor.R;
import io.mapsquare.osmcontributor.flickr.event.PhotosFoundEvent;
import io.mapsquare.osmcontributor.flickr.rest.asynctask.GetFlickrPhotos;
import io.mapsquare.osmcontributor.model.entities.Poi;
import io.mapsquare.osmcontributor.ui.activities.PhotoActivity;
import io.mapsquare.osmcontributor.ui.events.map.PleaseChangePoiPosition;
import io.mapsquare.osmcontributor.ui.events.map.PleaseChangeValuesDetailPoiFragmentEvent;
import io.mapsquare.osmcontributor.ui.events.map.PleaseDeletePoiFromMapEvent;
import io.mapsquare.osmcontributor.ui.events.map.PleaseOpenEditionEvent;
import io.mapsquare.osmcontributor.utils.ConfigManager;

/**
 * Fragment to display when the user click on marker
 * and want to see details.
 */
public class PoiDetailFragment extends Fragment {

    private Map<Long, String> thumbnailsCache = new HashMap<>();

    /*=========================================*/
    /*--------------INJECTIONS-----------------*/
    /*=========================================*/
    @Inject
    EventBus eventBus;

    @Inject
    ConfigManager configManager;

    /*=========================================*/
    /*----------------VIEWS--------------------*/
    /*=========================================*/
    @BindView(R.id.poi_name)
    TextView editTextPoiName;

    @BindView(R.id.poi_type_name)
    TextView editTextPoiTypeName;

    @BindView(R.id.osm_copyright)
    TextView osmCopyrightTextView;

    @BindView(R.id.floating_action_menu)
    FloatingActionMenu floatingActionMenu;

    @BindView(R.id.edit_poi_detail_floating_button)
    FloatingActionButton floatingButtonEditPoi;

    @BindView(R.id.edit_poi_position_floating_button)
    FloatingActionButton floatingButtonEditPosition;

    @BindView(R.id.thumbnail)
    SimpleDraweeView thumbnail;

    /*=========================================*/
    /*--------------ATTRIBUTES-----------------*/
    /*=========================================*/
    private Poi poi;

    private GetFlickrPhotos asyncGetPhotos;

    private boolean hasPhotos;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_poi_detail, container, false);

        ((OsmTemplateApplication) getActivity().getApplication()).getOsmTemplateComponent().inject(this);
        ButterKnife.bind(this, rootView);
        osmCopyrightTextView.setText(Html.fromHtml(getString(R.string.osm_copyright)));

        if (!configManager.hasPoiModification()) {
            floatingButtonEditPoi.setImageResource(R.drawable.eye);
        }

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        eventBus.register(this);
    }

    @Override
    public void onPause() {
        eventBus.unregister(this);
        super.onPause();
    }

    /*=========================================*/
    /*---------------EVENTS--------------------*/
    /*=========================================*/
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onPhotosFoundEvent(PhotosFoundEvent photosFoundEvent) {
        List<List<Size>> photos = photosFoundEvent.getPhotos();
        hasPhotos = false;
        // If photos are not null and not empty, a photo is in the map.
        if (photos != null && !photos.isEmpty()) {
            // Both index are 0 because we are requested one image and we print the thumbail
            // contained at the index 0 of size list.
            thumbnail.setImageURI(Uri.parse(photos.get(0).get(Size.THUMB).getSource()));
            thumbnailsCache.put(poi.getId(), photos.get(0).get(Size.THUMB).getSource());
            hasPhotos = true;
        } else if (!hasPhotos || thumbnailsCache.get(poi.getId()) == null) {
            // Default image when there is no image. The user can click to add an image.
            thumbnail.setImageURI(Uri.parse("res:///" + R.drawable.ic_picture));
            hasPhotos = false;
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onPleaseChangeValuesDetailPoiFragmentEvent(PleaseChangeValuesDetailPoiFragmentEvent event) {
        setPoiType(event.getPoiType());
        setPoiName(event.getPoiName());
        showMovePoi(!event.isWay());
        this.poi = event.getPoi();

        if (thumbnailsCache.get(poi.getId()) == null) {
            // Param 1, 1 : One photo and one page
            asyncGetPhotos = new GetFlickrPhotos(poi.getLongitude(), poi.getLatitude(),
                    ((OsmTemplateApplication) getActivity().getApplication()).getFlickr(), 1, 1);
            asyncGetPhotos.execute();
        } else {
            thumbnail.setImageURI(Uri.parse(thumbnailsCache.get(poi.getId())));
            hasPhotos = true;
        }
    }

    /*=========================================*/
    /*---------------ONCLICK-------------------*/
    /*=========================================*/
    @OnClick(R.id.thumbnail)
    public void onThumbnailClick(View view) {
        Intent photoActivity = new Intent(getActivity(), PhotoActivity.class);
        photoActivity.putExtra("latitude", poi.getLatitude());
        photoActivity.putExtra("longitude", poi.getLongitude());
        photoActivity.putExtra("poiId", poi.getId());
        photoActivity.putExtra("hasPhotos", hasPhotos);
        startActivity(photoActivity);
    }

    @OnClick(R.id.edit_poi_detail_floating_button)
    public void editPoiOnClick() {
        eventBus.post(new PleaseOpenEditionEvent());
    }

    @OnClick(R.id.duplicate_poi_detail_floating_button)
    public void duplicatePoiOnClick() {
        eventBus.post(new PleaseDuplicatePoiEvent());
    }

    @OnClick(R.id.edit_poi_position_floating_button)
    public void editPoiPositionOnClick() {
        eventBus.post(new PleaseChangePoiPosition());
    }

    @OnClick(R.id.delete_poi_floating_button)
    public void deletePoiOnClick() {
        if (configManager.hasPoiModification()) {
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());

            alertDialog.setTitle(R.string.delete_poi_title);
            alertDialog.setMessage(R.string.delete_poi_confirm_message);
            alertDialog.setPositiveButton(R.string.delete_poi_positive_btn, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    eventBus.post(new PleaseDeletePoiFromMapEvent());
                }
            });

            alertDialog.setNegativeButton(R.string.delete_poi_negative_btn, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });

            alertDialog.show();
        } else {
            Toast.makeText(getActivity(), getResources().getString(R.string.point_modification_forbidden), Toast.LENGTH_SHORT).show();
        }
    }

    /*=========================================*/
    /*-------------PRIVATE CODE----------------*/
    /*=========================================*/
    private void setPoiName(String poiName) {
        floatingActionMenu.close(true);
        if (poiName != null && !poiName.isEmpty()) {
            editTextPoiName.setText(poiName);
            editTextPoiName.setTextColor(ContextCompat.getColor(getActivity(), R.color.active_text));
        } else {
            editTextPoiName.setText(getResources().getString(R.string.no_poi_name));
            editTextPoiName.setTextColor(ContextCompat.getColor(getActivity(), R.color.disable_text));
        }
    }

    private void setPoiType(String poiTypeName) {
        if (poiTypeName != null && !poiTypeName.isEmpty()) {
            editTextPoiTypeName.setText(poiTypeName);
            editTextPoiTypeName.setTextColor(ContextCompat.getColor(getActivity(), R.color.active_text));
        } else {
            editTextPoiTypeName.setText(getResources().getString(R.string.no_poi_name));
            editTextPoiTypeName.setTextColor(ContextCompat.getColor(getActivity(), R.color.disable_text));
        }
    }

    private void showMovePoi(boolean showing) {
        floatingButtonEditPosition.setVisibility(showing ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onStop() {
        if (asyncGetPhotos != null) {
            asyncGetPhotos.cancel(true);
        }
        super.onStop();
    }
}