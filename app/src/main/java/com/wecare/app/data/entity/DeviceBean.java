package com.wecare.app.data.entity;

public class DeviceBean {
    private String name;
    private String value;

    public DeviceBean() {
    }

    public DeviceBean(String name, String value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
