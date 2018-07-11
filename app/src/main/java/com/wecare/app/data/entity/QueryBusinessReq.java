package com.wecare.app.data.entity;

public class QueryBusinessReq {
    private String device_id;

    private String type;

    public QueryBusinessReq() {
    }

    public QueryBusinessReq(String device_id, String type) {
        this.device_id = device_id;
        this.type = type;
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
}
