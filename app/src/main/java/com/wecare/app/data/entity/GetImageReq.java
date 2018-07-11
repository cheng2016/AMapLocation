package com.wecare.app.data.entity;

public class GetImageReq {
    /**
     * device_id : 000000000000000
     */

    private String device_id;

    public GetImageReq() {
    }

    public GetImageReq(String device_id) {
        this.device_id = device_id;
    }

    public String getDevice_id() {
        return device_id;
    }

    public void setDevice_id(String device_id) {
        this.device_id = device_id;
    }
}
