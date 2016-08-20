package com.avatara.hoardingdata;

import com.avatara.hoardingdata.database.DeviceLog;
import com.avatara.hoardingdata.requests.AppLogsRequest;
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
 * Created by shobhit on 12/7/16.
 */
public class AppLogHelper {

    private ContextWrapper contextWrapper;

    public AppLogHelper(ContextWrapper contextWrapper) {
        this.contextWrapper = contextWrapper;
    }

    public void sendAppLogsBulk() {
        if (!Aaho.isRegistered(contextWrapper)) return;

        List<DeviceLog> logs = L.logs(contextWrapper);
        if (logs.size() == 0) return;  // no logs in the database, return

        JSONObject bulkJsonData = getBulkJsonData(logs);
        if (!bulkJsonData.has("logs")) return;  // something is wrong quitely return

        if (!ConnectivityReceiver.isConnected(contextWrapper)) return;  // device has become offline, we'll try next time
        sendBulkData(bulkJsonData);
    }

    private JSONObject getBulkJsonData(List<DeviceLog> logs) {
        List<JSONObject> logList = new ArrayList<>();
        for (DeviceLog log : logs) {
            logList.add(log.getJsonData());
        }
        return _createRequestJson(logList);
    }

    private JSONObject _createRequestJson(List<JSONObject> logList) {
        JSONObject bulkJsonData = new JSONObject();
        try {
            bulkJsonData.put("logs", new JSONArray(logList));
            bulkJsonData.put("count", logList.size());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return bulkJsonData;
    }

    private void sendBulkData(JSONObject gpsData) {
        LogResponseListener responseListener = new LogResponseListener();
        AppLogsRequest logDataRequest = new AppLogsRequest(contextWrapper, gpsData, responseListener, null);
        RequestQueue queue = Volley.newRequestQueue(LocationService.getInstance());
        queue.add(logDataRequest);
        Log.e("[myLogAaho] [action]", "sendBulkData() AppLogs done");
    }

    private class LogResponseListener implements Response.Listener<JSONObject> {
        @Override
        public void onResponse(JSONObject response) {
            try {
                Log.e("response", response.toString());
                if (response.getString("status").equals("success")) {
                    L.deleteAllLogs(contextWrapper);
                } else {
                    throw new AssertionError(response.toString());
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }


}
