package com.wecare.app.util;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.os.Process;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.wecare.app.App;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.text.DateFormat;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 文件日志工具类
 * <p>
 * Created by chengzj 2018/06/29
 */
public class Logger {
    public static final String TAG = Logger.class.getSimpleName();
    private static final DateFormat LOG_TIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static final String LOG_FORMAT = "%s  %d/%d/%s  %c/%s  %s ";
    private static final String LINE_SEP = System.getProperty("line.separator");
    public static final int V = Log.VERBOSE;
    public static final int D = Log.DEBUG;
    public static final int I = Log.INFO;
    public static final int W = Log.WARN;
    public static final int E = Log.ERROR;
    public static final int A = Log.ASSERT;
    private static final char[] T = new char[]{'V', 'D', 'I', 'W', 'E', 'A'};

    private static final ExecutorService EXECUTOR_SERVICE = Executors.newSingleThreadExecutor();

    private static final Builder BUILDER = new Builder();

    public static class Builder {
        private boolean isWriter = true;

        private Level currentLevel = Level.VERBOSE;

        private String defaultTag = "Logger";

        private String defaultDir;

        private String pkgName;

        private int myPid;

        private int pkgCode;

        private Builder() {
            pkgName = App.getInstance().getPackageName();
            pkgCode = AppUtils.getVersionCode(App.getInstance());
            myPid = Process.myPid();
            if (isSDCardOK()) {
                defaultDir = Environment.getExternalStorageDirectory() + "/wecare/logger/";
            } else {
                defaultDir = App.getInstance().getCacheDir().getAbsolutePath() + "/wecare/logger";
            }
            Log.i(TAG, "pkgName：" + pkgName);
            Log.i(TAG, "pkgCode：" + pkgCode);
            Log.i(TAG, "myPid：" + myPid);
            Log.i(TAG, "defaultDir：" + defaultDir);
        }

        /**
         * 设置是否写入文件
         *
         * @param writer
         */
        public void setWriter(boolean writer) {
            this.isWriter = writer;
        }

        /**
         * 设置日志等级
         *
         * @param currentLevel
         */
        public void setCurrentLevel(Level currentLevel) {
            this.currentLevel = currentLevel;
        }

        /**
         * 设置日志默认存储路径
         *
         * @param defaultDir
         */
        public void setDefaultDir(String defaultDir) {
            this.defaultDir = defaultDir;
        }

        /**
         * 设置日志输出标记
         *
         * @param defaultTag
         */
        public void setDefaultTag(String defaultTag) {
            this.defaultTag = defaultTag;
        }
    }

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

    public static final void i(String target, String msg) {
        log(I, target, msg);
    }

    public static final void v(String target, String msg) {
        log(V, target, msg);
    }

    public static final void d(String target, String msg) {
        log(D, target, msg);
    }

    public static final void e(String target, String msg) {
        log(E, target, msg);
    }

    public static final void e(String target, String msg, Throwable throwable) {
        log(E, target, msg, throwable);
    }

    public static final void w(String target, String msg) {
        log(W, target, msg);
    }

    public static final void w(String target, String msg, Throwable throwable) {
        log(W, target, msg, throwable);
    }

    public static final void log(int type, String tag, String msg) {
        if (BUILDER.currentLevel.value > Level.WARN.value) {
            return;
        }
        if (BUILDER.isWriter) {
            write(tag, msg, type);
        }
        Log.println(type, TAG, tag + " " + msg);
    }

    public static final void log(int type, String tag, String msg, Throwable throwable) {
        if (BUILDER.currentLevel.value > Level.WARN.value) {
            return;
        }
        if (BUILDER.isWriter) {
            write(tag, msg, type, throwable);
        }
        Log.println(type, TAG, tag + " " + msg);
    }


    /**
     * 通过handler写入日志
     *
     * @param tag
     * @param msg
     * @param type
     */
    private static final void write(String tag, String msg, int type) {
        String time = LOG_TIME_FORMAT.format(new Date(System.currentTimeMillis()));
        String date = time.substring(0, 10);
        String fullPath = BUILDER.defaultDir + date + ".txt";
        String head = String.format(LOG_FORMAT, time, BUILDER.myPid, BUILDER.pkgCode, BUILDER.pkgName, T[type - V], BUILDER.defaultTag, tag);
        StringBuilder sb = new StringBuilder(head);
        sb.append(msg);
        sb.append(LINE_SEP);
        //打印到文件日志中
        input2File(sb.toString(), fullPath);
    }

    /**
     * 写文件操作
     *
     * @param tag       日志标签
     * @param msg       日志内容
     * @param type      日志级别
     * @param throwable 异常捕获
     */
    private static final void write(String tag, String msg, int type, Throwable throwable) {
        String time = LOG_TIME_FORMAT.format(new Date(System.currentTimeMillis()));
        String date = time.substring(0, 10);
        String fullPath = BUILDER.defaultDir + date + ".txt";
        String head = String.format(LOG_FORMAT, time, BUILDER.myPid, BUILDER.pkgCode, BUILDER.pkgName, T[type - V], BUILDER.defaultTag, tag);
        StringBuilder sb = new StringBuilder(head);
        sb.append(msg);
        sb.append(LINE_SEP);
        sb.append(saveCrashInfo(throwable));
        //打印到文件日志中
        input2File(sb.toString(), fullPath);
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

    private static void input2File(final String input, final String fullPath) {
        if (lacksPermissions(App.getInstance(), Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            Log.e(TAG, "很抱歉，没有读写权限，无法写入SD卡中");
            return;
        }
        if (!createOrExistsFile(fullPath)) {
            Log.e(TAG, "create " + fullPath + " failed!");
            return;
        }
        EXECUTOR_SERVICE.execute(new Runnable() {
            @Override
            public void run() {
                BufferedWriter bw = null;
                try {
                    bw = new BufferedWriter(new FileWriter(fullPath, true));
                    bw.write(input);
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.e(TAG, "log to " + fullPath + " failed!");
                } finally {
                    try {
                        if (bw != null) {
                            bw.close();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    /**
     * 判断权限集合
     * permissions 权限数组
     * return true-表示没有改权限  false-表示权限已开启
     */
    public static boolean lacksPermissions(Context mContexts, String... args) {
        for (String permission : args) {
            if (lacksPermission(mContexts, permission)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 判断是否缺少权限
     */
    private static boolean lacksPermission(Context context, String permission) {
        return ContextCompat.checkSelfPermission(context, permission) ==
                PackageManager.PERMISSION_DENIED;
    }

    private static boolean createOrExistsFile(String fullPath) {
        File file = new File(fullPath);
        if (file.exists()) return file.isFile();
        if (!createOrExistsDir(file.getParentFile())) return false;
        try {
            return file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    private static boolean createOrExistsDir(final File file) {
        return file != null && (file.exists() ? file.isDirectory() : file.mkdirs());
    }

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
        String timeStamp = LOG_TIME_FORMAT.format(new Date());
        char[] T = new char[]{'V', 'D', 'I', 'W', 'E', 'A'};
        String tag = "tag";
        String msg = "this is a message!";
        String str = String.format(LOG_FORMAT, timeStamp, 123, "com.cheng.app", T[0], "logg", tag);
        System.out.println(str + msg);
    }
}
