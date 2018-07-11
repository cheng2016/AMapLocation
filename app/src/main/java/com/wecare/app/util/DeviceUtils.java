package com.wecare.app.util;

import android.Manifest;
import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.telephony.TelephonyManager;
import android.util.Log;

import java.io.File;
import java.text.DecimalFormat;

/**
 * Created by chengzj on 2018/6/21.
 */
public class DeviceUtils {
    public static final String TAG = "DeviceUtils";

    /**
     * 获取当前App版本名称
     * @param context
     * @return
     */
    public String getAppVersionName(Context context) {
        String versionName = "";
        int versionCode = 0;
        try {
            // ---get the package info---
            PackageManager pm = context.getPackageManager();
            PackageInfo pi = pm.getPackageInfo(context.getPackageName(), 0);
            versionName = pi.versionName;
            versionCode = pi.versionCode;
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (versionName == null || versionName.length() <= 0) {
            versionName = "";
        }

        return versionName;
    }

    /**
     * 获取当前App版本code
     * @param context
     * @return
     */
    public int getAppVersionCode(Context context) {
        int versionCode = 0;
        try {
            // ---get the package info---
            PackageManager pm = context.getPackageManager();
            PackageInfo pi = pm.getPackageInfo(context.getPackageName(), 0);
            versionCode = pi.versionCode;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return versionCode;
    }


    /**
    * 外部存储(SDCard)是否可用
    * @return
    */
    public static boolean externalMemoryAvailable() {
        return android.os.Environment.getExternalStorageState().equals(
                android.os.Environment.MEDIA_MOUNTED);
    }

    /**
     * 获取设备MAC地址
     * 需添加权限<uses-permission android:name="android.permission.ACCESS_WIFI_STATE"/>
     */
    public static String getMacAddress(Context context) {
        String macAddress = null;
        WifiManager wifi = (WifiManager) context
                .getSystemService(Context.WIFI_SERVICE);
        WifiInfo info = wifi.getConnectionInfo();
        if (null != info) {
            macAddress = info.getMacAddress();
            if (null != macAddress) {
                macAddress = macAddress.replace(":", "");
            }
        }
        return macAddress;
    }

    /**
     * 获取设备厂商，如Xiaomi
     */
    public static String getManufacturer() {
        String MANUFACTURER = Build.MANUFACTURER;
        return MANUFACTURER;
    }

    /**
     * 获取设备型号，如MI2SC
     * 即 型号
     */
    public static String getModel() {
        String model = Build.MODEL;
        if (model != null) {
            model = model.trim().replaceAll("\\s*", "");
        } else {
            model = "";
        }
        return model;
    }

    /**
     * 获取设备SD卡是否可用
     */
    public static boolean isSDCardEnable() {
        return Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState());
    }

    /**
     * 获取设备SD卡路径
     */
    public static String getSDCardPath() {
        return Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator;
    }

    /**
     * 判断设备是否是手机
     */
    public static boolean isPhone(Context context) {
        TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        return tm.getPhoneType() != TelephonyManager.PHONE_TYPE_NONE;
    }

    /**
     * 获取当前设备的IMIE，需与上面的isPhone一起使用
     * 需添加权限<uses-permission android:name="android.permission.READ_PHONE_STATE"/>
     */
    public static String getDeviceIMEI(Context context) {
        String deviceId;
        if (isPhone(context)) {
            TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                Log.i(TAG, "getDeviceIMEI need requestPermissions");
                return "";
            }
            deviceId = tm.getDeviceId();
        } else {
            deviceId = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
        }
        return deviceId;
    }

    /**
     * 获取系统内存大小
     * @return
     */
    public static String getSysteTotalMemorySize(Context context){
        //获得ActivityManager服务的对象
        ActivityManager mActivityManager = (ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);
        //获得MemoryInfo对象
        ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo() ;
        //获得系统可用内存，保存在MemoryInfo对象上
        mActivityManager.getMemoryInfo(memoryInfo) ;
        long memSize = memoryInfo.totalMem ;
        //字符类型转换
        String availMemStr = formateFileSize(memSize);
        return availMemStr ;
    }

    /**
     * 获取系统可用的内存大小
     * @return
     */
    public static String getSystemAvaialbeMemorySize(Context context){
        //获得ActivityManager服务的对象
        ActivityManager mActivityManager = (ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);
        //获得MemoryInfo对象
        ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo() ;
        //获得系统可用内存，保存在MemoryInfo对象上
        mActivityManager.getMemoryInfo(memoryInfo) ;
        long memSize = memoryInfo.availMem ;

        //字符类型转换
        String availMemStr = formateFileSize(memSize);
        return availMemStr ;
    }

    /**
     * 返回为字符串数组[0]为大小[1]为单位KB或MB
     * @param size
     * @return
     */
    public static String formateFileSize(long size) {
        String suffix = "";
        float fSzie = 0;
        if (size >= 1024) {
            suffix = "KB";
            fSzie = size / 1024;
            if (fSzie >= 1024) {
                suffix = "MB";
                fSzie /= 1024;
                if (fSzie >= 1024) {
                    suffix = "GB";
                    fSzie /= 1024;
                }
            }
        }

        DecimalFormat formatter = new DecimalFormat("#0.00");// 字符显示格式
        /* 每3个数字用,分隔，如1,000 */
        formatter.setGroupingSize(3);
        StringBuilder resultBuffer = new StringBuilder(formatter.format(fSzie));
        if (suffix != null) {
            resultBuffer.append(suffix);
        }
        return resultBuffer.toString();
    }

    /**
     * 获取手机内部空间大小
     * @return
     */
    public static String getTotalInternalStorgeSize() {
        File path = Environment.getDataDirectory();
        StatFs mStatFs = new StatFs(path.getPath());
        long blockSize = mStatFs.getBlockSize();
        long totalBlocks = mStatFs.getBlockCount();
        long memSize = totalBlocks * blockSize;

        //字符类型转换
        String availMemStr = formateFileSize(memSize);
        return availMemStr ;
    }

    /**
     * 获取手机内部可用空间大小
     * @return
     */
    public static String getAvailableInternalStorgeSize() {
        File path = Environment.getDataDirectory();
        StatFs mStatFs = new StatFs(path.getPath());
        long blockSize = mStatFs.getBlockSize();
        long availableBlocks = mStatFs.getAvailableBlocks();
        long memSize = availableBlocks * blockSize;

        //字符类型转换
        String availMemStr = formateFileSize(memSize);
        return availMemStr ;
    }
}
