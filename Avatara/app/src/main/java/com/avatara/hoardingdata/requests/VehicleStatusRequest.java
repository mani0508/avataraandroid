package com.avatara.hoardingdata.requests;

import android.content.ContextWrapper;

import com.android.volley.Response;

import org.json.JSONObject;

/**
 * Created by mani on 17/5/16.
 */

public class VehicleStatusRequest extends AuthRequest {

    public VehicleStatusRequest(ContextWrapper contextWrapper, JSONObject jsonRequest, Response.Listener<JSONObject> listener, Response.ErrorListener errorListener) {
        super(contextWrapper, Method.POST, Api.VEHICLE_STATUS_URL, jsonRequest, listener, errorListener);
    }
}