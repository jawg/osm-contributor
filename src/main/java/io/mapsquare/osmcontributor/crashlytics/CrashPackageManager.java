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
package io.mapsquare.osmcontributor.crashlytics;

import android.annotation.TargetApi;
import android.content.ComponentName;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.FeatureInfo;
import android.content.pm.InstrumentationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageInstaller;
import android.content.pm.PackageManager;
import android.content.pm.PermissionGroupInfo;
import android.content.pm.PermissionInfo;
import android.content.pm.ProviderInfo;
import android.content.pm.ResolveInfo;
import android.content.pm.ServiceInfo;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.UserHandle;
import android.support.annotation.NonNull;

import java.util.List;

public class CrashPackageManager extends PackageManager {

    private final PackageManager mPackageManager;
    private final String mPackageName;
    private final String mOverridePackageName;

    /**
     * @param pm                  Real Package Manager
     * @param packageName         Real Package Name
     * @param overridePackageName Faking Package Name
     */
    CrashPackageManager(PackageManager pm, String packageName, String overridePackageName) {
        mPackageManager = pm;
        mPackageName = packageName;
        mOverridePackageName = overridePackageName;
    }

    /**
     * Only translate the overridden package name, the third party may need external lookups at some point
     *
     * @param packageName
     * @return
     */
    private String translatePackageName(String packageName) {
        if (mOverridePackageName.equals(packageName)) {
            return mPackageName;
        } else {
            return packageName;
        }
    }

    /**
     * Delegated Methods for Package Manager
     **/
    @Override
    public PackageInfo getPackageInfo(String packageName, int flags) throws PackageManager.NameNotFoundException {
        return mPackageManager.getPackageInfo(translatePackageName(packageName), flags);
    }

    @Override
    public String[] currentToCanonicalPackageNames(String[] names) {
        return mPackageManager.currentToCanonicalPackageNames(names);
    }

    @Override
    public String[] canonicalToCurrentPackageNames(String[] names) {
        return mPackageManager.canonicalToCurrentPackageNames(names);
    }

    @Override
    public Intent getLaunchIntentForPackage(String packageName) {
        return mPackageManager.getLaunchIntentForPackage(translatePackageName(packageName));
    }

    @Override
    @TargetApi(21)
    public Intent getLeanbackLaunchIntentForPackage(String packageName) {
        return mPackageManager.getLeanbackLaunchIntentForPackage(translatePackageName(packageName));
    }

    @Override
    public int[] getPackageGids(String packageName) throws PackageManager.NameNotFoundException {
        return mPackageManager.getPackageGids(translatePackageName(packageName));
    }

    @Override
    public PermissionInfo getPermissionInfo(String name, int flags) throws PackageManager.NameNotFoundException {
        return mPackageManager.getPermissionInfo(name, flags);
    }

    @Override
    public List<PermissionInfo> queryPermissionsByGroup(String group, int flags) throws PackageManager.NameNotFoundException {
        return mPackageManager.queryPermissionsByGroup(group, flags);
    }

    @Override
    public PermissionGroupInfo getPermissionGroupInfo(String name, int flags) throws PackageManager.NameNotFoundException {
        return mPackageManager.getPermissionGroupInfo(name, flags);
    }

    @Override
    public List<PermissionGroupInfo> getAllPermissionGroups(int flags) {
        return mPackageManager.getAllPermissionGroups(flags);
    }

    @Override
    public ApplicationInfo getApplicationInfo(String packageName, int flags) throws PackageManager.NameNotFoundException {
        return mPackageManager.getApplicationInfo(translatePackageName(packageName), flags);
    }

    @Override
    public ActivityInfo getActivityInfo(ComponentName component, int flags) throws PackageManager.NameNotFoundException {
        return mPackageManager.getActivityInfo(component, flags);
    }

    @Override
    public ActivityInfo getReceiverInfo(ComponentName component, int flags) throws PackageManager.NameNotFoundException {
        return mPackageManager.getReceiverInfo(component, flags);
    }

    @Override
    public ServiceInfo getServiceInfo(ComponentName component, int flags) throws PackageManager.NameNotFoundException {
        return mPackageManager.getServiceInfo(component, flags);
    }

    @Override
    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    public ProviderInfo getProviderInfo(ComponentName component, int flags) throws PackageManager.NameNotFoundException {
        return mPackageManager.getProviderInfo(component, flags);
    }

    @Override
    public List<PackageInfo> getInstalledPackages(int flags) {
        return mPackageManager.getInstalledPackages(flags);
    }

    @Override
    @TargetApi(18)
    public List<PackageInfo> getPackagesHoldingPermissions(String[] permissions, int flags) {
        return mPackageManager.getPackagesHoldingPermissions(permissions, flags);
    }

    @Override
    public int checkPermission(String permName, String pkgName) {
        return mPackageManager.checkPermission(permName, pkgName);
    }

    @Override
    public boolean isPermissionRevokedByPolicy(String permName, String pkgName) {
        return mPackageManager.isPermissionRevokedByPolicy(permName, pkgName);
    }

    @Override
    public boolean addPermission(PermissionInfo info) {
        return mPackageManager.addPermission(info);
    }

    @Override
    public boolean addPermissionAsync(PermissionInfo info) {
        return mPackageManager.addPermissionAsync(info);
    }

    @Override
    public void removePermission(String name) {
        mPackageManager.removePermission(name);
    }

    @Override
    public int checkSignatures(String pkg1, String pkg2) {
        return mPackageManager.checkSignatures(pkg1, pkg2);
    }

    @Override
    public int checkSignatures(int uid1, int uid2) {
        return mPackageManager.checkSignatures(uid1, uid2);
    }

    @Override
    public String[] getPackagesForUid(int uid) {
        return mPackageManager.getPackagesForUid(uid);
    }

    @Override
    public String getNameForUid(int uid) {
        return mPackageManager.getNameForUid(uid);
    }

    @Override
    public List<ApplicationInfo> getInstalledApplications(int flags) {
        return mPackageManager.getInstalledApplications(flags);
    }

    @Override
    public String[] getSystemSharedLibraryNames() {
        return mPackageManager.getSystemSharedLibraryNames();
    }

    @Override
    public FeatureInfo[] getSystemAvailableFeatures() {
        return mPackageManager.getSystemAvailableFeatures();
    }

    @Override
    public boolean hasSystemFeature(String name) {
        return mPackageManager.hasSystemFeature(name);
    }

    @Override
    public ResolveInfo resolveActivity(Intent intent, int flags) {
        return mPackageManager.resolveActivity(intent, flags);
    }

    @Override
    public List<ResolveInfo> queryIntentActivities(Intent intent, int flags) {
        return mPackageManager.queryIntentActivities(intent, flags);
    }

    @Override
    public List<ResolveInfo> queryIntentActivityOptions(ComponentName caller, Intent[] specifics, Intent intent, int flags) {
        return mPackageManager.queryIntentActivityOptions(caller, specifics, intent, flags);
    }

    @Override
    public List<ResolveInfo> queryBroadcastReceivers(Intent intent, int flags) {
        return mPackageManager.queryBroadcastReceivers(intent, flags);
    }

    @Override
    public ResolveInfo resolveService(Intent intent, int flags) {
        return mPackageManager.resolveService(intent, flags);
    }

    @Override
    public List<ResolveInfo> queryIntentServices(Intent intent, int flags) {
        return mPackageManager.queryIntentServices(intent, flags);
    }

    @Override
    @TargetApi(19)
    public List<ResolveInfo> queryIntentContentProviders(Intent intent, int flags) {
        return mPackageManager.queryIntentContentProviders(intent, flags);
    }

    @Override
    public ProviderInfo resolveContentProvider(String name, int flags) {
        return mPackageManager.resolveContentProvider(name, flags);
    }

    @Override
    public List<ProviderInfo> queryContentProviders(String processName, int uid, int flags) {
        return mPackageManager.queryContentProviders(processName, uid, flags);
    }

    @Override
    public InstrumentationInfo getInstrumentationInfo(ComponentName className, int flags) throws PackageManager.NameNotFoundException {
        return mPackageManager.getInstrumentationInfo(className, flags);
    }

    @Override
    public List<InstrumentationInfo> queryInstrumentation(String targetPackage, int flags) {
        return mPackageManager.queryInstrumentation(targetPackage, flags);
    }

    @Override
    public Drawable getDrawable(String packageName, int resid, ApplicationInfo appInfo) {
        return mPackageManager.getDrawable(translatePackageName(packageName), resid, appInfo);
    }

    @Override
    public Drawable getActivityIcon(ComponentName activityName) throws PackageManager.NameNotFoundException {
        return mPackageManager.getActivityIcon(activityName);
    }

    @Override
    public Drawable getActivityIcon(Intent intent) throws PackageManager.NameNotFoundException {
        return mPackageManager.getActivityIcon(intent);
    }

    @Override
    @TargetApi(20)
    public Drawable getActivityBanner(ComponentName activityName) throws NameNotFoundException {
        return mPackageManager.getActivityBanner(activityName);
    }

    @Override
    @TargetApi(20)
    public Drawable getActivityBanner(Intent intent) throws NameNotFoundException {
        return mPackageManager.getActivityBanner(intent);
    }

    @Override
    public Drawable getDefaultActivityIcon() {
        return mPackageManager.getDefaultActivityIcon();
    }

    @Override
    public Drawable getApplicationIcon(ApplicationInfo info) {
        return mPackageManager.getApplicationIcon(info);
    }

    @Override
    public Drawable getApplicationIcon(String packageName) throws PackageManager.NameNotFoundException {
        return mPackageManager.getApplicationIcon(translatePackageName(packageName));
    }

    @Override
    @TargetApi(20)
    public Drawable getApplicationBanner(ApplicationInfo info) {
        return mPackageManager.getApplicationBanner(info);
    }

    @Override
    @TargetApi(20)
    public Drawable getApplicationBanner(String packageName) throws NameNotFoundException {
        return mPackageManager.getApplicationBanner(translatePackageName(packageName));
    }

    @Override
    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    public Drawable getActivityLogo(ComponentName activityName) throws PackageManager.NameNotFoundException {
        return mPackageManager.getActivityLogo(activityName);
    }

    @Override
    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    public Drawable getActivityLogo(Intent intent) throws PackageManager.NameNotFoundException {
        return mPackageManager.getActivityLogo(intent);
    }

    @Override
    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    public Drawable getApplicationLogo(ApplicationInfo info) {
        return mPackageManager.getApplicationLogo(info);
    }

    @Override
    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    public Drawable getApplicationLogo(String packageName) throws PackageManager.NameNotFoundException {
        return mPackageManager.getApplicationLogo(translatePackageName(packageName));
    }

    @Override
    @TargetApi(21)
    public Drawable getUserBadgedIcon(Drawable icon, UserHandle user) {
        return mPackageManager.getUserBadgedIcon(icon, user);
    }

    @Override
    @TargetApi(21)
    public Drawable getUserBadgedDrawableForDensity(Drawable drawable, UserHandle user, Rect badgeLocation, int badgeDensity) {
        return mPackageManager.getUserBadgedDrawableForDensity(drawable, user, badgeLocation, badgeDensity);
    }

    @Override
    @TargetApi(21)
    public CharSequence getUserBadgedLabel(CharSequence label, UserHandle user) {
        return mPackageManager.getUserBadgedLabel(label, user);
    }

    @Override
    public CharSequence getText(String packageName, int resid, ApplicationInfo appInfo) {
        return mPackageManager.getText(translatePackageName(packageName), resid, appInfo);
    }

    @Override
    public XmlResourceParser getXml(String packageName, int resid, ApplicationInfo appInfo) {
        return mPackageManager.getXml(translatePackageName(packageName), resid, appInfo);
    }

    @Override
    public CharSequence getApplicationLabel(ApplicationInfo info) {
        return mPackageManager.getApplicationLabel(info);
    }

    @Override
    public Resources getResourcesForActivity(ComponentName activityName) throws PackageManager.NameNotFoundException {
        return mPackageManager.getResourcesForActivity(activityName);
    }

    @Override
    public Resources getResourcesForApplication(ApplicationInfo app) throws PackageManager.NameNotFoundException {
        return mPackageManager.getResourcesForApplication(app);
    }

    @Override
    public Resources getResourcesForApplication(String packageName) throws PackageManager.NameNotFoundException {
        return mPackageManager.getResourcesForApplication(translatePackageName(packageName));
    }

    @Override
    public PackageInfo getPackageArchiveInfo(String archiveFilePath, int flags) {
        return mPackageManager.getPackageArchiveInfo(archiveFilePath, flags);
    }

    @Override
    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    public void verifyPendingInstall(int id, int verificationCode) {
        mPackageManager.verifyPendingInstall(id, verificationCode);
    }

    @Override
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    public void extendVerificationTimeout(int id, int verificationCodeAtTimeout, long millisecondsToDelay) {
        mPackageManager.extendVerificationTimeout(id, verificationCodeAtTimeout, millisecondsToDelay);
    }

    @Override
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void setInstallerPackageName(String targetPackage, String installerPackageName) {
        mPackageManager.setInstallerPackageName(targetPackage, installerPackageName);
    }

    @Override
    public String getInstallerPackageName(String packageName) {
        return mPackageManager.getInstallerPackageName(translatePackageName(packageName));
    }

    @Override
    public void addPackageToPreferred(String packageName) {
        mPackageManager.addPackageToPreferred(translatePackageName(packageName));
    }

    @Override
    public void removePackageFromPreferred(String packageName) {
        mPackageManager.removePackageFromPreferred(translatePackageName(packageName));
    }

    @Override
    public List<PackageInfo> getPreferredPackages(int flags) {
        return mPackageManager.getPreferredPackages(flags);
    }

    @Override
    public void addPreferredActivity(IntentFilter filter, int match, ComponentName[] set, ComponentName activity) {
        mPackageManager.addPreferredActivity(filter, match, set, activity);
    }

    @Override
    public void clearPackagePreferredActivities(String packageName) {
        mPackageManager.clearPackagePreferredActivities(translatePackageName(packageName));
    }

    @Override
    public int getPreferredActivities(List<IntentFilter> outFilters, List<ComponentName> outActivities, String packageName) {
        return mPackageManager.getPreferredActivities(outFilters, outActivities, translatePackageName(packageName));
    }

    @Override
    public void setComponentEnabledSetting(ComponentName componentName, int newState, int flags) {
        mPackageManager.setComponentEnabledSetting(componentName, newState, flags);
    }

    @Override
    public int getComponentEnabledSetting(ComponentName componentName) {
        return mPackageManager.getComponentEnabledSetting(componentName);
    }

    @Override
    public void setApplicationEnabledSetting(String packageName, int newState, int flags) {
        mPackageManager.setApplicationEnabledSetting(translatePackageName(packageName), newState, flags);
    }

    @Override
    public int getApplicationEnabledSetting(String packageName) {
        return mPackageManager.getApplicationEnabledSetting(translatePackageName(packageName));
    }

    @Override
    public boolean isSafeMode() {
        return mPackageManager.isSafeMode();
    }

    @NonNull
    @Override
    @TargetApi(21)
    public PackageInstaller getPackageInstaller() {
        return mPackageManager.getPackageInstaller();
    }
}
