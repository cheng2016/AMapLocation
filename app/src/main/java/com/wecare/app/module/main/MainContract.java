package com.wecare.app.module.main;

import android.content.Context;
import android.location.Location;

import com.wecare.app.data.entity.QueryBusinessResp;
import com.wecare.app.module.BasePresenter;
import com.wecare.app.module.BaseView;

public interface MainContract {
    interface View extends BaseView<Presenter> {
        /**
         * @param location
         * @param gpsCount
         * @param lastPositionTime 上一次经纬度上传时间
         */
        void onLocationChanged(Location location,int gpsCount,long lastPositionTime);

        void showStates(String message);

        void queryZxingQrSuccess(String url);

        void queryBusinessSucess(QueryBusinessResp resp);
    }

    interface Presenter extends BasePresenter {

        void takePicture(Context context, long createtime, int cameraid, String key);

        void takeMicroRecord(Context context, long createtime, int cameraid, String key);

        void queryBusiness(String type);

        void queryZxingQr();
    }
}
