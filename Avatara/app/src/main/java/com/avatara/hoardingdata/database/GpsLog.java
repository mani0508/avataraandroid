package com.avatara.hoardingdata.database;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

/**
 * Created by shobhit on 22/6/16.
 */
public class GpsLog {

    private int id;
    private Date created;
    private String data;

    public GpsLog() {

    }

    public GpsLog(int id, Date created, String data) {
        this.id = id;
        this.created = created;
        this.data = data;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public void setData(String data) {
        this.data = data;
    }

    public int getId() {
        return id;
    }

    public Date getCreated() {
        return created;
    }

    public String getData() {
        return data;
    }

    public JSONObject getJsonData() {
        JSONObject jsonData;
        try {
            jsonData = new JSONObject(data);
        } catch (JSONException e) {
            e.printStackTrace();
            jsonData = new JSONObject();
        }
        try {
            jsonData.put("id", id);
        } catch (JSONException e) {
        }
        return jsonData;
    }

}
