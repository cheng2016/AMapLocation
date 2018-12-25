package com.wecare.app.module.main;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.AsyncTask;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;
import com.squareup.picasso.Picasso;
import com.wecare.app.App;
import com.wecare.app.data.entity.GetImageReq;
import com.wecare.app.data.entity.GetImageResp;
import com.wecare.app.data.entity.QueryBusinessReq;
import com.wecare.app.data.entity.QueryBusinessResp;
import com.wecare.app.data.source.remote.HttpApi;
import com.wecare.app.data.source.remote.HttpFactory;
import com.wecare.app.module.location.GpsLocationStrategy;
import com.wecare.app.module.location.LocationController;
import com.wecare.app.module.location.UpdateLocationListener;
import com.wecare.app.util.Constact;
import com.wecare.app.util.DimenUtils;
import com.wecare.app.util.ImageUtils;
import com.wecare.app.util.Logger;
import com.wecare.app.util.MD5Utils;
import com.wecare.app.util.PreferenceConstants;
import com.wecare.app.util.PreferenceUtils;
import com.wecare.app.util.ToastUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainPresenter implements MainContract.Presenter, UpdateLocationListener {
    public static final String TAG = "MainPresenter";

    public static final String WEIXIN_PICTURE = "downloadHeadImage_kd";

    private MainContract.View view;
    private HttpApi mHttpApi;
    private CompositeDisposable mCompositeDisposable;

    private Context mContext;

    private LocationController mLocationController;

    private GpsLocationStrategy gpsLocationStrategy;

//    private AMapLocationStrategy aMapLocationStrategy;

//    private LocationManager mLocationManager;

//    private int mGpsCount = 0;

    public MainPresenter(MainContract.View view, Context context) {
        this.view = view;
        this.mContext = context;
        view.setPresenter(this);
        mCompositeDisposable = new CompositeDisposable();
        mHttpApi = HttpFactory.createRetrofit2(HttpApi.class);
    }

    @Override
    public void unsubscribe() {
        mCompositeDisposable.clear();
        stopLocation();
        if (mSensorManager != null) {
            mSensorManager.unregisterListener(sensorListener);
            mSensorManager = null;
        }
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
        final QueryBusinessReq req = new QueryBusinessReq(PreferenceUtils.getPrefString(App.getInstance(), PreferenceConstants.IMEI, ""), type, Constact.APP_KEY);
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

    /**
     * 下载图片到本地，并修改系统图片路径
     *
     * @param context
     * @param url
     */
    public void loadImageToDisk(final Context context, final String url) {
        OkHttpClient okHttpClient = new OkHttpClient();
        Request request = new Request.Builder()
                .get()
                .url(url)
                .build();
        Call call = okHttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Logger.e(TAG, "loadImageToDisk onFailure：", e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String imgurl = PreferenceUtils.getPrefString(context, PreferenceConstants.IMAGE_CACHE_DIR, "") + MD5Utils.MD5(url) + ".jpg";
                Logger.i(TAG, "loadImageToDisk onResponse：" + imgurl);

                //将响应数据转化为输入流数据
                InputStream inputStream = response.body().byteStream();
                //将输入流数据转化为Bitmap位图数据
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                File file = new File(imgurl);
                file.createNewFile();
                //创建文件输出流对象用来向文件中写入数据
                FileOutputStream out = new FileOutputStream(file);
                //将bitmap存储为jpg格式的图片
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
                //刷新文件流
                out.flush();
                out.close();
                //设置系统中的图片
                Settings.System.putString(context.getContentResolver(), MainPresenter.WEIXIN_PICTURE, imgurl);
            }
        });
    }

    public void loadImageToSettings(final Context context, final String url) {
        Logger.i(TAG, "loadImageToSettings   url：" + url);
        Observable.create(new ObservableOnSubscribe<String>() {
            @Override
            public void subscribe(ObservableEmitter<String> emitter) throws Exception {
                Bitmap bitmap = Picasso.with(context).load(url).config(Bitmap.Config.RGB_565).get();
                String filePath = ImageUtils.saveBitmap(context, bitmap, PreferenceUtils.getPrefString(context, PreferenceConstants.IMAGE_CACHE_DIR, ""));
                Logger.d(TAG, "After observeOn(io), current thread is: " + Thread.currentThread().getName());
                emitter.onNext(filePath);
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<String>() {
                    @Override
                    public void accept(String path) throws Exception {
                        Logger.d(TAG, "After observeOn(mainThread), current thread is : " + Thread.currentThread().getName());
                        //设置系统中的图片
                        Settings.System.putString(context.getContentResolver(), MainPresenter.WEIXIN_PICTURE, path);
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        Logger.e(TAG, "loadImageToSetting", throwable);
                    }
                });
        /*new AsyncTask<Void,Void,String>(){
            @Override
            protected String doInBackground(Void... voids) {
                try {
                    int size = DimenUtils.dp2px(context,80);
                    Bitmap bitmap = Picasso.with(context).load(url).resize(size,size).centerCrop().config(Bitmap.Config.RGB_565).get();
                    String filePath = ImageUtils.saveBitmap(context, bitmap, PreferenceUtils.getPrefString(context, PreferenceConstants.IMAGE_CACHE_DIR, ""));
                    return filePath;
                } catch (IOException e) {
                    Logger.e(TAG,"loadImageToSettings IOException：" + e);
                }
                return null;
            }

            @Override
            protected void onPostExecute(String path) {
                super.onPostExecute(path);
                if(!TextUtils.isEmpty(path)){
                    //设置系统中的图片
                    Settings.System.putString(context.getContentResolver(), MainPresenter.WEIXIN_PICTURE, path);
                }
            }
        }.execute();*/
    }

    @Override
    public void queryZxingQr() {
        GetImageReq req = new GetImageReq(PreferenceUtils.getPrefString(App.getInstance(), PreferenceConstants.IMEI, ""), Constact.APP_KEY);
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

    SensorManager mSensorManager;

    @Override
    public void getSensorData(Context context) {
        mSensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        Sensor gsensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mSensorManager.registerListener(sensorListener, gsensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    long startStop = 0;
    long endStop = 0;
    /**
     * 是否处于泊车状态
     */
    boolean isDriving = true;

    SensorEventListener sensorListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            if (event.sensor == null || Sensor.TYPE_ACCELEROMETER != event.sensor.getType()) {
                return;
            }

            float[] values = event.values;
            float xAxis = values[0];
            float yAxis = values[1];
            float zAxis = values[2];

            float max_accelerometer = App.getInstance().LEVEL_1;
            boolean isOverAccelerometer = Math.abs(xAxis) > max_accelerometer
                    || Math.abs(yAxis) > max_accelerometer
                    || Math.abs(zAxis) > max_accelerometer;
            if (isOverAccelerometer) {
                startStop = 0;
                isDriving = true;
                Logger.i(TAG, "Driving.....");
            } else {
                if (startStop == 0) {
                    startStop = System.currentTimeMillis();
                } else {
                    endStop = System.currentTimeMillis();
                    if (endStop - startStop > App.getInstance().PARKING_TIME) {
                        Logger.i(TAG, "停车状态中..........");
                        startStop = 0;
                        endStop = 0;
                        isDriving = false;
                    }
                }
            }

            if (xAxis > 14 || yAxis > 14 || zAxis > 14) {
                ToastUtils.showShort("摇一摇成功");
//                Toast.makeText(mContext, "摇一摇成功", Toast.LENGTH_SHORT).show();
                for (int i = 0; i < values.length; i++) {
                    Logger.i(TAG, "values[" + i + "] = " + values[i]);
                }
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    };


    public void requestLocation() {
        if (gpsLocationStrategy == null) {
            gpsLocationStrategy = new GpsLocationStrategy(mContext);
        }
        if (mLocationController == null) {
            mLocationController = new LocationController();
        }
        mLocationController.setLocationStrategy(gpsLocationStrategy);
        mLocationController.setListener(this);
    }

    public void stopLocation() {
        if (gpsLocationStrategy != null) {
            gpsLocationStrategy.stopLocation();
        }
    }

   /* public void requestGpsCount() {
        //获取定位服务
        if (mLocationManager == null) {
            mLocationManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
        }
        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Logger.i(TAG, "GPS权限不够");
            T.showShort(mContext, "GPS权限不够");
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

            //
            if (mGpsCount >= 3) {
                if (gpsLocationStrategy == null) {
                    gpsLocationStrategy = new GpsLocationStrategy(mContext);
                }
                gpsLocationStrategy.setGpsCount(mGpsCount);//同步卫星数量
                if (mLocationController.getLocationStrategy() instanceof GpsLocationStrategy) {
                    return;
                }
                mLocationController.setLocationStrategy(gpsLocationStrategy);
            } else {
                if (aMapLocationStrategy == null) {
                    aMapLocationStrategy = new AMapLocationStrategy(mContext);
                }
                if (mLocationController.getLocationStrategy() instanceof AMapLocationStrategy) {
                    return;
                }
                mLocationController.setLocationStrategy(aMapLocationStrategy);
            }
            mLocationController.setListener(this);
        }
    }*/

    private Location lastLocation = null;

    private long lastTime = 0L;

    private long uploadGpsTime = 0L;

    private boolean isRequest = false;

    /**
     * 立即请求定位数据
     *
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
            view.onLocationChanged(location, gpsCount, uploadGpsTime);
        } else {
            if (location.getLatitude() == lastLocation.getLatitude() && location.getLongitude() == lastLocation.getLongitude()) {
                //经纬度相同，则最上半小时上传一次经纬度
                if ((System.currentTimeMillis() - uploadGpsTime >= App.getInstance().SAME_GPS_UPLOAD_TIME) && isDriving) {
                    uploadGpsTime = System.currentTimeMillis();
                    location.setTime(uploadGpsTime);//使用当前时间
                    lastTime = lastLocation.getTime();
                    lastLocation = location;
                    view.onLocationChanged(location, gpsCount, lastTime);
                }
            } else {
                //每次间隔15秒上传一次位置
                if ((System.currentTimeMillis() - uploadGpsTime >= App.getInstance().MIN_GPS_UPLOAD_TIME) && isDriving) {
                    uploadGpsTime = System.currentTimeMillis();
                    location.setTime(uploadGpsTime);//使用当前时间
                    lastTime = lastLocation.getTime();
                    lastLocation = location;
                    view.onLocationChanged(location, gpsCount, lastTime);
                }
            }
        }
        //是否立即获取定位数据
        if (isRequest) {
            isRequest = false;
            uploadGpsTime = System.currentTimeMillis();
            location.setTime(uploadGpsTime);//使用当前时间
            lastTime = lastLocation.getTime();
            view.onLocationChanged(location, gpsCount, lastTime);
        }
    }

    public static void main(String[] args) {
        String message = "C16|1|4a5f9cdc8ec1557f0b8fa2456145439c|358732036574479|8|D01:6142|20181031114455|";
        if (message.startsWith("C16")) {
            String[] results = message.split("\\|");
            if (results[5].startsWith("D01")) {
                int commandType = Integer.parseInt(results[5].substring(4, 6));
                String userId = results[5].substring(6);
                System.out.println("命令：" + commandType);
                System.out.println("userId：" + userId);
            }

        }
        System.out.println("命令：" + ("D01:61203232".substring(4, 6)));


        System.out.println("--------------------------------------------------------------");
        message = "C16|1|4a5f9cdc8ec1557f0b8fa2456145439c|358732036574479|8|D01:9|20181031114455|";
        String[] results = message.split("\\|");
        int commandType;
        if (results[5].substring(4).contains("61") || results[5].substring(4).contains("63")) {
            commandType = Integer.parseInt(results[5].substring(4, 6));
        } else {
            commandType = Integer.parseInt(results[5].substring(4));
        }
        System.out.println("命令：" + commandType);
    }
}
