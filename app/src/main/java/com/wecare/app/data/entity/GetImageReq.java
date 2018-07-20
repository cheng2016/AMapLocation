package com.wecare.app.data.entity;

public class GetImageReq {
    /**
     * device_id : 000000000000000
     */

    private String device_id;

    private String app_key;

    public GetImageReq() {
    }

    public GetImageReq(String device_id) {
        this.device_id = device_id;
    }

    public GetImageReq(String device_id, String app_key) {
        this.device_id = device_id;
        this.app_key = app_key;
    }

    public String getDevice_id() {
        return device_id;
    }

    public void setDevice_id(String device_id) {
        this.device_id = device_id;
    }

    public String getApp_key() {
        return app_key;
    }

    public void setApp_key(String app_key) {
        this.app_key = app_key;
    }
}
