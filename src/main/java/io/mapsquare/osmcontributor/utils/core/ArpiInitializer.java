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
package io.mapsquare.osmcontributor.utils.core;

import android.app.Application;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.DisplayMetrics;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

import javax.inject.Inject;

import org.greenrobot.eventbus.EventBus;
import io.mapsquare.osmcontributor.model.entities.PoiType;
import io.mapsquare.osmcontributor.ui.managers.PoiManager;
import io.mapsquare.osmcontributor.ui.utils.BitmapHandler;
import io.mapsquare.osmcontributor.ui.events.map.ArpiBitmapsPrecomputedEvent;
import io.mapsquare.osmcontributor.ui.events.map.PrecomputeArpiBitmapsEvent;
import mobi.designmyapp.arpigl.ArpiGlInstaller;
import timber.log.Timber;

public class ArpiInitializer {

    private Application application;
    private PoiManager poiManager;
    private BitmapHandler bitmapHandler;
    private EventBus eventBus;

    @Inject
    public ArpiInitializer(Application application, PoiManager poiManager, BitmapHandler bitmapHandler, EventBus eventBus) {
        this.application = application;
        this.poiManager = poiManager;
        this.bitmapHandler = bitmapHandler;
        this.eventBus = eventBus;
    }

    @Subscribe(threadMode = ThreadMode.ASYNC)
    public void onPrecomputeArpiBitmapsEvent(PrecomputeArpiBitmapsEvent event) {
        precomputeArpiBitmaps();
        eventBus.postSticky(new ArpiBitmapsPrecomputedEvent());
    }

    /**
     * Pre-compute the different PoiTypes bitmaps icons for the ArpiGL fragment and view.
     */
    public void precomputeArpiBitmaps() {
        try {
            if (!ArpiGlInstaller.getInstance(application.getApplicationContext()).isInstalled()) {
                ArpiGlInstaller.getInstance(application.getApplicationContext()).install();

                Map<Long, PoiType> poiTypes = poiManager.loadPoiTypes();

                for (Map.Entry<Long, PoiType> entry : poiTypes.entrySet()) {
                    Integer id = bitmapHandler.getIconDrawableId(entry.getValue());
                    if (id != null && id > 0) {
                        Drawable d = application.getApplicationContext().getResources().getDrawableForDensity(id, DisplayMetrics.DENSITY_XXHIGH);
                        int width = d.getIntrinsicWidth();
                        int height = d.getIntrinsicHeight();
                        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
                        Canvas c = new Canvas(bitmap);
                        d.setBounds(0, 0, width, height);
                        d.draw(c);

                        File dest = new File(application.getApplicationContext().getFilesDir(), ArpiGlInstaller.INSTALLATION_DIR + "/" + ArpiGlInstaller.TEXTURE_ICONS_SUBDIR + "/" + entry.getValue().getIcon() + ".png");
                        dest.getParentFile().mkdirs();

                        if (dest.exists()) {
                            dest.delete();
                        }
                        dest.createNewFile();
                        OutputStream stream = new FileOutputStream(dest);
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                        stream.close();
                        bitmap.recycle();
                    }
                }
            }
        } catch (IOException | JSONException e) {
            Timber.e("Error while initializing ArpiGl library: {}", e.getMessage());
        }
    }
}
