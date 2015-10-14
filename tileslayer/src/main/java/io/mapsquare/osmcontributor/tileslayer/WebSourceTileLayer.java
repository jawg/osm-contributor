/**
 * ## Original work
 *
 * Copyright Mapbox Inc 2014
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * ## Modifications
 *
 * Copyright 2015 eBusiness Information
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
package io.mapsquare.osmcontributor.tileslayer;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.text.TextUtils;
import android.util.Log;

import com.mapbox.mapboxsdk.constants.MapboxConstants;
import com.mapbox.mapboxsdk.tileprovider.MapTile;
import com.mapbox.mapboxsdk.tileprovider.MapTileCache;
import com.mapbox.mapboxsdk.tileprovider.modules.MapTileDownloader;
import com.mapbox.mapboxsdk.tileprovider.tilesource.TileLayer;
import com.mapbox.mapboxsdk.util.NetworkUtils;
import com.mapbox.mapboxsdk.views.util.TileLoadedListener;
import com.mapbox.mapboxsdk.views.util.TilesLoadedListener;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.atomic.AtomicInteger;

import uk.co.senab.bitmapcache.CacheableBitmapDrawable;

/**
 * Tweak of {@link com.mapbox.mapboxsdk.tileprovider.tilesource.WebSourceTileLayer} at v0.7.3.
 * When zoom > provider zoom limit, download the tile of zoom limit, cut the part of interest and zoom on it.
 * <br/>
 * Our changes are marked with "Custom tweak:" comments.
 * <p/>
 * An implementation of {@link TileLayer} that pulls tiles from the internet.
 */
public class WebSourceTileLayer extends TileLayer implements MapboxConstants {
    private static final String TAG = "WebSourceTileLayer";
    private static final int TILE_SIZE = 256;
    public static final int PROVIDER_ZOOM_LIMIT = 19;

    // Tracks the number of threads active in the getBitmapFromURL method.
    private AtomicInteger activeThreads = new AtomicInteger(0);
    protected boolean mEnableSSL = false;

    public WebSourceTileLayer(final String pId, final String url) {
        this(pId, url, false);
    }

    public WebSourceTileLayer(final String pId, final String url, final boolean enableSSL) {
        super(pId, url);
        initialize(pId, url, enableSSL);
    }

    private boolean checkThreadControl() {
        return activeThreads.get() == 0;
    }

    @Override
    public TileLayer setURL(final String aUrl) {
        if (aUrl.contains(String.format(MAPBOX_LOCALE, "http%s://", (mEnableSSL ? "" : "s")))) {
            super.setURL(aUrl.replace(String.format(MAPBOX_LOCALE, "http%s://", (mEnableSSL ? "" : "s")),
                    String.format(MAPBOX_LOCALE, "http%s://", (mEnableSSL ? "s" : ""))));
        } else {
            super.setURL(aUrl);
        }
        return this;
    }

    protected void initialize(String pId, String aUrl, boolean enableSSL) {
        mEnableSSL = enableSSL;
        setURL(aUrl);
    }

    /**
     * Gets a list of Tile URLs used by this layer for a specific tile.
     *
     * @param aTile a map tile
     * @param hdpi a boolean that indicates whether the tile should be at 2x or retina size
     * @return a list of tile URLS
     */
    public String[] getTileURLs(final MapTile aTile, boolean hdpi) {
        String url = getTileURL(aTile, hdpi);
        if (!TextUtils.isEmpty(url)) {
            return new String[]{url};
        }
        return null;
    }

    /**
     * Get a single Tile URL for a single tile.
     *
     * @param aTile a map tile
     * @param hdpi a boolean that indicates whether the tile should be at 2x or retina size
     * @return a list of tile URLs
     */
    public String getTileURL(final MapTile aTile, boolean hdpi) {
        return parseUrlForTile(mUrl, aTile, hdpi);
    }

    protected String parseUrlForTile(String url, final MapTile aTile, boolean hdpi) {
        // Custom tweak: if zoom > zoom limit, download the tile of zoom limit
        if (aTile.getZ() > PROVIDER_ZOOM_LIMIT) {
            return url.replace("{z}", String.valueOf(PROVIDER_ZOOM_LIMIT))
                    .replace("{x}", String.valueOf((int) (aTile.getX() / (int) Math.pow(2, (aTile.getZ() - PROVIDER_ZOOM_LIMIT))))) // Cast into int to truncate
                    .replace("{y}", String.valueOf((int) (aTile.getY() / (int) Math.pow(2, (aTile.getZ() - PROVIDER_ZOOM_LIMIT))))) // Cast into int to truncate
                    .replace("{2x}", hdpi ? "@2x" : "");
        }
        return url.replace("{z}", String.valueOf(aTile.getZ()))
                .replace("{x}", String.valueOf(aTile.getX()))
                .replace("{y}", String.valueOf(aTile.getY()))
                .replace("{2x}", hdpi ? "@2x" : "");
    }

    private static final Paint compositePaint = new Paint(Paint.FILTER_BITMAP_FLAG);

    private Bitmap compositeBitmaps(final Bitmap source, Bitmap dest) {
        Canvas canvas = new Canvas(dest);
        canvas.drawBitmap(source, 0, 0, compositePaint);
        return dest;
    }

    @Override
    public CacheableBitmapDrawable getDrawableFromTile(final MapTileDownloader downloader,
                                                       final MapTile aTile, boolean hdpi) {
        if (downloader.isNetworkAvailable()) {
            TilesLoadedListener listener = downloader.getTilesLoadedListener();

            String[] urls = getTileURLs(aTile, hdpi);
            CacheableBitmapDrawable result = null;
            Bitmap resultBitmap = null;
            if (urls != null) {
                MapTileCache cache = downloader.getCache();
                if (listener != null) {
                    listener.onTilesLoadStarted();
                }
                for (final String url : urls) {
                    Bitmap bitmap = getBitmapFromURL(aTile, url, cache);
                    if (bitmap == null) {
                        continue;
                    }
                    if (resultBitmap == null) {
                        resultBitmap = bitmap;
                    } else {
                        resultBitmap = compositeBitmaps(bitmap, resultBitmap);
                    }
                }
                if (resultBitmap != null) {
                    //get drawable by putting it into cache (memory and disk)
                    result = cache.putTileBitmap(aTile, resultBitmap);
                }
                if (checkThreadControl()) {
                    if (listener != null) {
                        listener.onTilesLoaded();
                    }
                }
            }

            if (result != null) {
                TileLoadedListener listener2 = downloader.getTileLoadedListener();
                result = listener2 != null ? listener2.onTileLoaded(result) : result;
            }

            return result;
        }
        return null;
    }

    /**
     * Requests and returns a bitmap object from a given URL, using aCache to decode it.
     *
     *
     * @param mapTile MapTile
     * @param url the map tile url. should refer to a valid bitmap resource.
     * @param aCache a cache, an instance of MapTileCache
     * @return the tile if valid, otherwise null
     */
    public Bitmap getBitmapFromURL(MapTile mapTile, final String url, final MapTileCache aCache) {
        // We track the active threads here, every exit point should decrement this value.
        activeThreads.incrementAndGet();

        if (TextUtils.isEmpty(url)) {
            activeThreads.decrementAndGet();
            return null;
        }

        try {
            HttpURLConnection connection = NetworkUtils.getHttpURLConnection(new URL(url));
            Bitmap bitmap = BitmapFactory.decodeStream(connection.getInputStream());

            // Custom tweak: If zoom > zoom limit, load the tile of zoom limit, cut the part of interest and resize it.
            if (mapTile.getZ() > PROVIDER_ZOOM_LIMIT) {
                int zoomFactor = 2 * (mapTile.getZ() - PROVIDER_ZOOM_LIMIT);
                int cutTileSize = TILE_SIZE / zoomFactor;
                // Cut the part of the Bitmap corresponding to the Tile of zoom 19
                Bitmap cutBitmap = Bitmap.createBitmap(bitmap, (mapTile.getX() % zoomFactor) * cutTileSize, (mapTile.getY() % zoomFactor) * cutTileSize, cutTileSize, cutTileSize);
                // Resize the map to the right size
                bitmap = Bitmap.createScaledBitmap(cutBitmap, TILE_SIZE, TILE_SIZE, false);
            }
            if (bitmap != null && mapTile.getZ() < PROVIDER_ZOOM_LIMIT) {
                aCache.putTileInMemoryCache(mapTile, bitmap);
            }
            return bitmap;
        } catch (final Throwable e) {
            Log.e(TAG, "Error downloading MapTile: " + url + ":" + e);
        } finally {
            activeThreads.decrementAndGet();
        }
        return null;
    }
}

