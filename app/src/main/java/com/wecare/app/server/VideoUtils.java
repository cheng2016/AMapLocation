package com.wecare.app.server;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.provider.MediaStore;

import com.google.gson.Gson;
import com.wecare.app.server.bean.VideoInfo;
import com.wecare.app.util.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;


/**
 * Created by Administrator Chengzj
 *
 * @date 2018/10/30 16:46
 */
public class VideoUtils {
    public static final String TAG = "VideoUtils";

    public static String getVideoTime(String path) {
        String duration = "";
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            retriever.setDataSource(path);
            duration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
            return stringForTime(Integer.parseInt(duration));
        } catch (Exception e) {
            Logger.e(TAG, "getVideoTime exception", e);
        } finally {
            retriever.release();
        }
        return duration;
    }

    /**
     *  * 把毫秒转换成：1:20:30这里形式
     *  * @param timeMs
     *  * @return
     *  
     */
    public static String stringForTime(int timeMs) {
        int totalSeconds = timeMs / 1000;
        int seconds = totalSeconds % 60;
        int minutes = (totalSeconds / 60) % 60;
        int hours = totalSeconds / 3600;
        if (hours > 0) {
            return String.format("%d时%02d分%02d秒", hours, minutes, seconds);
        } else if (minutes > 0) {
            return String.format("%02d分%02d秒", minutes, seconds);
        } else {
            return String.format("%02d秒", seconds);
        }
    }

/*
    // 获取指定视频信息
    public static String getPlayTime(Context context, Uri uri) {
        long duration = 0;
        String[] mediaColumns = {MediaStore.Video.Media.DURATION};
        Cursor cursor = context.getContentResolver().query(uri, mediaColumns, null, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                duration = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION));
                cursor.close();
            }
        }
        Logger.i(TAG, "getPlayTime duration：" + duration);
        duration = duration / 1000;
        int hour = (int) (duration / 3600);
        int minute = (int) (duration % 3600) / 60;
        int second = (int) (duration - hour * 3600 - minute * 60);
        String time = hour + "'" + minute + "''" + second + "'''";
        return time;
    }

    public static String getVideoLength(String videoPath) {
        File source = new File(videoPath);
        Encoder encoder = new Encoder();
        String length = "";
        try {
            MultimediaInfo m = encoder.getInfo(source);
            long ls = m.getDuration() / 1000;
            int hour = (int) (ls / 3600);
            int minute = (int) (ls % 3600) / 60;
            int second = (int) (ls - hour * 3600 - minute * 60);
            length = hour + "'" + minute + "''" + second + "'''";
        } catch (Exception e) {
            Logger.e(TAG, "exception", e);
        }
        return length;
    }
*/


    /**
     * 获取视频大小
     *
     * @param source
     * @return
     */
    public static String readVideoSize(File source) {
        FileChannel fc = null;
        String size = "";
        try {
            @SuppressWarnings("resource")
            FileInputStream fis = new FileInputStream(source);
            fc = fis.getChannel();
            BigDecimal fileSize = new BigDecimal(fc.size());
            size = fileSize.divide(new BigDecimal(1048576), 2, RoundingMode.HALF_UP) + "MB";
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (null != fc) {
                try {
                    fc.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return size;
    }
}
