package com.wecare.app.module.netty;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;

import com.wecare.app.App;
import com.wecare.app.data.entity.LocationData;
import com.wecare.app.data.source.local.LocationDaoUtils;
import com.wecare.app.module.main.MainActivity;
import com.wecare.app.util.Constact;
import com.wecare.app.util.Logger;
import com.wecare.app.util.NetUtils;
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

/**
 * TCP 服务
 * <p>
 * Created by chengzj 2018/07/14
 */
public class NettyService extends Service {
    public static final String TAG = "NettyService";

    //心跳包内容
    public static final String HEART_BEAT_STRING = StringTcpUtils.buildHeartReq(App.getInstance().IMEI);

    public static final String HEART_BEAT_STRING_RESPONSE = StringTcpUtils.buildHeartResp(App.getInstance().IMEI);

    public static final String INIT_BEAT_STRING = StringTcpUtils.buildInitReq(App.getInstance().IMEI);

    public static final String INIT_BEAT_STRING_RESPONSE = StringTcpUtils.buildInitResp(App.getInstance().IMEI);

    public static final String GET_DATA_STRING = StringTcpUtils.buildGetDataReq(App.getInstance().IMEI);

    public static final String UP_MSG_END_FLAG = new String(new byte[]{0x01, 0x01, 0x01});

    public final static int SQL_MAX_COUNT = 30;

    public final static long UPLOAD_OFFLINE_DATA_INTERVAL = 10 * 1000L;

    private EventLoopGroup group = new NioEventLoopGroup();

    private ChannelFuture mChannelFuture;

    private Bootstrap mBootstrap;

    private boolean isConnect = false;

    private Handler handler = new Handler();

    private Handler uploadHandler = new Handler();

    private long sendTime = 0L;

    private LocationDaoUtils daoUtils;

    public class MyBinder extends Binder {
        public NettyService getService() {
            return NettyService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Logger.i(TAG, "onCreate, Thread: " + Thread.currentThread().getName());
        initNettySocket();
        daoUtils = new LocationDaoUtils(this);
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
        super.onDestroy();
    }

    private void initNettySocket() {
        Logger.i(TAG, "initNettySocket, Thread: " + Thread.currentThread().getName());
        if (NetUtils.isConnected(this)) {
            synchronized (this) {
                if (mChannelFuture == null || mChannelFuture.channel() == null || !isConnect) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                if (mBootstrap == null) {
                                    mBootstrap = new Bootstrap();
                                    mBootstrap.group(group);
                                    mBootstrap.channel(NioSocketChannel.class);
                                    mBootstrap.option(ChannelOption.TCP_NODELAY, true);
                                    mBootstrap.handler(new ChannelInitializer() {
                                        @Override
                                        protected void initChannel(Channel ch) {
                                            // TODO Auto-generated method stub
                                            ChannelPipeline pipeline = ch.pipeline();
                                            ByteBuf delimiter = Unpooled.copiedBuffer(UP_MSG_END_FLAG.getBytes());
                                            pipeline.addLast(new DelimiterBasedFrameDecoder(Integer.MAX_VALUE, delimiter));
                                            pipeline.addLast("decoder", new StringDecoder());
                                            pipeline.addLast("encoder", new StringEncoder());
                                            pipeline.addLast(new NettyClientHandler());
                                        }
                                    });
                                }
                                connect();
                            } catch (Exception e) {
                                isConnect = false;
                                Logger.e(TAG, "initNettySocket, Exception: ", e);
                            }
                        }
                    }).start();
                }
            }
        }
    }

    private void connect() {
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
                        Logger.i(TAG, "Failed to connect to server，After 10s, the connection will be reconnected");
                        future.channel().eventLoop().schedule(new Runnable() {
                            @Override
                            public void run() {
                                connect();
                            }
                        }, 10, TimeUnit.SECONDS);
                    }
                }
            }).sync();
            // 当代客户端链路关闭
            mChannelFuture.channel().closeFuture().sync();
        } catch (Exception e) {
            Logger.e(TAG, "initNettySocket connect Exception: ", e);
            isConnect = false;
        }
    }

    public void sendMessage(String msg) {
        Logger.e(TAG, "sendMessage：" + msg);
        if (mChannelFuture != null && mChannelFuture.channel() != null && isConnect) {
            mChannelFuture.channel().writeAndFlush(msg);
        } else {
            initNettySocket();
        }
    }

    class NettyClientHandler extends ChannelInboundHandlerAdapter {
        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            ctx.writeAndFlush(INIT_BEAT_STRING);
            ctx.writeAndFlush(HEART_BEAT_STRING);
            ctx.writeAndFlush(GET_DATA_STRING);
            uploadHandler.postDelayed(uploadRunnable, 200);
        }

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            String message = (String) msg;
            //收到服务器过来的消息，就通过Broadcast发送出去
            if (INIT_BEAT_STRING_RESPONSE.startsWith(message)) {
                Logger.i(TAG, "response init success：" + message);
            } else if (HEART_BEAT_STRING_RESPONSE.startsWith(message)) {//处理心跳回复
                Logger.i(TAG, "response heart：" + message);
                sendTime = System.currentTimeMillis();//记录上一次发送心跳的时间
                handler.postDelayed(heartRunnable, App.getInstance().HEART_BEAT_RATE);
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
                    intent.putExtra("command_type", Constact.COMMAND_LOCATION);
                    sendBroadcast(intent);
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

        @Override
        public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
            ctx.flush();
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            Logger.i(TAG, "exceptionCaught：" + cause);
            cause.printStackTrace();
            ctx.close();
            isConnect = false;
        }
    }

    Runnable uploadRunnable = new Runnable() {
        @Override
        public void run() {
            List<LocationData> list = daoUtils.queryLocationData(SQL_MAX_COUNT);
            Logger.i(TAG, "greendao excute location data size is：" + list.size());
            if (list != null && list.size() > 0) {
                for (LocationData data : list) {
                    if (mChannelFuture != null && mChannelFuture.channel() != null && isConnect) {
                        mChannelFuture.channel().writeAndFlush(data.getContent());
                        daoUtils.deleteLocationData(data);
                    }
                }
            }
            if (list.size() == SQL_MAX_COUNT && isConnect) {
                uploadHandler.postDelayed(this, UPLOAD_OFFLINE_DATA_INTERVAL);
            }
        }
    };

    Runnable heartRunnable = new Runnable() {
        @Override
        public void run() {
            sendMessage(HEART_BEAT_STRING);
            Logger.i(TAG, "发送心跳时间间隔为：" + ((System.currentTimeMillis() - sendTime) / 1000)
                    + "s  约定心跳间隔为：" + (App.getInstance().HEART_BEAT_RATE / 1000) + "s");
        }
    };
}
