package com.avatara.hoardingdata;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by shobhit on 21/6/16.
 *
 * Abstract activity for pemission related methods, subclassed by MainActivity
 */

public class PermissionManagerActivity extends AppCompatActivity {
    private static final int PLAY_SERVICES_REQUEST = 1098;
    public static final int GPS_SETTINGS_REQUEST = 1099;

    // callback flag
    public final int PERM_CALLBACK = 102;

    @Override
    protected void onStart() {
        super.onStart();
        boolean hasPlayServices = checkPlayServices();
        if (hasPlayServices) {
            checkPermsAndRequest();
            checkGPSSettings();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case PERM_CALLBACK: checkPermsAndAlert();
        }
    }

    private void checkGPSSettings() {
        if (!Aaho.isRegistered(this) || !hasAllPerms()) {
            return;
        }
        LocationSettingHelper.locationSettingRequest(this);
    }

    private boolean checkPlayServices() {
        GoogleApiAvailability googleApiAvailability = GoogleApiAvailability.getInstance();
        int status = googleApiAvailability.isGooglePlayServicesAvailable(this);
        if (status == ConnectionResult.SUCCESS) {
            return true;
        }
        if(googleApiAvailability.isUserResolvableError(status)) {
            googleApiAvailability.getErrorDialog(this, status, PLAY_SERVICES_REQUEST,
                    new PlayServicesCancelListener()).show();
        }
        return false;
    }

    private class PlayServicesCancelListener implements DialogInterface.OnCancelListener {
        @Override
        public void onCancel(DialogInterface dialog) {
            showPlayServicesAlertDialog();
        }
    }

    private void showPlayServicesAlertDialog() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.play_services)
                .setMessage(R.string.play_services_msg)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        checkPlayServices();
                    }
                })
                .setNegativeButton(R.string.quit, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        stopServicesAndQuitApp();
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    private void showGPSSettingAlertDialog() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.gps_request_title)
                .setMessage(R.string.gps_request_msg)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        checkGPSSettings();
                    }
                })
                .setNegativeButton(R.string.quit, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        stopServicesAndQuitApp();
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    private boolean getLocationPerm() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private boolean getWritePerm() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }

    private boolean getPhonePerm() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED;
    }

    private boolean hasAllPerms() {
        return getLocationPerm() && getWritePerm() && getPhonePerm();
    }

    private void checkPermsAndAlert() {
        showPermAlertDialog(getLocationPerm(), getWritePerm(), getPhonePerm());
    }

    private void checkPermsAndRequest() {
        // check and request user for required permissions
        requestPerms(getLocationPerm(), getWritePerm(), getPhonePerm());
    }

    private void launchRegisterActivityIfRequired() {
        if(!Aaho.isRegistered(this)) {
            startActivity(new Intent(PermissionManagerActivity.this, RegisterVehicleActivity.class));
        }
    }

    private void requestPerms(boolean hasLocationPerm, boolean hasWritePerm, boolean hasPhonePerm) {
        // show the request permission dialog for permissions not granted yet
        if (hasLocationPerm && hasWritePerm && hasPhonePerm) {
            launchRegisterActivityIfRequired();
            return;
        }
        String[] missingPerms = getMissingPerms(hasLocationPerm, hasWritePerm, hasPhonePerm);
        ActivityCompat.requestPermissions(this, missingPerms, PERM_CALLBACK);
    }

    private String[] getMissingPerms(boolean hasLocationPerm, boolean hasWritePerm, boolean hasPhonePerm) {
        List<String> misPerms = new ArrayList<>();
        if (!hasLocationPerm) misPerms.add(Manifest.permission.ACCESS_FINE_LOCATION);
        if (!hasWritePerm) misPerms.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (!hasPhonePerm) misPerms.add(Manifest.permission.READ_PHONE_STATE);
        return misPerms.toArray(new String[misPerms.size()]);
    }

    private void showPermAlertDialog(final boolean hasLocationPerm, final boolean hasWritePerm, final boolean hasPhonePerm) {
        if (hasLocationPerm && hasWritePerm && hasPhonePerm) {
            launchRegisterActivityIfRequired();
            return; // do nothing if we have both perms
        }
        new AlertDialog.Builder(this)
                .setTitle(R.string.perm_required)
                .setMessage(R.string.perms_request_msg)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        requestPerms(hasLocationPerm, hasWritePerm, hasPhonePerm);
                    }
                })
                .setNegativeButton(R.string.quit, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        stopServicesAndQuitApp();
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    private void stopServicesAndQuitApp() {
        // no point in keeping services running if we do not hae the perms
        Intent locIntent = new Intent(getApplicationContext(), LocationService.class);
        locIntent.setAction(LocationService.STOPFOREGROUND_ACTION);
        startService(locIntent);

        finish();
        System.exit(0);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            // Check for the integer request code originally supplied to startResolutionForResult().
            case GPS_SETTINGS_REQUEST:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        break;
                    case Activity.RESULT_CANCELED:
                        showGPSSettingAlertDialog();  // keep asking
                        break;
                }
                break;
            case PLAY_SERVICES_REQUEST:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        break;
                    case Activity.RESULT_CANCELED:
                        showPlayServicesAlertDialog();  // keep asking
                        break;
                }
                break;
        }
    }
}
