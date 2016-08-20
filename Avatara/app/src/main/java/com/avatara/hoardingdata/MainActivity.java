package com.avatara.hoardingdata;

import com.avatara.hoardingdata.requests.Api;
import com.avatara.hoardingdata.requests.VehicleStatusRequest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;


public class MainActivity extends PermissionManagerActivity {

    public static final String STATUS_UNLOADED = "unloaded";
    public static final String STATUS_LOADING = "loading";
    public static final String STATUS_LOADED = "loaded";
    public static final String STATUS_UNLOADING = "unloading";

    public static final float BTN_DISABLED_ALPHA = 0.5f;
    public static final float BTN_ENABLED_ALPHA = 1.0f;

    public static ProgressDialog progress;

    private ImageButton btnStatus;
    private ImageButton btnUnloaded;
    private ImageButton btnLoading;
    private ImageButton btnLoaded;
    private ImageButton btnUnloading;
    private TextView txtVehicleStatus;

    private void setVehicleStatus(String status) {
        Aaho.setVehicleStatus(this, status);
        setVehicleStatusUI(status);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        L.e(this, "MainActivity.onCreate()");
        setContentView(R.layout.activity_main);
        setUpProgressDialog();

        Aaho.configureDeviceId(this);
        Aaho.startAppServices(MainActivity.this);

        setViewRefs();
        setClickListeners();
    }

    private void setClickListeners() {
        btnStatus.setOnClickListener(new StatusCycleOnClickListener());
        btnUnloaded.setOnClickListener(new StatusOnClickListener(STATUS_UNLOADED));
        btnLoading.setOnClickListener(new StatusOnClickListener(STATUS_LOADING));
        btnLoaded.setOnClickListener(new StatusOnClickListener(STATUS_LOADED));
        btnUnloading.setOnClickListener(new StatusOnClickListener(STATUS_UNLOADING));
    }

    private void setViewRefs() {

        txtVehicleStatus = (TextView) findViewById(R.id.vehicle_status);
    }


    private void setUpProgressDialog() {
        progress = new ProgressDialog(this);
        progress.setTitle(R.string.progress_title);
        progress.setMessage(getString(R.string.progress_msg));
    }

    private void setStatusBtnEnabledUI(ImageButton btn) {
        btn.setAlpha(BTN_ENABLED_ALPHA);
        btn.setBackgroundResource(R.color.truck_btn_enabled);
    }

    private void setStatusBtnDisabledUI(ImageButton btn) {
        btn.setAlpha(BTN_DISABLED_ALPHA);
        btn.setBackgroundResource(R.color.truck_btn_disabled);
    }
//
//    private static int vehicleStatusImage(String status) {
//        if (status.equals(STATUS_UNLOADED)) return R.drawable.unloaded;
//        if (status.equals(STATUS_LOADING)) return R.drawable.loading;
//        if (status.equals(STATUS_LOADED)) return R.drawable.loaded;
//        if (status.equals(STATUS_UNLOADING)) return R.drawable.unloading;
//        return R.drawable.unloaded;
//    }

    private static int vehicleStatusString(String status) {
        if (status.equals(STATUS_UNLOADED)) return R.string.unloaded;
        if (status.equals(STATUS_LOADING)) return R.string.loading;
        if (status.equals(STATUS_LOADED)) return R.string.loaded;
        if (status.equals(STATUS_UNLOADING)) return R.string.unloading;
        return R.string.unloaded;
    }

    private ImageButton vehicleStatusBtn(String status) {
        if (status.equals(STATUS_UNLOADED)) return btnUnloaded;
        if (status.equals(STATUS_LOADING)) return btnLoading;
        if (status.equals(STATUS_LOADED)) return btnLoaded;
        if (status.equals(STATUS_UNLOADING)) return btnUnloading;
        return btnUnloaded;
    }

    private void setStatusBtnUI(String status) {
        modifyBtnUI(status, STATUS_LOADED);
        modifyBtnUI(status, STATUS_LOADING);
        modifyBtnUI(status, STATUS_UNLOADED);
        modifyBtnUI(status, STATUS_UNLOADING);
    }

    private void modifyBtnUI(String status, String statusOfBtn) {
        ImageButton btn = vehicleStatusBtn(statusOfBtn);
        if (status.equals(statusOfBtn)) {
            setStatusBtnEnabledUI(btn);
        } else {
            setStatusBtnDisabledUI(btn);
        }
    }

    private void setVehicleStatusUI(String status) {
//        btnStatus.setImageResource(vehicleStatusImage(status));
        txtVehicleStatus.setText(getString(vehicleStatusString(status)));
        setStatusBtnUI(status);
    }

    private void showStatusChangeConfirmDialog(final String newStatus) {
        String msg = String.format(getString(R.string.action_status_change), newStatus);
        new AlertDialog.Builder(this)
                .setTitle(R.string.confirm_action)
                .setMessage(msg)
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        changeStatusOfTruck(newStatus);
                    }
                })
                .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                       // do nothing
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    private JSONObject jsonStatusData(String status){
        JSONObject data = new JSONObject();
        try {
            data.put("vehicle_status", status);  // TODO: define in Api
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return data;
    }

    private void changeStatusOfTruck(final String newStatus){
        if (!Aaho.isRegistered(this)) return;
        // Response received from the server
        Response.Listener<JSONObject> responseListener = new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    Log.e("response", response + "    from server");

                    if (response.getString("status").equals(Api.STATUS_SUCCESS)) {
                        Log.e("SUCCESS", Api.STATUS_SUCCESS);
                        setVehicleStatus(newStatus);
                    } else {
                        throw new AssertionError(response.toString());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                progress.dismiss();
            }
        };

        Response.ErrorListener errorListener = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
                progress.dismiss();
            }
        };

        VehicleStatusRequest statusChangeRequest = new VehicleStatusRequest(this, jsonStatusData(newStatus), responseListener, errorListener);
        RequestQueue queue = Volley.newRequestQueue(MainActivity.this);
        queue.add(statusChangeRequest);
        progress.show();
    }

    public static String nextStatus(String status) {
        if (status.equals(STATUS_UNLOADED)) return STATUS_LOADING;
        if (status.equals(STATUS_LOADING)) return STATUS_LOADED;
        if (status.equals(STATUS_LOADED)) return STATUS_UNLOADING;
        if (status.equals(STATUS_UNLOADING)) return STATUS_UNLOADED;
        return STATUS_UNLOADED;
    }


    private class StatusOnClickListener implements View.OnClickListener {
        private String newStatus = null;
        public StatusOnClickListener(String newStatus) {
            if (newStatus == null) throw new AssertionError("newStatus is null, this should not happen");
            this.newStatus = newStatus;
        }

        @Override
        public void onClick(View v) {
            String currStatus = Aaho.getVehicleStatus(MainActivity.this);
            if (!newStatus.equals(currStatus)) showStatusChangeConfirmDialog(newStatus);
        }
    }

    private class StatusCycleOnClickListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            String currStatus = Aaho.getVehicleStatus(MainActivity.this);
            String nextStatus = nextStatus(currStatus);
            showStatusChangeConfirmDialog(nextStatus);
        }
    }


//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        MenuInflater inflater = getMenuInflater();
//        inflater.inflate(R.menu.app_menu, menu);
//        return true;
//    }

//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        // Handle item selection
//        switch (item.getItemId()) {
//            case R.id.menu_edit_registration_details:
//                launchRegistrationEditActivity();
//                return true;
//            case R.id.menu_call_us:
//                dialAppSupportNumber();
//                return true;
//            case R.id.menu_send_action_logs:
//                sendAppLogs();
//                return true;
//            default:
//                return super.onOptionsItemSelected(item);
//        }
//    }

    private void sendAppLogs() {
        new AppLogHelper(this).sendAppLogsBulk();
    }

    private void launchRegistrationEditActivity() {
        Intent registerIntent = new Intent(MainActivity.this, RegisterVehicleActivity.class);
        registerIntent.putExtra("edit", true);
        startActivity(registerIntent);
    }

    private void dialAppSupportNumber() {
        Intent dialIntent = new Intent(Intent.ACTION_DIAL);
        dialIntent.setData(Uri.parse("tel:" + Aaho.APP_SUPPORT_NUMBER));
        startActivity(dialIntent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        L.e(this, "MainActivity.onDestroy()");
        Aaho.configureDeviceId(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        L.e(this, "MainActivity.onPause()");
        Aaho.configureDeviceId(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        L.e(this, "MainActivity.onStop()");
        Aaho.configureDeviceId(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        setVehicleStatusUI(Aaho.getVehicleStatus(this));
        L.e(this, "MainActivity.onStart()");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        L.e(this, "MainActivity.onRestart()");
    }

    @Override
    protected void onResume() {
        super.onResume();
        L.e(this, "MainActivity.onResume()");
    }
}