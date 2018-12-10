package com.wecare.app.server.bean;

import org.greenrobot.greendao.annotation.Entity;

/**
 * Created by Administrator Chengzj
 *
 * @date 2018/11/2 10:54
 */

public class VideoInfo {
    private String name;
    private long duration; // only for video, in ms
    private long size;

    public VideoInfo(long duration) {
        this.duration = duration;
    }

    public VideoInfo(String name, long duration) {
        this.name = name;
        this.duration = duration;
    }

    public VideoInfo(String name, long duration, long size) {
        this.name = name;
        this.duration = duration;
        this.size = size;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }
}
