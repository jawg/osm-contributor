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
package io.mapsquare.osmcontributor.flickr.rest;

import android.os.AsyncTask;

import com.flickr4java.flickr.Flickr;
import com.flickr4java.flickr.FlickrException;
import com.flickr4java.flickr.photos.Photo;
import com.flickr4java.flickr.photos.PhotoList;
import com.flickr4java.flickr.photos.SearchParameters;

import org.greenrobot.eventbus.EventBus;

import io.mapsquare.osmcontributor.flickr.event.PhotosFoundEvent;

/**
 * Get a list of photos from FLickr by latitude/longitude for openstreetmap tag.
 */
public class GetFlickrPhotos extends AsyncTask<Void, Void, PhotoList<Photo>> {

    private Flickr flickr;

    private final Integer RADIUS = 1;

    private final String[] TAGS = {"openstreetmap"};

    private Double longitude;

    private Double latitude;

    public GetFlickrPhotos(Double longitude, Double latitude, Flickr flickr) {
        this.longitude = longitude;
        this.latitude = latitude;
        this.flickr = flickr;
    }

    @Override
    protected PhotoList<Photo> doInBackground(Void... params) {
        SearchParameters parameters = new SearchParameters();
        parameters.setLatitude(String.valueOf(latitude));
        parameters.setLongitude(String.valueOf(longitude));
        parameters.setRadius(RADIUS);
        parameters.setTags(TAGS);
        try {
            return flickr.getPhotosInterface().search(parameters, 10, 1);
        } catch (FlickrException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(PhotoList<Photo> photos) {
        EventBus.getDefault().post(new PhotosFoundEvent(photos));
    }
}