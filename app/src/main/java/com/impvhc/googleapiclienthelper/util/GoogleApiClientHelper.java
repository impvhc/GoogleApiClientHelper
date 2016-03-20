package com.impvhc.googleapiclienthelper.util;

import android.app.Activity;
import android.content.Context;
import android.content.IntentSender;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

public class GoogleApiClientHelper implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    public static final String TAG = GoogleApiClientHelper.class.getSimpleName();


    public interface OnLocationChanged {
        void handleOnLocationChanged(Location location);
    }

    /**
     * The desired interval for location updates. Inexact. Updates may be more or less frequent.
     */
    private static long UPDATE_INTERVAL_IN_MILLISECONDS = 10000;

    /**
     * The fastest rate for active location updates. Exact. Updates will never be more frequent
     * than this value.
     */
    private static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS =
            UPDATE_INTERVAL_IN_MILLISECONDS / 2;

    // Keys for storing activity state in the Bundle.
    public final static String REQUESTING_LOCATION_UPDATES_KEY = "requesting-location-updates-key";
    public final static String LOCATION_KEY = "location-key";
    public final static String LAST_UPDATED_TIME_STRING_KEY = "last-updated-time-string-key";

    /**
     * Provides the entry point to Google Play services.
     */
    protected GoogleApiClient mGoogleApiClient;

    /**
     * Stores parameters for requests to the FusedLocationProviderApi.
     */
    protected LocationRequest mLocationRequest;

    /**
     * Represents a geographical location.
     */
    protected Location mCurrentLocation;
    /**
     * Tracks the status of the location updates request. Value changes when the user presses the
     * Start Updates and Stop Updates buttons.
     */
    protected Boolean mRequestingLocationUpdates=false;

    /**
     * Time when the location was updated represented as a String.
     */
    protected String mLastUpdateTime;
    /*
       * Define a request code to send to Google Play services
       * This code is returned in Activity.onActivityResult
       */
    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;

    private OnLocationChanged mOnLocationChanged;
    private Context mContext;
    public static LocationEnableHelper locationEnableHelper;
    /**
     * Builds a GoogleApiClient. Uses the {@code #addApi} method to request the
     * LocationServices API.
     */
    public GoogleApiClientHelper(Context mContext){
        this.mContext=mContext;
        mGoogleApiClient = new GoogleApiClient.Builder(this.mContext)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        createLocationRequest();
        locationEnableHelper=new LocationEnableHelper((Activity) mContext,mLocationRequest,mGoogleApiClient);
    }
    public GoogleApiClientHelper(Context mContext, OnLocationChanged mOnLocationChanged){
        this.mContext=mContext;
        this.mOnLocationChanged=mOnLocationChanged;
        mGoogleApiClient = new GoogleApiClient.Builder(this.mContext)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        createLocationRequest();
        locationEnableHelper=new LocationEnableHelper((Activity) mContext,mLocationRequest,mGoogleApiClient);
    }

    /**
     * Sets up the location request. Android has two location request settings:
     * {@code ACCESS_COARSE_LOCATION} and {@code ACCESS_FINE_LOCATION}. These settings control
     * the accuracy of the current location. This sample uses ACCESS_FINE_LOCATION, as defined in
     * the AndroidManifest.xml.
     * <p/>
     * When the ACCESS_FINE_LOCATION setting is specified, combined with a fast update
     * interval (5 seconds), the Fused Location Provider API returns location updates that are
     * accurate to within a few feet.
     * <p/>
     * These settings are appropriate for mapping applications that show real-time location
     * updates.
     */
    public void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        // Sets the desired interval for active location updates. This interval is
        // inexact. You may not receive updates at all if no location sources are available, or
        // you may receive them slower than requested. You may also receive updates faster than
        // requested if other applications are requesting location at a faster interval.
        mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);

        // Sets the fastest rate for active location updates. This interval is exact, and your
        // application will never receive updates faster than this value.
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);

        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }
    /**
     * Requests location updates from the FusedLocationApi.
     */
    public void startLocationUpdates() {
        Log.i(TAG,"StartLocationUpdates");
        mRequestingLocationUpdates=true;
        // The final argument to {@code requestLocationUpdates()} is a LocationListener
        // (http://developer.android.com/reference/com/google/android/gms/location/LocationListener.html).
        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, this);
    }
    /**
     * Removes location updates from the FusedLocationApi.
     */
    public void stopLocationUpdates() {
        Log.i(TAG,"StopLocationUpdates");
        mRequestingLocationUpdates=false;
        // It is a good practice to remove location requests when the activity is in a paused or
        // stopped state. Doing so helps battery performance and is especially
        // recommended in applications that request frequent location updates.

        // The final argument to {@code requestLocationUpdates()} is a LocationListener
        // (http://developer.android.com/reference/com/google/android/gms/location/LocationListener.html).
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
    }
    /**
     * Set the fastest rate for active location updates.
     *
     * @param updateInterval
     */
    public void setUpdateInterval(Long updateInterval){
        UPDATE_INTERVAL_IN_MILLISECONDS=updateInterval;
    }
    /**
     * Connects the client to Google Play services.
     * This method returns immediately, and connects to the service in the background.
     * If the connection is successful, onConnected(Bundle) is called and enqueued items are executed. On a failure, onConnectionFailed(ConnectionResult) is called.
     * If the client is already connected or connecting, this method does nothing.
     */
    public void connect() {
        if(!mGoogleApiClient.isConnected()){
            locationEnableHelper.verifyLocationEnable();
        }
        mGoogleApiClient.connect();
    }
    /**
     * Closes the connection to Google Play services. No calls can be made using this client after calling this method.
     * Any method calls that haven't executed yet will be canceled.
     * if connection to the service hasn't been established yet all calls already made will be canceled.
     */
    public void disconnect() {
        if (mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
            mGoogleApiClient.disconnect();
        }
    }
    public void setOnLocationChanged(OnLocationChanged mOnLocationChanged){
        this.mOnLocationChanged=mOnLocationChanged;
    }
    public Location getLocation(){
        if(mCurrentLocation==null){
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }
        return mCurrentLocation;
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.i(TAG, "Location services connected.");
        // If the initial location was never previously requested, we use
        // FusedLocationApi.getLastLocation() to get it. If it was previously requested, we store
        // its value in the Bundle and check for it in onCreate(). We
        // do not request it again unless the user specifically requests location updates by pressing
        // the Start Updates button.
        //
        // Because we cache the value of the initial location in the Bundle, it means that if the
        // user launches the activity,
        // moves to a new location, and then changes the device orientation, the original location
        // is displayed as the activity is re-created.

        mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (mCurrentLocation == null) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }
        else {
            if(mOnLocationChanged!=null){
                mOnLocationChanged.handleOnLocationChanged(mCurrentLocation);
            }
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        // The connection to Google Play services was lost for some reason. We call connect() to
        // attempt to re-establish the connection.
        Log.i(TAG, "Connection suspended");
        connect();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {/*
         * Google Play services can resolve some errors it detects.
         * If the error has a resolution, try sending an Intent to
         * start a Google Play services activity that can resolve
         * error.
         */
        if (connectionResult.hasResolution() && mContext instanceof Activity) {
            try {
                Activity activity = (Activity) mContext;
                // Start an Activity that tries to resolve the error
                connectionResult.startResolutionForResult(activity, CONNECTION_FAILURE_RESOLUTION_REQUEST);
            /*
             * Thrown if Google Play services canceled the original
             * PendingIntent
             */
            } catch (IntentSender.SendIntentException e) {
                // Log the error
                e.printStackTrace();
            }
        } else {
            /*
             * If no resolution is available, display a dialog to the
             * user with the error.
             */
            Log.i(TAG, "Location services connection failed with code " + connectionResult.getErrorCode());
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        if(mOnLocationChanged!=null){
            mOnLocationChanged.handleOnLocationChanged(location);
        }
        mCurrentLocation=location;
    }

}
