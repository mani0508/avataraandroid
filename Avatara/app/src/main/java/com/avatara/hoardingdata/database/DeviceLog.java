package com.avatara.hoardingdata.database;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

/**
 * Created by shobhit on 22/6/16.
 */
public class DeviceLog {

    private int id;
    private Date datetime;
    private String log;

    public DeviceLog() {

    }

    public DeviceLog(int id, Date datetime, String log) {
        this.id = id;
        this.datetime = datetime;
        this.log = log;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setDatetime(Date datetime) {
        this.datetime = datetime;
    }

    public void setLog(String log) {
        this.log = log;
    }

    public int getId() {
        return id;
    }

    public Date getDatetime() {
        return datetime;
    }

    public String getLog() {
        return log;
    }

    // TODO: remove later
    public JSONObject getLegecyJsonData() {
        JSONObject jsonData = new JSONObject();
        try {
            jsonData.put("datetime", datetime.getTime());
            jsonData.put("log", log);
        } catch (JSONException e) {
        }
        return jsonData;
    }

    public JSONObject getJsonData() {
        JSONObject jsonData;
        try {
            jsonData = new JSONObject(log);
        } catch (JSONException e) {
            e.printStackTrace();
            jsonData = getLegecyJsonData();
        }
        try {
            jsonData.put("id", id);
        } catch (JSONException e) {
        }
        return jsonData;
    }

}
