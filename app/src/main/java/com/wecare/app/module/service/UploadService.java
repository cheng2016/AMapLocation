package com.wecare.app.module.service;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.wecare.app.App;
import com.wecare.app.data.entity.VideoData;
import com.wecare.app.data.source.local.VideoDaoUtils;
import com.wecare.app.util.AirplaneModeUtils;
import com.wecare.app.util.Constact;
import com.wecare.app.util.DeviceUtils;
import com.wecare.app.util.Logger;
import com.wecare.app.util.NetUtils;
import com.wecare.app.util.PreferenceConstants;
import com.wecare.app.util.PreferenceUtils;
import com.wecare.app.util.StringTcpUtils;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.Socket;
import java.util.Arrays;

/**
 * TCP文件上传服务类，用完即走
 */
public class UploadService extends IntentService {
    public static final String TAG = "UploadService";

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public UploadService(String name) {
        super(name);
    }

    public UploadService() {
        super(TAG);
    }

//    private static final String SERVER_IP = SocketService.HOST; // 服务端IP

    //    private static final int SERVER_PORT = SocketService.PORT + 4; // 服务端端口
    //连接超时时间
    public final static int SOCKET_CONNECT_TIME_OUT = 10 * 1000;
    //读写超时时间
    public final static int SOCKET_READ_WRITE_TIME_OUT = 5 * 1000;

    private Socket client;

    private FileInputStream fis;

    private DataOutputStream dos;

    private InputStream is;

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        String userId = intent.getStringExtra("userId");
        int type = intent.getIntExtra("type", Constact.FILE_TYPE_IMAGE);
        String path = intent.getStringExtra("path");
        long timeTemp = intent.getLongExtra("timeTemp", System.currentTimeMillis());
        Logger.i(TAG, "type：" + type + " path：" + path);

        if (!NetUtils.isConnected(this) && type == Constact.FILE_TYPE_COLLISION) {
            if (AirplaneModeUtils.isAirplaneModeOn(this)) {//监测是否打开飞行模式
                Logger.i(TAG, "............onHandleIntent 关闭飞行模式上传碰撞视频............");
                AirplaneModeUtils.setAirplaneModeOn(this, false);//如果已经打开则关闭飞行模式
            }

            VideoDaoUtils daoUtils = new VideoDaoUtils(this);
            VideoData videoData = new VideoData();
            videoData.setUserId(userId);
            videoData.setType(type);
            videoData.setPath(path);
            videoData.setTimeTemp(timeTemp);
            daoUtils.insert(videoData);
            Logger.i(TAG, "离线碰撞视频存储到数据库中...............");
            return;
        }
        try {
            client = new Socket(App.getInstance().HOST, App.getInstance().PORT + 4);
            client.setSoTimeout(SOCKET_READ_WRITE_TIME_OUT);//设置socket超时时间，超过该时间自动断开连接
            File file = new File(path);
            if (file.exists()) {
                fis = new FileInputStream(file);
                dos = new DataOutputStream(client.getOutputStream());
                String content;
                if (TextUtils.isEmpty(userId)) {
                    content = StringTcpUtils.buildUploadString(type, DeviceUtils.getDeviceIMEI(App.getInstance()), file.length(), timeTemp);
                } else {
                    content = StringTcpUtils.buildUploadString(userId, type, DeviceUtils.getDeviceIMEI(App.getInstance()), file.length(), timeTemp);
                }
                Logger.i(TAG, "content：" + content);
                //总包长度 = 文本length +　文件length　＋ 1
                long allByteLength = content.length();
                allByteLength += file.length() + 1;
                dos.write(StringTcpUtils.intToByte4((int) allByteLength));
                dos.flush();
                dos.write((byte) content.length());
                dos.flush();
                dos.write(content.getBytes());
                dos.flush();
                // 开始传输文件
                Logger.i(TAG, "======== 开始传输文件 ========");
                byte[] bytes = new byte[1024];
                int length = 0;
                long progress = 0;
                while ((length = fis.read(bytes, 0, bytes.length)) != -1) {
                    dos.write(bytes, 0, length);
                    dos.flush();
                    progress += length;
                }
                Logger.i(TAG, "======== 文件传输成功 ========");

                is = client.getInputStream();
                byte[] buffer = new byte[1024];
                length = 0;
                while (!client.isClosed() && !client.isInputShutdown()
                        && ((length = is.read(buffer)) != -1)) {
                    if (length > 0) {
                        String message = new String(Arrays.copyOf(buffer,
                                length));
                        //收到服务器过来的消息，就通过Broadcast发送出去
                        Logger.i(TAG, "======== 服务器返回 ：" + message);
                        String[] results = message.split("\\|");
                        if (results.length > 0) {
                            if ("D01:72".equals(results[5].trim())) {
                                Logger.i(TAG, "======== upload success ==========");
                            }
                        }
                    }
                    closelAll();
                }
            }
        } catch (Exception e) {
            Logger.e(TAG, "onHandleIntent Exception", e);
        } finally {
            try {
                closelAll();
            } catch (Exception e) {
                Logger.e(TAG, "onHandleIntent Exception", e);
            }
        }
    }

    public void closelAll() throws Exception {
        if (client != null && !client.isClosed()) {
            client.shutdownOutput();
            client.shutdownInput();
            fis.close();
            is.close();
            dos.close();
            client.close();
        }
        //熄火状态下才开启重新开启飞行模式
        if (!AirplaneModeUtils.isAccOn()) {
            Logger.i(TAG, "............closelAll 打开飞行模式............");
            AirplaneModeUtils.setAirplaneModeOn(this, true);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Logger.i(TAG, "onDestroy");
    }
}
