package com.wecare.app.module.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

import com.wecare.app.server.CoreService;
import com.wecare.app.util.Logger;

/**
 * Created by Administrator Chengzj
 *
 * @date 2018/11/2 18:05
 */
public class WifiReceiver extends BroadcastReceiver {
    public static final String TAG = "WifiReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {

        String action = intent.getAction();
        if (action.equals(WifiManager.RSSI_CHANGED_ACTION)) {
            //signal strength changed
        } else if (action.equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)) {//wifi连接上与否
            Logger.i(TAG, "网络状态改变");
            NetworkInfo info = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
            if (info.getState().equals(NetworkInfo.State.DISCONNECTED)) {
                Logger.i(TAG, "wifi网络连接断开");
            } else if (info.getState().equals(NetworkInfo.State.CONNECTED)) {
                WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
                WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                //获取当前wifi名称
                Logger.i(TAG, "连接到网络 " + wifiInfo.getSSID());
            }
        } else if ("android.net.wifi.WIFI_AP_STATE_CHANGED".equals(action)) {
            //便携式热点的状态为：10---正在关闭；11---已关闭；12---正在开启；13---已开启
            int state = intent.getIntExtra("wifi_state", 0);
            Logger.i(TAG, "热点开关状态：state= " + String.valueOf(state));
            if (state == 13) {
                Logger.i(TAG, "热点已开启");
                startHttpService(context);
            } else if (state == 11) {
                Logger.i(TAG, "热点已关闭");
                stopHttpService(context);
            } else if (state == 10) {
                Logger.i(TAG, "热点正在关闭");
            } else if (state == 12) {
                Logger.i(TAG, "热点正在开启");
            }

        } else if (action.equals(WifiManager.WIFI_STATE_CHANGED_ACTION)) {//wifi打开与否
            int wifistate = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, WifiManager.WIFI_STATE_DISABLED);
            if (wifistate == WifiManager.WIFI_STATE_DISABLED) {
                Logger.i(TAG, "系统关闭wifi");
            } else if (wifistate == WifiManager.WIFI_STATE_ENABLED) {
                Logger.i(TAG, "系统开启wifi");
            }
        }
    }


    void startHttpService(Context context) {
        Intent i = new Intent();
        i.setClass(context, CoreService.class);
        context.startService(i);
    }

    void stopHttpService(Context context) {
        Intent i = new Intent();
        i.setClass(context, CoreService.class);
        context.stopService(i);
    }
}