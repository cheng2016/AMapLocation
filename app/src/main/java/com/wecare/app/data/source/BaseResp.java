package com.wecare.app.data.source;

public class BaseResp<T> {

    /**
     * data : {"user_id":21}
     * info : {"code":"5007","title":"确认解除该设备？","info":"解除后，您将无法使用该设备，管理员权限将转移到顺位第一个用户"}
     */

    private BaseInfo info;

    private T data;


    public BaseResp() {
    }

    public BaseResp(BaseInfo info) {
        this.info = info;
    }

    public BaseResp(BaseInfo info, T data) {
        this.info = info;
        this.data = data;
    }

    public BaseInfo getInfo() {
        return info;
    }

    public void setInfo(BaseInfo info) {
        this.info = info;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
