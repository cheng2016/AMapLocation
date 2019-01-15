package com.wecare.app.module.netty;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.net.ConnectivityManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.amap.api.location.AMapLocation;
import com.wecare.app.App;
import com.wecare.app.data.entity.LocationData;
import com.wecare.app.data.entity.QueryBusinessResp;
import com.wecare.app.data.source.local.LocationDaoUtils;
import com.wecare.app.module.main.MainContract;
import com.wecare.app.module.main.MainPresenter;
import com.wecare.app.module.service.UploadService;
import com.wecare.app.util.AMapUtils;
import com.wecare.app.util.Constact;
import com.wecare.app.util.DeviceUtils;
import com.wecare.app.util.Logger;
import com.wecare.app.util.NetUtils;
import com.wecare.app.util.PreferenceConstants;
import com.wecare.app.util.PreferenceUtils;
import com.wecare.app.util.StringTcpUtils;
import com.wecare.app.util.ToastUtils;

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

public class BootService extends Service implements MainContract.View {
    public static final String TAG = "BootService";

    public static final String UP_MSG_END_FLAG = new String(new byte[]{0x01, 0x01, 0x01});

    public final static int SQL_MAX_COUNT = 30;

    public final static long UPLOAD_OFFLINE_DATA_INTERVAL = 10 * 1000L;
    //心跳包内容
    public String HEART_BEAT_STRING;

    private String HEART_BEAT_STRING_RESPONSE;

    private String INIT_BEAT_STRING;

    private String INIT_BEAT_STRING_RESPONSE;

    private String GET_DATA_STRING;

    private Bootstrap mBootstrap;

    private EventLoopGroup eventLoopGroup;

    private Channel mChannel;

    /**
     * 是否连接到服务器
     */
    private boolean isConnect = false;

    private Handler handler = new Handler();

    private LocationDaoUtils mLocationDaoUtils;

    private Context context;

    private MainPresenter mMainPresenter;

    private String imei;

    private void initData() {
        imei = PreferenceUtils.getPrefString(this, PreferenceConstants.IMEI, DeviceUtils.getDeviceIMEI(this));
        HEART_BEAT_STRING = StringTcpUtils.buildHeartReq(imei);
        HEART_BEAT_STRING_RESPONSE = StringTcpUtils.buildHeartResp(imei);
        INIT_BEAT_STRING = StringTcpUtils.buildInitReq(imei);
        INIT_BEAT_STRING_RESPONSE = StringTcpUtils.buildInitResp(imei);
        GET_DATA_STRING = StringTcpUtils.buildGetDataReq(imei);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Logger.i(TAG, "onCreate, Thread: " + Thread.currentThread().getName());
        context = this;
        initData();
        new MainPresenter(this, this);
        ToastUtils.showShort("BootService 启动成功！");
        mLocationDaoUtils = new LocationDaoUtils(this);
        PreferenceUtils.setPrefBoolean(this, PreferenceConstants.SERVICE_BOOT_STATE, true);
        startNettyService();
        registerActionReceiver();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Logger.i(TAG, "onStartCommand, Thread: " + Thread.currentThread().getName());
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Logger.i(TAG, "onDestroy, Thread: " + Thread.currentThread().getName());
        PreferenceUtils.setPrefBoolean(this, PreferenceConstants.SERVICE_BOOT_STATE, false);
        mMainPresenter.unsubscribe();
        unregisterActionReceiver();
        shutdownNetty();
        //释放资源
        workThread.quit();
    }

    //是否关闭
    public boolean isClose = false;

    private void shutdownNetty() {
        isClose = true;
        Logger.e(TAG, "shutdownNetty!");
        if (null != mChannel) {
            mChannel.close();
            mChannel = null;
        }
        if (null != eventLoopGroup) {
            eventLoopGroup.shutdownGracefully();
            eventLoopGroup = null;
            mBootstrap = null;
        }
    }

    public void startNettyService() {
        Logger.w(TAG, "startNettyService, Thread: " + Thread.currentThread().getName());
        isClose = false;
        /**
         * 在子线程 建立连接并向服务器发送请求，这里采用了`HanlderThread`+`Handler`的方案。
         * 通过`Looper`依次从`Handler`的队列中获取信息，逐个进行处理，保证安全，不会出现混乱。
         */
        workThread = new HandlerThread(BootService.class.getName());
        workThread.start();
        mWorkHandler = new Handler(workThread.getLooper(), mWorkHandlerCallback);
        mWorkHandler.sendEmptyMessage(MESSAGE_INIT);
        mWorkHandler.sendEmptyMessageDelayed(MESSAGE_CONNECT,100);
    }


    @Override
    public void onLocationChanged(Location location, int gpsCount, long lastPositionTime) {
        int positionType;
        if (location instanceof AMapLocation) {
            Logger.i(TAG, "高德定位成功：" + ((AMapLocation) location).getAddress() + " 定位类型：" + ((AMapLocation) location).getLocationType());
            positionType = 4;
            ToastUtils.showShort(context, "高德定位成功：" + ((AMapLocation) location).getAddress());
        } else {
            Logger.i(TAG, "GPS定位成功：经    度：" + location.getLongitude() + " 纬    度：" + location.getLatitude());
            positionType = 1;
            ToastUtils.showShort(context, "GPS定位成功！");
        }
        String content = StringTcpUtils.buildGpsContent(location.getLongitude(), location.getLatitude(), location.getAltitude(),
                location.getSpeed(), location.getBearing(), gpsCount, location.getAccuracy(), positionType, location.getTime(), lastPositionTime, "");
        content = StringTcpUtils.buildGpsString(imei, content);
        if (NetUtils.isNetworkAvailable(this)) {
            sendMsg(content);
        } else {
            if (mLocationDaoUtils == null) {
                mLocationDaoUtils = new LocationDaoUtils(this);
            }
            LocationData data = new LocationData();
            data.setContent(content);
            mLocationDaoUtils.insert(data);
        }
    }

    @Override
    public void queryZxingQrSuccess(String url) {
        String headUrl = PreferenceUtils.getPrefString(this, PreferenceConstants.HEAD_URL, "");
        String nickName = PreferenceUtils.getPrefString(this, PreferenceConstants.NICK_NAME, "");
        if (!TextUtils.isEmpty(headUrl) && !TextUtils.isEmpty(nickName)) {
            return;
        }
        //下载图片到本地，并写入系统设置
        mMainPresenter.loadImageToSettings(this, url);
    }

    @Override
    public void queryBusinessSucess(QueryBusinessResp resp) {
        if (resp != null && resp.getData() != null) {
            if (!TextUtils.isEmpty(resp.getData().getHead_image_url())
                    && !TextUtils.isEmpty(resp.getData().getNick_name())) {
                String headUrl = resp.getData().getHead_image_url();
                String nickName = resp.getData().getNick_name();
                //下载图片到本地，并写入系统设置
                mMainPresenter.loadImageToSettings(this, headUrl);
            } else {
                mMainPresenter.queryZxingQr();
            }

            QueryBusinessResp.DataBean bean = resp.getData();
            if (!TextUtils.isEmpty(bean.getLat())
                    && !TextUtils.isEmpty(bean.getLng())) {
                AMapUtils.startNaviActivity(this, "", bean.getName(), Double.valueOf(bean.getLat()),
                        Double.valueOf(bean.getLng()), Integer.valueOf(TextUtils.isEmpty(bean.getDev()) ? "0" : bean.getDev()),
                        Integer.valueOf(TextUtils.isEmpty(bean.getStyle()) ? "0" : bean.getStyle()));
            }
        } else {
            mMainPresenter.queryZxingQr();
        }
    }

    @Override
    public void setPresenter(MainContract.Presenter presenter) {
        mMainPresenter = (MainPresenter) presenter;
    }

    class NettyClientHandler extends ChannelInboundHandlerAdapter {
        //连接成功触发channelActive
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
                    new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mMainPresenter.requestLocation();
                        }
                    }, 300);
                }
            } else {
                if (message.startsWith("C16")) {
                    Logger.i(TAG, "server give me a commad：" + message);
                    String[] results = message.split("\\|");
                    if (results[5].startsWith("D01")) {
                        //优先回复服务器消息，告诉服务器我已接受到该消息，以免广播接收器太多卡顿
                        sendMsg(StringTcpUtils.buildSuccessString(imei, results[6]));

                        int commandType;
                        if (results[5].substring(4).contains("61") || results[5].substring(4).contains("63")) {
                            commandType = Integer.parseInt(results[5].substring(4, 6));
                            String userId = results[5].substring(6);
                            switch (commandType) {
                                case Constact.COMMAND_TACK_IMAGE:
                                    mMainPresenter.takePicture(context, System.currentTimeMillis(), CAMERA_FRONT, userId);
                                    break;
                                case Constact.COMMAND_TACK_VIDEO:
                                    mMainPresenter.takeMicroRecord(context, System.currentTimeMillis(), CAMERA_FRONT, userId);
                                    break;
                            }
                        } else {
                            commandType = Integer.parseInt(results[5].substring(4));
                            switch (commandType) {
                                case Constact.COMMAND_SUCCESS:
                                    ToastUtils.showShort(context, "上传成功");
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
                                case Constact.COMMAND_LOCATION:
                                    mMainPresenter.requestLocation(true);
                                    break;
                                case Constact.COMMAND_START_LOCATION:
                                    mMainPresenter.requestLocation();
                                    Logger.i(TAG, "TCP服务启动成功，启动定位模式！");
                                    break;
                                default:
                                    break;
                            }
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
                    Logger.i(TAG, "userEventTriggered READER_IDLE 读超时，长期没收到服务器推送数据！Channel is active：" + mChannel.isOpen());
                    //可以选择重新连接
                    isConnect = false;
                    ctx.close();
                } else if (event.state().equals(IdleState.WRITER_IDLE)) {
                    Logger.i(TAG, "userEventTriggered WRITER_IDLE 写超时，长期未向服务器发送数据！Channel is active：" + mChannel.isOpen());
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

        ////断开连接触发channelInactive
        @Override
        public void channelInactive(ChannelHandlerContext ctx) {
            // TODO: 重连操作
            Logger.e(TAG, "channelInactive 掉线了");
            sendReconnectMessage();
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable throwable) {
            Logger.e(TAG, "exceptionCaught", throwable);
            ctx.close();
        }
    }

    Runnable uploadRunnable = new Runnable() {
        @Override
        public void run() {
            List<LocationData> list = mLocationDaoUtils.queryLocationData(SQL_MAX_COUNT);
            Logger.i(TAG, "greendao excute location data size is：" + list.size());
            if (list != null && list.size() > 0) {
                for (LocationData data : list) {
                    if (mChannel != null && mChannel.isOpen() && isConnect) {
                        Logger.e(TAG, "sendMessage：" + data.getContent());
                        mChannel.writeAndFlush(data.getContent());
                        mLocationDaoUtils.deleteLocationData(data);
                    }
                }
            }
            if (list.size() == SQL_MAX_COUNT && isConnect) {
                handler.postDelayed(this, UPLOAD_OFFLINE_DATA_INTERVAL);
            }
        }
    };

    /**
     * 注册广播监听；
     */
    public void registerActionReceiver() {
        Logger.d(TAG, "---------------- registerReceiver----------------");
        mReceiver = new ActionReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_RE_MICRO_OR_PICTURE);
        intentFilter.addAction(ACTION_GSENSOR_ALERT);
        intentFilter.addAction(ACTION_GSENSOR_ALERT_RUN);
        registerReceiver(mReceiver, intentFilter);

        mNetworkStateReceiver = new NetworkStateReceiver();
        IntentFilter netIntentFilter = new IntentFilter();
        netIntentFilter.addAction(NETWORK_RECEIVER);
        registerReceiver(mNetworkStateReceiver, netIntentFilter);
    }

    /**
     * 注销广播；
     */
    public void unregisterActionReceiver() {
        Logger.d(TAG, "-------------------- unRegisterReceiver------------------");
        unregisterReceiver(mReceiver);
        unregisterReceiver(mNetworkStateReceiver);
    }

    public static final String NETWORK_RECEIVER = "android.net.conn.CONNECTIVITY_CHANGE";

    private NetworkStateReceiver mNetworkStateReceiver;

    /**
     * 监听系统网络状态广播，7.0 后只支持动态注册
     */
    class NetworkStateReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Logger.i(TAG, "NetworkStateReceiver is work！ action：" + intent.getAction());
            if (intent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
                sendMsg(HEART_BEAT_STRING);
            }
        }
    }

    public static final String ACTION_GSENSOR_ALERT = "com.discovery.action.GSENSOR_ALERT";

    public static final String ACTION_GSENSOR_ALERT_RUN = "com.discovery.action.GSENSOR_ALERT_RUN";

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

    //前摄像头
    public static final int CAMERA_FRONT = 0;

    private ActionReceiver mReceiver;

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
                        if (!TextUtils.isEmpty(path)) {
                            Intent i = new Intent().setClass(context, UploadService.class);
                            i.putExtra("userId", key);
                            i.putExtra("path", path);
                            i.putExtra("timeTemp", createtime);
                            if ("GSENSOR".equals(key)) {
                                if ("pic".equals(type)) {
                                    i.putExtra("type", Constact.FILE_TYPE_IMAGE);
                                    //系统发出的拍照不上传
                                    return;
                                } else if ("vid".equals(type)) {
                                    i.putExtra("type", Constact.FILE_TYPE_COLLISION);
                                }
                                i.putExtra("userId", "");
                            } else {
                                if ("pic".equals(type)) {
                                    i.putExtra("type", Constact.FILE_TYPE_IMAGE);
                                } else if ("vid".equals(type)) {
                                    i.putExtra("type", Constact.FILE_TYPE_VEDIO);
                                }
                                if (isCollision && createtime == collisionTime) {
                                    isCollision = false;
                                    i.putExtra("type", Constact.FILE_TYPE_COLLISION);
                                }
                            }
                            startService(i);
                        }
                    }

                    if (action.equals(ACTION_GSENSOR_ALERT) || action.equals(ACTION_GSENSOR_ALERT_RUN)) {
                        Logger.i(TAG, "检车到停车碰撞或行车碰撞，启动相机录像");
                        isCollision = true;
                        collisionTime = System.currentTimeMillis();
                        mMainPresenter.takeMicroRecord(context, collisionTime, Constact.CAMERA_FRONT, "");
                    }
                }
            } else {
                Logger.d(TAG, "ActionReceiver ------- intent == null");
            }
        }
    }

    //是否碰撞
    boolean isCollision = false;
    //碰撞时间
    long collisionTime;

    private final String ACTION_SEND_MSG = "action_send_msg";

    private final int MESSAGE_INIT = 0x1;
    private final int MESSAGE_CONNECT = 0x2;
    private final int MESSAGE_SEND = 0x3;

    private HandlerThread workThread = null;

    private Handler mWorkHandler = null;

    private Handler.Callback mWorkHandlerCallback = new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case MESSAGE_INIT: {
                    Logger.d(TAG, "WorkHandlerCallback 初始化Tcp服务 ....");
                    initNetty();
                    break;
                }
                case MESSAGE_CONNECT: {
                    Logger.d(TAG, "WorkHandlerCallback 连接远程服务器 ....");
                    connectNetty();
                    break;
                }
                case MESSAGE_SEND: {
                    Logger.d(TAG, "WorkHandlerCallback 向服务器发送数据 ....");
                    String str = (String) msg.obj;
                    try {
                        if (mChannel != null && mChannel.isOpen() && isConnect) {
                            mChannel.writeAndFlush(str).sync();
                            Logger.d(TAG, "send succeed " + str);
                        } else {
                            throw new Exception("channel is null | closed | isConnect：" + isConnect);
                        }
                    } catch (Exception e) {
                        Logger.e(TAG, "send failed ", e);
                        sendReconnectMessage();
                    }
                    break;
                }
            }
            return true;
        }
    };

    private void initNetty(){
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

    private void connectNetty(){
        if (!isClose && !isConnect) {
            try {
                // 发起异步连接操作
                mChannel = mBootstrap.connect(App.getInstance().HOST, App.getInstance().PORT).addListener(new ChannelFutureListener() {
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
                                    sendReconnectMessage();
                                }
                            }, 10, TimeUnit.SECONDS);
                        }
                    }
                }).sync().channel();
                // 当代客户端链路关闭
                mChannel.closeFuture().sync();
            } catch (Exception e) {
                Logger.e(TAG, "channel connect exception", e);
                sendReconnectMessage();
            }
        }
    }

    private void sendReconnectMessage() {
        if (NetUtils.isNetworkAvailable(App.getInstance())) {
            mWorkHandler.sendEmptyMessage(MESSAGE_CONNECT);
        } else {
            Logger.e(TAG, "sendReconnectMessage failed ，网络异常");
        }
    }

    public void sendMsg(String str) {
        Message message = new Message();
        message.what = MESSAGE_SEND;
        message.obj = str;
        mWorkHandler.sendMessage(message);
    }

}
