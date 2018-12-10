package com.wecare.app.module.main;

/*

import com.bumptech.glide.Glide;
import com.bumptech.glide.GlideBuilder;
import com.bumptech.glide.load.engine.bitmap_recycle.LruBitmapPool;
import com.bumptech.glide.load.engine.cache.LruResourceCache;
import com.bumptech.glide.load.engine.cache.MemorySizeCalculator;
import com.bumptech.glide.module.GlideModule;

import java.io.File;

public class MyGlideModule implements GlideModule {
    int memorySize = (int) (Runtime.getRuntime().maxMemory()) / 8;  // 取1/8最大内存作为最大缓存

    @Override
    public void applyOptions(Context context, GlideBuilder builder) {
        //设置图片清晰度
//        builder.setDecodeFormat(DecodeFormat.PREFER_RGB_565);
        builder.setMemoryCache(new LruResourceCache(memorySize));

        MemorySizeCalculator calculator = new MemorySizeCalculator(context);
        int defaultBitmapPoolSize = calculator.getBitmapPoolSize();
        //修改bitmappool的大小为默认的0.6倍
        int customBitmapPoolSize = (int) (0.2 * defaultBitmapPoolSize);
        builder.setBitmapPool(new LruBitmapPool(customBitmapPoolSize));

        File cacheDir = context.getExternalCacheDir();//指定的是数据的缓存地址
        int diskCacheSize = 1024 * 1024 * 400;//最多可以缓存多少字节的数据 400M
        //设置磁盘缓存大小
//        builder.setDiskCache(new DiskLruCacheFactory(cacheDir.getPath(), "glide", diskCacheSize));
    }

    @Override
    public void registerComponents(Context context, Glide glide) {

    }
}
*/
