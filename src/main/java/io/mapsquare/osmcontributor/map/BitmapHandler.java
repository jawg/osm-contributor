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

import android.app.Application;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.v4.util.LruCache;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.mapsquare.osmcontributor.R;
import io.mapsquare.osmcontributor.core.model.Note;
import io.mapsquare.osmcontributor.core.model.Poi;
import io.mapsquare.osmcontributor.core.model.PoiType;

/**
 * Handle the bitmaps and cache them into the memory for future uses.
 */
@Singleton
public class BitmapHandler {


    public static final String BITMAP_NOTE_ID = "BITMAP_NOTE_ID";
    private LruCache<String, Bitmap> cache;
    private final Map<String, Integer> icons = new HashMap<>();
    private Map<Long, PoiType> poiTypes;

    private final Context context;

    @Inject
    public BitmapHandler(Application osmTemplateApplication) {
        context = osmTemplateApplication.getApplicationContext();

        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
        // Use 1/8th of the available memory for this memory cache.
        final int cacheSize = maxMemory / 8;

        cache = new LruCache<String, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(String key, Bitmap bitmap) {
                // The cache size will be measured in kilobytes rather than items count
                return bitmap.getByteCount() / 1024;
            }
        };

        icons.put("administrative", R.drawable.administrative);
        icons.put("aerodrome", R.drawable.aerodrome);
        icons.put("aircraft", R.drawable.aerodrome);
        icons.put("alcohol", R.drawable.bar);
        icons.put("atm", R.drawable.atm);
        icons.put("attraction", R.drawable.attraction);
        icons.put("bank", R.drawable.bank);
        icons.put("bar", R.drawable.bar);
        icons.put("bus_stop", R.drawable.bus_stop);
        icons.put("bicycle_parking", R.drawable.bicycle_parking);
        icons.put("bicycle_rental", R.drawable.bicycle_rental);
        icons.put("biergarten", R.drawable.biergarten);
        icons.put("cafe", R.drawable.cafe);
        icons.put("car_rental", R.drawable.car_rental);
        icons.put("church", R.drawable.place_of_worship);
        icons.put("cinema", R.drawable.cinema);
        icons.put("city", R.drawable.town);
        icons.put("commercial", R.drawable.mall);
        icons.put("courthouse", R.drawable.courthouse);
        icons.put("dentist", R.drawable.dentist);
        icons.put("doctors", R.drawable.doctors);
        icons.put("drinking_water", R.drawable.drinking_water);
        icons.put("embassy", R.drawable.embassy);
        icons.put("fast_food", R.drawable.fast_food);
        icons.put("fire_station", R.drawable.fire_station);
        icons.put("fuel", R.drawable.fuel);
        icons.put("hamlet", R.drawable.town);
        icons.put("hospital", R.drawable.hospital);
        icons.put("hotel", R.drawable.hotel);
        icons.put("house", R.drawable.house);
        icons.put("housenumber", R.drawable.house);
        icons.put("hunting_stand", R.drawable.hunting_stand);
        icons.put("locality", R.drawable.town);
        icons.put("mall", R.drawable.mall);
        icons.put("nightclub", R.drawable.nightclub);
        icons.put("neighbourhood", R.drawable.house);
        icons.put("parking", R.drawable.parking);
        icons.put("pharmacy", R.drawable.pharmacy);
        icons.put("place_of_worship", R.drawable.place_of_worship);
        icons.put("police", R.drawable.police);
        icons.put("political", R.drawable.administrative);
        icons.put("post_office", R.drawable.administrative);
        icons.put("primary", R.drawable.street);
        icons.put("prison", R.drawable.prison);
        icons.put("pub", R.drawable.pub);
        icons.put("recycling", R.drawable.recycling);
        icons.put("religious_administrative", R.drawable.place_of_worship);
        icons.put("residential", R.drawable.house);
        icons.put("restaurant", R.drawable.restaurant);
        icons.put("retail", R.drawable.mall);
        icons.put("road", R.drawable.street);
        icons.put("secondary", R.drawable.street);
        icons.put("stadium", R.drawable.stadium);
        icons.put("station", R.drawable.bus_stop);
        icons.put("street", R.drawable.street);
        icons.put("suburb", R.drawable.town);
        icons.put("subway_entrance", R.drawable.bus_stop);
        icons.put("supermarket", R.drawable.mall);
        icons.put("terminal", R.drawable.aerodrome);
        icons.put("tertiary", R.drawable.street);
        icons.put("theatre", R.drawable.theatre);
        icons.put("toilets", R.drawable.toilets);
        icons.put("town", R.drawable.town);
        icons.put("townhall", R.drawable.townhall);
        icons.put("track", R.drawable.street);
        icons.put("village", R.drawable.town);
    }

    public void setPoiTypes(Map<Long, PoiType> poiTypes) {
        this.poiTypes = poiTypes;
    }

    /**
     * Add a bitmap to the memory cache.
     *
     * @param key    Key in the cache.
     * @param bitmap Bitmap to add.
     */
    public void addBitmapToMemoryCache(String key, Bitmap bitmap) {
        cache.put(key, bitmap);
    }

    /**
     * Get a bitmap from the memory cache.
     *
     * @param key Key of the bitmap in the cache.
     * @return The Bitmap.
     */
    public Bitmap getBitmapFromMemCache(String key) {
        return cache.get(key);
    }


    /**
     * Handle Bitmap load, storage and retrieval for MapFragment.
     * <br/>
     * Put the marker corresponding to a poiType into a color pin. The color depends of the poi's state.
     *
     * @param poiTypeId The PoiType id of the desired bitmap.
     * @param state     State of the Poi.
     * @return The marker corresponding to the poiType and the poi state.
     */
    Bitmap getMarkerBitmap(Long poiTypeId, Poi.State state) {

        try {

            Integer markerId = null;
            Integer iconId = getIconDrawableId(poiTypeId);


            Bitmap markerWrapper;
            Bitmap icon;

            // 3 states, n-pois -> 3 x n bitmaps.
            // Two choices: either we store all overlay combinations, or we store the markers and always process overlays.
            // As there might be more POIs than the number of combinations, I chose to store all combinations.

            switch (state) {
                case NORMAL:
                    markerId = R.drawable.marker_white;
                    break;
                case NOT_SYNCED:
                    markerId = R.drawable.marker_grey;
                    break;
                case SELECTED:
                    markerId = R.drawable.marker_blue;
                    break;
                case MOVING:
                    markerId = R.drawable.marker_red;
                    break;
            }

            String bitmapCacheId = markerId.toString() + iconId.toString();

            // Try to retrieve bmOverlay from cache
            Bitmap bmOverlay = getBitmapFromMemCache(bitmapCacheId);

            // If we don't have the combination into memory yet, compute it manually
            if (bmOverlay == null) {

                // If still too slow (lots of sources), we might change this and also include partials into cache
                // Right now, I don't think the use case proves its usefulness
                markerWrapper = BitmapFactory.decodeResource(context.getResources(), markerId);
                icon = BitmapFactory.decodeResource(context.getResources(), iconId);

                bmOverlay = Bitmap.createBitmap(markerWrapper.getWidth(), markerWrapper.getHeight(), markerWrapper.getConfig());
                Canvas canvas = new Canvas(bmOverlay);

                int x = markerWrapper.getWidth() / 2 - icon.getWidth() / 2;
                int y = markerWrapper.getHeight() / 4 - icon.getHeight() / 2 - (int) (0.05 * markerWrapper.getHeight());

                canvas.drawBitmap(markerWrapper, 0, 0, null);
                canvas.drawBitmap(icon, x, y, null);
                addBitmapToMemoryCache(bitmapCacheId, bmOverlay);

            }

            return bmOverlay;

        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Get the white icon corresponding to a poiType.
     *
     * @param poiTypeId The id of the PoiType.
     * @return The white icon.
     */
    public Drawable getIconWhite(Long poiTypeId) {
        Bitmap myBitmap = BitmapFactory.decodeResource(context.getResources(), poiTypeId == null ? R.drawable.open_book : getIconDrawableId(poiTypeId));
        myBitmap = myBitmap.copy(myBitmap.getConfig(), true);

        int[] allpixels = new int[myBitmap.getHeight() * myBitmap.getWidth()];

        myBitmap.getPixels(allpixels, 0, myBitmap.getWidth(), 0, 0, myBitmap.getWidth(), myBitmap.getHeight());

        for (int i = 0; i < myBitmap.getHeight() * myBitmap.getWidth(); i++) {
            if (allpixels[i] != 0) {
                int A = Color.alpha(allpixels[i]);
                // inverting byte for each R/G/B channel
                int R = 255 - Color.red(allpixels[i]);
                int G = 255 - Color.green(allpixels[i]);
                int B = 255 - Color.blue(allpixels[i]);
                // set newly-inverted pixel to output image
                allpixels[i] = Color.argb(A, R, G, B);
            }
        }

        myBitmap.setPixels(allpixels, 0, myBitmap.getWidth(), 0, 0, myBitmap.getWidth(), myBitmap.getHeight());

        return new BitmapDrawable(context.getResources(), myBitmap);
    }

    /**
     * Get the icon drawable id corresponding to the PoiType id.
     *
     * @param poiTypeId The PoiType id.
     * @return The icon drawable id.
     */
    public Integer getIconDrawableId(Long poiTypeId) {
        String icon = null;
        if (poiTypes != null) {
            PoiType poiType = poiTypes.get(poiTypeId);
            if (poiType != null) {
                icon = poiType.getIcon();
            }
        }
        return getDrawableId(icon);
    }

    /**
     * Get the note bitmap with the color corresponding to the state of the note.
     *
     * @param state The state of the note.
     * @return The bitmap in the corresponding color.
     */
    Bitmap getNoteBitmap(Note.State state) {
        // Try to retrieve bmOverlay from cache
        Bitmap bmOverlay = getBitmapFromMemCache(state.toString());
        Integer markerId;

        // If we don't have the combination into memory yet, compute it manually
        if (bmOverlay == null) {
            switch (state) {
                case OPEN:
                    markerId = R.drawable.note_pink;
                    break;
                case CLOSED:
                    markerId = R.drawable.note_green;
                    break;
                case SYNC:
                    markerId = R.drawable.note_grey;
                    break;
                case SELECTED:
                    markerId = R.drawable.note_blue;
                    break;
                case MOVING:
                    markerId = R.drawable.note_orange;
                    break;

                default:
                    markerId = R.drawable.note_green;
                    break;
            }

            // If still too slow (lots of sources), we might change this and also include partials into cache
            // Right now, I don't think the use case proves its usefulness
            bmOverlay = BitmapFactory.decodeResource(context.getResources(), markerId);
            addBitmapToMemoryCache(state.toString(), bmOverlay);
        }

        return bmOverlay;
    }

    /**
     * Get the drawable corresponding to the icon name.
     *
     * @param iconName The icon name.
     * @return The drawable.
     */
    public Drawable getDrawable(String iconName) {
        return context.getResources().getDrawable(getDrawableId(iconName));
    }

    /**
     * Get the drawable id corresponding to the icon name.
     *
     * @param iconName The icon name.
     * @return The drawable id.
     */
    int getDrawableId(String iconName) {
        int iconId = 0;
        if (iconName != null) {
            if (icons.containsKey(iconName)) {
                return icons.get(iconName);
            } else {
                iconId = context.getResources().getIdentifier(iconName, "drawable", context.getPackageName());
            }
        }
        return iconId != 0 ? iconId : R.drawable.default_marker;
    }
}
