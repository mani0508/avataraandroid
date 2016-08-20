package com.avatara.hoardingdata;

import android.content.Context;
import android.content.IntentSender;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.android.gms.common.ConnectionResult;
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

/**
 * Created by shobhit on 3/7/16.
 */
public class LocationSettingHelper implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private static GoogleApiClient googleApiClient = null;
    private static LocationSettingHelper instance = null;

    private LocationSettingHelper() {

    }

    private static LocationSettingHelper getInstance() {
        if (instance == null) {
            instance = new LocationSettingHelper();
        }
        return instance;
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {
        googleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    private GoogleApiClient googleApiClient(Context context) {
        if (googleApiClient == null) {
            googleApiClient = new GoogleApiClient.Builder(context)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .setAccountName(String.valueOf(R.string.google_maps_key)).build();
            if (googleApiClient != null) {
                googleApiClient.connect();
            }
        }
        return googleApiClient;
    }

    public static void locationSettingRequest(PermissionManagerActivity activity) {
        GoogleApiClient gApiClient = getInstance().googleApiClient(activity);
        LocationRequest locRequest = GPSLocation.newLocationRequest();

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locRequest);
        builder.setAlwaysShow(true); //this is the key ingredient

        PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(gApiClient, builder.build());
        result.setResultCallback(new LocationSettingCallback(activity));
    }

    private static class LocationSettingCallback implements ResultCallback<LocationSettingsResult> {

        private PermissionManagerActivity activity;

        public LocationSettingCallback(PermissionManagerActivity activity) {
            this.activity = activity;
        }

        @Override
        public void onResult(LocationSettingsResult result) {
            final Status status = result.getStatus();
            final LocationSettingsStates state = result.getLocationSettingsStates();
            switch (status.getStatusCode()) {
                case LocationSettingsStatusCodes.SUCCESS:
                    // All location settings are satisfied. The client can initialize location requests here.
                    break;
                case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                    // Location settings are not satisfied. But could be fixed by showing the user a dialog.
                    try {
                        // Show the dialog by calling startResolutionForResult() and check the result in onActivityResult().
                        status.startResolutionForResult(activity, PermissionManagerActivity.GPS_SETTINGS_REQUEST);
                    } catch (IntentSender.SendIntentException e) {
                        // Ignore the error.
                    }
                    break;
                case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                    // Location settings are not satisfied. However, we have no way to fix the settings so we won't show the dialog.
                    break;
            }
        }
    }
}
