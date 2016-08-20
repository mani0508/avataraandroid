package com.avatara.hoardingdata;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;

public class BootReceiver extends BroadcastReceiver {
    public static final String TAG = "boot_broadcast_receiver";
    public BootReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        L.e(context, "BootReceiver.onReceive() called");
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG);
        wl.acquire();
        Aaho.startAppServices(context);
        wl.release();
    }
}
