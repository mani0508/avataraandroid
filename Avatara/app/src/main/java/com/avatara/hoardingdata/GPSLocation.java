package com.avatara.hoardingdata;

/**
 * Created by mani on 17/5/16.
 */

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

/**
 * Created by mani on 4/27/2016.
 */
public class GPSLocation implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private LocationListener locationListener;

    // Location updates intervals in sec
    private static int UPDATE_INTERVAL = 5000; // 2 sec
    private static int FATEST_INTERVAL = 1000; // 1 sec
    private static int DISPLACEMENT = 0; // 0 meters so we get updates even when stationary

    private Context context;
    private GoogleApiClient googleApiClient = null;
    private LocationRequest locationRequest;

    private static GPSLocation instance;
    // private Location lastKnownLocation;

    public static GPSLocation getInstance() {
        if (instance == null) instance = new GPSLocation();
        return instance;
    }

    private GPSLocation() {
    }

    public void connect(Context context, LocationListener locationListener) {
        this.context = context;
        this.locationListener = locationListener;
        // create a new one every time, known hack
        if (googleApiClient != null && (googleApiClient.isConnected() || googleApiClient.isConnecting())) {
            googleApiClient.disconnect();
            L.e(context, "GPSLocation.connect() googleApiClient already exists and is connected, disconnecting");
        }
        if (googleApiClient == null) {
            L.e(context, "GPSLocation.connect() googleApiClient = null, creating new");
            googleApiClient = newGoogleApiClient();
        } else {
            L.e(context, "GPSLocation.connect() googleApiClient not null, no need to create");
        }
        if (googleApiClient == null) {
            L.e(context, "GPSLocation.connect() googleApiClient creation failed, returning without doing anything");
            return;
        } else {
            L.e(context, "GPSLocation.connect() googleApiClient not null, calling connect");
            googleApiClient.connect();
        }
    }

    private GoogleApiClient newGoogleApiClient() {
        GoogleApiClient mGoogleApiClient = new GoogleApiClient.Builder(context)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .setAccountName(String.valueOf(R.string.google_maps_key)).build();
        if (mGoogleApiClient == null) {
            L.e(context, "GPSLocation.newGoogleApiClient() ERROR! could not create new api client");
        }
        return mGoogleApiClient;
    }

    public void disconnect() {
        if (googleApiClient != null) googleApiClient.disconnect();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        L.e(context, "GPSLocation.onConnected()");
        startLocationTracking();
    }

    private void startLocationTracking() {
        L.e(context, "GPSLocation.startLocationTracking()");
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            L.e(context, "GPSLocation.startLocationTracking() location perms not granted");
            return;
        }

        if (locationRequest == null) {
            locationRequest = newLocationRequest();
            L.e(context, "GPSLocation.startLocationTracking(), locationRequest = null, creating new");
        }

        if (googleApiClient.isConnected()) {
            L.e(context, "GPSLocation.startLocationTracking() googleApiClient.isConnected() = true, requesting location updates");
            LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
        } else {
            L.e(context, "GPSLocation.startLocationTracking() googleApiClient.isConnected() = false, calling connect");
            googleApiClient.connect();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        L.e(context, "GPSLocation.onConnectionSuspended()");
        googleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        L.e(context, "GPSLocation.onConnectionFailed(), errorCode = " + connectionResult.getErrorCode());
    }

    @Override
    public void onLocationChanged(Location location) {
        // lastKnownLocation = location;
        if (locationListener != null) {
            locationListener.onLocationChanged(location);
            L.e(context, "GPSLocation.onLocationChanged(), locationListener != null, calling locationListener.onLocationChanged()");
        } else {
            L.e(context, "GPSLocation.onLocationChanged(), locationListener = null");
        }
        stopTrackingAndDisconnect();
    }

    public static LocationRequest newLocationRequest() {
        LocationRequest locRequest = new LocationRequest();
        locRequest.setInterval(UPDATE_INTERVAL);
        locRequest.setFastestInterval(FATEST_INTERVAL);
        locRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locRequest.setSmallestDisplacement(DISPLACEMENT);
        return locRequest;
    }

    public void stopTrackingAndDisconnect() {
        L.e(context, "GPSLocation.stopTrackingAndDisconnect()");
        if (googleApiClient != null && googleApiClient.isConnected()) {
            L.e(context, "GPSLocation.stopTrackingAndDisconnect(), removeLocationUpdates called");
            LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this);
            googleApiClient.disconnect();
        }
    }

    /*
    public JSONObject getLocationInfo(ContextWrapper contextWrapper) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            L.e(context, "GPSLocation.getLocationInfo() location perms not granted");
            return null;
        }
        Location location = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
        lastKnownLocation = location;

        if (location == null) {
            L.e(context, "GPSLocation.getLocationInfo() location = null, didn't get location, returning");
            return null;
        }

        if (googleApiClient.isConnected()) {
            L.e(context, "GPSLocation.getLocationInfo() googleApiClient.isConnected() = true");
            JSONObject jsonObject = new JSONObject();
            try {
                TimeZone timeZone = TimeZone.getTimeZone("UTC+05:30");
                Calendar calendar = Calendar.getInstance(timeZone);
                jsonObject.put("lat", location.getLatitude());
                jsonObject.put("lng", location.getLongitude());
                jsonObject.put("datetime", calendar.getTimeInMillis());
                jsonObject.put("speed", location.getSpeed());
                jsonObject.put("device_id", Aaho.getDeviceId(contextWrapper));
                return jsonObject;
            } catch (JSONException e) {
                e.printStackTrace();
                L.e(context, "GPSLocation.getLocationInfo(), JSONException!!");
            }
        } else {
            L.e(context, "GPSLocation.getLocationInfo() googleApiClient.isConnected() = false");
        }
        return null;
    }
*/
    // public Location getLastKnownLocation() {
    //     return lastKnownLocation;
    // }
}
