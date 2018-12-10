package com.wecare.app.util;

import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Created by Administrator Chengzj
 *
 * @date 2018/10/31 14:17
 */
public class WifiUtils {
    public static final String ACTION_OPEN_WIFIAP = "com.discovery.action.OPEN_WIFIAP";
    public static final String ACTION_CLOSE_WIFIAP = "com.discovery.action.CLOSE_WIFIAP";

    /**
     * 打开AP热点
     */
    public static void openWifiAP(Context context){
        context.sendBroadcast(new Intent(ACTION_OPEN_WIFIAP));
    }
    /**
     * 关闭AP热点
     */
    public static void closeWifiAP(Context context){
        context.sendBroadcast(new Intent(ACTION_CLOSE_WIFIAP));
    }


    /**
     * 判断wifi热点是否打开
     * @param context
     * @return
     */
    public static boolean isWifiApOpen(Context context) {
        try {
            WifiManager manager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            //通过反射获取 getWifiApState()方法
            Method method = manager.getClass().getDeclaredMethod("getWifiApState");
            //调用getWifiApState() ，获取返回值
            int state = (int) method.invoke(manager);
            //通过放射获取 WIFI_AP的开启状态属性
            Field field = manager.getClass().getDeclaredField("WIFI_AP_STATE_ENABLED");
            //获取属性值
            int value = (int) field.get(manager);
            //判断是否开启
            if (state == value) {
                return true;
            } else {
                return false;
            }
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
        return false;
    }


    // wifi热点开关
    public boolean setWifiApEnabled(Context context,boolean enabled) {
        //获取wifi管理服务
        WifiManager  wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        if (enabled) { // disable WiFi in any case
            //wifi和热点不能同时打开，所以打开热点的时候需要关闭wifi
            wifiManager.setWifiEnabled(false);
        }
        try {
            //热点的配置类
            WifiConfiguration apConfig = new WifiConfiguration();
            //配置热点的名称(可以在名字后面加点随机数什么的)
            apConfig.SSID = "YRCCONNECTION";
            //配置热点的密码
            apConfig.preSharedKey="1234567890";
            //通过反射调用设置热点
            Method method = wifiManager.getClass().getMethod(
                    "setWifiApEnabled", WifiConfiguration.class, Boolean.TYPE);
            //返回热点打开状态
            return (Boolean) method.invoke(wifiManager, apConfig, enabled);
        } catch (Exception e) {
            return false;
        }
    }
}
