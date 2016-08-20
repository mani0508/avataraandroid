    package com.avatara.hoardingdata.requests;

import android.content.ContextWrapper;

import com.android.volley.Response;

import org.json.JSONObject;

/**
 * Created by mani on 31/5/16.
 */
public class EditDriverDetailsRequest extends AuthRequest {

    public EditDriverDetailsRequest(ContextWrapper contextWrapper, JSONObject jsonRequest, Response.Listener<JSONObject> listener, Response.ErrorListener errorListener) {
        super(contextWrapper, Method.POST, Api.DRIVER_DETAIL_EDIT_URL, jsonRequest, listener, errorListener);
    }
}
