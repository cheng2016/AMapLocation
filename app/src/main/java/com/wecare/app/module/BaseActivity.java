package com.wecare.app.module;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.wecare.app.module.netty.NettyService;
import com.wecare.app.util.Logger;

/**
 * Created by chengzj on 2017/6/17.
 */

public abstract class BaseActivity extends AppCompatActivity {
    public String TAG = "";

    public NettyService mSocketService;

    protected ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            NettyService.MyBinder binder = (NettyService.MyBinder) service;
            mSocketService = binder.getService();
            Logger.v(TAG,"onServiceConnected");
        }
        //client 和service连接意外丢失时，会调用该方法
        @Override
        public void onServiceDisconnected(ComponentName name) {
            Logger.v(TAG,"onServiceDisconnected");
        }
    };


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        TAG = this.getClass().getSimpleName();
        super.onCreate(savedInstanceState);
        setContentView(getLayoutId());
        initView();
        initData(savedInstanceState);
        Intent intent = new Intent(this, NettyService.class);
        intent.putExtra("from", TAG);
        bindService(intent,mServiceConnection,BIND_AUTO_CREATE);
    }

    protected abstract int getLayoutId();

    protected abstract void initView();

    protected abstract void initData(Bundle savedInstanceState);

    @Override
    protected void onDestroy() {
        unbindService(mServiceConnection);
        Logger.i(TAG,"onDestroy");
        super.onDestroy();
    }
}
