package com.wecare.app.util;

import android.content.Context;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.Process;
import android.util.Log;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * 文件日志工具类
 * <p>
 * Created by chengzj 2018/06/29
 */
public class Logger {

    private static boolean isWriter;

    private static Level currentLevel;

    private static String pkgName;

    private static int myPid;

    private static String logFilePath;

    private static DateFormat FILE_NAME_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

    private static DateFormat LOG_TIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private static String LOG_FORMAT = "%s  %d-%d/%s  %s/%s：";

    /**
     * 日志级别
     */
    public enum Level {
        VERBOSE(Log.VERBOSE),

        DEBUG(Log.DEBUG),

        INFO(Log.INFO),

        WARN(Log.WARN),

        ERROR(Log.ERROR),

        ASSERT(Log.ASSERT),

        CLOSE(Log.ASSERT + 1);

        int value;

        Level(int value) {
            this.value = value;
        }
    }

    public static final void i(String tag, String msg) {
        if (currentLevel.value > Level.INFO.value)
            return;
        if (isWriter) {
            write(tag, msg, "I");
        }
        Log.i(tag, msg);
    }

    public static final void i(String tag, String msg, Throwable throwable) {
        if (currentLevel.value > Level.INFO.value)
            return;
        if (isWriter) {
            write(tag, msg, "I", throwable);
        }
        Log.i(tag, msg, throwable);
    }

    public static final void v(String tag, String msg) {
        if (currentLevel.value > Level.VERBOSE.value)
            return;
        if (isWriter) {
            write(tag, msg, "V");
        }
        Log.v(tag, msg);
    }

    public static final void v(String tag, String msg, Throwable throwable) {
        if (currentLevel.value > Level.VERBOSE.value)
            return;
        if (isWriter) {
            write(tag, msg, "V", throwable);
        }
        Log.v(tag, msg, throwable);
    }

    public static final void d(String tag, String msg) {
        if (currentLevel.value > Level.DEBUG.value)
            return;
        if (isWriter) {
            write(tag, msg, "D");
        }
        Log.d(tag, msg);
    }

    public static final void d(String tag, String msg, Throwable throwable) {
        if (currentLevel.value > Level.DEBUG.value)
            return;
        if (isWriter) {
            write(tag, msg, "D", throwable);
        }
        Log.d(tag, msg, throwable);
    }

    public static final void e(String tag, String msg) {
        if (currentLevel.value > Level.ERROR.value)
            return;
        if (isWriter) {
            write(tag, msg, "E");
        }
        Log.e(tag, msg);
    }

    public static final void e(String tag, String msg, Throwable throwable) {
        if (currentLevel.value > Level.ERROR.value)
            return;
        if (isWriter) {
            write(tag, msg, "E", throwable);
        }
        Log.e(tag, msg, throwable);
    }

    public static final void w(String tag, String msg) {
        if (currentLevel.value > Level.WARN.value)
            return;
        if (isWriter) {
            write(tag, msg, "W");
        }
        Log.w(tag, msg);
    }

    public static final void w(String tag, String msg, Throwable throwable) {
        if (currentLevel.value > Level.WARN.value)
            return;
        if (isWriter) {
            write(tag, msg, "W", throwable);
        }
        Log.w(tag, msg, throwable);
    }

    public static final void i(Object target, String msg) {
        i(target.getClass().getSimpleName(), msg);
    }

    public static final void i(Object target, String msg, Throwable throwable) {
        i(target.getClass().getSimpleName(), msg, throwable);
    }

    public static final void v(Object target, String msg) {
        v(target.getClass().getSimpleName(), msg);
    }

    public static final void v(Object target, String msg, Throwable throwable) {
        v(target.getClass().getSimpleName(), msg, throwable);
    }

    public static final void d(Object target, String msg) {
        d(target.getClass().getSimpleName(), msg);
    }

    public static final void d(Object target, String msg, Throwable throwable) {
        d(target.getClass().getSimpleName(), msg, throwable);
    }

    public static final void e(Object target, String msg) {
        e(target.getClass().getSimpleName(), msg);
    }

    public static final void e(Object target, String msg, Throwable throwable) {
        e(target.getClass().getSimpleName(), msg, throwable);
    }

    public static final void w(Object target, String msg) {
        w(target.getClass().getSimpleName(), msg);
    }

    public static final void w(Object target, String msg, Throwable throwable) {
        w(target.getClass().getSimpleName(), msg, throwable);
    }

    /**
     * 通过handler写入日志
     * @param tag
     * @param msg
     * @param level
     */
    private static final void write(String tag, String msg, String level) {
        String timeStamp = LOG_TIME_FORMAT.format(Calendar.getInstance().getTime());
        final StringBuilder sb = new StringBuilder(String.format(LOG_FORMAT, timeStamp, myPid, myPid, pkgName, level, tag));
        sb.append(msg);
        Message message = new Message();
        message.obj = sb.toString();
        mHandler.sendMessage(message);
    }

    /**
     * 写文件操作
     *
     * @param tag       日志标签
     * @param msg       日志内容
     * @param level     日志级别
     * @param throwable 异常捕获
     */
    private static final void write(String tag, String msg, String level, Throwable throwable) {
        String timeStamp = LOG_TIME_FORMAT.format(Calendar.getInstance().getTime());
        StringBuilder sb = new StringBuilder(String.format(LOG_FORMAT, timeStamp, myPid, myPid, pkgName, level, tag));
        sb.append(msg);
        sb.append(System.getProperty("line.separator"));
        sb.append(saveCrashInfo(throwable));
        Message message = new Message();
        message.obj = sb.toString();
        mHandler.sendMessage(message);
    }

    private static String saveCrashInfo(Throwable ex) {
        StringBuffer sb = new StringBuffer();
        Writer writer = new StringWriter();
        PrintWriter printWriter = new PrintWriter(writer);
        ex.printStackTrace(printWriter);
        Throwable cause = ex.getCause();
        while (cause != null) {
            cause.printStackTrace(printWriter);
            cause = cause.getCause();
        }
        printWriter.close();
        String result = writer.toString();
        sb.append(result);
        return sb.toString();
    }

    /**
     * 日志组件初始化
     *
     * @param appCtx   application 上下文
     * @param isWriter 是否保存文件
     * @param level    日志级别
     */
    public static final void initialize(Context appCtx, boolean isWriter, Level level) {
        currentLevel = level;
        if (level == Level.CLOSE) {
            Logger.isWriter = false;
            return;
        }
        Logger.isWriter = isWriter;
        if (!Logger.isWriter) {//不保存日志到文件
            return;
        }
        String logFoldPath;
        if (isSDCardOK()) {
            logFoldPath = Environment.getExternalStorageDirectory() + "/wecare/logger/";
        } else {
            logFoldPath = appCtx.getCacheDir().getAbsolutePath() + "/../logger/log";
        }
        pkgName = appCtx.getPackageName();
        myPid = Process.myPid();
        File logFold = new File(logFoldPath);
        boolean flag;
        if (!(flag = logFold.exists()))
            flag = logFold.mkdirs();
        if (!flag) {
            Logger.isWriter = false;
            return;
        }
        logFilePath = logFoldPath + FILE_NAME_FORMAT.format(Calendar.getInstance().getTime()) + ".txt";
        try {
            File logFile = new File(logFilePath);
            if (!(flag = logFile.exists())) {
                flag = logFile.createNewFile();
            }
            Logger.isWriter = isWriter && flag;
        } catch (IOException e) {
            e.printStackTrace();
            Logger.isWriter = false;
        }
    }

    private static Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            String content = (String) msg.obj;

            FileWriter fileWriter = null;
            File logFile = new File(logFilePath);
            try {
                fileWriter = new FileWriter(logFile, true);
                fileWriter.write(System.getProperty("line.separator"));
                fileWriter.append(content);
                fileWriter.flush();
                fileWriter.close();
            } catch (IOException e) {
                e.printStackTrace();
            }finally {
                if(fileWriter != null){
                    try {
                        fileWriter.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    };

    //读写sd卡时的判断
    public static boolean isSDCardOK() {
        String sdStatus = Environment.getExternalStorageState();
        if (!sdStatus.equals(Environment.MEDIA_MOUNTED)) {
            return false;
        } else {
            return true;
        }
    }

    public static void main(String[] args) {
        String timeStamp = LOG_TIME_FORMAT.format(Calendar.getInstance().getTime());
        String tag = "tag";
        String msg = "this is a message!";
        String str = String.format(LOG_FORMAT, timeStamp, 123, 123, "com.cheng.app", "V", tag);
        System.out.println(str + msg);
    }
}
