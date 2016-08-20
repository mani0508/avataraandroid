package com.avatara.hoardingdata;

import com.avatara.hoardingdata.database.GpsLogDbHelper;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.v4.app.NotificationCompat;

import com.google.android.gms.location.LocationListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.TimeZone;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class LocationService extends Service {

    private static LocationService instance = null;
    private AlarmManager alarmManager;
    private static GpsLogDbHelper logdb;

    public static final int GPS_REQUEST_RATE = 2 * 60;
    public static final int NETWORK_REQUEST_RATE = 4 * 60;

    private static final String WAKE_LOCK_TAG = "AahoLocationServiceWakeLock";
    private PowerManager.WakeLock mWakeLock;

    public static final String STARTFOREGROUND_ACTION = "android.aaho.in.driver.action.startforeground";
    public static final String STOPFOREGROUND_ACTION = "android.aaho.in.driver.action.stopforeground";

    public static final int NOTIFICATION_ID = 101;

    public LocationService() {

    }

    public static GpsLogDbHelper getLogDbHelper() {
        return logdb;
    }

    private void aquireWakeLock() {
        L.e(this, "LocationService.aquireWakeLock()");
        PowerManager mgr = (PowerManager) getSystemService(Context.POWER_SERVICE);
        mWakeLock = mgr.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, WAKE_LOCK_TAG);
        mWakeLock.acquire();
    }

    private void releaseWakeLock() {
        L.e(this, "LocationService.releaseWakeLock()");
        if (mWakeLock != null) {
            mWakeLock.release();
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        L.e(this, "LocationService.onBind()");
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        L.e(this, "LocationService.onCreate()");

        instance = this;
        // order of these statements is important !!
        logdb = new GpsLogDbHelper(getApplicationContext());

        scheduleGPSLocation();
        scheduleSendLogs();
        setUpAlarmManager();

    }

    public void scheduleGPSLocation() {
        L.e(this, "LocationService.scheduleGPSLocation()");
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(new GpsListenerRunnable(), 0, GPS_REQUEST_RATE, TimeUnit.SECONDS);
    }

    public void scheduleSendLogs() {
        L.e(this, "LocationService.scheduleSendLogs()");
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(new SendLogsRunnable(getApplication()), 0, NETWORK_REQUEST_RATE, TimeUnit.SECONDS);
    }

    private void setUpAlarmManager() {
        L.e(this, "LocationService.setUpAlarmManager()");
        Intent intent = new Intent(getApplicationContext(), LocationService.class);
        final PendingIntent pIntent = PendingIntent.getBroadcast(this, LocationService.BIND_ABOVE_CLIENT,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);
        alarmManager = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
        alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(),
                AlarmManager.INTERVAL_FIFTEEN_MINUTES, pIntent);
    }

    private class GpsListenerRunnable implements Runnable {
        @Override
        public void run() {
            L.e(LocationService.this, "LocationService.GpsListenerRunnable.run()");
            GPSLocation.getInstance().connect(getApplicationContext(), new GpsLogListener());
        }
    }

    private class GpsLogListener implements LocationListener {
        @Override
        public void onLocationChanged(Location location) {
            L.e(LocationService.this, "LocationService.GpsLogListener.onLocationChanged()");
            try {
                String jsonString = getLocationJson(location).toString();
                L.e(LocationService.this, "LocationService.GpsLogListener.onLocationChanged() data = " + jsonString);
                if (Aaho.isRegistered(LocationService.this)) {
                    L.e(LocationService.this, "LocationService.GpsLogListener.onLocationChanged() Aaho.isRegistered = true");
                    logdb.addGpsLog(jsonString);  // write to db
                } else {
                    L.e(LocationService.this, "LocationService.GpsLogListener.onLocationChanged() Aaho.isRegistered = false");
                }
            } catch (JSONException e) {
                L.e(LocationService.this, "LocationService.GpsLogListener.onLocationChanged() JSONException!!");
                e.printStackTrace();
            }
        }
    }

    private JSONObject getLocationJson(Location location) throws JSONException {
        JSONObject jsonObject = new JSONObject();
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC+05:30"));
        jsonObject.put("lat", location.getLatitude());
        jsonObject.put("lng", location.getLongitude());
        jsonObject.put("datetime", calendar.getTimeInMillis());
        jsonObject.put("speed", location.getSpeed());
        jsonObject.put("device_id", Aaho.getDeviceId(this));
        return jsonObject;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        L.e(this, "LocationService.onStartCommand(), Received startId = " + startId + ": " + intent);
        aquireWakeLock();

        if (intent == null || intent.getAction() == null || intent.getAction().equals(STARTFOREGROUND_ACTION)) {
            startLocationServiceForeground();
        } else if (intent.getAction().equals(STOPFOREGROUND_ACTION)) {
            stopLocationServiceForeground();
        }
        return START_STICKY;
    }

    private void startLocationServiceForeground() {
        L.e(this, "LocationService.startLocationServiceForeground()");

        Intent foregroundIntent = new Intent(getApplicationContext(), MainActivity.class);
        foregroundIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent contentIntent = PendingIntent.getActivity(getApplicationContext(), 0, foregroundIntent, 0);

        Notification notification = getForegroundNotification(contentIntent);

        startForeground(NOTIFICATION_ID, notification);
    }

    private Notification getForegroundNotification(PendingIntent contentIntent) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext());
//        builder.setSmallIcon(R.drawable.location);  // icon id of the image
        builder.setContentTitle("Aaho Driver")
                .setContentText("Collecting location data")
                .setContentInfo("Aaho Driver");
        builder.setContentIntent(contentIntent);
        return builder.build();
    }

    private void stopLocationServiceForeground() {
        L.e(this, "LocationService.stopLocationServiceForeground()");
        GPSLocation.getInstance().disconnect();
        stopForeground(true);
        stopSelf();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        releaseWakeLock();
        L.e(this, "LocationService.onDestroy()");
    }

    public static LocationService getInstance() {
        return instance;
    }
}