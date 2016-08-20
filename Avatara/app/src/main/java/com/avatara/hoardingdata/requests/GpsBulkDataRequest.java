package com.avatara.hoardingdata.requests;

/**
 * Created by mani on 17/5/16.
 */

import android.content.ContextWrapper;

import com.android.volley.Response;

import org.json.JSONObject;


public class GpsBulkDataRequest extends AuthRequest {

    public GpsBulkDataRequest(ContextWrapper contextWrapper, JSONObject jsonRequest, Response.Listener<JSONObject> listener, Response.ErrorListener errorListener) {
        super(contextWrapper, Method.POST, Api.LOCATION_UPDATE_URL, jsonRequest, listener, errorListener);
    }

    @Override
    protected int getTimeout() {
        return 20 * 1000;  // large timeout
    }

    @Override
    protected int getRetries() {
        return 0;  // no retries prevent duplicate requests
    }
}