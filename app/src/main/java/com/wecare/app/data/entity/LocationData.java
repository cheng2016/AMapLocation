package com.wecare.app.data.entity;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Generated;

@Entity
public class LocationData {

    @Id(autoincrement = true)
    private Long id;

    private String content;

//    private String Latitude;
//
//    private String Longitude;
//
//    private String altitude;
//
//    private String speed;
//
//    private String bearing;
//
//    private String accuracy;


    @Generated(hash = 105334506)
    public LocationData(Long id, String content) {
        this.id = id;
        this.content = content;
    }

    @Generated(hash = 1606831457)
    public LocationData() {
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getContent() {
        return this.content;
    }

    public void setContent(String content) {
        this.content = content;
    }


}
