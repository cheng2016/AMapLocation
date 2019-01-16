package com.wecare.app.module.netty;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Message;

import com.google.gson.Gson;
import com.wecare.app.App;
import com.wecare.app.data.entity.LocationData;
import com.wecare.app.data.entity.VideoData;
import com.wecare.app.data.source.local.LocationDaoUtils;
import com.wecare.app.data.source.local.VideoDaoUtils;
import com.wecare.app.module.main.MainActivity;
import com.wecare.app.module.service.UploadService;
import com.wecare.app.util.Constact;
import com.wecare.app.util.DeviceUtils;
import com.wecare.app.util.LogUtils;
import com.wecare.app.util.Logger;
import com.wecare.app.util.NetUtils;
import com.wecare.app.util.PreferenceConstants;
import com.wecare.app.util.PreferenceUtils;
import com.wecare.app.util.StringTcpUtils;

import java.util.List;
import java.util.concurrent.TimeUnit;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;

/**
 * TCP 服务
 * <p>
 * Created by chengzj 2018/07/14
 */
public class NettyService extends Service implements NettyContract.View {
    public static final String TAG = "NettyService";

    public static final String UP_MSG_END_FLAG = new String(new byte[]{0x01, 0x01, 0x01});

    public final static int SQL_MAX_COUNT = 30;

    public final static long UPLOAD_OFFLINE_DATA_INTERVAL = 10 * 1000L;

    public final static long UPLOAD_VIDEO_OFFLINE_INTERVAL = 15 * 1000L;
    //心跳包内容
    public String HEART_BEAT_STRING;

    private String HEART_BEAT_STRING_RESPONSE;

    private String INIT_BEAT_STRING;

    private String INIT_BEAT_STRING_RESPONSE;

    private String GET_DATA_STRING;

    private Bootstrap mBootstrap;

    private EventLoopGroup eventLoopGroup;

    ChannelFuture mChannelFuture;

    private boolean isConnect = false;

    private Handler handler = new Handler();

    private LocationDaoUtils daoUtils;

    private Context context;

    private String imei;

    NettyContract.Presenter mPresenter;

    private void initData() {
        imei = PreferenceUtils.getPrefString(this, PreferenceConstants.IMEI, DeviceUtils.getDeviceIMEI(this));
        HEART_BEAT_STRING = StringTcpUtils.buildHeartReq(imei);
        HEART_BEAT_STRING_RESPONSE = StringTcpUtils.buildHeartResp(imei);
        INIT_BEAT_STRING = StringTcpUtils.buildInitReq(imei);
        INIT_BEAT_STRING_RESPONSE = StringTcpUtils.buildInitResp(imei);
        GET_DATA_STRING = StringTcpUtils.buildGetDataReq(imei);
        daoUtils = new LocationDaoUtils(this);
        context = this;
    }

    @Override
    public void setPresenter(NettyContract.Presenter presenter) {
        mPresenter = presenter;
    }

    public class MyBinder extends Binder {
        public NettyService getService() {
            return NettyService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Logger.i(TAG, "onCreate, Thread: " + Thread.currentThread().getName());
        PreferenceUtils.setPrefBoolean(this, PreferenceConstants.SERVICE_NETTY_STATE, true);
        new NettyPresenter(this);
        initData();
        /**
         * 在子线程 建立连接并向服务器发送请求，这里采用了`HanlderThread`+`Handler`的方案。
         * 通过`Looper`依次从`Handler`的队列中获取信息，逐个进行处理，保证安全，不会出现混乱。
         */
        workThread = new HandlerThread(NettyService.class.getName());
        workThread.start();
        mWorkHandler = new Handler(workThread.getLooper(), mWorkHandlerCallback);
        mWorkHandler.sendEmptyMessage(MESSAGE_INIT);
        mWorkHandler.sendEmptyMessageDelayed(MESSAGE_CONNECT, 100);
    }

    @Override
    public IBinder onBind(Intent intent) {
        Logger.i(TAG, "onBind, Thread: " + Thread.currentThread().getName());
        return new NettyService.MyBinder();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Logger.i(TAG, "onUnbind, from:" + intent.getStringExtra("from"));
        return false;
    }

    @Override
    public void onDestroy() {
        Logger.i(TAG, "onDestroy, Thread: " + Thread.currentThread().getName());
        PreferenceUtils.setPrefBoolean(this, PreferenceConstants.SERVICE_NETTY_STATE, false);
        super.onDestroy();
        //释放资源
        workThread.quit();
    }

    final class NettyClientHandler extends ChannelInboundHandlerAdapter {
        @Override
        public void channelActive(ChannelHandlerContext ctx) {
            Logger.i(TAG, "channelActive 上线了");
            ctx.write(INIT_BEAT_STRING);
            ctx.write(HEART_BEAT_STRING);
            ctx.writeAndFlush(GET_DATA_STRING);
            Logger.i(TAG, "channelActive wirte：" + INIT_BEAT_STRING);
            Logger.i(TAG, "channelActive wirte：" + HEART_BEAT_STRING);
            Logger.i(TAG, "channelActive wirte：" + GET_DATA_STRING);
            handler.postDelayed(uploadRunnable, 200);
        }

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) {
            String message = (String) msg;
            LogUtils.file(message);
            //收到服务器过来的消息，就通过Broadcast发送出去
            if (INIT_BEAT_STRING_RESPONSE.startsWith(message)) {
                Logger.i(TAG, "response init success：" + message);
            } else if (HEART_BEAT_STRING_RESPONSE.startsWith(message)) {//处理心跳回复
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
                        Logger.i(TAG, "init data reset Nettysocket");
                        isConnect = false;
                    }
                    App.getInstance().BASE_URL = results[0];
                    App.getInstance().PORT = Integer.valueOf(results[1]);
                    App.getInstance().HOST = results[2];
                    App.getInstance().MIN_GPS_UPLOAD_TIME = Integer.valueOf(results[3]) * 1000L;
                    App.getInstance().SAME_GPS_UPLOAD_TIME = Integer.valueOf(results[4]) * 1000L;
                    App.getInstance().HEART_BEAT_RATE = Integer.valueOf(results[5]) * 1000L;

                    Intent intent = new Intent(MainActivity.ACTION_RECEIVER_COMMAND);
                    intent.putExtra("command_type", Constact.COMMAND_START_LOCATION);
                    sendBroadcast(intent);
                }
            } else {
                if (message.startsWith("C16")) {
                    Logger.i(TAG, "server give me a commad：" + message);
                    String[] results = message.split("\\|");
                    if (results[5].startsWith("D01")) {
                        //优先回复服务器消息，告诉服务器我已接受到该消息，以免广播接收器卡顿
                        sendMessage(StringTcpUtils.buildSuccessString(imei, results[6]));
                        int commandType;
                        if (results[5].substring(4).contains("61") || results[5].substring(4).contains("63")) {
                            commandType = Integer.parseInt(results[5].substring(4, 6));
                            String userId = results[5].substring(6);
                            Logger.i(TAG, "命令：" + commandType + "  userId：" + userId);
                            switch (commandType) {
                                case Constact.COMMAND_TACK_IMAGE:
                                    mPresenter.takePicture(context, System.currentTimeMillis(), Constact.CAMERA_FRONT, userId);
                                    break;
                                case Constact.COMMAND_TACK_VIDEO:
                                    mPresenter.takeMicroRecord(context, System.currentTimeMillis(), Constact.CAMERA_FRONT, userId);
                                    break;
                                default:
                                    break;
                            }
                        } else {
                            commandType = Integer.parseInt(results[5].substring(4));
                            //发送广播
                            Intent intent = new Intent(MainActivity.ACTION_RECEIVER_COMMAND);
                            intent.putExtra("command_type", commandType);
                            sendBroadcast(intent);
                        }
                    }
                } else {
                    //其他消息回复
                    Logger.i(TAG, "response this is a message：" + message);
                }
            }
        }

        @Override
        public void channelReadComplete(ChannelHandlerContext ctx) {
            ctx.flush();
        }

        @Override
        public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
            super.userEventTriggered(ctx, evt);
            if (evt instanceof IdleStateEvent) {
                IdleStateEvent event = (IdleStateEvent) evt;
                if (event.state().equals(IdleState.READER_IDLE)) {
                    Logger.i(TAG, "userEventTriggered READER_IDLE 读超时，长期没收到服务器推送数据！Channel is active：" + mChannelFuture.channel().isActive());
                    //可以选择重新连接
                    isConnect = false;
                    ctx.close();
                } else if (event.state().equals(IdleState.WRITER_IDLE)) {
                    Logger.i(TAG, "userEventTriggered WRITER_IDLE 写超时，长期未向服务器发送数据！Channel is active：" + mChannelFuture.channel().isActive());
                    //发送心跳包
                    Logger.e(TAG, "sendMessage：" + HEART_BEAT_STRING);
                    ctx.writeAndFlush(HEART_BEAT_STRING);
                } else if (event.state().equals(IdleState.ALL_IDLE)) {
                    Logger.i(TAG, "userEventTriggered ALL 没有接收或发送数据一段时间");
                    //发送心跳包
                    Logger.e(TAG, "sendMessage：" + HEART_BEAT_STRING);
                    ctx.writeAndFlush(HEART_BEAT_STRING);
                }
            }
        }

        @Override
        public void channelInactive(ChannelHandlerContext ctx) {
            Logger.e(TAG, "channelInactive 掉线了");
            isConnect = false;
            //在线情况下，断线直接执行重连机制
            sendReconnectMessage();
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            Logger.i(TAG, "exceptionCaught：" + cause);
            isConnect = false;
            ctx.close();
        }
    }

    Runnable uploadRunnable = new Runnable() {
        @Override
        public void run() {
            List<LocationData> list = daoUtils.queryLocationData(SQL_MAX_COUNT);
            Logger.i(TAG, "greendao excute location data size is：" + list.size());
            if (list != null && list.size() > 0) {
                for (LocationData data : list) {
                    if (mChannelFuture.channel() != null && mChannelFuture.channel().isOpen() && isConnect) {
                        Logger.e(TAG, "sendMessage：" + data.getContent());
                        mChannelFuture.channel().writeAndFlush(data.getContent());
                        daoUtils.deleteLocationData(data);
                    }
                }
                if (list.size() == SQL_MAX_COUNT && isConnect) {
                    handler.postDelayed(this, UPLOAD_OFFLINE_DATA_INTERVAL);
                }
            } else {
                handler.post(videoRunnable);
            }
        }
    };


    Runnable videoRunnable = new Runnable() {
        @Override
        public void run() {
            VideoDaoUtils videoDaoUtils = new VideoDaoUtils(context);
            List<VideoData> list = videoDaoUtils.queryAllVideoData();
            Logger.i(TAG, "greendao excute Video data size is：" + list.size());
            if (list != null && list.size() > 0) {
                VideoData data = list.get(0);
                Logger.i(TAG, "离线碰撞视频上传中：" + new Gson().toJson(data));
                Intent i = new Intent().setClass(context, UploadService.class);
                i.putExtra("userId", data.getUserId());
                i.putExtra("type", Constact.FILE_TYPE_COLLISION);
                i.putExtra("timeTemp", data.getTimeTemp());
                i.putExtra("path", data.getPath());
                startService(i);
                videoDaoUtils.deleteVideoData(data);
                handler.postDelayed(this, UPLOAD_VIDEO_OFFLINE_INTERVAL);
            }
        }
    };

    private final String ACTION_SEND_MSG = "action_send_msg";

    private final int MESSAGE_INIT = 0x01;
    private final int MESSAGE_CONNECT = 0x02;
    private final int MESSAGE_SEND = 0x03;

    private HandlerThread workThread = null;

    private Handler mWorkHandler = null;

    private Handler.Callback mWorkHandlerCallback = new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case MESSAGE_INIT:
                    Logger.d(TAG, "WorkHandlerCallback 初始化Tcp服务 ....");
                    initNetty();
                    break;
                case MESSAGE_CONNECT:
                    Logger.d(TAG, "WorkHandlerCallback 连接远程服务器 ....");
                    connectNetty();
                    break;
                case MESSAGE_SEND:
                    String sendMsg = msg.getData().getString(ACTION_SEND_MSG);
                    Logger.d(TAG, "WorkHandlerCallback 向服务器发送数据 .... " + sendMsg);
                    try {
                        if (mChannelFuture.channel() != null && mChannelFuture.channel().isOpen() && isConnect) {
                            mChannelFuture.channel().writeAndFlush(sendMsg).sync();
                            Logger.d(TAG, "send succeed " + sendMsg);
                        } else {
                            throw new Exception("channel is null | closed | isConnect：" + isConnect);
                        }
                    } catch (Exception e) {
                        Logger.e(TAG, "send failed ", e);
                        sendReconnectMessage();
                    }
                    break;
            }
            return true;
        }
    };

    private void initNetty() {
        mBootstrap = new Bootstrap();
        eventLoopGroup = new NioEventLoopGroup();
        mBootstrap.group(eventLoopGroup);
        mBootstrap.channel(NioSocketChannel.class);
        mBootstrap.option(ChannelOption.TCP_NODELAY, true);
//                                    mBootstrap.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 3000);
        mBootstrap.option(ChannelOption.WRITE_BUFFER_HIGH_WATER_MARK, 64 * 1024);
        mBootstrap.option(ChannelOption.WRITE_BUFFER_LOW_WATER_MARK, 32 * 1024);
        mBootstrap.handler(new ChannelInitializer() {
            @Override
            protected void initChannel(Channel ch) {
                ChannelPipeline pipeline = ch.pipeline();
                ByteBuf delimiter = Unpooled.copiedBuffer(UP_MSG_END_FLAG.getBytes());
                pipeline.addLast(new DelimiterBasedFrameDecoder(Integer.MAX_VALUE, delimiter));
                pipeline.addLast(new IdleStateHandler(0,
                        0, 2, TimeUnit.MINUTES));
                pipeline.addLast("decoder", new StringDecoder());
                pipeline.addLast("encoder", new StringEncoder());
                pipeline.addLast(new NettyClientHandler());
            }
        });
    }

    private void connectNetty() {
        if (!isConnect) {
            try {
                // 发起异步连接操作
                mChannelFuture = mBootstrap.connect(App.getInstance().HOST, App.getInstance().PORT).addListener(new ChannelFutureListener() {
                    @Override
                    public void operationComplete(ChannelFuture future) throws Exception {
                        if (future.isSuccess()) {
                            isConnect = true;
                            Logger.i(TAG, "Connect to server successfully!");
                        } else {
                            isConnect = false;
                            Logger.i(TAG, "Failed to connect to server，After the connection will be reconnected");
                        }
                    }
                }).sync();
                // 当代客户端链路关闭
                mChannelFuture.channel().closeFuture().sync();
            } catch (Exception e) {
                Logger.e(TAG, "channel connect exception", e);
                sendReconnectMessage();
            }
        }
    }

    private void sendReconnectMessage() {
        int netWorkState = NetUtils.getNetworkState(App.getInstance());
        switch (netWorkState) {
            case NetUtils.NETWORK_MOBILE:// 移动网络 8 秒重连一次
                mWorkHandler.sendEmptyMessageDelayed(MESSAGE_CONNECT, 8 * 1000);
                break;
            case NetUtils.NETWORK_WIFI:// wifi网络 10 秒重连一次
                mWorkHandler.sendEmptyMessageDelayed(MESSAGE_CONNECT, 10 * 1000);
                break;
            case NetUtils.NETWORK_NONE:
                Logger.e(TAG,"sendReconnectMessage 无网络不执行重连操作");
                break;
        }
    }

    public void sendMessage(String msg) {
        Message message = new Message();
        message.what = MESSAGE_SEND;
        Bundle bundle = new Bundle();
        bundle.putString(ACTION_SEND_MSG, msg);
        message.setData(bundle);
        mWorkHandler.sendMessage(message);
    }
}