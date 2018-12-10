package com.wecare.app.data.source;

public class BaseInfo {
    /**
     * code : 200
     * title：
     * info : 成功
     */

    private String code;

    private String info;

    public BaseInfo() {
    }

    public BaseInfo(String code, String info) {
        this.code = code;
        this.info = info;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }


    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

}
