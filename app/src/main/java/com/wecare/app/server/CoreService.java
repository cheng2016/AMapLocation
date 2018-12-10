/*
 * Copyright © 2018 Yan Zhenjie.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.wecare.app.server;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.wecare.app.App;
import com.wecare.app.util.Logger;
import com.wecare.app.util.NetUtils;
import com.wecare.app.util.PreferenceConstants;
import com.wecare.app.util.PreferenceUtils;
import com.wecare.app.util.ToastUtils;
import com.yanzhenjie.andserver.AndServer;
import com.yanzhenjie.andserver.Server;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.TimeUnit;

/**
 * Created by Yan Zhenjie on 2018/6/9.
 */
public class CoreService extends Service {
    public static final String TAG = "CoreService";

    private Server mServer;

    @Override
    public void onCreate() {
        Logger.i(TAG, "onCreate, Thread: " + Thread.currentThread().getName());
        mServer = createServerBuilder().build();
    }

    private Server.Builder createServerBuilder() {
        Server.Builder builder = AndServer.serverBuilder()
//                .inetAddress(NetUtils.getLocalIPAddress())
                .inetAddress(getAddress())
                .port(8080)
                .timeout(10, TimeUnit.SECONDS)
                .listener(new Server.ServerListener() {
                    @Override
                    public void onStarted() {
                        String ip = mServer.getInetAddress().getHostAddress();
                        String rootUrl = "http://" + ip + ":8080/";
                        PreferenceUtils.setPrefString(App.getInstance(), PreferenceConstants.ROOT_URL, rootUrl);
                        ToastUtils.showLong("Server  onStarted  rootUrl：" + rootUrl);

                        Logger.i(TAG, "onStarted  hostAddress：" + ip + "  rootUrl：" + rootUrl);
//                        ServerManager.onServerStart(CoreService.this, ip);
                        PreferenceUtils.setPrefBoolean(App.getInstance(), PreferenceConstants.HTTP_SERVER_STATE, true);
                    }

                    @Override
                    public void onStopped() {
                        Logger.i(TAG, "onStarted  onStopped");
//                        ServerManager.onServerStop(CoreService.this);
                        ToastUtils.showLong("Server  onStopped");
                        PreferenceUtils.setPrefBoolean(App.getInstance(), PreferenceConstants.HTTP_SERVER_STATE, false);
                    }

                    @Override
                    public void onException(Exception e) {
                        Logger.e(TAG, "onStarted  onException", e);
//                        ServerManager.onServerError(CoreService.this, e.getMessage());
                        ToastUtils.showLong("Server  onException");
                    }
                });
        return builder;
    }

    InetAddress getAddress(){
        String ip = "192.168.43.1";
        InetAddress address;
        try{
            address = InetAddress.getByName(ip);
            return address;
        }catch (UnknownHostException e){
            Logger.e(TAG,"getAddress",e);
        }
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Logger.i(TAG, "onStartCommand, Thread: " + Thread.currentThread().getName());
//        boolean isConnect = PreferenceUtils.getPrefBoolean(App.getInstance(), PreferenceConstants.HTTP_SERVER_STATE, true);
//        if (!isConnect) {
//            mServer = createServerBuilder().build();
//            Logger.i(TAG, "onStartCommand 重新启动服务！");
//        }
        startServer();
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        PreferenceUtils.setPrefBoolean(App.getInstance(), PreferenceConstants.HTTP_SERVER_STATE, false);
        stopServer();
        Logger.i(TAG, "onDestroy, Thread: " + Thread.currentThread().getName());
        super.onDestroy();
    }

    /**
     * Start server.
     */
    private void startServer() {
        if (mServer.isRunning()) {
            String hostAddress = mServer.getInetAddress().getHostAddress();
//            ServerManager.onServerStart(CoreService.this, hostAddress);
            Logger.i(TAG, "startServer：" + hostAddress);
        } else {
            mServer.startup();
        }
    }

    /**
     * Stop server.
     */
    private void stopServer() {
        Logger.i(TAG, "stopServer");
        mServer.shutdown();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}