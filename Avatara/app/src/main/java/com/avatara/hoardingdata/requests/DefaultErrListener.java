package com.avatara.hoardingdata.requests;

import com.android.volley.Response;
import com.android.volley.VolleyError;

/**
 * Created by shobhit on 2/7/16.
 */
public class DefaultErrListener implements Response.ErrorListener {

    @Override
    public void onErrorResponse(VolleyError error) {
        error.printStackTrace();
    }
}
