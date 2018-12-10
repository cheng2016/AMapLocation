package com.wecare.app.module.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.wecare.app.module.main.MainActivity;
import com.wecare.app.module.netty.BootService;
import com.wecare.app.util.Logger;
import com.wecare.app.util.ToastUtils;

public class BootBroadcastReceiver extends BroadcastReceiver {
    public static final String TAG = "BootBroadcastReceiver";

//    public static final String ACTION_DISCOVERTY = "com.discovery.action.DEVICE_ACC_ON";

    @Override
    public void onReceive(Context context, Intent intent) {
        Logger.i(TAG, " action：" + intent.getAction());

        //接收广播：系统启动完成后运行程序
        if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
//            ToastUtils.showShort(context, "监听开机状态广播成功！");
            Logger.i(TAG, "监听开机状态广播接收成功，startService excute！");
            Intent service = new Intent(context, BootService.class);
            context.startService(service);
        }

        //接收广播：安装更新后，自动启动自己。
        if (intent.getAction().equals(Intent.ACTION_PACKAGE_ADDED) || intent.getAction().equals(Intent.ACTION_PACKAGE_REPLACED)) {
//            ToastUtils.showShort(context, "监听安装更新广播成功！");
            Intent intentActivity = new Intent(context, MainActivity.class);
            intentActivity.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);//不加此句会报错。
            context.startActivity(intentActivity);
            Logger.i(TAG, "安装更新广播接收成功，startService excute！");
        }

        // 开机启动服务（1.有些手机重启后要等3至5分钟才会接收到开机广播  2.启动服务时会先启动主进程,通过监听 application 的执行可以得知）
        // 如果是远程服务,会先启动主进程,再启动一个新的服务进程（该进程只包含一个服务,并且 application 也会得到执行）

        // 开机启动Activity（启动activity时会先启动主进程）
//        Intent intentActivity = new Intent(context, MainActivity.class);
//        intentActivity.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);//不加此句会报错。
//        context.startActivity(intentActivity);
        if("com.discovery.action.DEVICE_ACC_ON".equals(intent.getAction())){
            Logger.i(TAG,"com.discovery.action.DEVICE_ACC_ON  is Receiver! ");
        }

        // 开机启动应用
//        Intent intentApp = context.getPackageManager().getLaunchIntentForPackage("com.wecare.app");
//        context.startActivity(intentApp);
    }
/*
    *//**
     * 判断某个界面是否在前台
     *
     * @param context   Context
     * @param className 界面的类名
     * @return 是否在前台显示
     *//*
    public static boolean isForeground(Context context, String className) {
        if (context == null || TextUtils.isEmpty(className))
            return false;
        ActivityManager am = (ActivityManager) context.getSystemService(context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> list = am.getRunningTasks(1);
//        boolean flag=false;
        for (ActivityManager.RunningTaskInfo taskInfo : list) {
            if (taskInfo.topActivity.getShortClassName().contains(className)) { // 说明它已经启动了
//                flag = true;
                return true;
            }
        }
        return false;
    }


    *//**
     * 判断服务是否开启
     *
     * @return
     *//*
    public static boolean isServiceRunning(Context context, String ServiceName) {
        if (("").equals(ServiceName) || ServiceName == null)
            return false;
        ActivityManager myManager = (ActivityManager) context
                .getSystemService(Context.ACTIVITY_SERVICE);
        ArrayList<RunningServiceInfo> runningService = (ArrayList<RunningServiceInfo>) myManager
                .getRunningServices(30);
        for (int i = 0; i < runningService.size(); i++) {
            Logger.i(TAG, "isServiceRunning：" + runningService.get(i).service.getClassName());
            if (runningService.get(i).service.getClassName().toString()
                    .equals(ServiceName)) {
                return true;
            }
        }
        return false;
    }


    *//**
     * 判断服务是否处于运行状态.
     *
     * @param servicename
     * @param context
     * @return
     *//*
    public static boolean isServiceRunning(String servicename, Context context) {
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<RunningServiceInfo> infos = am.getRunningServices(100);
        for (RunningServiceInfo info : infos) {
            Logger.i(TAG, "isServiceRunning：" + info.service.getClassName());
            if (servicename.equals(info.service.getClassName())) {
                return true;
            }
        }
        return false;
    }*/
}
