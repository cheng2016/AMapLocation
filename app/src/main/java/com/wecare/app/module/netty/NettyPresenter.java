package com.wecare.app.module.netty;

import android.content.Context;
import android.content.Intent;

import com.wecare.app.util.Logger;

/**
 * Created by Administrator Chengzj
 *
 * @date 2018/10/18 18:24
 */
public class NettyPresenter implements NettyContract.Presenter {
    public static final String TAG = "NettyPresenter";

    private NettyContract.View view;

    public NettyPresenter(NettyContract.View view) {
        this.view = view;
        view.setPresenter(this);
    }

    public static final String ACTION_CAMERE_CORE_MICRO_RECORD = "com.discovery.action.CAMERA_CORE_MICRO_RECORD";
    public static final String ACTION_CAMERE_CORE_TAKE_PICTURE = "com.discovery.action.CAMERA_CORE_TAKE_PICTURE";
    public static final String KEY_CREATE_TIME = "key_create_time";
    public static final String KEY_CAMERAID = "key_camerid";
    public static final String KEY_KEY = "key_key";
//    public static final String KEY_USERID = "key_userid";

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
    public void unsubscribe() {

    }
}
