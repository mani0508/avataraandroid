package com.avatara.hoardingdata;

import com.avatara.hoardingdata.database.GpsLog;
import com.avatara.hoardingdata.database.GpsLogDbHelper;
import com.avatara.hoardingdata.requests.GpsBulkDataRequest;
import android.content.ContextWrapper;
import android.util.Log;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by shobhit on 23/6/16.
 */
public class SendLogsRunnable implements Runnable {
    private ContextWrapper contextWrapper;

    public SendLogsRunnable(ContextWrapper contextWrapper) {
        this.contextWrapper = contextWrapper;
    }

    @Override
    public void run() {
        sendGpsLogsBulk();
    }

    private void sendGpsLogsBulk() {
        if (!Aaho.isRegistered(contextWrapper)) return;

        GpsLogDbHelper logdb = LocationService.getLogDbHelper();

        List<GpsLog> logs = logdb.getAllGpsLogs();
        if (logs.size() == 0) return;  // no logs in the database, return

        JSONObject bulkJsonData = getBulkJsonData(logs);
        if (!bulkJsonData.has("logs") || !bulkJsonData.has("ids")) return;  // something is wrong quitely return

        if (!ConnectivityReceiver.isConnected(contextWrapper)) return;  // device has become offline, we'll try next time
        sendBulkData(bulkJsonData, false);
    }

    private JSONObject getBulkJsonData(List<GpsLog> logs) {
        List<JSONObject> logList = new ArrayList<>();
        List<Integer> ids = new ArrayList<>();
        for (GpsLog log : logs) {
            logList.add(log.getJsonData());
            ids.add(log.getId());
        }
        return _createRequestJson(logList, ids);
    }

    private JSONObject _createRequestJson(List<JSONObject> logList, List<Integer> ids) {
        JSONObject bulkJsonData = new JSONObject();
        try {
            bulkJsonData.put("logs", new JSONArray(logList));
            bulkJsonData.put("ids", new JSONArray(ids));
            bulkJsonData.put("count", ids.size());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return bulkJsonData;
    }

    private void sendBulkData(JSONObject gpsData, boolean debug) {
        Log.e("[myLogAaho] [action]", "sendBulkData()");
        if (debug) {
            Log.e("[SENDING DATA]", gpsData.toString());
            return;
        }
        LogResponseListener responseListener = new LogResponseListener();
        GpsBulkDataRequest gpsDataRequest = new GpsBulkDataRequest(contextWrapper, gpsData, responseListener, null);
        RequestQueue queue = Volley.newRequestQueue(LocationService.getInstance());
        queue.add(gpsDataRequest);
        Log.e("[myLogAaho] [action]", "sendBulkData() done");
    }

    private class LogResponseListener implements Response.Listener<JSONObject> {
        @Override
        public void onResponse(JSONObject response) {
            try {
                Log.e("response", response.toString());
                if (response.getString("status").equals("success")) {
                    int[] ids = jsonToIntArray(response.getJSONArray("ids"));
                    GpsLogDbHelper logdb = LocationService.getLogDbHelper();
                    logdb.deleteGpsLog(ids);
                } else {
                    throw new AssertionError(response.toString());
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public static int[] jsonToIntArray(JSONArray jsonArray) throws JSONException {
        int[] array = new int[jsonArray.length()];
        for (int i = 0; i < array.length; i++) {
            array[i] = jsonArray.getInt(i);
        }
        return array;
    }
}
