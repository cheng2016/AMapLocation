package com.wecare.app.module.location;

public class LocationController {
    private LocationStrategy locationStrategy;

    public LocationController() {
    }

    public void setLocationStrategy(LocationStrategy locationStrategy) {
        if (this.locationStrategy != null) {
            stopLocation();
        }
        this.locationStrategy = locationStrategy;
        requestLocation();
    }

    public LocationStrategy getLocationStrategy() {
        return locationStrategy;
    }

    public void requestLocation() {
        locationStrategy.requestLocation();
    }

    public void stopLocation() {
        locationStrategy.stopLocation();
    }

    public void setListener(UpdateLocationListener listener) {
        locationStrategy.setListener(listener);
    }
}
