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
package io.mapsquare.osmcontributor.edition;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.BindView;
import org.greenrobot.eventbus.EventBus;
import io.mapsquare.osmcontributor.OsmTemplateApplication;
import io.mapsquare.osmcontributor.R;
import io.mapsquare.osmcontributor.edition.events.PleaseApplyTagChangeView;

public class EditPoiActivity extends AppCompatActivity {

    public static final String POI_ID = "POI_ID";
    public static final String CREATION_MODE = "CREATION_MODE";
    public static final String POI_LAT = "POI_LAT";
    public static final String POI_LNG = "POI_LNG";
    public static final String POI_TYPE = "POI_TYPE";
    public static final String POI_LEVEL = "POI_LEVEL";
    public static final int EDIT_POI_ACTIVITY_CODE = 2;
    public static final int POI_CREATED = 3;
    public static final int POI_EDITED = 4;
    public static final String EDIT_POI_FRAGMENT_TAG = "EDIT_POI_FRAGMENT_TAG";

    @Inject
    EventBus eventBus;

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_poi);
        ((OsmTemplateApplication) getApplication()).getOsmTemplateComponent().inject(this);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        Intent intent = getIntent();
        Bundle args = new Bundle();

        if (savedInstanceState == null) {
            Long poiId = intent.getLongExtra(POI_ID, 1);
            boolean creationMode = intent.getBooleanExtra(CREATION_MODE, true);
            Double lat = intent.getDoubleExtra(POI_LAT, 0);
            Double lng = intent.getDoubleExtra(POI_LNG, 0);
            Long poiType = intent.getLongExtra(POI_TYPE, 0);
            Double level = intent.getDoubleExtra(POI_LEVEL, 0);

            args.putLong(POI_ID, poiId);
            args.putBoolean(CREATION_MODE, creationMode);
            args.putDouble(POI_LAT, lat);
            args.putDouble(POI_LNG, lng);
            args.putLong(POI_TYPE, poiType);
            args.putDouble(POI_LEVEL, level);

            EditPoiFragment editPoiFragment = new EditPoiFragment();
            editPoiFragment.setArguments(args);

            getFragmentManager().beginTransaction()
                    .add(R.id.container, editPoiFragment, EDIT_POI_FRAGMENT_TAG)
                    .commit();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        if (requestCode == PickValueActivity.PICK_VALUE_ACTIVITY_CODE && resultCode == Activity.RESULT_OK && intent != null) {
            String key = intent.getStringExtra(PickValueActivity.KEY);
            String value = intent.getStringExtra(PickValueActivity.VALUE);

            eventBus.postSticky(new PleaseApplyTagChangeView(key, value));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_edit_poi, menu);
        return true;
    }
}
