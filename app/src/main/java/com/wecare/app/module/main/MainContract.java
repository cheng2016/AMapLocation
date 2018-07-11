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

        void setImageResouse(String url);

        void excuteBusiness(QueryBusinessResp resp);
    }

    interface Presenter extends BasePresenter {
        /**
         * 初始化定位组件
         */
        void initLocation();

        /**
         * 开始定位
         */
        void startLocation();

        /**
         * 停止定位
         */
        void stopLocation();
        /**
         * 销毁定位
         */
        void destroyLocation();

        void takePicture(Context context, long createtime, int cameraid, String key);

        void takeMicroRecord(Context context, long createtime, int cameraid, String key);

        void queryBusiness(String type);

        void queryZxingQr();
    }
}
