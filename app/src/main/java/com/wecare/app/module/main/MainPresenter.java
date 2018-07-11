package com.wecare.app.module.main;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.text.TextUtils;
import android.widget.Toast;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.location.AMapLocationQualityReport;
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
import com.wecare.app.module.location.LocationStrategy;
import com.wecare.app.module.location.UpdateLocationListener;
import com.wecare.app.util.DeviceUtils;
import com.wecare.app.util.LocationUtils;
import com.wecare.app.util.Logger;
import com.wecare.app.util.T;
import com.wecare.app.util.Utils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
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

    private Location lastAMapLocation = null;

    private long uploadGpsTime = 0L;

    //声明AMapLocationClient类对象
    private AMapLocationClient mLocationClient = null;

    //声明AMapLocationClientOption对象
    private AMapLocationClientOption mLocationOption = null;

    public MainPresenter(MainContract.View view, Context context) {
        this.view = view;
        this.mContext = context;
        view.setPresenter(this);
        mCompositeDisposable = new CompositeDisposable();
        mHttpApi = HttpFactory.createRetrofit2(HttpApi.class);
    }

    @Override
    public void subscribe() {
        gpsLocationStrategy = new GpsLocationStrategy(mContext);
        aMapLocationStrategy = new AMapLocationStrategy(mContext);
    }

    @Override
    public void unsubscribe() {
        mCompositeDisposable.clear();
        destroyLocation();
    }

    /**
     * 初始化定位
     */
    @Override
    public void initLocation() {
        //初始化client
        mLocationClient = new AMapLocationClient(mContext.getApplicationContext());
        mLocationOption = getDefaultOption();
        //设置定位参数
        mLocationClient.setLocationOption(mLocationOption);
        // 设置定位监听
        mLocationClient.setLocationListener(mLocationListener);
    }

    @Override
    public void startLocation() {
        //根据控件的选择，重新设置定位参数
//        resetOption();
        // 设置定位参数
        mLocationClient.setLocationOption(mLocationOption);
        // 启动定位
        mLocationClient.startLocation();
    }

    @Override
    public void stopLocation() {
        // 停止定位
        if (mLocationClient != null)
            mLocationClient.stopLocation();
    }

    @Override
    public void destroyLocation() {
        if (null != mLocationClient) {
            /**
             * 如果AMapLocationClient是在当前Activity实例化的，
             * 在Activity的onDestroy中一定要执行AMapLocationClient的onDestroy
             */
            mLocationClient.onDestroy();
            mLocationClient = null;
        }
    }

    /**
     * 默认的定位参数
     *
     * @author hongming.wang
     * @since 2.8.0
     */
    private AMapLocationClientOption getDefaultOption() {
        AMapLocationClientOption mOption = new AMapLocationClientOption();
        mOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);//可选，设置定位模式，可选的模式有高精度、仅设备、仅网络。默认为高精度模式
        mOption.setGpsFirst(false);//可选，设置是否gps优先，只在高精度模式下有效。默认关闭
        mOption.setHttpTimeOut(30 * 1000);//可选，设置网络请求超时时间。默认为30秒。在仅设备模式下无效
//        mOption.setInterval(App.getInstance().LOCATION_MIN_TIME);//可选，设置定位间隔。默认为2秒
        mOption.setNeedAddress(true);//可选，设置是否返回逆地理地址信息。默认是true
        mOption.setOnceLocation(false);//可选，设置是否单次定位。默认是false
        mOption.setOnceLocationLatest(false);//可选，设置是否等待wifi刷新，默认为false.如果设置为true,会自动变为单次定位，持续定位时不要使用
        AMapLocationClientOption.setLocationProtocol(AMapLocationClientOption.AMapLocationProtocol.HTTP);//可选， 设置网络请求的协议。可选HTTP或者HTTPS。默认为HTTP
        mOption.setSensorEnable(false);//可选，设置是否使用传感器。默认是false
        mOption.setWifiScan(true); //可选，设置是否开启wifi扫描。默认为true，如果设置为false会同时停止主动刷新，停止以后完全依赖于系统刷新，定位位置可能存在误差
        mOption.setLocationCacheEnable(true); //可选，设置是否使用缓存定位，默认为true
        mOption.setGeoLanguage(AMapLocationClientOption.GeoLanguage.DEFAULT);//可选，设置逆地理信息的语言，默认值为默认语言（根据所在地区选择语言）
        return mOption;
    }

    //声明定位回调监听器
    private final AMapLocationListener mLocationListener = new AMapLocationListener() {
        @Override
        public void onLocationChanged(AMapLocation location) {
            if (null != location) {
                StringBuilder sb = new StringBuilder();
                //errCode等于0代表定位成功，其他的为定位失败，具体的可以参照官网定位错误码说明
                if (location.getErrorCode() == 0) {
                    Logger.i(TAG, "onLocationChanged success，location ：经    度：" + location.getLongitude() + " 纬    度：" + location.getLatitude());
                    sb.append("定位成功" + "\n");
                    sb.append("定位类型: " + location.getLocationType() + "\n");
                    sb.append("经    度    : " + location.getLongitude() + "\n");
                    sb.append("纬    度    : " + location.getLatitude() + "\n");
                    sb.append("精    度    : " + location.getAccuracy() + "米" + "\n");
                    sb.append("提供者    : " + location.getProvider() + "\n");

                    sb.append("速    度    : " + location.getSpeed() + "米/秒" + "\n");
                    sb.append("角    度    : " + location.getBearing() + "\n");
                    // 获取当前提供定位服务的卫星个数
                    sb.append("星    数    : " + location.getSatellites() + "\n");
                    sb.append("国    家    : " + location.getCountry() + "\n");
                    sb.append("省            : " + location.getProvince() + "\n");
                    sb.append("市            : " + location.getCity() + "\n");
                    sb.append("城市编码 : " + location.getCityCode() + "\n");
                    sb.append("区            : " + location.getDistrict() + "\n");
                    sb.append("区域 码   : " + location.getAdCode() + "\n");
                    sb.append("地    址    : " + location.getAddress() + "\n");
                    sb.append("兴趣点    : " + location.getPoiName() + "\n");
                    //定位完成的时间
                    sb.append("定位时间: " + Utils.formatUTC(location.getTime(), "yyyy-MM-dd HH:mm:ss") + "\n");

                    if (lastAMapLocation == null) {
                        lastAMapLocation = location;
                        uploadGpsTime = System.currentTimeMillis();
                        view.onLocationChanged(location, location.getSatellites(), System.currentTimeMillis());
                    } else {
                        if (location.getLatitude() == lastAMapLocation.getLatitude() && location.getLongitude() == lastAMapLocation.getLongitude()) {
                            //经纬度相同，则最上半小时上传一次经纬度
                            if (System.currentTimeMillis() - uploadGpsTime >= App.getInstance().SAME_GPS_UPLOAD_TIME) {
                                uploadGpsTime = System.currentTimeMillis();
                                view.onLocationChanged(location, location.getSatellites(), lastAMapLocation.getTime());
                                lastAMapLocation = location;
                            }
                        } else {
                            //每次间隔5秒上传一次位置
                            if (System.currentTimeMillis() - uploadGpsTime >= App.getInstance().MIN_GPS_UPLOAD_TIME) {
                                uploadGpsTime = System.currentTimeMillis();
                                view.onLocationChanged(location, location.getSatellites(), lastAMapLocation.getTime());
                                lastAMapLocation = location;
                            }
                        }
                    }
                } else {
                    Logger.e(TAG, "onLocationChanged error，location ：" + new Gson().toJson(location));
                    sb.append("经    度    : " + location.getLongitude() + "\n");
                    sb.append("纬    度    : " + location.getLatitude() + "\n");
                    //定位失败
                    sb.append("定位失败" + "\n");
                    sb.append("错误码:" + location.getErrorCode() + "\n");
                    sb.append("错误信息:" + location.getErrorInfo() + "\n");
                    sb.append("错误描述:" + location.getLocationDetail() + "\n");
                }
                sb.append("***定位质量报告***").append("\n");
                sb.append("* WIFI开关：").append(location.getLocationQualityReport().isWifiAble() ? "开启" : "关闭").append("\n");
                sb.append("* GPS状态：").append(getGPSStatusString(location.getLocationQualityReport().getGPSStatus())).append("\n");
                sb.append("* GPS星数：").append(location.getLocationQualityReport().getGPSSatellites()).append("\n");
                sb.append("* 网络类型：" + location.getLocationQualityReport().getNetworkType()).append("\n");
                sb.append("* 网络耗时：" + location.getLocationQualityReport().getNetUseTime()).append("\n");
                sb.append("*****************").append("\n");
                //定位之后的回调时间
                sb.append("回调时间: " + Utils.formatUTC(System.currentTimeMillis(), "yyyy-MM-dd HH:mm:ss") + "\n");

                sb.append("IMEI：" + DeviceUtils.getDeviceIMEI(mContext));
                //解析定位结果，
                String result = sb.toString();
                view.showStates(result);
            } else {
                Toast.makeText(mContext, "定位失败，loc is null", Toast.LENGTH_SHORT).show();
            }
        }
    };

    /**
     * 获取GPS状态的字符串
     *
     * @param statusCode GPS状态码
     * @return
     */
    private String getGPSStatusString(int statusCode) {
        String str = "";
        switch (statusCode) {
            case AMapLocationQualityReport.GPS_STATUS_OK:
                str = "GPS状态正常";
                break;
            case AMapLocationQualityReport.GPS_STATUS_NOGPSPROVIDER:
                str = "手机中没有GPS Provider，无法进行GPS定位";
                break;
            case AMapLocationQualityReport.GPS_STATUS_OFF:
                str = "GPS关闭，建议开启GPS，提高定位质量";
                break;
            case AMapLocationQualityReport.GPS_STATUS_MODE_SAVING:
                str = "选择的定位模式中不包含GPS定位，建议选择包含GPS定位的模式，提高定位质量";
                break;
            case AMapLocationQualityReport.GPS_STATUS_NOGPSPERMISSION:
                str = "没有GPS定位权限，建议开启gps定位权限";
                break;
            default:
                break;
        }
        return str;
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

                    }

                    @Override
                    public void onNext(QueryBusinessResp resp) {
                        Logger.e(TAG, "onNext resp " + new Gson().toJson(resp));
                        view.excuteBusiness(resp);
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
                                view.setImageResouse(url);
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

    private static final String GPS_LOCATION_NAME = android.location.LocationManager.GPS_PROVIDER;
    private LocationManager mLocationManager;
    private boolean isGpsEnabled;
    private String locateType;
    //默认位置可更新的最短距离为0m
    private float mMinDistance = 0;
    private int mGpsCount = 0;
    private long lastLocationTime;

    public void initGPSlocation() {
        //获取定位服务
        mLocationManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
        String providerName = mLocationManager.getBestProvider(getCriteria(), false);
        //判断是否开启GPS定位功能
        isGpsEnabled = mLocationManager.isProviderEnabled(GPS_LOCATION_NAME);
        Logger.i(TAG, "isGpsEnabled：" + isGpsEnabled + "  providerName：" + providerName);
        //定位类型：GPS
        locateType = mLocationManager.GPS_PROVIDER;
    }


    public void requestGpsLocation(Context context) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Logger.i(TAG, "requestGpsLocation permission denied");
            return;
        }
        LocationUtils.addLocationListener(context, mLocationManager.GPS_PROVIDER, 1000, 0, new LocationUtils.ILocationListener() {
            @Override
            public void onSuccessLocation(Location location) {
                updatelocation(location);
            }
        });
        LocationUtils.getLocationManager(context).addGpsStatusListener(mGpsStatusCallback);
        Logger.i(TAG, "requestGpsLocation location is ：" + LocationUtils.getLocationManager(context));
        mLocationManager = LocationUtils.getLocationManager(context);
        mLocationManager.requestLocationUpdates(mLocationManager.GPS_PROVIDER, 1000, 0, new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                Logger.i(TAG, "requestLocationUpdates onLocationChanged ：" + location);
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
                Logger.i(TAG, "requestLocationUpdates onStatusChanged  provider：" + provider + "， status：" + status);
            }

            @Override
            public void onProviderEnabled(String provider) {
                Logger.i(TAG, "requestLocationUpdates onProviderEnabled  provider：" + provider);
            }

            @Override
            public void onProviderDisabled(String provider) {
                Logger.i(TAG, "requestLocationUpdates onProviderDisabled  provider：" + provider);
            }
        });
        Logger.i(TAG, "requestGpsLocation location is ：" + mLocationManager);
    }

    //定义要获取的GPS数据
    public Criteria getCriteria() {
        Criteria c = new Criteria();
        c.setAccuracy(Criteria.ACCURACY_FINE);//高精度
        c.setAltitudeRequired(true);//包含高度信息
        c.setBearingRequired(true);//包含方位信息
        c.setSpeedRequired(true);//包含速度信息
        c.setCostAllowed(true);//允许付费
        c.setPowerRequirement(Criteria.POWER_HIGH);//高耗电
        return c;
    }

    public void startGpsLocation(Context context) {
        if (ActivityCompat.checkSelfPermission(App.getInstance(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(App.getInstance(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Logger.i(TAG, "权限不够");
            view.showStates("GPS权限不够！！！");
            return;
        }
//        Location location = mLocationManager.getLastKnownLocation(locateType);
//        updatelocation(location);
        mLocationManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, mMinDistance, mGPSLocationListener);
//        mLocationManager.addGpsStatusListener(mGpsStatusCallback);
        mLocationManager.addGpsStatusListener(mGpsStatusCallback);
    }

    public void stopGpsLocation() {
//        mLocationManager.removeGpsStatusListener(mGpsStatusCallback);
        mLocationManager.removeUpdates(mGPSLocationListener);
        mLocationManager.removeGpsStatusListener(mGpsStatusCallback);
    }

    private LocationListener mGPSLocationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            Logger.i(TAG, "GPSLocationListener success，location ：经    度：" + location.getLongitude() + " 纬    度：" + location.getLatitude());
            //当GPS位置发生改变时，更新位置
            updatelocation(location);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            Logger.i(TAG, "GPSLocationListener onStatusChanged" + status);
            switch (status) {
                case LocationProvider.AVAILABLE:
                    break;
                case LocationProvider.OUT_OF_SERVICE:
                    break;
                case LocationProvider.TEMPORARILY_UNAVAILABLE:
                    break;
                default:
                    break;
            }
        }

        @Override
        public void onProviderEnabled(String provider) {
            Logger.i(TAG, "GPSLocationListener onProviderEnabled GPS开启了");
        }

        @Override
        public void onProviderDisabled(String provider) {
            Logger.i(TAG, "GPSLocationListener onProviderDisabled GPS关闭了");
        }
    };

    public void requestLocation() {
        LocationStrategy strategy = new GpsLocationStrategy(mContext);
        LocationController locationController = new LocationController();
        locationController.setLocationStrategy(strategy);
        locationController.setListener(this);
    }

    public void requestGpsCount() {
//        gpsLocationStrategy = new GpsLocationStrategy(mContext);
//        aMapLocationStrategy = new AMapLocationStrategy(mContext);
        mLocationManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Logger.i(TAG, "GPS权限不够");
            return;
        }
//        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, mMinDistance, mGPSLocationListener);
        mLocationManager.addGpsStatusListener(mGpsStatusCallback);
    }


    private GpsStatus.Listener mGpsStatusCallback = new GpsStatus.Listener() {
        public void onGpsStatusChanged(int event) { // GPS状态变化时的回调，如卫星数
            LocationManager locationManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
            if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                Logger.i(TAG, "GPS权限不够");
                return;
            }
            GpsStatus status = locationManager.getGpsStatus(null); //取当前状态
            updateGpsStatus(event, status);
        }
    };

    private List<GpsSatellite> numSatelliteList = new ArrayList<>();

//    int excutecount = 0;

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
//                if (gpsLocationStrategy == null) {
//                    gpsLocationStrategy = new GpsLocationStrategy(mContext);
//                }
                gpsLocationStrategy.setGpsCount(mGpsCount);
                mLocationController.setLocationStrategy(gpsLocationStrategy);
            } else {
//                if (aMapLocationStrategy == null) {
//                    aMapLocationStrategy = new AMapLocationStrategy(mContext);
//                }
                mLocationController.setLocationStrategy(aMapLocationStrategy);
            }
            mLocationController.setListener(this);


/*            if (aMapLocationStrategy == null) {
                aMapLocationStrategy = new AMapLocationStrategy(mContext);
            }
            mLocationController.setLocationStrategy(aMapLocationStrategy);
            mLocationController.setListener(this);*/
        }
    }


    LocationController mLocationController;

    GpsLocationStrategy gpsLocationStrategy;

    AMapLocationStrategy aMapLocationStrategy;

    private void updatelocation(Location location) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        if (location != null) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("实时位置信息\n");
            stringBuilder.append("经度：");
            stringBuilder.append(location.getLongitude());
            stringBuilder.append("\n纬度：");
            stringBuilder.append(location.getLatitude());
            stringBuilder.append("\n高度：");
            stringBuilder.append(location.getAltitude());
            stringBuilder.append("\n速度：");
            stringBuilder.append(location.getSpeed());
            stringBuilder.append("\n时间：");
            stringBuilder.append(location.getTime());
            stringBuilder.append("\n精度：");
            stringBuilder.append(location.getAccuracy());
            stringBuilder.append("\n方位：");
            stringBuilder.append(location.getBearing());
            stringBuilder.append("\n时间：");
            stringBuilder.append(simpleDateFormat.format(new Date(location.getTime())));
            stringBuilder.append("\n星数：");
            stringBuilder.append(mGpsCount);

            view.showStates(stringBuilder.toString());
            T.showShort(mContext, "currentLocation：" + stringBuilder.toString());

            if (lastAMapLocation == null) {
                lastAMapLocation = location;
                uploadGpsTime = System.currentTimeMillis();
                view.onLocationChanged(location, mGpsCount, 0);
            } else {
                if (location.getLatitude() != lastAMapLocation.getLatitude() || location.getLongitude() != lastAMapLocation.getLongitude()) {
                    //每次间隔5秒上传一次位置
                    if (System.currentTimeMillis() - uploadGpsTime >= App.getInstance().MIN_GPS_UPLOAD_TIME) {
                        lastLocationTime = lastAMapLocation.getTime();
                        uploadGpsTime = System.currentTimeMillis();
                        lastAMapLocation = location;
                        view.onLocationChanged(location, mGpsCount, lastLocationTime);
                    }
                } else {
                    //经纬度相同，则最上半小时上传一次经纬度
                    if (System.currentTimeMillis() - uploadGpsTime >= App.getInstance().SAME_GPS_UPLOAD_TIME) {
                        lastLocationTime = lastAMapLocation.getTime();
                        uploadGpsTime = System.currentTimeMillis();
                        lastAMapLocation = location;
                        view.onLocationChanged(location, mGpsCount, lastLocationTime);
                    }
                }
            }
        }
    }

    @Override
    public void updateLocationChanged(Location location, int gpsCount, long lastPositionTime) {
        view.onLocationChanged(location, gpsCount, lastPositionTime);
    }
}
