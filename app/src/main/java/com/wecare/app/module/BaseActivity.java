package com.wecare.app.module;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;

import com.wecare.app.module.service.SocketService;
import com.wecare.app.util.Logger;

/**
 * Created by chengzj on 2017/6/17.
 */

public abstract class BaseActivity extends AppCompatActivity {
    public String TAG = "";

    public SocketService mSocketService;

    protected ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            SocketService.MyBinder binder = (SocketService.MyBinder) service;
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
        init(savedInstanceState);
        Intent intent = new Intent(this, SocketService.class);
        intent.putExtra("from", TAG);
        bindService(intent,mServiceConnection,BIND_AUTO_CREATE);
    }

    protected abstract int getLayoutId();

    protected abstract void init(Bundle savedInstanceState);

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(mServiceConnection);
        Logger.i(TAG,"onDestroy");
    }
}
