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
package io.mapsquare.osmcontributor.flickr.rest.asynctask;

import android.os.AsyncTask;

import com.flickr4java.flickr.Flickr;
import com.flickr4java.flickr.FlickrException;
import com.flickr4java.flickr.photos.Photo;
import com.flickr4java.flickr.photos.PhotoList;
import com.flickr4java.flickr.photos.SearchParameters;
import com.flickr4java.flickr.photos.Size;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

import io.mapsquare.osmcontributor.flickr.event.PhotosFoundEvent;

/**
 * Get a list of photos from FLickr by latitude/longitude for openstreetmap tag.
 */
public class GetFlickrPhotos extends AsyncTask<Void, Void, List<List<Size>>> {

    /*=========================================*/
    /*-------------CONSTANTS-------------------*/
    /*=========================================*/
    /**
     * Radius in km
     */
    private static final Integer RADIUS = 1;

    private static final String[] TAGS = {"openstreetmap"};

    /*=========================================*/
    /*------------ATTRIBUTES-------------------*/
    /*=========================================*/
    private Flickr flickr;

    private Double longitude;

    private Double latitude;

    private Integer limitPerPage;

    private Integer nbPage = 1;

    public GetFlickrPhotos(Double longitude, Double latitude, Flickr flickr, Integer limitPerPage, Integer nbPage) {
        this.longitude = longitude;
        this.latitude = latitude;
        this.flickr = flickr;
        this.limitPerPage = limitPerPage;
        this.nbPage = nbPage;
    }

    @Override
    protected List<List<Size>> doInBackground(Void... params) {
        SearchParameters parameters = new SearchParameters();
        parameters.setLatitude(String.valueOf(latitude));
        parameters.setLongitude(String.valueOf(longitude));
        parameters.setRadius(RADIUS);
        parameters.setTags(TAGS);
        try {
            PhotoList<Photo> photos = flickr.getPhotosInterface().search(parameters, limitPerPage, nbPage);
            List<List<Size>> photosList = new ArrayList<>();
            for (Photo photo : photos) {
                photosList.add((List<Size>) flickr.getPhotosInterface().getSizes(photo.getId()));
            }
            return photosList;
        } catch (FlickrException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(List<List<Size>>  photos) {
        EventBus.getDefault().post(new PhotosFoundEvent(photos));
    }
}