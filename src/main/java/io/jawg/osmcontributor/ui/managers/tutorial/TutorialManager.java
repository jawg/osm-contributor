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
import android.view.View;

import co.mobiwise.materialintro.MaterialIntroConfiguration;
import co.mobiwise.materialintro.animation.MaterialIntroListener;
import co.mobiwise.materialintro.prefs.PreferencesManager;
import co.mobiwise.materialintro.view.MaterialIntroView;

/**
 * This class handle tutorials view.
 */
public class TutorialManager {

    /*=========================================*/
    /*-----------STATIC VARIABLES--------------*/
    /*=========================================*/
    public static boolean forceDisplayAddTuto;

    public static boolean forceDisplaySyncTuto;

    public static boolean forceDisplayOfflineTuto;

    /*=========================================*/
    /*--------------ATTRIBUTES-----------------*/
    /*=========================================*/
    /**
     * Default configuration for tutorial view.
     */
    protected MaterialIntroConfiguration defaultConfiguration;

    /**
     * Context.
     */
    protected Activity activity;

    /**
     * Flag used to reset id if the tutorial must be shown again.
     */
    protected boolean forceDisplay;


    /*=========================================*/
    /*--------------CONSTRUCTOR----------------*/
    /*=========================================*/
    public TutorialManager(Activity activity) {
        this.activity = activity;

        // Init default configuration for tutorials
        defaultConfiguration = new MaterialIntroConfiguration();
        defaultConfiguration.setFadeAnimationEnabled(true);
        defaultConfiguration.setDotViewEnabled(false);
        defaultConfiguration.setDelayMillis(10);
    }

    /*=========================================*/
    /*------------------CODE-------------------*/
    /*=========================================*/
    /**
     * Show helping view.
     * @param uniqueId uniqueId used to display the view only once
     * @param view target view
     * @param infoTip helping text
     * @param listener launch an action on click
     */
    public void showView(String uniqueId, View view, String infoTip, MaterialIntroListener listener) {
        if (forceDisplay) {
            resetUniqueId(uniqueId);
        }

        new MaterialIntroView.Builder(activity)
                .setConfiguration(defaultConfiguration)
                .setTarget(view)
                .setInfoText(infoTip)
                .setUsageId(uniqueId)
                .setListener(listener)
                .show();
    }

    /*=========================================*/
    /*--------------GETTER/SETTER--------------*/
    /*=========================================*/
    public void setForceDisplay(boolean forceDisplay) {
        this.forceDisplay = forceDisplay;
    }

    /*=========================================*/
    /*--------------PRIVATE CODE---------------*/
    /*=========================================*/
    /**
     * If we want to replay tutorials, reset preferences.
     */
    private void resetUniqueId(String uniqueId) {
        new PreferencesManager(activity).reset(uniqueId);
    }
}