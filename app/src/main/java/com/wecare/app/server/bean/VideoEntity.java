package com.wecare.app.server.bean;

/**
 * Created by Administrator Chengzj
 *
 * @date 2018/10/30 16:28
 */
public class VideoEntity {
    private String name;

    private String length;

    private String imagePath;

    private String videoPath;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLength() {
        return length;
    }

    public void setLength(String length) {
        this.length = length;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public String getVideoPath() {
        return videoPath;
    }

    public void setVideoPath(String videoPath) {
        this.videoPath = videoPath;
    }
}
