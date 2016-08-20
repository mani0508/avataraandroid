package com.avatara.hoardingdata;

import android.app.Application;

import org.acra.ACRA;
import org.acra.annotation.ReportsCrashes;
import org.acra.sender.HttpSender;

/**
 * Created by shobhit on 2/7/16.
 */

@ReportsCrashes(
        httpMethod = HttpSender.Method.PUT,
        reportType = HttpSender.Type.JSON,
        formUri = "http://aaho.in:5984/acra-aaho/_design/acra-storage/_update/report",
        formUriBasicAuthLogin = "shobhit",
        formUriBasicAuthPassword = "optimus"
)
public class DriverApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        ACRA.init(this);
    }
}