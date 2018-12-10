package com.wecare.app;

import android.os.Environment;
import android.text.TextUtils;

import com.squareup.okhttp.Cache;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.picasso.LruCache;
import com.squareup.picasso.OkHttpDownloader;
import com.squareup.picasso.Picasso;
import com.wecare.app.module.BaseApplication;
import com.wecare.app.util.CrashHandler;
import com.wecare.app.util.DeviceUtils;
import com.wecare.app.util.Logger;
import com.wecare.app.util.PreferenceConstants;
import com.wecare.app.util.PreferenceUtils;
import com.wecare.app.util.ToastUtils;

import java.io.File;

public class App extends BaseApplication {
    public final static String TAG = "App";
    private static App mInstance;

    public String HOST = "sit.wecarelove.com";

    public int PORT = 2993;

    public String BASE_URL = "47.106.148.192";
    //心跳时间
    public long HEART_BEAT_RATE = 5 * 60 * 1000;
    //定位时间
    public long LOCATION_MIN_TIME = 2 * 1000;
    //最长gps上传时间（即：相同经纬度上传时间）
    public long SAME_GPS_UPLOAD_TIME = 60 * 1000;
    //最短gps上传时间
    public long MIN_GPS_UPLOAD_TIME = 15 * 1000;

    public float LEVEL_1 = 1.11f;

    public long PARKING_TIME = 2 * 60 * 1000;

    private String appCacheDir;

    private String imageCacheDir;

    private File mRootDir;

    public static App getInstance() {
        return mInstance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;
        ToastUtils.initialize(this, false);
        CrashHandler.getInstance().init(this);
        initPicasoConfig();
        PreferenceUtils.setPrefString(this, PreferenceConstants.IMEI, DeviceUtils.getDeviceIMEI(this));
    }

    private void initPicasoConfig() {
        if (DeviceUtils.isSDCardEnable()) {
            appCacheDir = Environment.getExternalStorageDirectory() + "/wecare/";
        } else {
            appCacheDir = getCacheDir().getAbsolutePath() + "/wecare/";
        }
        File directory = new File(appCacheDir);
        if (!directory.exists()) {
            directory.mkdirs();
        }
        PreferenceUtils.setPrefString(this,PreferenceConstants.APP_CACHE_DIR,appCacheDir);
        imageCacheDir = appCacheDir + "image/";
        File file = new File(imageCacheDir);
        if (!file.exists()) {
            file.mkdirs();
        }
        PreferenceUtils.setPrefString(this, PreferenceConstants.IMAGE_CACHE_DIR, imageCacheDir);
        //设置图片内存缓存大小为运行时内存的八分之一
        long l = Runtime.getRuntime().maxMemory();
        OkHttpClient client = new OkHttpClient();
        client.setCache(new Cache(file, l / 8));
        Picasso picasso = new Picasso.Builder(this)
                .memoryCache(new LruCache((int) (l / 8)))
                .downloader(new OkHttpDownloader(client))
                .loggingEnabled(false)//picasso log日志
                .build();
        Picasso.setSingletonInstance(picasso);
        //打印日志
//        Picasso.with(this).setLoggingEnabled(true);
        //缓存指示器，查看图片来源于何处；这样每张图片在显示的时候，左上角都会有一个小标记，分别是蓝色、绿色、红色三种颜色。
//        Picasso.with(this).setIndicatorsEnabled(true);
    }


    public String getAppCacheDir() {
        if(TextUtils.isEmpty(appCacheDir)){
            Logger.i(TAG, "getAppCacheDir：" + appCacheDir);
            appCacheDir = PreferenceUtils.getPrefString(this,PreferenceConstants.APP_CACHE_DIR,"");
        }
        return appCacheDir;
    }
}
