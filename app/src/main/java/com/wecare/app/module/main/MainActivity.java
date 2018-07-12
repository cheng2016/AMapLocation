package com.wecare.app.module.main;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.os.Bundle;
import android.support.v7.widget.AppCompatImageView;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.amap.api.location.AMapLocation;
import com.bumptech.glide.Glide;
import com.squareup.picasso.Picasso;
import com.wecare.app.App;
import com.wecare.app.R;
import com.wecare.app.data.entity.LocationData;
import com.wecare.app.data.entity.QueryBusinessResp;
import com.wecare.app.data.source.local.LocationDaoUtils;
import com.wecare.app.module.service.UploadService;
import com.wecare.app.module.setting.SettingActivity;
import com.wecare.app.util.AMapUtils;
import com.wecare.app.util.Constact;
import com.wecare.app.util.Logger;
import com.wecare.app.util.NetUtils;
import com.wecare.app.util.PreferenceConstants;
import com.wecare.app.util.PreferenceUtils;
import com.wecare.app.util.StringTcpUtils;
import com.wecare.app.util.T;

public class MainActivity extends CheckPermissionsActivity implements MainContract.View, View.OnClickListener {
    public static final long ONE_DAY = 24 * 60 * 60 * 1000L;

    private Button btLocation, thridLocation;

    private TextView tvResult, thridResult, statesTv, zxingTitleTv, zxingCodeTv;

    public MainPresenter mMainPresenter;

    private AppCompatImageView centerImage;

    private boolean showZxing = true;

    private String qrUrl;

    private String headUrl;

    private String nickName;

//    private Handler handler = new Handler();

    private LocationDaoUtils mLocationDaoUtils;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_main;
    }

    @Override
    protected void initView() {
        zxingTitleTv = findViewById(R.id.zxing_title_tv);
        zxingCodeTv = findViewById(R.id.zxing_code_tv);

        centerImage = findViewById(R.id.center_img);
        centerImage.setOnClickListener(this);
        tvResult = findViewById(R.id.result_tv);
        thridResult = findViewById(R.id.thrid_result_tv);
        statesTv = findViewById(R.id.states_tv);
        btLocation = findViewById(R.id.location_btn);
        btLocation.setOnClickListener(this);
        thridLocation = findViewById(R.id.thrid_location_btn);
        thridLocation.setOnClickListener(this);
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
        new MainPresenter(this, MainActivity.this);
//        mMainPresenter.subscribe();
        registerActionReceiver();
        headUrl = PreferenceUtils.getPrefString(this, PreferenceConstants.HEAD_URL, "");
        nickName = PreferenceUtils.getPrefString(this, PreferenceConstants.NICK_NAME, "");
        if (!TextUtils.isEmpty(headUrl) && !TextUtils.isEmpty(nickName)) {
            showZxing = false;
            Picasso.with(MainActivity.this).load(headUrl).transform(new PicassoRoundTransform()).into(centerImage);
            zxingTitleTv.setText(nickName);
            zxingCodeTv.setText("点击可切换 头像/二维码");
            zxingCodeTv.setVisibility(View.VISIBLE);
        }
        mMainPresenter.queryZxingQr();

//        mMainPresenter.requestGpsCount();

//        mMainPresenter.initLocation();
//        mMainPresenter.startLocation();

        mMainPresenter.requestLocation();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterActionReceiver();
        mMainPresenter.unsubscribe();
//        mMainPresenter.stopGpsLocation();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.right_layout:
                Intent intent = new Intent();
                intent.setClass(MainActivity.this, SettingActivity.class);
                startActivity(intent);
                break;
            case R.id.zxing_bottom_layout:
                if (showZxing && !TextUtils.isEmpty(nickName) && !TextUtils.isEmpty(headUrl)) {
                    showZxing = false;
                    Picasso.with(MainActivity.this).load(headUrl).transform(new PicassoRoundTransform()).into(centerImage);

//                    Glide.with(this).load(headUrl).centerCrop().transform(new GlideRoundTransform(this)).into(centerImage);

                    zxingTitleTv.setText(nickName);
                    zxingCodeTv.setText("点击可切换 头像/二维码");
                    zxingCodeTv.setVisibility(View.VISIBLE);
                } else {
                    if (!TextUtils.isEmpty(qrUrl)) {
                        showZxing = true;
                        Picasso.with(MainActivity.this).load(qrUrl).transform(new PicassoRoundTransform()).into(centerImage);

//                        Glide.with(this).load(qrUrl).centerCrop().transform(new GlideRoundTransform(this)).into(centerImage);

                        zxingTitleTv.setText("扫码绑定后视镜");
                        zxingCodeTv.setVisibility(View.INVISIBLE);
                    }
                }
                break;
            case R.id.center_img:
                mMainPresenter.requestGpsCount();
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
        int positionType;
        if (location instanceof AMapLocation) {
            Logger.i(TAG, "onLocationChanged success，location ：经    度：" + location.getLongitude() + " 纬    度：" + location.getLatitude());
            positionType = 4;
            T.showShort(this, "高德定位成功！");
        } else {
            T.showShort(this, "GPS定位成功！");
            positionType = 1;
        }
        String content = StringTcpUtils.buildGpsContent(location.getLongitude(), location.getLatitude(), location.getAltitude(),
                location.getSpeed(), location.getBearing(), gpsCount, location.getAccuracy(), positionType, location.getTime(), lastPositionTime, "");
        content = StringTcpUtils.buildGpsString(App.getInstance().IMEI, content);
        if (mSocketService != null && NetUtils.isConnected(this)) {
            if (mSocketService.isConnect()) {
                mSocketService.sendMessage(content);
            } else {
                mSocketService.initSocketClient();
            }
        } else {
            if (mLocationDaoUtils == null) {
                mLocationDaoUtils = new LocationDaoUtils(this);
            }
            LocationData data = new LocationData();
            data.setContent(content);
            mLocationDaoUtils.insert(data);
        }
        zxingTitleTv.setText("定位成功，经    度：" + location.getLongitude() + " 纬    度：" + location.getLatitude());
    }

    @Override
    public void showStates(String message) {
        statesTv.setText(message);
    }

    @Override
    public void queryZxingQrSuccess(String url) {
        qrUrl = url;
//        mMainPresenter.requestGpsCount();
        if (!TextUtils.isEmpty(headUrl) && !TextUtils.isEmpty(nickName)) {
            return;
        }
        showZxing = true;
//        Picasso.with(this).load(url).transform(new PicassoRoundTransform()).into(centerImage);

        Glide.with(this).load(url).centerCrop().transform(new GlideRoundTransform(this)).into(centerImage);

        zxingTitleTv.setText("扫码绑定后视镜");
        zxingCodeTv.setText("微信码：FSDFWEQRRQWEWQ");
        zxingCodeTv.setVisibility(View.INVISIBLE);
    }

    @Override
    public void queryBusinessSucess(QueryBusinessResp resp) {
        if (resp != null && resp.getData() != null && !TextUtils.isEmpty(resp.getData().getHead_image_url())) {
            showZxing = false;
            headUrl = resp.getData().getHead_image_url();
            nickName = resp.getData().getNick_name();
            PreferenceUtils.setPrefString(this, PreferenceConstants.HEAD_URL, headUrl);
            PreferenceUtils.setPrefString(this, PreferenceConstants.NICK_NAME, nickName);

            Picasso.with(MainActivity.this).load(headUrl).transform(new PicassoRoundTransform()).into(centerImage);
            zxingTitleTv.setText(nickName);
            zxingCodeTv.setText("点击可切换 头像/二维码");
            zxingCodeTv.setVisibility(View.VISIBLE);
            QueryBusinessResp.DataBean bean = resp.getData();
            if (!TextUtils.isEmpty(bean.getLat())
                    && !TextUtils.isEmpty(bean.getLng())) {
                AMapUtils.startNaviActivity(this, "", bean.getName(), Double.valueOf(bean.getLat()),
                        Double.valueOf(bean.getLng()), Integer.valueOf(TextUtils.isEmpty(bean.getDev()) ? "0" : bean.getDev()),
                        Integer.valueOf(TextUtils.isEmpty(bean.getStyle()) ? "0" : bean.getStyle()));
            }
        } else {
            mMainPresenter.queryZxingQr();
            headUrl = "";
            nickName = "";
            PreferenceUtils.setPrefString(this, PreferenceConstants.HEAD_URL, "");
            PreferenceUtils.setPrefString(this, PreferenceConstants.NICK_NAME, "");
        }
    }

    public static final String ACTION_RE_MICRO_OR_PICTURE = "com.discovery.action.RE_MICRO_OR_PICTURE";
    public static final String KEY_CREATE_TIME = "key_create_time";
    public static final String KEY_CAMERAID = "key_camerid";
    public static final String KEY_TIME = "key_time";
    public static final String KEY_WIDTH = "key_width";
    public static final String KEY_HEIGHT = "key_heigh";
    public static final String KEY_KEY = "key_key";
    public static final String KEY_TYPE = "key_type";
    public static final String KEY_PATH = "key_path";
    public static final String KEY_ERROR = "key_error";

    public static final String MY_KEY = "my_demo";
    private ActionReceiver mReceiver;

    private CommandReceiver mCommandReceiver;

    /**
     * 注册广播监听；
     */
    public void registerActionReceiver() {
        Logger.d(TAG, "---------------- registerReceiver----------------");
        mReceiver = new ActionReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_RE_MICRO_OR_PICTURE);
        registerReceiver(mReceiver, intentFilter);

        mCommandReceiver = new CommandReceiver();
        IntentFilter commandFilter = new IntentFilter();
        commandFilter.addAction(ACTION_RECEIVER_COMMAND);
        registerReceiver(mCommandReceiver, commandFilter);
    }

    /**
     * 注销广播；
     */
    public void unregisterActionReceiver() {
        Logger.d(TAG, "-------------------- unRegisterReceiver------------------");
        unregisterReceiver(mReceiver);
        unregisterReceiver(mCommandReceiver);
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
                        if (key != null && key.equals(MY_KEY)) {
                            Logger.i(TAG, "回调成功" + path);
                            statesTv.setText("回调成功" + path);
                            if (!TextUtils.isEmpty(path)) {
                                Intent i = new Intent().setClass(MainActivity.this, UploadService.class);
                                i.putExtra("path", path);
                                if (path.endsWith("mp4")) {
                                    i.putExtra("type", Constact.FILE_TYPE_VEDIO);
                                } else if (path.endsWith("png") || path.endsWith("jpg")) {
                                    i.putExtra("type", Constact.FILE_TYPE_IMAGE);
                                }
                                startService(i);
                            }
                        }
                    }
                }
            } else {
                Logger.d(TAG, "ActionReceiver ------- intent == null");
            }
        }
    }

    public static final String ACTION_RECEIVER_COMMAND = "com.discovery.action.COMMAND";

    class CommandReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                String action = intent.getAction();
                Logger.d(TAG, "CommandReceiver ------- action : " + action);
                int type = intent.getIntExtra("command_type", Constact.COMMAND_TACK_IMAGE);
                int cameraid = intent.getIntExtra("cameraid", Constact.CAMERA_FRONT);
                switch (type) {
                    case Constact.COMMAND_TACK_IMAGE:
                        mMainPresenter.takePicture(MainActivity.this, System.currentTimeMillis(), cameraid, MY_KEY);
                        break;
                    case Constact.COMMAND_TACK_VIDEO:
                        mMainPresenter.takeMicroRecord(MainActivity.this, System.currentTimeMillis(), cameraid, MY_KEY);
                        break;
                    case Constact.COMMAND_SUCCESS:
                        T.showShort(MainActivity.this, "上传成功");
                        statesTv.setText("上传成功");
                        break;
                    case Constact.COMMAND_LOCATION:
//                        mMainPresenter.startLocation();
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
                    default:
                        break;
                }
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
