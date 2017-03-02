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
package io.jawg.osmcontributor.ui.managers.tutorial;


import android.app.Activity;
import android.os.Handler;
import android.support.design.widget.NavigationView;
import android.view.View;
import android.widget.ListView;

import com.github.clans.fab.FloatingActionMenu;

import co.mobiwise.materialintro.animation.MaterialIntroListener;
import co.mobiwise.materialintro.shape.Focus;
import io.jawg.osmcontributor.R;
import io.jawg.osmcontributor.ui.activities.MapActivity;
import io.jawg.osmcontributor.ui.events.map.PleaseToggleDrawer;

/**
 * Handle tutorial for MapActivity and fragments/activities associated.
 * All the tutorials of this class concern the addition/modification of POI.
 */
public class AddPoiTutoManager extends TutorialManager {

    /*=========================================*/
    /*---------------CONSTRUCTOR---------------*/
    /*=========================================*/
    /**
     * Construct manager.
     * @param activity activity linked to the tutorial
     * @param forceDisplay indicates if the tutorial must me forced
     */
    public AddPoiTutoManager(Activity activity, boolean forceDisplay) {
        super(activity);
        this.forceDisplay = forceDisplay;
    }

    /*=========================================*/
    /*------------TUTORIAL METHODS-------------*/
    /*=========================================*/
    /**
     * Step one : Highlight add poi button with explanations.
     * @param targetView POI add button.
     */
    public void addPoiBtnTuto(final FloatingActionMenu targetView) {
        showView("addPoi_step1", targetView.getChildAt(1), activity.getString(R.string.tuto_text_press_create), new MaterialIntroListener() {
            @Override
            public void onUserClicked(String s) {
                // Perform click when user confirms
                targetView.getChildAt(1).performClick();
            }
        });
    }

    /**
     * Step two : lauched after user clicks POI add button.
     * @param targetView listview with the POI type list
     */
    public void choosePoiTypeTuto(final ListView targetView) {
        // Change the size of focus
        defaultConfiguration.setFocusType(Focus.NORMAL);
        showView("addPoi_step2", targetView.getChildAt(4), activity.getString(R.string.tuto_text_choose_type), new MaterialIntroListener() {
            @Override
            public void onUserClicked(String s) {
                // Perform click when user confirms
                targetView.getChildAt(4).performClick();
                // Continue the tutorial
                confirmPositionTuto(activity.findViewById(R.id.action_confirm_position));
            }
        });
    }

    /**
     * Step three : User is invited to confirm the position of the POI.
     * @param targetView menu confirm
     */
    private void confirmPositionTuto(final View targetView) {
        // Change the size of the focus
        defaultConfiguration.setFocusType(Focus.MINIMUM);
        showView("addPoi_step3", targetView, activity.getString(R.string.tuto_text_confirm_position_creation), null);
    }

    /**
     * Step four : user is invited to fill the informations of the POI.
     * @param targetView menu confirm
     */
    public void addInfoTuto(final View targetView) {
        showView("addPoi_step4", targetView, activity.getString(R.string.tuto_text_confirm_creation), null);
    }

    /**
     * Step five : Indicates to the user that POI in gray are not synchronised.
     * @param targetView mapview with markers
     */
    public void synchronizedModificationsTuto(final View targetView) {
        defaultConfiguration.setFocusType(Focus.MINIMUM);
        showView("addPoi_step5", targetView, activity.getString(R.string.tuto_text_synchronized), new MaterialIntroListener() {
            @Override
            public void onUserClicked(String s) {
                // Open mleft menu
                ((MapActivity) activity).onPleaseToggleDrawer(new PleaseToggleDrawer());
                // Continue tutorial
                showSynchronizedButton();
            }
        });
    }

    /**
     * Step six : Highlit save menu to indicate to the user how the modifications can be
     * synchronized.
     */
    private void showSynchronizedButton() {
        // Wait for the menu to open
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // Set focus to ALL
                defaultConfiguration.setFocusType(Focus.ALL);
                showView("addPoi_step6", ((NavigationView) activity.getWindow().getDecorView().findViewById(R.id.navigation)).getHeaderView(0),
                        activity.getString(R.string.tuto_text_clic_to_synchronized), null);
            }
        }, 500);
    }
}