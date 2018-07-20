package com.wecare.app.data.entity;

public class QueryBusinessReq {
    private String device_id;

    private String type;

    private String app_key;

    public QueryBusinessReq() {
    }

    public QueryBusinessReq(String device_id, String type) {
        this.device_id = device_id;
        this.type = type;
    }

    public QueryBusinessReq(String device_id, String type, String app_key) {
        this.device_id = device_id;
        this.type = type;
        this.app_key = app_key;
    }

    public String getDevice_id() {
        return device_id;
    }

    public void setDevice_id(String device_id) {
        this.device_id = device_id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getApp_key() {
        return app_key;
    }

    public void setApp_key(String app_key) {
        this.app_key = app_key;
    }
}
