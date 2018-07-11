package com.wecare.app;

import com.wecare.app.module.BaseApplication;
import com.wecare.app.util.CrashHandler;
import com.wecare.app.util.DeviceUtils;
import com.wecare.app.util.Logger;

public class App extends BaseApplication {
    private static App mInstance;

    public String HOST = "sit.wecarelove.com";

    public int PORT = 2993;

    public String BASE_URL = "47.106.148.192";
    //心跳时间
    public long HEART_BEAT_RATE = 10 * 1000;
    //定位时间
    public long LOCATION_MIN_TIME = 2 * 1000;
    //最长gps上传时间（即：相同经纬度上传时间）
    public long SAME_GPS_UPLOAD_TIME = 8 * 1000;
    //最短gps上传时间
    public long MIN_GPS_UPLOAD_TIME = 5 * 1000;

    public String IMEI;

    public static App getInstance(){
        return mInstance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;
        Logger.initialize(this,true, Logger.Level.VERBOSE);
        CrashHandler.getInstance().init(this);
        IMEI = DeviceUtils.getDeviceIMEI(this);
//        IMEI = "358732036574479";
    }
}
