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
import android.os.storage.StorageManager;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.text.format.Formatter;
import android.util.Log;

import com.google.gson.Gson;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by chengzj on 2018/6/21.
 */
public class DeviceUtils {
    public static final String TAG = "DeviceUtils";

    /**
     * 获取当前App版本名称
     *
     * @param context
     * @return
     */
    public static String getAppVersionName(Context context) {
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
     *
     * @param context
     * @return
     */
    public static int getAppVersionCode(Context context) {
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
     *
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
     * 得到内置存储空间的总容量
     *
     * @param context
     * @return
     */
    public static String getInternalToatalSpace(Context context) {
        String path = Environment.getDataDirectory().getPath();
        Log.d(TAG, "root path is " + path);
        StatFs statFs = new StatFs(path);
        long blockSize = statFs.getBlockSize();
        long totalBlocks = statFs.getBlockCount();
        long availableBlocks = statFs.getAvailableBlocks();

        long totalSize = totalBlocks * blockSize;
        long availSize = availableBlocks * blockSize;

        String totalStr = Formatter.formatFileSize(context, totalSize);
        String availStr = Formatter.formatFileSize(context, availSize);
        Log.i(TAG, "总空间:" + totalStr + "\r" + "可存:" + availStr);
        return totalStr;
    }

    public static String getInternalAvailableSpace(Context context) {
        String path = Environment.getDataDirectory().getPath();
        Log.d(TAG, "root path is " + path);
        StatFs statFs = new StatFs(path);
        long blockSize = statFs.getBlockSize();
        long totalBlocks = statFs.getBlockCount();
        long availableBlocks = statFs.getAvailableBlocks();

        long totalSize = totalBlocks * blockSize;
        long availSize = availableBlocks * blockSize;

        String totalStr = Formatter.formatFileSize(context, totalSize);
        String availStr = Formatter.formatFileSize(context, availSize);
        Log.i(TAG, "总空间:" + totalStr + "\r" + "可存:" + availStr);
        return availStr;
    }

    /**
     * 获取系统内存大小
     *
     * @param context
     * @return
     */
    public static String getSysteTotalMemorySize(Context context) {
        //获得ActivityManager服务的对象
        ActivityManager mActivityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        //获得MemoryInfo对象
        ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
        //获得系统可用内存，保存在MemoryInfo对象上
        mActivityManager.getMemoryInfo(memoryInfo);
        long memSize = memoryInfo.totalMem;
        //字符类型转换
        String availMemStr = Formatter.formatFileSize(context, memSize);
        return availMemStr;
    }


    /**
     * 获取系统可用的内存大小
     *
     * @param context
     * @return
     */
    public static String getSystemAvaialbeMemorySize(Context context) {
        //获得ActivityManager服务的对象
        ActivityManager mActivityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        //获得MemoryInfo对象
        ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
        //获得系统可用内存，保存在MemoryInfo对象上
        mActivityManager.getMemoryInfo(memoryInfo);
        long memSize = memoryInfo.availMem;

        //字符类型转换
        String availMemStr = Formatter.formatFileSize(context, memSize);
        return availMemStr;
    }


    /**
     * 返回为字符串数组[0]为大小[1]为单位KB或MB
     *
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
        formatter.setGroupingSize(3);
        StringBuilder resultBuffer = new StringBuilder(formatter.format(fSzie));
        if (suffix != null) {
            resultBuffer.append(suffix);
        }
        return resultBuffer.toString();
    }


    /**
     * @return List<String>
     * @throws IOException
     * @Title: getExtSDCardPaths
     * @Description: to obtain storage paths, the first path is theoretically
     * the returned value of
     * Environment.getExternalStorageDirectory(), namely the
     * primary external storage. It can be the storage of internal
     * device, or that of external sdcard. If paths.size() >1,
     * basically, the current device contains two type of storage:
     * one is the storage of the device itself, one is that of
     * external sdcard. Additionally, the paths is directory.
     */
    public static List<String> getExtSDCardPaths() {
        List<String> paths = new ArrayList<String>();
        String extFileStatus = Environment.getExternalStorageState();
        File extFile = Environment.getExternalStorageDirectory();
        if (extFileStatus.equals(Environment.MEDIA_MOUNTED)
                && extFile.exists() && extFile.isDirectory()
                && extFile.canWrite()) {
            paths.add(extFile.getAbsolutePath());
        }
        try {
            // obtain executed result of command line code of 'mount', to judge
            // whether tfCard exists by the result
            Runtime runtime = Runtime.getRuntime();
            Process process = runtime.exec("mount");
            InputStream is = process.getInputStream();
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);
            String line = null;
            int mountPathIndex = 1;
            while ((line = br.readLine()) != null) {
                // format of sdcard file system: vfat/fuse
                if ((!line.contains("fat") && !line.contains("fuse") && !line
                        .contains("storage"))
                        || line.contains("secure")
                        || line.contains("asec")
                        || line.contains("firmware")
                        || line.contains("shell")
                        || line.contains("obb")
                        || line.contains("legacy") || line.contains("data")) {
                    continue;
                }
                String[] parts = line.split(" ");
                int length = parts.length;
                if (mountPathIndex >= length) {
                    continue;
                }
                String mountPath = parts[mountPathIndex];
                if (!mountPath.contains("/") || mountPath.contains("data")
                        || mountPath.contains("Data")) {
                    continue;
                }
                File mountRoot = new File(mountPath);
                if (!mountRoot.exists() || !mountRoot.isDirectory()
                        || !mountRoot.canWrite()) {
                    continue;
                }
                boolean equalsToPrimarySD = mountPath.equals(extFile
                        .getAbsolutePath());
                if (equalsToPrimarySD) {
                    continue;
                }

                paths.add(mountPath);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        Logger.i("getExtSDCardPaths  paths：", new Gson().toJson(paths));
        return paths;
    }


    /**
     * 获取外置SD卡路径
     *
     * @return
     */
    public static List<String> getSDCardPaths() {
        List<String> sdcardPaths = new ArrayList<String>();
        String cmd = "cat /proc/mounts";
        Runtime run = Runtime.getRuntime();// 返回与当前 Java 应用程序相关的运行时对象
        try {
            Process p = run.exec(cmd);// 启动另一个进程来执行命令
            BufferedInputStream in = new BufferedInputStream(p.getInputStream());
            BufferedReader inBr = new BufferedReader(new InputStreamReader(in));

            String lineStr;
            while ((lineStr = inBr.readLine()) != null) {
                // 获得命令执行后在控制台的输出信息
                Logger.i("CommonUtil:getSDCardPath", lineStr);

                String[] temp = TextUtils.split(lineStr, " ");
                // 得到的输出的第二个空格后面是路径
                String result = temp[1];
                File file = new File(result);
                if (file.isDirectory() && file.canRead() && file.canWrite()) {
                    Logger.d("directory can read can write:",
                            file.getAbsolutePath());
                    // 可读可写的文件夹未必是sdcard，我的手机的sdcard下的Android/obb文件夹也可以得到
                    sdcardPaths.add(result);

                }

                // 检查命令是否执行失败。
                if (p.waitFor() != 0 && p.exitValue() == 1) {
                    // p.exitValue()==0表示正常结束，1：非正常结束
                    Logger.e("CommonUtil:getSDCardPath", "命令执行失败!");
                }
            }
            inBr.close();
            in.close();
        } catch (Exception e) {
            Logger.e("CommonUtil:getSDCardPath", e.toString());
            sdcardPaths.add(Environment.getExternalStorageDirectory()
                    .getAbsolutePath());
        }

        optimize(sdcardPaths);
        for (Iterator iterator = sdcardPaths.iterator(); iterator.hasNext(); ) {
            String string = (String) iterator.next();
            Logger.e("清除过后", string);
        }
        return sdcardPaths;
    }


    private static void optimize(List<String> sdcaredPaths) {
        if (sdcaredPaths.size() == 0) {
            return;
        }
        int index = 0;
        while (true) {
            if (index >= sdcaredPaths.size() - 1) {
                String lastItem = sdcaredPaths.get(sdcaredPaths.size() - 1);
                for (int i = sdcaredPaths.size() - 2; i >= 0; i--) {
                    if (sdcaredPaths.get(i).contains(lastItem)) {
                        sdcaredPaths.remove(i);
                    }
                }
                return;
            }

            String containsItem = sdcaredPaths.get(index);
            for (int i = index + 1; i < sdcaredPaths.size(); i++) {
                if (sdcaredPaths.get(i).contains(containsItem)) {
                    sdcaredPaths.remove(i);
                    i--;
                }
            }

            index++;
        }
    }

    /**
     * 检查是否有外部TF卡
     * 多余一个，则可以表示TF卡已挂载
     * true 表示有外部TF卡
     * @param context
     * @return
     */
    public static boolean hasTFCard(Context context) {
        try {
            StorageManager sm = (StorageManager) context.getSystemService(Context.STORAGE_SERVICE);
            Method getVolumePathsMethod = StorageManager.class.getMethod("getVolumePaths", null);
            String[] paths = (String[]) getVolumePathsMethod.invoke(sm, null); // first element in paths[] is primary storage path
            if (paths != null) {
                for (int i = 0; i < paths.length; i++) {
                    Log.d(TAG, "------------------ paths - " + i + " --------------- " + paths[i]);
                }
            }
            return paths.length == 2;
        } catch (Exception e) {
            Log.e(TAG, "getPrimaryStoragePath() failed", e);
        }
        return false;
    }
}
