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
package io.jawg.osmcontributor.ui.activities;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.FileProvider;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.drawee.view.SimpleDraweeView;
import com.flickr4java.flickr.photos.Size;
import com.github.scribejava.core.model.Verb;
import com.yarolegovich.lovelydialog.LovelyStandardDialog;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.jawg.osmcontributor.OsmTemplateApplication;
import io.jawg.osmcontributor.R;
import io.jawg.osmcontributor.flickr.event.PhotosFoundEvent;
import io.jawg.osmcontributor.flickr.oauth.FlickrOAuth;
import io.jawg.osmcontributor.flickr.oauth.OAuthRequest;
import io.jawg.osmcontributor.flickr.rest.FlickrAddTagClient;
import io.jawg.osmcontributor.flickr.rest.FlickrPhotoClient;
import io.jawg.osmcontributor.flickr.rest.FlickrUploadClient;
import io.jawg.osmcontributor.flickr.rest.asynctask.GetFlickrPhotos;
import io.jawg.osmcontributor.flickr.util.FlickrPhotoUtils;
import io.jawg.osmcontributor.flickr.util.FlickrUploadUtils;
import io.jawg.osmcontributor.flickr.util.OAuthParams;
import io.jawg.osmcontributor.flickr.util.ResponseConverter;
import io.jawg.osmcontributor.model.entities.Poi;
import io.jawg.osmcontributor.ui.adapters.ImageAdapter;
import io.jawg.osmcontributor.ui.events.edition.PleaseApplyPoiChanges;
import io.jawg.osmcontributor.utils.ConfigManager;
import io.jawg.osmcontributor.utils.edition.PoiChanges;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import retrofit.mime.TypedFile;
import retrofit.mime.TypedString;

import static org.apache.http.protocol.HTTP.UTF_8;

public class PhotoActivity extends AppCompatActivity {

    private static final String TAG = "PhotoActivity";

    /*=========================================*/
    /*------------CONSTANTS--------------------*/
    /*=========================================*/
    private static final int NB_IMAGE_REQUESTED = 35;

    private static final int NB_PAGE_REQUESTED = 1;

    private static final int ALLOW_CAMERA_PERMISSION = 100;

    private static final int REQUEST_CAMERA = 200;

    private static final int MAX_RETRY_UPLOAD = 5;

    private static final String PARAM_OAUTH_PREFIX = "oauth_";

    private static final String FLICKR_API_SERVICES = "https://api.flickr.com/services/rest";

    private static final String FLICKR_API_UPLOAD = "https://up.flickr.com/services/upload";

    private static final String FLICKR_DEFAULT_TAG = "openstreetmap";

    private static final String FLICKR_METHOD_ADDTAGS = "flickr.photos.addTags";

    private static final String OSM_FLICKR_TAG = "flickr";

    /*=========================================*/
    /*------------INJECTIONS-------------------*/
    /*=========================================*/
    @Inject
    SharedPreferences sharedPreferences;

    @Inject
    ConfigManager configManager;

    @Inject
    EventBus eventBus;

    /*=========================================*/
    /*---------------VIEWS---------------------*/
    /*=========================================*/
    @BindView(R.id.grid_photos)
    GridView gridPhotos;

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.loading)
    ProgressBar loadingImage;

    @BindView(R.id.add_photo)
    FloatingActionButton addPhoto;

    @BindView(R.id.zoom_photo)
    SimpleDraweeView zoomPhoto;

    @BindView(R.id.no_photos)
    TextView noPhotos;

    /*=========================================*/
    /*------------ATTRIBUTES-------------------*/
    /*=========================================*/
    private ImageAdapter imageAdapter;

    private int lastVisiblePos;

    private Long poiId;

    private GetFlickrPhotos asyncGetPhotos;

    private OsmTemplateApplication application;

    private FlickrPhotoClient flickrPhotoClient;

    private FlickrUploadClient flickrUploadClient;

    private FlickrAddTagClient flickrAddTagClient;

    private FlickrOAuth flickrOAuth;

    private File photoFile;

    private ProgressDialog progressDialog;

    private double latitude;

    private double longitude;

    private int nbTry;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo);
        ButterKnife.bind(this);
        EventBus.getDefault().register(this);
        application = (OsmTemplateApplication) getApplication();
        flickrOAuth = new FlickrOAuth();
        application.getOsmTemplateComponent().inject(this);
        application.getOsmTemplateComponent().inject(flickrOAuth);
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(getString(R.string.upload_message));
        progressDialog.setCancelable(false);

        // Set action bar infos.
        toolbar.setTitle("Photos");
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        // Loading image.
        addPhoto.setVisibility(View.VISIBLE);

        // Init parameters.
        latitude = getIntent().getDoubleExtra("latitude", 0);
        longitude = getIntent().getDoubleExtra("longitude", 0);
        poiId = getIntent().getLongExtra("poiId", 0);
        imageAdapter = new ImageAdapter(this, poiId);
        gridPhotos.setAdapter(imageAdapter);

        // Init listener and view
        initScrollListener();
        initOnClickItemListener();
        initView();
    }

    /*=========================================*/
    /*----------------ONCLICK------------------*/
    /*=========================================*/
    @OnClick(R.id.add_photo)
    public void onClickAddPhoto(View v) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissionIfNeeded();
        } else {
            takePicture();
        }
    }

    @OnClick(R.id.zoom_photo)
    public void onClickZoomPhoto(View v) {
        zoomPhoto.setVisibility(View.INVISIBLE);
        addPhoto.setVisibility(View.VISIBLE);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (zoomPhoto.getVisibility() == View.VISIBLE) {
            zoomPhoto.setVisibility(View.INVISIBLE);
        } else {
            if (asyncGetPhotos != null) {
                asyncGetPhotos.cancel(true);
            }
            onBackPressed();
        }
        return true;
    }

    /*=========================================*/
    /*-------------PRIVATE CODE----------------*/
    /*=========================================*/
    private void initScrollListener() {
        // Hide button on scroll down and show it on scroll up.
        lastVisiblePos = gridPhotos.getFirstVisiblePosition();
        gridPhotos.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) { }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                int currentFirstVisPos = view.getFirstVisiblePosition();
                // Scroll down.
                if (currentFirstVisPos > lastVisiblePos) {
                    addPhoto.setVisibility(View.INVISIBLE);
                }

                // Scroll up.
                if (currentFirstVisPos < lastVisiblePos) {
                    addPhoto.setVisibility(View.VISIBLE);
                }
                lastVisiblePos = currentFirstVisPos;
            }
        });
    }

    private void initOnClickItemListener() {
        gridPhotos.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                zoomPhoto.setImageURI(Uri.parse(ImageAdapter.getPhotosOriginals(poiId).get(position)));
                zoomPhoto.setVisibility(View.VISIBLE);
                addPhoto.setVisibility(View.INVISIBLE);
            }
        });
    }

    private void initView() {
        if (ImageAdapter.getPhotoUrlsCachedThumbs(poiId) == null || ImageAdapter.getPhotoUrlsCachedThumbs(poiId).isEmpty()) {
            gridPhotos.setVisibility(View.INVISIBLE);
            loadingImage.setVisibility(View.VISIBLE);
        }
        asyncGetPhotos = new GetFlickrPhotos(longitude, latitude, application.getFlickr(), NB_IMAGE_REQUESTED, NB_PAGE_REQUESTED);
        asyncGetPhotos.execute();
    }

    /*=========================================*/
    /*-------------PHOTOS CODE-----------------*/
    /*=========================================*/
    private void takePicture() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this, this.getPackageName() + ".fileprovider", photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_CAMERA);
            }
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.FRANCE).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        return File.createTempFile(imageFileName, ".jpg", storageDir);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CAMERA && resultCode == RESULT_OK) {
            flickrUploadClient = null;
            uploadPhoto();
        }
    }

    /*=========================================*/
    /*---------------EVENTS--------------------*/
    /*=========================================*/
    /**
     * Event called when GetFlickrPhotos AsyncTask is done.
     * @param photosFoundEvent event with photos found
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onPhotosFoundEvent(PhotosFoundEvent photosFoundEvent) {
        List<List<Size>> photos = photosFoundEvent.getPhotos();
        if (photos != null && !photos.isEmpty()) {
            noPhotos.setVisibility(View.INVISIBLE);
            for (List<Size> size : photos) {
                imageAdapter.addPhoto(size.get(Size.SQUARE).getSource(), poiId, Size.SQUARE);
                imageAdapter.addPhoto(size.get(Size.ORIGINAL).getSource(), poiId, Size.ORIGINAL);
            }
        } else {
            noPhotos.setVisibility(View.VISIBLE);
        }
        loadingImage.setVisibility(View.INVISIBLE);
        gridPhotos.setVisibility(View.VISIBLE);
    }

    private void uploadPhoto() {
        OAuthRequest oAuthRequest = flickrOAuth.getOAuthRequest();
        if (oAuthRequest == null) {
            oAuthRequest = new OAuthRequest(application.getFlickr().getApiKey(), application.getFlickr().getSharedSecret());
            oAuthRequest.setOAuthToken(configManager.getFlickrToken());
            oAuthRequest.setOAuthTokenSecret(configManager.getFlickrTokenSecret());
            flickrOAuth.setOAuthRequest(oAuthRequest);
        }
        oAuthRequest.setRequestUrl(FLICKR_API_UPLOAD);
        oAuthRequest.initParam(OAuthParams.getOAuthParams().put(OAuthParams.OAUTH_TOKEN, oAuthRequest.getOAuthToken()).toMap());
        oAuthRequest.signRequest(Verb.POST);

        progressDialog.show();
        if (flickrUploadClient == null) {
            flickrUploadClient = FlickrUploadUtils.getRestAdapter(oAuthRequest.getParams()).create(FlickrUploadClient.class);
        }
        TypedFile typedFile = new TypedFile("multipart/form-data", photoFile);
        flickrUploadClient.upload(typedFile, new Callback<String>() {
            @Override
            public void success(String s, Response response) {
                onUploadFinishedEvent(ResponseConverter.convertImageId(s));
            }

            @Override
            public void failure(RetrofitError error) {
                Log.e(TAG, error.getBody().toString());
                if (nbTry < MAX_RETRY_UPLOAD) {
                    nbTry++;
                    uploadPhoto();
                } else {
                    nbTry = 0;
                    Toast.makeText(PhotoActivity.this, R.string.flickr_communication_failure, Toast.LENGTH_LONG).show();
                    progressDialog.dismiss();
                }
            }
        });
    }

    public void onUploadFinishedEvent(final String photoId) {
        //Retrieve picture URL
        if (flickrPhotoClient == null) {
            flickrPhotoClient = FlickrPhotoUtils.getAdapter().create(FlickrPhotoClient.class);
        }
        OAuthRequest oauthRequest = new OAuthRequest(configManager.getFlickrApiKey(), configManager.getFlickrApiKeySecret());
        oauthRequest.setRequestUrl(FLICKR_API_SERVICES);
        oauthRequest.setOAuthToken(configManager.getFlickrToken());
        oauthRequest.setOAuthTokenSecret(configManager.getFlickrTokenSecret());
        oauthRequest.initParam(OAuthParams.getOAuthParams()
                .put(OAuthParams.OAUTH_TOKEN, configManager.getFlickrToken())
                .put("method", "flickr.photos.getInfo")
                .put("photo_id", photoId).toMap());
        oauthRequest.signRequest(Verb.GET);
        flickrPhotoClient.setProperties(oauthRequest.getParams(), new Callback<String>() {
            @Override
            public void success(String s, Response response) {
                defineImageOnPoi(ResponseConverter.convertImageUrl(s));
                setPhotoLocation(photoId);
            }

            @Override
            public void failure(RetrofitError error) {
                Toast.makeText(PhotoActivity.this, R.string.flickr_communication_failure, Toast.LENGTH_LONG).show();
                progressDialog.dismiss();
            }
        });
    }

    private void setPhotoLocation(final String photoId) {
        if (flickrPhotoClient == null) {
            flickrPhotoClient = FlickrPhotoUtils.getAdapter().create(FlickrPhotoClient.class);
        }
        OAuthRequest oauthRequest = new OAuthRequest(configManager.getFlickrApiKey(), configManager.getFlickrApiKeySecret());
        oauthRequest.setRequestUrl(FLICKR_API_SERVICES);
        oauthRequest.setOAuthToken(configManager.getFlickrToken());
        oauthRequest.setOAuthTokenSecret(configManager.getFlickrTokenSecret());
        oauthRequest.initParam(OAuthParams.getOAuthParams()
                .put(OAuthParams.OAUTH_TOKEN, configManager.getFlickrToken())
                .put("method", "flickr.photos.geo.setLocation")
                .put("photo_id", photoId)
                .put("lat", String.valueOf(latitude))
                .put("lon", String.valueOf(longitude)).toMap());
        oauthRequest.signRequest(Verb.GET);
        flickrPhotoClient.setProperties(oauthRequest.getParams(), new Callback<String>() {
            @Override
            public void success(String s, Response response) {
                try {
                    setPhotoTag(photoId);
                } catch (UnsupportedEncodingException e) {
                    failure(null);
                }
            }

            @Override
            public void failure(RetrofitError error) {
                Toast.makeText(PhotoActivity.this, R.string.picture_sent_failure, Toast.LENGTH_LONG).show();
                progressDialog.dismiss();
            }
        });
    }

    private void setPhotoTag(final String photoId) throws UnsupportedEncodingException {
        //Create the machine tag for Flickr to associate picture to this Poi
        Poi currentPoi = application.getOsmTemplateComponent().getPoiManager().queryForId(poiId);
        StringBuilder flickrOsmTagBuilder = new StringBuilder(FLICKR_DEFAULT_TAG);
        if (currentPoi.getBackendId() != null && currentPoi.getBackendId().length() > 0) {
            flickrOsmTagBuilder.append(",osm:");
            flickrOsmTagBuilder.append((currentPoi.getWay()) ? "way" : "node");
            flickrOsmTagBuilder.append("=");
            flickrOsmTagBuilder.append(currentPoi.getBackendId());
        } else {
            //TODO Handle pictures added on new POIs which haven't an OSM ID yet
            //Such POIs might be sent to OSM first, and when an ID is defined, update the Flickr picture
        }

        String flickrOsmTag = URLEncoder.encode(flickrOsmTagBuilder.toString(), UTF_8);

        OAuthRequest oauthRequest = flickrOAuth.getOAuthRequest();
        if (oauthRequest == null) {
            oauthRequest = new OAuthRequest(application.getFlickr().getApiKey(), application.getFlickr().getSharedSecret());
            oauthRequest.setOAuthToken(configManager.getFlickrToken());
            oauthRequest.setOAuthTokenSecret(configManager.getFlickrTokenSecret());
            flickrOAuth.setOAuthRequest(oauthRequest);
        }

        oauthRequest.setRequestUrl(FLICKR_API_SERVICES);
        oauthRequest.initParam(OAuthParams.getOAuthParams()
                .put(OAuthParams.OAUTH_TOKEN, oauthRequest.getOAuthToken())
                .put("method", FLICKR_METHOD_ADDTAGS)
                .put("photo_id", photoId)
                .put("tags", flickrOsmTag).toMap());

        oauthRequest.signRequest(Verb.POST);

        //Filter params for Authorization header
        Map<String, String> oauthParams = new TreeMap<String, String>();
        for (Map.Entry<String, String> entry : oauthRequest.getParams().entrySet()) {
            if (entry.getKey().startsWith(PARAM_OAUTH_PREFIX)) {
                oauthParams.put(entry.getKey(), entry.getValue());
            }
        }

        flickrAddTagClient = FlickrPhotoUtils.getAdapter(oauthParams).create(FlickrAddTagClient.class);

        flickrAddTagClient.addTags(
                new TypedString(
                        new StringBuilder("method=")
                                .append(FLICKR_METHOD_ADDTAGS)
                                .append("&photo_id=")
                                .append(photoId)
                                .append("&tags=")
                                .append(flickrOsmTag)
                                .toString()
                ),
                new Callback<String>() {
                    @Override
                    public void success(String s, Response response) {
                        Log.d(TAG, s);

                        progressDialog.dismiss();
                        photoFile.delete();
                        Toast.makeText(PhotoActivity.this, R.string.picture_sent_success, Toast.LENGTH_LONG).show();
                        if (ImageAdapter.getPhotoUrlsCachedThumbs(poiId) == null || ImageAdapter.getPhotoUrlsCachedThumbs(poiId).isEmpty()) {
                            finish();
                        }
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        if (nbTry < MAX_RETRY_UPLOAD) {
                            nbTry++;
                            try {
                                setPhotoTag(photoId);
                            } catch (UnsupportedEncodingException e) {
                                failure(null);
                            }
                        } else {
                            nbTry = 0;
                            Toast.makeText(PhotoActivity.this, R.string.poi_association_failure, Toast.LENGTH_LONG).show();
                            progressDialog.dismiss();
                        }
                    }
                }
        );
    }

    /**
     * Associate the newly uploaded flickr picture to the current Poi
     * @param imageUrl The complete URL to access this picture
     */
    private void defineImageOnPoi(String imageUrl) {
        if (imageUrl.length() > 0) {
            Poi currentPoi = application.getOsmTemplateComponent().getPoiManager().queryForId(poiId);
            Map<String, String> currentPoiTagsMap = new HashMap<String, String>(currentPoi.getTagsMap());

            if (currentPoiTagsMap.containsKey(OSM_FLICKR_TAG)) {
                StringBuilder newFlickrTag = new StringBuilder(currentPoiTagsMap.get(OSM_FLICKR_TAG).trim())
                        .append(";")
                        .append(imageUrl);
                currentPoiTagsMap.put(OSM_FLICKR_TAG, newFlickrTag.toString());
            } else {
                currentPoiTagsMap.put(OSM_FLICKR_TAG, imageUrl);
            }

            PoiChanges currentPoiChanges = new PoiChanges(poiId);
            currentPoiChanges.setTagsMap(currentPoiTagsMap);
            eventBus.post(new PleaseApplyPoiChanges(currentPoiChanges));
        } else {
            Log.e(TAG, "Invalid Flickr URL: " + imageUrl);
        }
    }

    /*=========================================*/
    /*-----------FOR ANDROID 6.0---------------*/
    /*=========================================*/
    /**
     * This method must be called if the android version is 6.0 or higher.
     * Check if the app has ACCESS_LOCATION (COARSE and FINE) and WRITE_EXTERNAL_STORAGE.
     * If the app doesn't have both permission, a request is prompted to the user.
     * For more informations about permission in Android 6.0, please refer to
     * https://developer.android.com/training/permissions/requesting.html?hl=Fr#explain
     */
    private void requestPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Check if permission are enable by the user. Dangerous permissions requested are :
            int hasEnabledCameraPerm = checkSelfPermission(Manifest.permission.CAMERA);

            // If the user have already allow permission, launch map activity, else, ask the user to allow permission
            if (hasEnabledCameraPerm == PackageManager.PERMISSION_DENIED) {
                requestPermissions(new String[] {Manifest.permission.CAMERA}, ALLOW_CAMERA_PERMISSION);
            } else {
                takePicture();
            }
        }
    }

    /**
     * This method is a callback. Check the user's answer after requesting permission.
     *
     * @param requestCode app request code (here, we only handle ALLOW_PERMISSION
     * @param permissions permissions requested (here, ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION, WRITE_EXTERNAL_STORAGE)
     * @param grantResults user's decision
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        // If request is cancelled, the result arrays are empty.
        if (requestCode == ALLOW_CAMERA_PERMISSION && grantResults.length > 0) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                takePicture();
            } else {
                permissionNotEnabled();
            }
        } else {
            permissionNotEnabled();
        }
    }

    /**
     * This method is called is a permission (or all permissions) are declined by the user.
     * If permissions are refused, we indicate why we request permissions and that, whitout it,
     * the app can not work.
     */
    private void permissionNotEnabled() {
        new LovelyStandardDialog(this)
                .setTopColorRes(R.color.colorPrimaryDark)
                .setIcon(R.mipmap.icon)
                .setTitle(R.string.permissions_title)
                .setMessage(R.string.permissions_information)
                .setPositiveButton(android.R.string.ok, null).show();
    }

}