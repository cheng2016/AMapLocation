package com.wecare.app.data.entity;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Generated;

/**
 * Created by Administrator Chengzj
 *
 * @date 2018/10/22 10:58
 */

@Entity
public class VideoData {
    @Id(autoincrement = true)
    private Long id;

    private String userId;

    private Integer type;

    private Long timeTemp;

    private String path;

    @Generated(hash = 1026594192)
    public VideoData(Long id, String userId, Integer type, Long timeTemp,
            String path) {
        this.id = id;
        this.userId = userId;
        this.type = type;
        this.timeTemp = timeTemp;
        this.path = path;
    }

    @Generated(hash = 1783392456)
    public VideoData() {
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getType() {
        return this.type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public Long getTimeTemp() {
        return this.timeTemp;
    }

    public void setTimeTemp(Long timeTemp) {
        this.timeTemp = timeTemp;
    }

    public String getPath() {
        return this.path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getUserId() {
        return this.userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
