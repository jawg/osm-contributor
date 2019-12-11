/**
 * Copyright (C) 2019 Takima
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
import android.view.View;

import co.mobiwise.materialintro.animation.MaterialIntroListener;
import io.jawg.osmcontributor.R;

/**
 * Handle tutorial for UploadActivity and fragments/activities associated.
 * All the tutorials of this class concern the synchronization of POI.
 */
public class SyncTutoManager extends TutorialManager {

    /*=========================================*/
    /*---------------CONSTRUCTOR---------------*/
    /*=========================================*/
    /**
     * Construct manager.
     * @param activity activity linked to the tutorial
     * @param forceDisplay indicates if the tutorial must me forced
     */
    public SyncTutoManager(Activity activity, boolean forceDisplay) {
        super(activity);
        this.forceDisplay = forceDisplay;
    }

    /*=========================================*/
    /*------------TUTORIAL METHODS-------------*/
    /*=========================================*/
    /**
     * Start tuto for sync. Present cancel action.
     * @param targetView1 cancel action
     * @param targetView2 comment action
     * @param targetView3 confirm action
     */
    public void launchTuto(View targetView1, final View targetView2, final View targetView3) {
        showView("syncTuto_part1", targetView1, activity.getString(R.string.tuto_text_cancel_sync), new MaterialIntroListener() {
            @Override
            public void onUserClicked(String s) {
                continueToStep2(targetView2, targetView3);
            }
        });
    }

    /**
     * Present comment action.
     * @param targetView2 comment action
     * @param targetView3 confirm action
     */
    private void continueToStep2(View targetView2, final View targetView3) {
        showView("syncTuto_part2", targetView2, activity.getString(R.string.tuto_text_comment), new MaterialIntroListener() {
            @Override
            public void onUserClicked(String s) {
                continueToStep3(targetView3);
            }
        });
    }

    /**
     * Present confirm action.
     * @param targetView3 confirm action
     */
    private void continueToStep3(View targetView3) {
        showView("syncTuto_part3", targetView3, activity.getString(R.string.tuto_text_confirm_sync), null);
    }
}
