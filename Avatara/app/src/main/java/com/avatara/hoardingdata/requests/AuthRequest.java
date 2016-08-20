package com.avatara.hoardingdata.requests;

import com.avatara.hoardingdata.Aaho;
import android.content.ContextWrapper;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by shobhit on 2/7/16.
 */
public class AuthRequest extends ApiRequest {
    private ContextWrapper contextWrapper;

    public AuthRequest(ContextWrapper contextWrapper, int method, String url, JSONObject jsonRequest, Response.Listener<JSONObject> listener, Response.ErrorListener errorListener) {
        super(method, url, jsonRequest, listener, errorListener);
        this.contextWrapper = contextWrapper;
    }

    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", "Token " + Aaho.getAuthToken(contextWrapper));
        return headers;
    }
}
