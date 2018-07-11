package com.wecare.app.util;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.telephony.TelephonyManager;

import java.lang.reflect.Method;
import java.util.Locale;

/**
 * 系统工具类
 * <p>
 * Created by chengzj 2018/06/28
 */
public class SystemUtils {

    /**
     * 获取当前手机系统语言。
     *
     * @return 返回当前系统语言。例如：当前设置的是“中文-中国”，则返回“zh-CN”
     */
    public static String getSystemLanguage() {
        return Locale.getDefault().getLanguage();
    }

    /**
     * 获取当前系统上的语言列表(Locale列表)
     *
     * @return 语言列表
     */
    public static Locale[] getSystemLanguageList() {
        return Locale.getAvailableLocales();
    }

    /**
     * 获取当前手机系统Android版本号
     *
     * @return 系统版本号
     */
    public static String getAndroidVersion() {
        return android.os.Build.VERSION.RELEASE;
    }

    /**
     * 获取当前手机系统Android版本号
     *
     * @return
     */
    public static String getAndroidSDKVersion() {
        switch (Build.VERSION.SDK_INT) {
            case Build.VERSION_CODES.JELLY_BEAN_MR1:
                return "4.2";
            case Build.VERSION_CODES.JELLY_BEAN_MR2:
                return "4.3";
            case Build.VERSION_CODES.KITKAT:
                return "4.4";
            case Build.VERSION_CODES.KITKAT_WATCH:
                return "4.4W";
            case Build.VERSION_CODES.LOLLIPOP:
                return "5.0";
            case Build.VERSION_CODES.LOLLIPOP_MR1:
                return "5.1";
            case Build.VERSION_CODES.M:
                return "6.0";
            case Build.VERSION_CODES.N:
                return "7.0";
            case Build.VERSION_CODES.N_MR1:
                return "7.1.1";
            case Build.VERSION_CODES.O:
                return "8.0";
            case Build.VERSION_CODES.O_MR1:
                return "8.1";
            default:
                return "" + Build.VERSION.SDK_INT;
        }
    }

    /**
     * 获取手机型号
     *
     * @return 手机型号
     */
    public static String getSystemModel() {
        return android.os.Build.MODEL;
    }

    /**
     * 获取手机厂商
     *
     * @return 手机厂商
     */
    public static String getDeviceBrand() {
        return android.os.Build.BRAND;
    }

    /**
     * 获取手机IMEI(需要“android.permission.READ_PHONE_STATE”权限)
     *
     * @return 手机IMEI
     */
    public static String getIMEI(Context ctx) {
        TelephonyManager tm = (TelephonyManager) ctx.getSystemService(Activity.TELEPHONY_SERVICE);
        if (tm != null) {
            return tm.getDeviceId();
        }
        return null;
    }

    /**
     * 获取当前系统版本号
     * @return
     */
    public static String getSystemVersion(){
        return android.os.Build.DISPLAY;
    }


    /**
     * 获取序列号
     *
     * @return
     */
    public static String getSerialNumber() {
        String serial = null;
        try {
            Class<?> c = Class.forName("android.os.SystemProperties");
            Method get = c.getMethod("get", String.class);
            serial = (String) get.invoke(c, "ro.serialno");

        } catch (Exception e) {
            e.printStackTrace();
        }
        return serial;

    }

    /**
     * 取出ICCID:集成电路卡识别码（固化在手机SIM卡中,就是SIM卡的序列号）很容易伪造哦
     *
     * @param ctx
     * @return
     */
    public static String getICCID(Context ctx) {
        TelephonyManager telManager = (TelephonyManager) ctx
                .getSystemService(Context.TELEPHONY_SERVICE);
        String iccid = telManager.getSimSerialNumber();
        return iccid;
    }

}
