package com.avatara.hoardingdata;

import com.avatara.hoardingdata.requests.Api;
import com.avatara.hoardingdata.requests.EditDriverDetailsRequest;
import com.avatara.hoardingdata.requests.RegisterVehicleRequest;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

public class RegisterVehicleActivity extends AppCompatActivity {

    EditText etVehicleType;
    EditText etVehicleNumber;

    EditText etDriverName;
    EditText etMobileNumber;
    Button btRegister;

    boolean edit = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        edit = getIntent().getBooleanExtra("edit", false);

        setContentView(R.layout.activity_register_vehicle);
        setViewVariables();
        setSavedValues();
        btRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (edit) {
                    editDeviceDetails();
                } else {
                    registerDevice();
                }
            }
        });
    }

    public void setSavedValues() {
        SharedPreferences mSharedPreference = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        String vehicleNumber = mSharedPreference.getString(Aaho.VEHICLE_NUMBER_KEY, null);
        String vehicleType = mSharedPreference.getString(Aaho.VEHICLE_TYPE_KEY, null);
        String driverName = mSharedPreference.getString(Aaho.DRIVER_NAME_KEY, null);
        String driverNumber = mSharedPreference.getString(Aaho.MOBILE_NUMBER_KEY, null);

        if (vehicleNumber != null) etVehicleNumber.setText(vehicleNumber);
        if (vehicleType != null) etVehicleType.setText(vehicleType);
        if (driverName != null) etDriverName.setText(driverName);
        if (driverNumber != null) etMobileNumber.setText(driverNumber);
    }

    public void setViewVariables() {
        etVehicleNumber = (EditText) findViewById(R.id.input_veh_number);
        etVehicleType = (EditText) findViewById(R.id.input_vehicle_type);
        etDriverName = (EditText) findViewById(R.id.input_driver_name);
        etMobileNumber = (EditText) findViewById(R.id.input_driver_phone_number);
        btRegister = (Button) findViewById(R.id.register);
        if (edit) {
            btRegister.setText(R.string.edit_registration);
        }

    }
    private JSONObject getData(){
        setViewVariables();
        JSONObject jsonObject = new JSONObject();
        String vehicleNumber = etVehicleNumber.getText().toString();
        String vehicleType = etVehicleType.getText().toString();
        String driverName = etDriverName.getText().toString();
        String mobileNumber = etMobileNumber.getText().toString();
        Aaho.saveDriverDetails(this, vehicleNumber, vehicleType, driverName, mobileNumber);
        try {
            jsonObject.put("device_id", Aaho.getDeviceId(this));
            jsonObject.put("vehicle_number", vehicleNumber);
            jsonObject.put("vehicle_type", vehicleType);
            jsonObject.put("driver_name", driverName);
            jsonObject.put("driver_number", mobileNumber);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject;
    }


    private void registerDevice(){

        // Response received from the server
        Response.Listener<JSONObject> responseListener = new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    Log.e("response", response + "    from server");

                    if (response.getString("status").equals(Api.STATUS_SUCCESS)) {
                        Log.e("SUCCESS", Api.STATUS_SUCCESS);
                        String authToken = response.getString("auth_token");
                        Aaho.setAuthToken(RegisterVehicleActivity.this, authToken);
                        Toast.makeText(getApplicationContext(), R.string.register_success, Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        // TODO: implement retry in background
                        throw new AssertionError(response.toString());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                MainActivity.progress.dismiss();
            }
        };

        Response.ErrorListener errorListener = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
                MainActivity.progress.dismiss();
            }
        };

        RegisterVehicleRequest registerVehicleRequest = new RegisterVehicleRequest(getData(), responseListener, errorListener);
        RequestQueue queue = Volley.newRequestQueue(RegisterVehicleActivity.this);
        queue.add(registerVehicleRequest);
        MainActivity.progress.show();
    }

    private void editDeviceDetails(){

        // Response received from the server
        Response.Listener<JSONObject> responseListener = new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    Log.e("response", response + "    from server");

                    if (response.getString("status").equals(Api.STATUS_SUCCESS)) {
                        Log.e("SUCCESS", Api.STATUS_SUCCESS);
                        Toast.makeText(getApplicationContext(), R.string.edit_success, Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        throw new AssertionError(response.toString());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };

        EditDriverDetailsRequest editDriverDetailsRequest = new EditDriverDetailsRequest(this, getData(), responseListener, null);
        RequestQueue queue = Volley.newRequestQueue(RegisterVehicleActivity.this);
        queue.add(editDriverDetailsRequest);
    }

}
