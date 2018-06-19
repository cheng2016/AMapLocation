package com.wecare.app.data.source.remote;

import android.util.Log;

import com.wecare.app.App;
import com.wecare.app.util.NetUtils;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.CacheControl;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by chengzj on 2017/6/18.
 */

public class HttpFactory {
    public static final  String TAG = "HttpFactory";

    private HttpFactory() {
    }

    public static <T> T createRetrofit2(Class<T> service){
        Retrofit retrofit = new Retrofit.Builder()
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl(HttpApi.yt_url)
                .client(getOkClient())
                .build();
        return retrofit.create(service);
    }

    private static OkHttpClient getOkClient(){
        //设置缓存路径
        final File httpCacheDirectory = new File(App.getInstance().getCacheDir(), "okhttpCache");
        Log.i(TAG, httpCacheDirectory.getAbsolutePath());
        //设置缓存 10M
//        Cache cache = new Cache(httpCacheDirectory, 10 * 1024 * 1024);   //缓存可用大小为10M

        OkHttpClient okHttpClient = new OkHttpClient().newBuilder()
                .writeTimeout(30 * 1000, TimeUnit.MILLISECONDS)
                .readTimeout(20 * 1000, TimeUnit.MILLISECONDS)
                .connectTimeout(15 * 1000, TimeUnit.MILLISECONDS)
                //设置拦截器，显示日志信息
                .addInterceptor(httpLoggingInterceptor)
//                .addNetworkInterceptor(REWRITE_CACHE_CONTROL_INTERCEPTOR)
//                .addInterceptor(REWRITE_CACHE_CONTROL_INTERCEPTOR)
//                .cache(cache)
                .build();
        return okHttpClient;
    }

    private static final  HttpLoggingInterceptor httpLoggingInterceptor = new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY);

    private static final Interceptor REWRITE_CACHE_CONTROL_INTERCEPTOR = new Interceptor() {
        @Override
        public Response intercept(Chain chain) throws IOException {
            Request request = chain.request();
            //获取网络状态
            int netWorkState = NetUtils.getNetworkState(App.getInstance());
            //无网络，请求强制使用缓存
            if (netWorkState == NetUtils.NETWORK_NONE) {
                request = request.newBuilder()
                        .cacheControl(CacheControl.FORCE_CACHE)
                        .build();
            }

            Response originalResponse = chain.proceed(request);
            switch (netWorkState) {
                case NetUtils.NETWORK_MOBILE://moblie network 情况下缓存15s
                    int maxAge = 0;
                    return originalResponse.newBuilder()
                            .removeHeader("Pragma")
                            .removeHeader("Cache-Control")
                            .header("Cache-Control", "public, max-age=" + maxAge)
                            .build();
                case NetUtils.NETWORK_WIFI://wifi network 情况下不使用缓存
                    maxAge = 0;
                    return originalResponse.newBuilder()
                            .removeHeader("Pragma")
                            .removeHeader("Cache-Control")
                            .header("Cache-Control", "public, max-age=" + maxAge)
                            .build();

                case NetUtils.NETWORK_NONE://none network 情况下离线缓存4周
                    int maxStale = 60 * 60 * 24 * 4 * 7;
                    return originalResponse.newBuilder()
                            .removeHeader("Pragma")
                            .removeHeader("Cache-Control")
                            .header("Cache-Control", "public, only-if-cached, max-stale=" + maxStale)
                            .build();
                default:
                    throw new IllegalStateException("network state  is Erro!");
            }
        }
    };
}
