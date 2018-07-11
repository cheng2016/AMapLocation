package com.wecare.app.module.location;

public interface LocationStrategy {
    void requestLocation();

    void stopLocation();

    void setListener(UpdateLocationListener listener);
}
