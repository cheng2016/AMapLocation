package com.wecare.app.module.main.location;

import android.content.Context;
import android.location.Location;

public interface LocationStrategy {
    void requestLocation();

    void stopLocation();

    void setListener(UpdateLocationListener listener);
}
