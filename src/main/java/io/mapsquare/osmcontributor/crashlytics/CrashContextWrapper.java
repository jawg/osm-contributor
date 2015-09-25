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
package io.mapsquare.osmcontributor.crashlytics;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.pm.PackageManager;

public class CrashContextWrapper extends ContextWrapper {
    private String mOverridePackageName;

    /**
     * @param base        Context
     * @param packageName package Name to override with
     */
    public CrashContextWrapper(Context base, String packageName) {
        super(base);
        mOverridePackageName = packageName;
    }

    /**
     * @return the override package name
     */
    @Override
    public String getPackageName() {
        return mOverridePackageName;
    }

    /**
     * @return Application Context Wrapped in CrashContextWrapper
     */
    @Override
    public Context getApplicationContext() {
        return new CrashContextWrapper(super.getApplicationContext(), mOverridePackageName);
    }

    /**
     * @return PackageManager Wrapped in CrashPackageManager
     */
    @Override
    public PackageManager getPackageManager() {
        return new CrashPackageManager(super.getPackageManager(), super.getPackageName(), mOverridePackageName);
    }

}
