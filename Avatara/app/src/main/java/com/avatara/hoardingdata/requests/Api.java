package com.avatara.hoardingdata.requests;

/**
 * Created by shobhit on 2/7/16.
 *
 * Api Helper
 */
public class Api {
    public static final String SERVER_URL = "http://aaho.in";
    // public static final String SERVER_URL = "http://192.168.1.9:8080";
    // public static final String SERVER_URL = "http://192.168.43.5:8080";

    // Api urls
    public static final String DRIVER_REGISTER_URL = url("/api/driver/register/");
    public static final String DRIVER_DETAIL_EDIT_URL = url("/api/driver/edit-details/");
    public static final String LOCATION_UPDATE_URL = url("/api/driver/location-update/");
    public static final String VEHICLE_STATUS_URL = url("/api/driver/vehicle-status/");
    public static final String APP_LOGS_URL = url("/api/driver/store-app-logs/");

    // Status strings
    public static final String STATUS_SUCCESS = "success";
    public static final String STATUS_ERROR = "error";

    public static String url(String path) {
        return SERVER_URL + path;
    }
}
