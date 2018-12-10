package com.wecare.app.util;

import android.content.Context;
import android.content.Intent;
import android.provider.Settings;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Created by Administrator Chengzj
 *
 * @date 2018/10/22 14:25
 */
public class AirplaneModeUtils {
    public static final String TAG = "AirplaneModeUtils";

    /**
     * 获取飞行模式状态：
     *
     * @param context
     * @return
     */
    public static boolean isAirplaneModeOn(Context context) {
        boolean isEnable = Settings.Global.getInt(context.getContentResolver(), Settings.Global.AIRPLANE_MODE_ON, -1) == 1;
        return isEnable;
    }

    public static final String ACTION_AIRPLAN_CHANGE = "com.discovery.action.AIRPLANE_MODE_CHANGE";
    public static final String EXTRA_MODE = "MODE";

    /**
     * 改变飞行模式状态：
     *
     * @param context
     * @param enabling
     */
    public static void setAirplaneModeOn(Context context, boolean enabling) {
        boolean isEnable = Settings.Global.getInt(context.getContentResolver(), Settings.Global.AIRPLANE_MODE_ON, -1) == 1;
        Logger.d(TAG, "setAirplaneModeOn - isEnable : " + isEnable + ", action : " + enabling);
        if (enabling != isEnable) {
            Intent i = new Intent(ACTION_AIRPLAN_CHANGE);
            i.putExtra(EXTRA_MODE, enabling);
            context.sendBroadcast(i);
        }
    }

    public static final String FILE_ACC = "/sys/bus/platform/drivers/car_acc/state";

    /**
     * 检测车辆是否停火，及是否插电
     *  1 - 表示ACCON； 0 - 表示ACCOFF
     * @return
     */
    public static boolean isAccOn() {
        File file = new File(FILE_ACC);
        if (file != null && file.exists()) {
            try {
                InputStream instream = new FileInputStream(file);
                if (instream != null) {
                    InputStreamReader inputreader = new InputStreamReader(instream);
                    BufferedReader buffreader = new BufferedReader(inputreader);
                    String line;
                    // 分行读取
                    while ((line = buffreader.readLine()) != null) {
                        int state = Integer.parseInt(line);
                        if (state == 1) {
                            instream.close();
                            return true;
                        } else {
                            instream.close();
                            return false;
                        }
                    }
                    instream.close();
                }
            } catch (java.io.FileNotFoundException e) {
                Logger.d(TAG, "isAccOn The File doesn't not exist.");
            } catch (IOException e) {
                Logger.d(TAG, e.getMessage());
            }
        } else {
            Logger.d(TAG, "isAccOn The File doesn't not exist.");
        }
        return false;
    }
}
