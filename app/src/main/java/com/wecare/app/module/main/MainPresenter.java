package com.wecare.app.module.main;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.wecare.app.App;
import com.wecare.app.data.entity.GetImageReq;
import com.wecare.app.data.entity.GetImageResp;
import com.wecare.app.data.entity.QueryBusinessReq;
import com.wecare.app.data.entity.QueryBusinessResp;
import com.wecare.app.data.source.remote.HttpApi;
import com.wecare.app.data.source.remote.HttpFactory;
import com.wecare.app.module.location.AMapLocationStrategy;
import com.wecare.app.module.location.GpsLocationStrategy;
import com.wecare.app.module.location.LocationController;
import com.wecare.app.module.location.UpdateLocationListener;
import com.wecare.app.util.Logger;
import com.wecare.app.util.T;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class MainPresenter implements MainContract.Presenter, UpdateLocationListener {
    public static final String TAG = "MainPresenter";
    private MainContract.View view;
    private HttpApi mHttpApi;
    private CompositeDisposable mCompositeDisposable;

    private Context mContext;

    private LocationController mLocationController;

    private GpsLocationStrategy gpsLocationStrategy;

    private AMapLocationStrategy aMapLocationStrategy;

    private LocationManager mLocationManager;

    private int mGpsCount = 0;

    public MainPresenter(MainContract.View view, Context context) {
        this.view = view;
        this.mContext = context;
        view.setPresenter(this);
        mCompositeDisposable = new CompositeDisposable();
        mHttpApi = HttpFactory.createRetrofit2(HttpApi.class);
    }

    @Override
    public void subscribe() {
//        gpsLocationStrategy = new GpsLocationStrategy(mContext);
//        aMapLocationStrategy = new AMapLocationStrategy(mContext);
    }

    @Override
    public void unsubscribe() {
        mCompositeDisposable.clear();
    }

    public static final String ACTION_CAMERE_CORE_MICRO_RECORD = "com.discovery.action.CAMERA_CORE_MICRO_RECORD";
    public static final String ACTION_CAMERE_CORE_TAKE_PICTURE = "com.discovery.action.CAMERA_CORE_TAKE_PICTURE";
    public static final String KEY_CREATE_TIME = "key_create_time";
    public static final String KEY_CAMERAID = "key_camerid";
    public static final String KEY_KEY = "key_key";

    @Override
    public void takePicture(Context context, long createtime, int cameraid, String key) {
        Logger.d(TAG, "takePicture : createtime = " + createtime + ", cameraid = " + cameraid + ", key = " + key);
        Logger.d(TAG, "action：" + ACTION_CAMERE_CORE_TAKE_PICTURE);
        Intent intentPicture = new Intent(ACTION_CAMERE_CORE_TAKE_PICTURE);
        intentPicture.putExtra(KEY_CREATE_TIME, createtime);
        intentPicture.putExtra(KEY_CAMERAID, cameraid);
        intentPicture.putExtra(KEY_KEY, key); //如“MyDemo”
        context.sendBroadcast(intentPicture);
    }

    @Override
    public void takeMicroRecord(Context context, long createtime, int cameraid, String key) {
        Logger.d(TAG, "---------- takeMicroRecord : createtime = " + createtime + ", cameraid = " + cameraid + ", key = " + key);
        Logger.d(TAG, "action：" + ACTION_CAMERE_CORE_MICRO_RECORD);
        Intent intentPicture = new Intent(ACTION_CAMERE_CORE_MICRO_RECORD);
        intentPicture.putExtra(KEY_CREATE_TIME, createtime);
        intentPicture.putExtra(KEY_CAMERAID, cameraid);
        intentPicture.putExtra(KEY_KEY, key);
        context.sendBroadcast(intentPicture);
    }

    @Override
    public void queryBusiness(String type) {
        final QueryBusinessReq req = new QueryBusinessReq(App.getInstance().IMEI, type);
        mHttpApi.queryBusiness(new Gson().toJson(req))
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<QueryBusinessResp>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        mCompositeDisposable.add(d);
                    }

                    @Override
                    public void onNext(QueryBusinessResp resp) {
                        Logger.e(TAG, "onNext resp " + new Gson().toJson(resp));
                        view.queryBusinessSucess(resp);
                    }

                    @Override
                    public void onError(Throwable e) {
                        Logger.e(TAG, "queryBusiness onError ", e);
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    @Override
    public void queryZxingQr() {
        GetImageReq req = new GetImageReq(App.getInstance().IMEI);
        mHttpApi.getImageUrl(new Gson().toJson(req))
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<GetImageResp>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        mCompositeDisposable.add(d);
                    }

                    @Override
                    public void onNext(GetImageResp resp) {
                        Logger.i(TAG, "getImageUrl onNext " + new Gson().toJson(resp));
                        if (resp.getData() != null) {
                            String url = resp.getData().getFile_http_path();
                            if (!TextUtils.isEmpty(url)) {
                                view.queryZxingQrSuccess(url);
                            }
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        Logger.e(TAG, "getImageUrl onError ", e);
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }


    public void requestLocation() {
        if(gpsLocationStrategy == null){
            gpsLocationStrategy = new GpsLocationStrategy(mContext);
        }
        if(mLocationController == null){
            mLocationController = new LocationController();
        }
        mLocationController.setLocationStrategy(gpsLocationStrategy);
        mLocationController.setListener(this);
    }

    public void requestGpsCount() {
        //获取定位服务
        if(mLocationManager == null){
            mLocationManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
        }
        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Logger.i(TAG, "GPS权限不够");
            T.showShort(mContext,"GPS权限不够");
            return;
        }
        mLocationManager.addGpsStatusListener(mGpsStatusCallback);
    }

    private GpsStatus.Listener mGpsStatusCallback = new GpsStatus.Listener() {
        public void onGpsStatusChanged(int event) { // GPS状态变化时的回调，如卫星数
            if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                Logger.i(TAG, "GPS权限不够");
                return;
            }
            GpsStatus status = mLocationManager.getGpsStatus(null); //取当前状态
            updateGpsStatus(event, status);
        }
    };

    private List<GpsSatellite> numSatelliteList = new ArrayList<>();

    private void updateGpsStatus(int event, GpsStatus status) {
        if (event == GpsStatus.GPS_EVENT_SATELLITE_STATUS) {
            int maxSatellites = status.getMaxSatellites();
            Iterator<GpsSatellite> it = status.getSatellites().iterator();
            numSatelliteList.clear();
            int count = 0;
            while (it.hasNext() && count <= maxSatellites) {
                GpsSatellite s = it.next();
                if (s.getSnr() != 0)//只有信躁比不为0的时候才算搜到了星
                {
                    numSatelliteList.add(s);
                    count++;
                }
            }
            mGpsCount = numSatelliteList.size();

            if (mLocationController == null) {
                mLocationController = new LocationController();
            }

            if (mGpsCount >= 3) {
                if(gpsLocationStrategy == null){
                    gpsLocationStrategy = new GpsLocationStrategy(mContext);
                }
                gpsLocationStrategy.setGpsCount(mGpsCount);//同步卫星数量
                if(mLocationController.getLocationStrategy() instanceof GpsLocationStrategy){
                    return;
                }
                mLocationController.setLocationStrategy(gpsLocationStrategy);
            } else {
                if(aMapLocationStrategy == null){
                    aMapLocationStrategy = new AMapLocationStrategy(mContext);
                }
                if(mLocationController.getLocationStrategy() instanceof AMapLocationStrategy){
                    return;
                }
                mLocationController.setLocationStrategy(aMapLocationStrategy);
            }
            mLocationController.setListener(this);

//            mLocationController.setLocationStrategy(aMapLocationStrategy);
//            mLocationController.setListener(this);
        }
    }

    private Location lastLocation = null;

    private long lastTime = 0L;

    private long uploadGpsTime = 0L;

    private boolean isRequest = false;

    /**
     * 立即请求定位数据
     * @param request
     */
    public void requestLocation(boolean request) {
        isRequest = request;
    }

    @Override
    public void updateLocationChanged(Location location, int gpsCount) {
        if (lastLocation == null) {
            lastLocation = location;
            uploadGpsTime = System.currentTimeMillis();
            view.onLocationChanged(location, mGpsCount, System.currentTimeMillis());
        } else {
            if (location.getLatitude() == lastLocation.getLatitude() && location.getLongitude() == lastLocation.getLongitude()) {
                //经纬度相同，则最上半小时上传一次经纬度
                if (System.currentTimeMillis() - uploadGpsTime >= App.getInstance().SAME_GPS_UPLOAD_TIME) {
                    uploadGpsTime = System.currentTimeMillis();
                    lastTime = lastLocation.getTime();
                    lastLocation = location;
                    view.onLocationChanged(location, mGpsCount, lastTime);
                }
            } else {
                //每次间隔5秒上传一次位置
                if (System.currentTimeMillis() - uploadGpsTime >= App.getInstance().MIN_GPS_UPLOAD_TIME) {
                    uploadGpsTime = System.currentTimeMillis();
                    lastTime = lastLocation.getTime();
                    lastLocation = location;
                    view.onLocationChanged(location, mGpsCount, lastTime);
                }
            }
        }

        //是否立即获取定位数据
        if(isRequest){
            isRequest = false;
            uploadGpsTime = System.currentTimeMillis();
            lastTime = lastLocation.getTime();
            view.onLocationChanged(location,gpsCount,lastTime);
        }
    }
}
