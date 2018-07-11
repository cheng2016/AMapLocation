package com.wecare.app.module.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;

import com.wecare.app.App;
import com.wecare.app.data.entity.LocationData;
import com.wecare.app.data.source.local.LocationDaoUtils;
import com.wecare.app.module.main.MainActivity;
import com.wecare.app.util.Logger;
import com.wecare.app.util.StringTcpUtils;
import com.wecare.app.util.ThreadPoolManager;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.net.Socket;
import java.util.Arrays;
import java.util.List;

public class SocketService extends Service {
    public static final String TAG = "SocketService";
    //心跳包频率
//    private long HEART_BEAT_RATE = App.getInstance().HEART_BEAT_RATE;

//    public String HOST = App.getInstance().HOST;// //

//    public int PORT =  App.getInstance().PORT;

//    public static final String IMEI = DeviceUtils.getDeviceIMEI(App.getInstance());

    //心跳包内容
    public static final String HEART_BEAT_STRING = StringTcpUtils.buildHeartReq(App.getInstance().IMEI);

    public static final String HEART_BEAT_STRING_RESPONSE = StringTcpUtils.buildHeartResp(App.getInstance().IMEI);

    public static final String INIT_BEAT_STRING = StringTcpUtils.buildInitReq(App.getInstance().IMEI);

    public static final String INIT_BEAT_STRING_RESPONSE = StringTcpUtils.buildInitResp(App.getInstance().IMEI);

    public static final String GET_DATA_STRING = StringTcpUtils.buildGetDataReq(App.getInstance().IMEI);

    private WeakReference<Socket> mWeakSocketReference;

    private ReadThread mReadThread;

    private long sendTime = 0L;

    private boolean isConnect = false;

    private boolean isReset = false;

    private LocationDaoUtils daoUtils;

    public final static int SQL_MAX_COUNT = 30;

    private Handler initHandler = new Handler();

/*    Runnable initRunnable = new Runnable() {
        @Override
        public void run() {
            sendMessage(INIT_BEAT_STRING);
        }
    };
    Runnable heartRunnable = new Runnable() {
        @Override
        public void run() {
            sendMessage(HEART_BEAT_STRING);
        }
    };
    Runnable getDataRunnable = new Runnable() {
        @Override
        public void run() {
            sendMessage(GET_DATA_STRING);
        }
    };*/

    // For heart Beat
    private Handler handler = new Handler();
    private Runnable heartBeatRunnable = new Runnable() {
        @Override
        public void run() {
            if (System.currentTimeMillis() - sendTime >= App.getInstance().HEART_BEAT_RATE) {
                sendMessage(HEART_BEAT_STRING);
                boolean isSuccess = isConnect;//就发送一个HEART_BEAT_STRING过去 如果发送失败，就重新初始化一个socket
                if (!isSuccess || isReset) {
                    isReset = false;
                    handler.removeCallbacks(heartBeatRunnable);
                    mReadThread.release();
                    releaseLastSocket(mWeakSocketReference);
                    initSocketClient();
                }
            }
            handler.postDelayed(this, App.getInstance().HEART_BEAT_RATE);
        }
    };

    public boolean sendMsg(String msg) {
        Logger.e(TAG, "sendMsg msg：" + msg);
        if (null == mWeakSocketReference || null == mWeakSocketReference.get()) {
            return false;
        }
        Socket socket = mWeakSocketReference.get();
        try {
            if (!socket.isClosed() && !socket.isOutputShutdown()) {
                OutputStream os = socket.getOutputStream();
                String message = msg;
                os.write(message.getBytes());
                os.flush();
                if (HEART_BEAT_STRING.equals(msg)) {
                    sendTime = System.currentTimeMillis();//每次发送成数据，就改一下最后成功发送的时间，节省心跳间隔时间
                }
            } else {
                return false;
            }
        } catch (IOException e) {
            Logger.w(TAG, "sendMsg exception：", e);
            return false;
        }
        return true;
    }

    public void sendMessage(final String msg) {
        ThreadPoolManager.getInstance().execute(new Runnable() {
            @Override
            public void run() {
                isConnect = sendMsg(msg);
            }
        });
    }

    /**
     * 释放socket资源
     *
     * @param weakReference
     */
    private void releaseLastSocket(WeakReference<Socket> weakReference) {
        try {
            if (null != weakReference) {
                Socket socket = weakReference.get();
                if (socket != null && !socket.isClosed()) {
                    socket.close();
                }
            }
        } catch (IOException e) {
            Logger.w(TAG, "releaseLastSocket exception：", e);
        }
    }

    private class InitSocketThread extends Thread {
        @Override
        public void run() {
            super.run();
            initSocket();
        }

        void initSocket() {//初始化Socket
            Logger.i(TAG, "initSocket");
            try {
                Socket so = new Socket(App.getInstance().HOST, App.getInstance().PORT);
                mWeakSocketReference = new WeakReference<>(so);
                mReadThread = new ReadThread(so);
                mReadThread.start();
                handler.postDelayed(heartBeatRunnable, App.getInstance().HEART_BEAT_RATE);//初始化成功后，就准备发送心跳包
            } catch (Exception e) {
                Logger.w(TAG, "initSocket exception：", e);
            }
        }
    }

    // Thread to read content from Socket
    private class ReadThread extends Thread {
        private WeakReference<Socket> weakSocketReference;
        private boolean isStart = true;

        public ReadThread(Socket socket) {
            weakSocketReference = new WeakReference<>(socket);
        }

        public void release() {
            isStart = false;
            releaseLastSocket(weakSocketReference);
        }

        @Override
        public void run() {
            super.run();
            Socket socket = weakSocketReference.get();
            if (null != socket) {
                try {
                    InputStream is = socket.getInputStream();
                    byte[] buffer = new byte[1024];
                    int length = 0;
                    while (!socket.isClosed() && !socket.isInputShutdown()
                            && isStart && ((length = is.read(buffer)) != -1)) {
                        if (length > 0) {
                            String message = new String(Arrays.copyOf(buffer,
                                    length));
                            //收到服务器过来的消息，就通过Broadcast发送出去
                            if (message.equals(INIT_BEAT_STRING_RESPONSE)) {
                                Logger.i(TAG, "response init success：" + message);
                            } else if (message.equals(HEART_BEAT_STRING_RESPONSE)) {//处理心跳回复
                                Logger.i(TAG, "response heart：" + message);
                            } else if (message.startsWith("C02")) {
                                Logger.i(TAG, "response init data：" + message);
                                String[] results = message.split("\\|");
                                if (results[5].startsWith("D04")) {
                                    String data = results[5].substring(4);
                                    results = data.split("\\;");
                                    data = results[0];
                                    results = data.split("\\,");
                                    //如果域名和端口号有一个不一样则重启socket连接
                                    if (!App.getInstance().HOST.equals(results[2])
                                            || App.getInstance().PORT != Integer.valueOf(results[1])) {
                                        isReset = true;
                                    }
                                    App.getInstance().BASE_URL = results[0];
                                    App.getInstance().PORT = Integer.valueOf(results[1]);
                                    App.getInstance().HOST = results[2];
//                                    App.getInstance().MIN_GPS_UPLOAD_TIME = Integer.valueOf(results[3]) * 1000L;
//                                    App.getInstance().SAME_GPS_UPLOAD_TIME = Integer.valueOf(results[4]) * 1000L;
                                    App.getInstance().HEART_BEAT_RATE = Integer.valueOf(results[5]) * 1000L;
                                }
                            } else {
                                if (message.startsWith("C16")) {
                                    Logger.i(TAG, "server give me a commad：" + message);
                                    String[] results = message.split("\\|");
                                    if (results[5].startsWith("D01")) {
                                        //发送广播
                                        int commandType = Integer.parseInt(results[5].substring(4));
                                        Intent intent = new Intent(MainActivity.ACTION_RECEIVER_COMMAND);
                                        intent.putExtra("command_type", commandType);
                                        sendBroadcast(intent);
                                        //回复服务器消息，告诉服务器我已接受到该消息
                                        sendMessage(StringTcpUtils.buildSuccessString(App.getInstance().IMEI, results[6]));
                                    }

                                } else {
                                    //其他消息回复
                                    Logger.i(TAG, "response this is a message：" + message);
                                }
                            }
                        }
                    }
                } catch (IOException e) {
                    Logger.w(TAG, "read exception：", e);
                }
            }
        }
    }



    public void initSocketClient() {
        if (daoUtils == null) {
            daoUtils = new LocationDaoUtils(this);
        }
        new InitSocketThread().start();
        initHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                sendMessage(INIT_BEAT_STRING);
            }
        }, 200);
        initHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                sendMessage(HEART_BEAT_STRING);
            }
        }, 400);
        initHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                sendMessage(GET_DATA_STRING);
            }
        }, 600);
        initHandler.postDelayed(uploadRunnable, 900);
    }

    Runnable uploadRunnable = new Runnable() {
        @Override
        public void run() {
            List<LocationData> list = daoUtils.queryLocationData(SQL_MAX_COUNT);
            Logger.i(TAG, "greendao excute location data size is：" + list.size());
            if (list != null && list.size() > 0) {
                for (LocationData data : list) {
                    if(isConnect){
                        sendMessage(data.getContent());
                        daoUtils.deleteLocationData(data);
                    }
                }
            }
            if(list.size() == SQL_MAX_COUNT && isConnect){
                initHandler.postDelayed(this,10 * 1000);
            }
        }
    };

    public class MyBinder extends Binder {
        public SocketService getService() {
            return SocketService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Logger.i(TAG, "onCreate, Thread: " + Thread.currentThread().getName());
        initSocketClient();

        //动态注册广播接收器
//        msgReceiver = new MsgReceiver();
//        IntentFilter intentFilter = new IntentFilter();
//        intentFilter.addAction("com.example.communication.RECEIVER");
//        registerReceiver(msgReceiver, intentFilter);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Logger.i(TAG, "onStartCommand, Thread: " + Thread.currentThread().getName());
        return START_REDELIVER_INTENT;
    }

    @Override
    public IBinder onBind(Intent intent) {
        Logger.i(TAG, "onBind, Thread: " + Thread.currentThread().getName());
        return new MyBinder();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Logger.i(TAG, "onUnbind, from:" + intent.getStringExtra("from"));
        return false;
    }

    @Override
    public void onDestroy() {
        Logger.i(TAG, "onDestroy, Thread: " + Thread.currentThread().getName());
        super.onDestroy();
//        unregisterReceiver(msgReceiver);
    }

    public boolean isConnect() {
        return isConnect;
    }

    /*    MsgReceiver msgReceiver;
     *
     * 广播接收器
     * @author len
     *
     *//*
	public class MsgReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
		    String message = intent.getStringExtra("message");
		    if(isConnect){
                sendMessage(message);
            }else{
		        initSocketClient();
            }
		}
	}*/
}
