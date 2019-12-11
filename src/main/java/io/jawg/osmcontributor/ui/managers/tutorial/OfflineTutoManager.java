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

import co.mobiwise.materialintro.animation.MaterialIntroListener;
import co.mobiwise.materialintro.shape.Focus;
import io.jawg.osmcontributor.R;

/**
 * Handle tutorial for Offline Activities.
 */
public class OfflineTutoManager extends TutorialManager {

    public OfflineTutoManager(Activity activity, boolean forceDisplay) {
        super(activity);
        this.forceDisplay = forceDisplay;
    }

    public void startTuto() {
        defaultConfiguration.setFocusType(Focus.MINIMUM);
        showView("offline_step1", activity.findViewById(R.id.add_offline_region_floating_button), activity.getString(R.string.offline_step1), new MaterialIntroListener() {
            @Override
            public void onUserClicked(String s) {
                activity.findViewById(R.id.add_offline_region_floating_button).performClick();
                continueToNextStep(1);
            }
        });
    }

    private void continueToNextStep(int step) {
        switch (step) {
            case 1:
                showView("offline_step2", activity.findViewById(R.id.mapview), activity.getString(R.string.offline_step2), new MaterialIntroListener() {
                    @Override
                    public void onUserClicked(String s) {

                        continueToNextStep(2);
                    }
                });
                break;
            case 2:
                defaultConfiguration.setFocusType(Focus.MINIMUM);
                showView("offline_step3", activity.findViewById(R.id.download_new_region_floating_button), activity.getString(R.string.offline_step3), new MaterialIntroListener() {
                    @Override
                    public void onUserClicked(String s) {
                        continueToNextStep(3);
                    }
                });
                break;
            case 3:
                defaultConfiguration.setFocusType(Focus.ALL);
                showView("offline_step4", activity.findViewById(R.id.offline_regions_list), activity.getString(R.string.offline_step4), null);
                forceDisplayOfflineTuto = false;
                break;
        }
    }
}
