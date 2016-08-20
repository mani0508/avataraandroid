package com.avatara.hoardingdata;

import android.Manifest;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.telephony.TelephonyManager;

import java.util.UUID;

/**
 * Created by shobhit on 30/6/16.
 *
 * Project helper sort of thingy
 *
 */

public class Aaho {
    // helper class for global/common variables, constants and functons
    private static String deviceId = null;
    private static String authToken = null;
    private static String vehicleStatus = null;

    // keys for shared preferences
    public static final String DEVICE_ID_KEY = "device_id";
    public static final String AUTH_TOKEN_KEY = "auth_token";
    public static final String VEHICLE_NUMBER_KEY = "vehicle_number";
    public static final String VEHICLE_TYPE_KEY = "vehicle_type";
    public static final String VEHICLE_STATUS_KEY = "vehicle_status";
    public static final String DRIVER_NAME_KEY = "driver_name";
    public static final String MOBILE_NUMBER_KEY = "mobile_number";

    // App constants
    public static final String APP_SUPPORT_NUMBER = "+919969607841";

    public static String getVehicleStatus(ContextWrapper contextWrapper) {
        if (vehicleStatus == null) {
            SharedPreferences mSharedPreference = PreferenceManager.getDefaultSharedPreferences(contextWrapper.getBaseContext());
            vehicleStatus = mSharedPreference.getString(VEHICLE_STATUS_KEY, null);
            if (vehicleStatus == null) {
                vehicleStatus = MainActivity.STATUS_UNLOADED;
                SharedPreferences.Editor editor = mSharedPreference.edit();
                editor.putString(VEHICLE_STATUS_KEY, vehicleStatus);
                editor.commit();
            }
        }
        return vehicleStatus;
    }

    public static void setVehicleStatus(ContextWrapper contextWrapper, String status) {
        if (!vehicleStatus.equals(status)) {
            vehicleStatus = status;
            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(contextWrapper.getBaseContext()).edit();
            editor.putString(VEHICLE_STATUS_KEY, vehicleStatus);
            editor.commit();
        }
    }

    public static void startAppServices(Context context) {
        Intent locIntent = new Intent(context, LocationService.class);
        locIntent.setAction(LocationService.STARTFOREGROUND_ACTION);
        context.startService(locIntent);
    }

    public static String getDeviceId(ContextWrapper contextWrapper) {
        configureDeviceId(contextWrapper);
        return deviceId == null ? "" : deviceId;
    }

    public static void configureDeviceId(ContextWrapper contextWrapper) {
        if (deviceId == null) {
            // stored mac address
            SharedPreferences mSharedPreference = PreferenceManager.getDefaultSharedPreferences(contextWrapper.getBaseContext());
            String storedDeviceId = mSharedPreference.getString(DEVICE_ID_KEY, null);

            if (storedDeviceId == null) {  // opening app for the first time or after clearing data
                if (!hasPhonePerm(contextWrapper)) {
                    return;  // wait, user has not granted perms yet
                }
                // we have perms
                storedDeviceId = getImei(contextWrapper);
                if (storedDeviceId == null || storedDeviceId == "") {
                    // we have perms but still no imei, our hands are tied, we need a unique id somehow
                    storedDeviceId = UUID.randomUUID().toString();
                }
                SharedPreferences.Editor editor = mSharedPreference.edit();
                editor.putString(DEVICE_ID_KEY, storedDeviceId);
                editor.commit();
            }
            deviceId = storedDeviceId;
        }
    }

    private static boolean hasPhonePerm(ContextWrapper contextWrapper) {
        return ContextCompat.checkSelfPermission(contextWrapper, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED;
    }

    private static String getImei(ContextWrapper contextWrapper) {
        String imei = null;
        try {
            TelephonyManager manager = (TelephonyManager) contextWrapper.getSystemService(Context.TELEPHONY_SERVICE);
            imei = manager.getDeviceId();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (imei != null) {
            return imei;
        }
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                TelephonyManager manager = (TelephonyManager) contextWrapper.getSystemService(Context.TELEPHONY_SERVICE);
                imei = manager.getDeviceId(0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return imei;
    }

    public static void saveDriverDetails(ContextWrapper contextWrapper, String vehicleNumber,
                                         String vehicleType, String driverName, String mobileNumber) {
        SharedPreferences mSharedPreference = PreferenceManager.getDefaultSharedPreferences(contextWrapper.getBaseContext());
        SharedPreferences.Editor editor = mSharedPreference.edit();
        editor.putString(VEHICLE_NUMBER_KEY, vehicleNumber);
        editor.putString(VEHICLE_TYPE_KEY, vehicleType);
        editor.putString(DRIVER_NAME_KEY, driverName);
        editor.putString(MOBILE_NUMBER_KEY, mobileNumber);
        editor.commit();
    }

    public static boolean isRegistered(ContextWrapper contextWrapper) {
        return getAuthToken(contextWrapper) == null ? false : true;
    }

    public static String getAuthToken(ContextWrapper contextWrapper) {
        if (authToken == null) {
            SharedPreferences mSharedPreference = PreferenceManager.getDefaultSharedPreferences(contextWrapper.getBaseContext());
            authToken = mSharedPreference.getString(AUTH_TOKEN_KEY, null);
        }
        return authToken;
    }

    public static void setAuthToken(ContextWrapper contextWrapper, String token) {
        SharedPreferences mSharedPreference = PreferenceManager.getDefaultSharedPreferences(contextWrapper.getBaseContext());
        SharedPreferences.Editor editor = mSharedPreference.edit();
        editor.putString(AUTH_TOKEN_KEY, token);
        editor.commit();
        authToken = token;
    }

}
