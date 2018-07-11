package com.wecare.app.module.service;

import android.content.Context;
import android.content.Intent;

import com.wecare.app.App;
import com.wecare.app.module.main.MainActivity;
import com.wecare.app.util.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.Socket;
import java.util.Arrays;

public class ReadRunnable implements Runnable {
    public static final String TAG = "ReadRunnable";

    private WeakReference<Socket> weakSocketReference;
    private boolean isStart = true;

    private Context context;

    public ReadRunnable(Socket socket,Context context) {
        weakSocketReference = new WeakReference<>(socket);
        this.context = context;
    }

    public void release() {
        isStart = false;
        releaseLastSocket(weakSocketReference);
    }

    @Override
    public void run() {
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
                        if (message.equals(SocketService.INIT_BEAT_STRING_RESPONSE)) {
                            Logger.i(TAG, "init success：" + message);
                        } else if (message.equals(SocketService.HEART_BEAT_STRING_RESPONSE)) {//处理心跳回复
                            Logger.i(TAG, "response heart：" + message);
                        } else if (message.startsWith("C02")) {
                            Logger.i(TAG, "init data：" + message);
                            String[] results = message.split("\\|");
                            if (results[5].startsWith("D04")) {
                                String data = results[5].substring(4);
                                results = data.split("\\;");
                                data = results[0];
                                results = data.split("\\,");
                                //如果域名和端口号有一个不一样则重启socket连接
                                if (!App.getInstance().HOST.equals(results[2])
                                        || App.getInstance().PORT != Integer.valueOf(results[1])) {
//                                    isReset = true;
                                }
                                App.getInstance().BASE_URL = results[0];
                                App.getInstance().PORT = Integer.valueOf(results[1]);
                                App.getInstance().HOST = results[2];
                                App.getInstance().MIN_GPS_UPLOAD_TIME = Integer.valueOf(results[3]) * 1000L;
//                                App.getInstance().SAME_GPS_UPLOAD_TIME = Integer.valueOf(results[4]) * 1000L;
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
                                    context.sendBroadcast(intent);
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
}
