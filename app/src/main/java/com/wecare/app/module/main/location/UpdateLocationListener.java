package com.wecare.app.module.main.location;

import android.location.Location;

public interface UpdateLocationListener {
    void updateLocationChanged(Location location,int gpsCount,long lastPositionTime);
}
