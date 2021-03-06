package com.avatara.hoardingdata.requests;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONObject;

/**
 * Created by shobhit on 3/7/16.
 */
public class ApiRequest extends JsonObjectRequest {

    // attempt 1: socket_timeout = timeout + timeout * backoff = 4 + 4 * 2 = 12s, timeout = socket_timeout
    // attempt 2: socket_timeout = timeout + timeout * backoff = 12 + 12 * 2 = 36s, and so on

    public ApiRequest(int method, String url, JSONObject jsonRequest, Response.Listener<JSONObject> listener, Response.ErrorListener errorListener) {
        super(method, url, jsonRequest, listener, errorListener != null ? errorListener : new DefaultErrListener());
        this.setRetryPolicy(new DefaultRetryPolicy(this.getTimeout(), this.getRetries(), this.getBackoff()));
    }

    protected int getTimeout() {
        return 4 * 1000;  // 4 seconds
    }

    protected int getRetries() {
        return 1;  // retry once
    }

    protected float getBackoff() {
        return 2.0f;
    }
}

