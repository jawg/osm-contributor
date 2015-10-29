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
package io.mapsquare.osmcontributor.map;


import android.content.Context;

import io.mapsquare.osmcontributor.R;

public enum MapMode {
    POI_CREATION(new MapModeProperties("POI creation mode")
            .title(R.string.poi_creation)
            .showDownloadArea()
            .unSelectIcon()
            .showConfirmBtn()
            .showCreationPin()
            .editColor()
            .lockDrawer()),

    NOTE_CREATION(new MapModeProperties("Note creation mode")
            .title(R.string.note_creation)
            .showDownloadArea()
            .unSelectIcon()
            .showConfirmBtn()
            .showCreationPin()
            .editColor()
            .lockDrawer()),

    TYPE_PICKER(new MapModeProperties("Type picker mode")
            .title(R.string.poi_creation)
            .unSelectIcon()
            .editColor()
            .lockDrawer()),

    DETAIL_POI(new MapModeProperties("Detail Poi mode")
            .showPoiBanner()
            .showDownloadArea()
            .lockDrawer()),

    DETAIL_NOTE(new MapModeProperties("Detail Note mode")
            .showNodeBanner()
            .lockDrawer()),

    POI_POSITION_EDITION(new MapModeProperties("Poi Position edition mode")
            .title(R.string.edit_poi_position)
            .showDownloadArea()
            .showCreationPin()
            .showConfirmBtn()
            .lockDrawer()
            .editColor()),

    NODE_REF_POSITION_EDITION(new MapModeProperties("Node ref position edition mode")
            .title(R.string.edit_noderef_position_title)
            .showDownloadArea()
            .showConfirmBtn()
            .editColor()
            .showEditWays()
            .zoomOutLimited()
            .lockDrawer()),

    WAY_EDITION(new MapModeProperties("Way edition mode")
            .title(R.string.edit_way_title)
            .showDownloadArea()
            .showEditWays()
            .zoomOutLimited()
            .lockDrawer()),

    DEFAULT(new MapModeProperties("Default mode")
            .showAddPoiFab()
            .showDownloadArea()
            .unSelectIcon()
            .menuBtn());

    private final MapModeProperties properties;

    MapMode(MapModeProperties properties) {
        this.properties = properties;
    }

    public MapModeProperties getProperties() {
        return properties;
    }

    public static class MapModeProperties {
        private boolean unSelectIcon = false;
        private boolean showAddPoiFab = false;
        private boolean showPoiBanner = false;
        private boolean showNodeBanner = false;
        private boolean showCreationPin = false;
        private boolean showEditWays = false;
        private boolean zoomOutLimited = false;
        private String analyticsAction = "";

        //Toolbar
        private boolean showConfirmBtn = false;
        private boolean showDownloadArea = false;
        private boolean lockDrawer = false;
        private boolean editColor = false;
        private boolean menuBtn = false;
        private int title = R.string.name;


        public MapModeProperties unSelectIcon() {
            this.unSelectIcon = true;
            return this;
        }

        public MapModeProperties showAddPoiFab() {
            this.showAddPoiFab = true;
            return this;
        }

        public MapModeProperties showPoiBanner() {
            this.showPoiBanner = true;
            return this;
        }

        public MapModeProperties showNodeBanner() {
            this.showNodeBanner = true;
            return this;
        }

        public MapModeProperties showCreationPin() {
            this.showCreationPin = true;
            return this;
        }

        public MapModeProperties zoomOutLimited() {
            this.zoomOutLimited = true;
            return this;
        }

        public MapModeProperties(String analyticsAction) {
            this.analyticsAction = analyticsAction;
        }

        public MapModeProperties title(int title) {
            this.title = title;
            return this;
        }

        public MapModeProperties showConfirmBtn() {
            this.showConfirmBtn = true;
            return this;
        }

        public MapModeProperties showDownloadArea() {
            this.showDownloadArea = true;
            return this;
        }

        public MapModeProperties lockDrawer() {
            this.lockDrawer = true;
            return this;
        }

        public MapModeProperties editColor() {
            this.editColor = true;
            return this;
        }

        public MapModeProperties menuBtn() {
            this.menuBtn = true;
            return this;
        }

        public MapModeProperties showEditWays() {
            this.showEditWays = true;
            return this;
        }

        public boolean isUnSelectIcon() {
            return unSelectIcon;
        }

        public boolean isShowAddPoiFab() {
            return showAddPoiFab;
        }

        public boolean isShowPoiBanner() {
            return showPoiBanner;
        }

        public boolean isShowNodeBanner() {
            return showNodeBanner;
        }

        public boolean isShowCreationPin() {
            return showCreationPin;
        }

        public boolean isShowEditWays() {
            return showEditWays;
        }

        public boolean isZoomOutLimited() {
            return zoomOutLimited;
        }

        public String getAnalyticsAction() {
            return analyticsAction;
        }

        public boolean isShowConfirmBtn() {
            return showConfirmBtn;
        }

        public boolean isShowDownloadArea() {
            return showDownloadArea;
        }

        public boolean isLockDrawer() {
            return lockDrawer;
        }

        public boolean isEditColor() {
            return editColor;
        }

        public boolean isMenuBtn() {
            return menuBtn;
        }

        public String getTitle(Context context) {
            return context.getResources().getString(title);
        }
    }
}



