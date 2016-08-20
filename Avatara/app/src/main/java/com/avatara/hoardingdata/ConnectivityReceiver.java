package com.avatara.hoardingdata;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import java.io.IOException;

public class ConnectivityReceiver extends BroadcastReceiver {
    private static boolean isOnline = false;

    public ConnectivityReceiver() {

    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (isConnected(context) && canConnectOutside()) {
            isOnline = true;
            //NetworkService.startScheduler();
        } else {
            isOnline = false;
            //NetworkService.stopScheduler();
        }
    }

    public static boolean getIsOnline() {
        return isOnline;
    }

    public static boolean isConnected(Context context) {
        ConnectivityManager conn = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = conn.getActiveNetworkInfo();
        boolean connected =  networkInfo != null && networkInfo.isConnected();
        L.e(context, "ConnectivityReceiver.isConncted() = " + String.valueOf(connected));
        return connected;
    }

    public boolean canConnectOutside() {
        Runtime runtime = Runtime.getRuntime();
        try {
            Process ipProcess = runtime.exec("/system/bin/ping -c 1 8.8.8.8");
            int exitValue = ipProcess.waitFor();
            return exitValue == 0;
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return false;
    }
}