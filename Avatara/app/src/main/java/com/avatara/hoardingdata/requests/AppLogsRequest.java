package com.avatara.hoardingdata.requests;

import android.content.ContextWrapper;

import com.android.volley.Response;

import org.json.JSONObject;

/**
 * Created by mani on 31/5/16.
 */
public class AppLogsRequest extends AuthRequest {

    public AppLogsRequest(ContextWrapper contextWrapper, JSONObject jsonRequest, Response.Listener<JSONObject> listener, Response.ErrorListener errorListener) {
        super(contextWrapper, Method.POST, Api.APP_LOGS_URL, jsonRequest, listener, errorListener);
    }
}
