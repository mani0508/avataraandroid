package com.avatara.hoardingdata;

import com.avatara.hoardingdata.database.DeviceLog;
import com.avatara.hoardingdata.database.DeviceLogDbHelper;
import android.content.Context;
import android.util.Log;

import java.util.List;

/**
 * Created by shobhit on 8/7/16.
 */
public class L {

    // private static DeviceLogDbHelper deviceLogDbHelper = null;

    public static DeviceLogDbHelper getDeviceLogHelper(Context context) {
        // if (deviceLogDbHelper == null) {
        //    deviceLogDbHelper = new DeviceLogDbHelper(context);
        // }
        // return deviceLogDbHelper;
        return new DeviceLogDbHelper(context);
    }

    public static void e(Context context, String log) {
        Log.e("[DeviceLog]", log);
        getDeviceLogHelper(context).addDeviceLog(log);
    }

    public static List<DeviceLog> logs(Context context) {
        return getDeviceLogHelper(context).getAllDeviceLogs();
    }

    public static void deleteAllLogs(Context context) {
        getDeviceLogHelper(context).deleteAllDeviceLogs();
    }

}
