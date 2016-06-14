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

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.yarolegovich.lovelydialog.LovelyStandardDialog;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.mapsquare.osmcontributor.OsmTemplateApplication;
import io.mapsquare.osmcontributor.R;
import io.mapsquare.osmcontributor.database.events.DbInitializedEvent;
import io.mapsquare.osmcontributor.database.events.InitDbEvent;
import io.mapsquare.osmcontributor.model.events.InitCredentialsEvent;
import io.mapsquare.osmcontributor.ui.events.login.SplashScreenTimerFinishedEvent;
import io.mapsquare.osmcontributor.ui.events.map.ArpiBitmapsPrecomputedEvent;
import io.mapsquare.osmcontributor.ui.events.map.PrecomputeArpiBitmapsEvent;
import io.mapsquare.osmcontributor.ui.utils.views.EventCountDownTimer;
import timber.log.Timber;

public class SplashScreenActivity extends AppCompatActivity {

    /*=========================================*/
    /*------------ATTRIBUTES-------------------*/
    /*=========================================*/
    /**
     * For Android 6.0 and higher, we have to request dangerous permissions like
     * ACCESS_FINE_LOCATION or WRITE_EXTERNAL_STORAGE at runtime. This static variable
     * is used to get the user response in callback.
     */
    private static final int ALLOW_PERMISSIONS = 100;

    @Inject
    EventBus bus;

    @BindView(R.id.edited_by)
    TextView editBy;

    @BindView(R.id.powered_by)
    TextView poweredBy;

    @BindView(R.id.mapsquare)
    TextView mapsquare;

    /*=========================================*/
    /*------------CODE-------------------------*/
    /*=========================================*/
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        ((OsmTemplateApplication) getApplication()).getOsmTemplateComponent().inject(this);
        ButterKnife.bind(this);
        bus.register(this);

        mapsquare.setText(Html.fromHtml(getString(R.string.mapsquare)));
        editBy.setText(Html.fromHtml(getString(R.string.splash_screen_edited_by)));
        poweredBy.setText(Html.fromHtml(getString(R.string.splash_screen_powered_by)));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissionIfNeeded();
        } else {
            initEvent();
        }
    }



    /*=========================================*/
    /*------------PRIVATE CODE-----------------*/
    /*=========================================*/
    /**
     * Check whether all the initialization finished response events are there and we should start MapActivity.
     *
     * @return Whether we should start MapActivity.
     */
    private boolean shouldStartMapActivity() {
        return bus.getStickyEvent(DbInitializedEvent.class) != null &&
                bus.getStickyEvent(SplashScreenTimerFinishedEvent.class) != null &&
                bus.getStickyEvent(ArpiBitmapsPrecomputedEvent.class) != null;
    }

    /**
     * Remove all the initialization finished sticky events and start the MapActivity.
     */
    private void startMapActivity() {
        bus.removeStickyEvent(DbInitializedEvent.class);
        bus.removeStickyEvent(SplashScreenTimerFinishedEvent.class);
        bus.removeStickyEvent(ArpiBitmapsPrecomputedEvent.class);
        Intent intent = new Intent(this, MapActivity.class);
        startActivity(intent);
        finish();
    }

    private void initEvent() {
        EventCountDownTimer timer = new EventCountDownTimer(3000, 3000, bus);
        timer.setStickyEvent(new SplashScreenTimerFinishedEvent());
        timer.start();

        bus.post(new InitCredentialsEvent());
        bus.post(new InitDbEvent());
        bus.post(new PrecomputeArpiBitmapsEvent());
    }

    private void startMapActivityIfNeeded() {
        if (shouldStartMapActivity()) {
            startMapActivity();
        }
    }

    /*=========================================*/
    /*----------------EVENTS-------------------*/
    /*=========================================*/
    @Subscribe(sticky = true, threadMode = ThreadMode.ASYNC)
    public void onDbInitializedEvent(DbInitializedEvent event) {
        Timber.d("Database initialized");
        startMapActivityIfNeeded();
    }

    @Subscribe(threadMode = ThreadMode.ASYNC)
    public void onSplashScreenTimerFinishedEvent(SplashScreenTimerFinishedEvent event) {
        Timber.d("Timer finished");
        startMapActivityIfNeeded();
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.ASYNC)
    public void onArpiBitmapsPrecomputedEvent(ArpiBitmapsPrecomputedEvent event) {
        Timber.d("Arpi bitmaps precomputed");
        startMapActivityIfNeeded();
    }

    /*=========================================*/
    /*-----------FOR ANDROID 6.0---------------*/
    /*=========================================*/
    /**
     * This method must be called if the android version is 6.0 or higher.
     * Check if the app has ACCESS_LOCATION (COARSE and FINE) and WRITE_EXTERNAL_STORAGE.
     * If the app doesn't have both permission, a request is prompted to the user.
     * For more informations about permission in Android 6.0, please refer to
     * https://developer.android.com/training/permissions/requesting.html?hl=Fr#explain
     */
    private void requestPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Check if permission are enable by the user. Dangerous permissions requested are :
            int hasEnableCoarseLocationPerm = checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION);
            int hasEnableFineLocationPerm = checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION);
            int hasEnableExternalWritePerm = checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);

            // Add to list permission to be requested.
            List<String> permissionToRequest = new ArrayList<>();
            if (hasEnableCoarseLocationPerm == PackageManager.PERMISSION_DENIED) {
                permissionToRequest.add(Manifest.permission.ACCESS_COARSE_LOCATION);
            }
            if (hasEnableFineLocationPerm == PackageManager.PERMISSION_DENIED) {
                permissionToRequest.add(Manifest.permission.ACCESS_FINE_LOCATION);
            }
            if (hasEnableExternalWritePerm == PackageManager.PERMISSION_DENIED) {
                permissionToRequest.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            }

            // If the user have already allow permission, launch map activity, else, ask the user to allow permission
            if (!permissionToRequest.isEmpty()) {
                requestPermissions(permissionToRequest.toArray(new String[permissionToRequest.size()]), ALLOW_PERMISSIONS);
            } else {
                initEvent();
            }
        }
    }

    /**
     * This method is a callback. Check the user's answer after requesting permission.
     *
     * @param requestCode app request code (here, we only handle ALLOW_PERMISSION
     * @param permissions permissions requested (here, ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION, WRITE_EXTERNAL_STORAGE)
     * @param grantResults user's decision
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.i("osmcontributorlog", "onRequestPermissionsResult: ");
        // If request is cancelled, the result arrays are empty.
        if (requestCode == ALLOW_PERMISSIONS && grantResults.length > 0) {
            List<String> permissionsNotAllowed = new ArrayList<>();
            // For each permission, check if is granted
            for (int i = 0; i < permissions.length; i++) {
                if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                    permissionsNotAllowed.add(permissions[i]);
                }
            }

            if (permissionsNotAllowed.isEmpty()) {
                initEvent();
            } else {
                permissionNotEnabled();
            }
        } else {
            permissionNotEnabled();
        }
    }

    /**
     * This method is called is a permission (or all permissions) are declined by the user.
     * If permissions are refused, we indicate why we request permissions and that, whitout it,
     * the app can not work.
     *
     */
    private void permissionNotEnabled() {
        Log.i("osmcontributorlog", "permissionNotEnabled: ");
        new LovelyStandardDialog(this)
                .setTopColorRes(R.color.colorPrimaryDark)
                .setIcon(R.mipmap.icon)
                .setTitle(R.string.permissions_title)
                .setMessage(R.string.permissions_information)
                .setPositiveButton(android.R.string.ok, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        requestPermissionIfNeeded();
                    }
                }).show();
    }
}