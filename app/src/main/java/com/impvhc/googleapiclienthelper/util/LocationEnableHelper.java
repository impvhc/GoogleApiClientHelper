package com.impvhc.googleapiclienthelper.util;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStates;
import com.google.android.gms.location.LocationSettingsStatusCodes;

public class LocationEnableHelper implements ResultCallback<LocationSettingsResult> {
public static boolean isLocationEnable;
        LocationRequest mLocationRequest;
        GoogleApiClient mGoogleApiClient;
        Activity mActivity;

    public LocationEnableHelper(Activity mActivity,LocationRequest mLocationRequest, GoogleApiClient mGoogleApiClient) {
        this.mLocationRequest = mLocationRequest;
        this.mGoogleApiClient = mGoogleApiClient;
        this.mActivity = mActivity;
    }

    public void verifyLocationEnable() {
        LocationSettingsRequest.Builder builder =
                new LocationSettingsRequest.Builder().addLocationRequest(mLocationRequest);

        builder.setAlwaysShow(true);

        PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient, builder.build());
        result.setResultCallback(this);

    }
    @Override
    public void onResult(LocationSettingsResult locationSettingsResult) {
        final Status status = locationSettingsResult.getStatus();
        final LocationSettingsStates locationSettingsStates = locationSettingsResult.getLocationSettingsStates();
        switch (status.getStatusCode()){
            case LocationSettingsStatusCodes.SUCCESS:
                isLocationEnable=true;
                break;
            case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                // Location settings are not satisfied. But could be fixed by showing the user
                // a dialog
                // Show the dialog by calling startResolutionForResult(),
                // and check the result in onActivityResult().
                isLocationEnable=false;
                try {
                    status.startResolutionForResult(mActivity,1000);
                } catch (IntentSender.SendIntentException e) {
                    e.printStackTrace();
                }
                break;
            case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                break;
        }
    }

    /**
     * This method should be called in the onActivityResult of the calling activity whenever we are
     * trying to implement the Check Location Settings fix dialog.
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case 1000:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        // All required changes were successfully made
                        isLocationEnable=true;
                        break;
                    case Activity.RESULT_CANCELED:
                        // The user was asked to change settings, but chose not so
                        isLocationEnable=false;
                        break;
                    default:
                        break;
                }
                break;
        }
    }
}
