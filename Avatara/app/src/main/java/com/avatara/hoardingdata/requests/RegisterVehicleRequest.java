package com.avatara.hoardingdata.requests;

import com.android.volley.Request;
import com.android.volley.Response;

import org.json.JSONObject;

/**
 * Created by mani on 31/5/16.
 */
public class RegisterVehicleRequest extends ApiRequest {

    public RegisterVehicleRequest(JSONObject jsonRequest, Response.Listener<JSONObject> listener, Response.ErrorListener errorListener) {
        super(Request.Method.POST, Api.DRIVER_REGISTER_URL, jsonRequest, listener, errorListener);
    }
}
