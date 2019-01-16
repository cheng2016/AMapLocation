package com.wecare.app.module.main;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.location.Location;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.v7.widget.AppCompatImageView;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;

import com.amap.api.location.AMapLocation;
import com.squareup.picasso.Picasso;
import com.wecare.app.R;
import com.wecare.app.data.entity.LocationData;
import com.wecare.app.data.entity.QueryBusinessResp;
import com.wecare.app.data.source.local.LocationDaoUtils;
import com.wecare.app.module.netty.BootService;
import com.wecare.app.module.service.UploadService;
import com.wecare.app.module.setting.SettingActivity;
import com.wecare.app.server.CoreService;
import com.wecare.app.util.AMapUtils;
import com.wecare.app.util.Constact;
import com.wecare.app.util.DimenUtils;
import com.wecare.app.util.Logger;
import com.wecare.app.util.NetUtils;
import com.wecare.app.util.PreferenceConstants;
import com.wecare.app.util.PreferenceUtils;
import com.wecare.app.util.StringTcpUtils;
import com.wecare.app.util.ToastUtils;
import com.wecare.app.util.WifiUtils;

public class MainActivity extends CheckPermissionsActivity implements MainContract.View, View.OnClickListener {
    public static final long ONE_DAY = 9 * 60 * 60 * 1000L;

    private TextView zxingTitleTv, zxingCodeTv;

    public MainPresenter mMainPresenter;

    private AppCompatImageView centerImage;

    private boolean showZxing = true;

    private String qrUrl;

    private String headUrl;

    private String nickName;

    private LocationDaoUtils mLocationDaoUtils;

    private boolean isStartLocation = false;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_main;
    }

    @Override
    protected void initView() {
        zxingTitleTv = findViewById(R.id.zxing_title_tv);
        zxingCodeTv = findViewById(R.id.zxing_code_tv);

        centerImage = findViewById(R.id.center_img);
        findViewById(R.id.center_img).setOnClickListener(this);

        findViewById(R.id.connect_btn).setOnClickListener(this);
        findViewById(R.id.send_btn).setOnClickListener(this);

        findViewById(R.id.tackPicture_btn).setOnClickListener(this);
        findViewById(R.id.record_btn).setOnClickListener(this);

        findViewById(R.id.back_layout).setVisibility(View.GONE);
        findViewById(R.id.right_layout).setVisibility(View.VISIBLE);
        findViewById(R.id.right_layout).setOnClickListener(this);
        findViewById(R.id.zxing_bottom_layout).setOnClickListener(this);
    }

    @Override
    protected void initData(Bundle savedInstanceState) {
        //查看后台服务是否启动
        boolean isBootRunning = PreferenceUtils.getPrefBoolean(this, PreferenceConstants.SERVICE_BOOT_STATE, true);
        if (isBootRunning) {
            Intent i = new Intent().setClass(this, BootService.class);
            stopService(i);
        }
        registerActionReceiver();
        new MainPresenter(this, MainActivity.this);
        mMainPresenter.queryBusiness(Constact.COMMAND_GET_DATA + "");
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (TextUtils.isEmpty(qrUrl)) {
                    mMainPresenter.queryZxingQr();
                }
            }
        }, 500);
        //获取传感器
//        mMainPresenter.getSensorData(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterActionReceiver();
        mMainPresenter.unsubscribe();
    }

    @Override
    public void onClick(View v) {
        Intent intent = new Intent();
        switch (v.getId()) {
            case R.id.right_layout:
                intent.setClass(MainActivity.this, SettingActivity.class);
                startActivity(intent);
                break;
            case R.id.zxing_bottom_layout:
                if (showZxing && !TextUtils.isEmpty(nickName) && !TextUtils.isEmpty(headUrl)) {
                    showWxing();
                } else {
                    if (!TextUtils.isEmpty(qrUrl)) {
                        showZxing();
                    } else {
                        mMainPresenter.queryZxingQr();
                    }
                }
                if (!isStartLocation) {
                    mMainPresenter.requestLocation();
                }

                long requestQR = PreferenceUtils.getPrefLong(this, PreferenceConstants.QUERY_QR_TIME, 0);
                //补偿机制，解决长时间计时不准确的问题
                if (System.currentTimeMillis() - requestQR >= ONE_DAY || TextUtils.isEmpty(qrUrl)) {
                    Logger.i(TAG, "--------------超过指定时间刷新二维码-------------------");
                    mMainPresenter.queryZxingQr();
                }
                break;
            case R.id.center_img:
                if (WifiUtils.isWifiApOpen(MainActivity.this)) {
                    Intent i = new Intent();
                    i.setClass(MainActivity.this, CoreService.class);
                    startService(i);
                }
                break;
            default:
                break;
        }
    }


    @Override
    public void setPresenter(MainContract.Presenter presenter) {
        mMainPresenter = (MainPresenter) presenter;
    }

    @Override
    public void onLocationChanged(Location location, int gpsCount, long lastPositionTime) {
        isStartLocation = true;
        int positionType;
        if (location instanceof AMapLocation) {
            Logger.i(TAG, "高德定位成功：" + ((AMapLocation) location).getAddress() + " 定位类型：" + ((AMapLocation) location).getLocationType());
            positionType = 4;
            ToastUtils.showShort(this, "高德定位成功：" + ((AMapLocation) location).getAddress());
        } else {
            Logger.i(TAG, "GPS定位成功：经    度：" + location.getLongitude() + " 纬    度：" + location.getLatitude());
            positionType = 1;
            ToastUtils.showShort(this, "GPS定位成功！");
        }
        String content = StringTcpUtils.buildGpsContent(location.getLongitude(), location.getLatitude(), location.getAltitude(),
                location.getSpeed(), location.getBearing(), gpsCount, location.getAccuracy(), positionType, location.getTime(), lastPositionTime, "");
        content = StringTcpUtils.buildGpsString(PreferenceUtils.getPrefString(this, PreferenceConstants.IMEI, ""), content);
        if (mSocketService != null && NetUtils.isConnected(this)) {
            mSocketService.sendMessage(content);
        } else {
            if (mLocationDaoUtils == null) {
                mLocationDaoUtils = new LocationDaoUtils(this);
            }
            LocationData data = new LocationData();
            data.setContent(content);
            mLocationDaoUtils.insert(data);
        }
    }

    @Override
    public void queryZxingQrSuccess(String url) {
        PreferenceUtils.setPrefLong(this, PreferenceConstants.QUERY_QR_TIME, System.currentTimeMillis());
        qrUrl = url;
        if (!TextUtils.isEmpty(headUrl) && !TextUtils.isEmpty(nickName)) {
            return;
        }
        showZxing();
        //下载图片到本地，并写入系统设置
        mMainPresenter.loadImageToSettings(this, url);
    }

    @Override
    public void queryBusinessSucess(QueryBusinessResp resp) {
        if (resp != null && resp.getData() != null) {
            if (!TextUtils.isEmpty(resp.getData().getHead_image_url())
                    && !TextUtils.isEmpty(resp.getData().getNick_name())) {
                headUrl = resp.getData().getHead_image_url();
                nickName = resp.getData().getNick_name();
                showWxing();
                //下载图片到本地，并写入系统设置
                mMainPresenter.loadImageToSettings(this, headUrl);
            } else {
                headUrl = "";
                nickName = "";
                mMainPresenter.queryZxingQr();
            }

            QueryBusinessResp.DataBean bean = resp.getData();
            if (!TextUtils.isEmpty(bean.getLat())
                    && !TextUtils.isEmpty(bean.getLng())) {
                AMapUtils.startNaviActivity(this, "", bean.getName(), Double.valueOf(bean.getLat()),
                        Double.valueOf(bean.getLng()), Integer.valueOf(TextUtils.isEmpty(bean.getDev()) ? "0" : bean.getDev()),
                        Integer.valueOf(TextUtils.isEmpty(bean.getStyle()) ? "0" : bean.getStyle()));
            }
        } else {
            headUrl = "";
            nickName = "";
            mMainPresenter.queryZxingQr();
        }
    }

    /**
     * 显示二维码信息
     */
    private void showZxing() {
        showZxing = true;
        Picasso.with(this).load(qrUrl).config(Bitmap.Config.RGB_565).transform(new PicassoRoundTransform()).into(centerImage);
        zxingTitleTv.setText("扫码绑定后视镜");
        zxingCodeTv.setText("点击可切换 头像/二维码");
        zxingCodeTv.setVisibility(View.INVISIBLE);
    }

    /**
     * 显示微信信息
     */
    private void showWxing() {
        showZxing = false;
        int size = DimenUtils.dp2px(this, 160);
        Picasso.with(this).load(headUrl).config(Bitmap.Config.RGB_565).resize(size, size).centerCrop().transform(new PicassoRoundTransform()).into(centerImage);
        zxingTitleTv.setText(nickName);
        zxingCodeTv.setText("点击可切换 头像/二维码");
        zxingCodeTv.setVisibility(View.VISIBLE);
    }

    public static final String ACTION_RE_MICRO_OR_PICTURE = "com.discovery.action.RE_MICRO_OR_PICTURE";

    public static final String ACTION_GSENSOR_ALERT = "com.discovery.action.GSENSOR_ALERT";

    public static final String ACTION_GSENSOR_ALERT_RUN = "com.discovery.action.GSENSOR_ALERT_RUN";

    //    public static final String KEY_USERID = "key_userid";
    public static final String KEY_CREATE_TIME = "key_create_time";
    public static final String KEY_CAMERAID = "key_camerid";
    public static final String KEY_TIME = "key_time";
    public static final String KEY_WIDTH = "key_width";
    public static final String KEY_HEIGHT = "key_heigh";
    public static final String KEY_KEY = "key_key";
    public static final String KEY_TYPE = "key_type";
    public static final String KEY_PATH = "key_path";
    public static final String KEY_ERROR = "key_error";

    //    public static final String MY_KEY = "my_demo";
    private ActionReceiver mReceiver;

    private CommandReceiver mCommandReceiver;

    private NetworkStateReceiver mNetworkStateReceiver;

    /**
     * 注册广播监听；
     */
    public void registerActionReceiver() {
        Logger.d(TAG, "---------------- registerReceiver ----------------");
        mReceiver = new ActionReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_RE_MICRO_OR_PICTURE);
        intentFilter.addAction(ACTION_GSENSOR_ALERT);
        intentFilter.addAction(ACTION_GSENSOR_ALERT_RUN);
        registerReceiver(mReceiver, intentFilter);

        mCommandReceiver = new CommandReceiver();
        IntentFilter commandFilter = new IntentFilter();
        commandFilter.addAction(ACTION_RECEIVER_COMMAND);
        registerReceiver(mCommandReceiver, commandFilter);

        mNetworkStateReceiver = new NetworkStateReceiver();
        IntentFilter netIntentFilter = new IntentFilter();
        netIntentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        netIntentFilter.addAction(Intent.ACTION_AIRPLANE_MODE_CHANGED);
        registerReceiver(mNetworkStateReceiver, netIntentFilter);
    }

    /**
     * 注销广播；
     */
    public void unregisterActionReceiver() {
        Logger.d(TAG, "-------------------- unRegisterReceiver ------------------");
        unregisterReceiver(mReceiver);
        unregisterReceiver(mCommandReceiver);
        unregisterReceiver(mNetworkStateReceiver);
    }

    /**
     * 广播监听；
     */
    public class ActionReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                String action = intent.getAction();
                Logger.d(TAG, "ActionReceiver ------- action : " + action);
                if (action != null) {
                    if (action.equals(ACTION_RE_MICRO_OR_PICTURE)) {
                        long createtime = intent.getLongExtra(KEY_CREATE_TIME, 0);
                        int cameraid = intent.getIntExtra(KEY_CAMERAID, 0);
                        long time = intent.getLongExtra(KEY_TIME, 0);
                        int width = intent.getIntExtra(KEY_WIDTH, 0);
                        int height = intent.getIntExtra(KEY_HEIGHT, 0);

                        String key = intent.getStringExtra(KEY_KEY);
                        String type = intent.getStringExtra(KEY_TYPE);
                        String path = intent.getStringExtra(KEY_PATH);
                        String error = intent.getStringExtra(KEY_ERROR);
                        Logger.d(TAG, "    ------------------------------ createtime : " + createtime);
                        Logger.d(TAG, "    ------------------------------ cameraid : " + cameraid);
                        Logger.d(TAG, "    ------------------------------ time : " + time);
                        Logger.d(TAG, "    ------------------------------ width : " + width);
                        Logger.d(TAG, "    ------------------------------ height : " + height);
                        Logger.d(TAG, "    ------------------------------ key : " + key);
                        Logger.d(TAG, "    ------------------------------ type : " + type);
                        Logger.d(TAG, "    ------------------------------ path : " + path);
                        Logger.d(TAG, "    ------------------------------ error : " + error);
                        if (!TextUtils.isEmpty(path)) {
                            Intent i = new Intent().setClass(MainActivity.this, UploadService.class);
                            i.putExtra("path", path);
                            i.putExtra("timeTemp", createtime);
                            if ("GSENSOR".equals(key)) {
                                if ("pic".equals(type)) {
                                    i.putExtra("type", Constact.FILE_TYPE_IMAGE);
                                    //系统发出的拍照不上传
                                    return;
                                } else if ("vid".equals(type)) {
                                    i.putExtra("type", Constact.FILE_TYPE_COLLISION);
                                }
                                i.putExtra("userId", "");
                            } else if ("CLICK".equals(key)) {
                                Logger.i(TAG, "用户手动拍照，不予处理");
                                return;
                            } else {
                                i.putExtra("userId", key);
                                if ("pic".equals(type)) {
                                    i.putExtra("type", Constact.FILE_TYPE_IMAGE);
                                } else if ("vid".equals(type)) {
                                    i.putExtra("type", Constact.FILE_TYPE_VEDIO);
                                }
                                if (isCollision && createtime == collisionTime) {
                                    isCollision = false;
                                    i.putExtra("type", Constact.FILE_TYPE_COLLISION);
                                }
                            }
                            startService(i);
                        }
                    }
                }
                if (action.equals(ACTION_GSENSOR_ALERT) || action.equals(ACTION_GSENSOR_ALERT_RUN)) {
                    Logger.i(TAG, "检车到停车碰撞或行车碰撞，启动相机录像");
                    isCollision = true;
                    collisionTime = System.currentTimeMillis();
                    mMainPresenter.takeMicroRecord(MainActivity.this, collisionTime, Constact.CAMERA_FRONT, "");
                }
            }
        }
    }


    //是否碰撞
    boolean isCollision = false;
    //碰撞时间
    long collisionTime;

    public static final String ACTION_RECEIVER_COMMAND = "com.discovery.action.COMMAND";

    class CommandReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                String action = intent.getAction();
                int type = intent.getIntExtra("command_type", Constact.COMMAND_TACK_IMAGE);
                int cameraid = intent.getIntExtra("cameraid", Constact.CAMERA_FRONT);
                String key = intent.getStringExtra("userId");
                Logger.d(TAG, "CommandReceiver ------- action : " + action + " type：" + type);
                switch (type) {
                    case Constact.COMMAND_TACK_IMAGE:
                        mMainPresenter.takePicture(MainActivity.this, System.currentTimeMillis(), cameraid, key);
                        break;
                    case Constact.COMMAND_TACK_VIDEO:
                        mMainPresenter.takeMicroRecord(MainActivity.this, System.currentTimeMillis(), cameraid, key);
                        break;
                    case Constact.COMMAND_SUCCESS:
                        ToastUtils.showShort(MainActivity.this, "上传成功");
                        break;
                    case Constact.COMMAND_GO_NAVI:
                        mMainPresenter.queryBusiness(Constact.COMMAND_GO_NAVI + "");
                        break;
                    case Constact.COMMAND_WX_NAVI:
                        mMainPresenter.queryBusiness(Constact.COMMAND_WX_NAVI + "");
                        break;
                    case Constact.COMMAND_GET_DATA:
                        mMainPresenter.queryBusiness(Constact.COMMAND_GET_DATA + "");
                        break;
                    case Constact.COMMAND_LOCATION:
                        mMainPresenter.requestLocation(true);
                        break;
                    case Constact.COMMAND_START_LOCATION:
                        mMainPresenter.requestLocation();
                        Logger.i(TAG, "TCP服务启动成功，启动定位模式！");
                        break;
                    default:
                        break;
                }
            }
        }
    }

    /**
     * 监听系统网络状态广播，7.0 后只支持动态注册
     */
    class NetworkStateReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Logger.i(TAG, "NetworkStateReceiver is work， action：" + action);
            if (action.equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
                if (mSocketService != null)
                    mSocketService.sendMessage(mSocketService.HEART_BEAT_STRING);
            }
            if (action.equals(Intent.ACTION_AIRPLANE_MODE_CHANGED)) {// 飞行模式状态改变
                // To Do
                boolean isEnable = Settings.Global.getInt(context.getContentResolver(), Settings.Global.AIRPLANE_MODE_ON, -1) == 1;
                Logger.d(TAG, "AirplaneModeReceiver - isEnable : " + isEnable);
            }
        }

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            moveTaskToBack(false);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
