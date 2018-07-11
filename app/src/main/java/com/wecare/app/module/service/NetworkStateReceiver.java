package com.wecare.app.module.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.wecare.app.App;
import com.wecare.app.module.main.MainActivity;
import com.wecare.app.util.Logger;

public class NetworkStateReceiver extends BroadcastReceiver {
    public static final String TAG = "NetworkStateReceiver";
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent != null) {
            if (App.getInstance().getCurrentActivity() instanceof MainActivity) {
//                MainActivity mainActivity = (MainActivity) App.getInstance().getCurrentActivity();
//                mainActivity.mSocketService.onCreate();
            }
        }
        Logger.i(TAG,"NetworkStateReceiver is work！");
    }
}
