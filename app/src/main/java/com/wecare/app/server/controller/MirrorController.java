package com.wecare.app.server.controller;

import android.graphics.Bitmap;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.wecare.app.App;
import com.wecare.app.data.source.BaseInfo;
import com.wecare.app.data.source.BaseResp;
import com.wecare.app.server.VideoUtils;
import com.wecare.app.server.bean.VideoEntity;
import com.wecare.app.util.DeviceUtils;
import com.wecare.app.util.FileUtils;
import com.wecare.app.util.ImageUtils;
import com.wecare.app.util.Logger;
import com.wecare.app.util.PreferenceConstants;
import com.wecare.app.util.PreferenceUtils;
import com.yanzhenjie.andserver.annotation.GetMapping;
import com.yanzhenjie.andserver.annotation.PathVariable;
import com.yanzhenjie.andserver.annotation.RequestMapping;
import com.yanzhenjie.andserver.annotation.RequestParam;
import com.yanzhenjie.andserver.annotation.RestController;
import com.yanzhenjie.andserver.framework.MessageConverter;
import com.yanzhenjie.andserver.framework.body.FileBody;
import com.yanzhenjie.andserver.framework.body.StreamBody;
import com.yanzhenjie.andserver.http.HttpMethod;
import com.yanzhenjie.andserver.http.HttpRequest;
import com.yanzhenjie.andserver.http.HttpResponse;
import com.yanzhenjie.andserver.http.RequestBody;
import com.yanzhenjie.andserver.http.ResponseBody;
import com.yanzhenjie.andserver.util.MediaType;

import org.apache.commons.io.Charsets;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by Administrator Chengzj
 *
 * @date 2018/10/30 16:12
 */

@RestController
@RequestMapping(path = "/mirror")
public class MirrorController {
    public static final String TAG = "MirrorController";

    public static final int LOCK_VEDIO = 1;

    public static final int DEFAULT_IMAGE = 0;

    public static final String COLLISION_VIDEO_DIR = "/storage/sdcard1/CarCamera/Camera-Lock/";

    public static final String FONT_VIDEO_DIR = "/storage/sdcard1/CarCamera/Camera-Front/";

    public static final String IMAGE_DIR = "/storage/sdcard1/CarCamera/Picture/";

    private static final ExecutorService EXECUTOR_SERVICE = Executors.newSingleThreadExecutor();

    @GetMapping(path = "/imageInfo/{imei}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    String getImageInfo(@PathVariable(name = "imei") String imei, @RequestParam(name = "offset") int offset, @RequestParam(name = "limit") int limit) {
        Logger.i(TAG, "getImageInfo  imei：" + imei + "，offset：" + offset + "，limit" + limit);
        if (!DeviceUtils.hasTFCard(App.getInstance())) {
            return new Gson().toJson(new BaseResp<>(new BaseInfo("5001", "请先将TF卡插入设备后重试")));
        }
        List<File> files = FileUtils.getImageFileList(IMAGE_DIR);
        if (files.isEmpty() || files.size() == 0) {
            return new Gson().toJson(new BaseResp<>(new BaseInfo("200", "成功"), new ArrayList<VideoEntity>()));
        }

        files = fenye(files, offset, limit);

        List<VideoEntity> entities = new ArrayList<>();
        String rootUrl = PreferenceUtils.getPrefString(App.getInstance(), PreferenceConstants.ROOT_URL, "");
        for (File file : files) {
            VideoEntity entity = new VideoEntity();
            entity.setName(file.getName());
            String imageFold = TextUtils.concat(rootUrl, "mirror/get/image/0/").toString();
            entity.setImagePath(TextUtils.concat(imageFold, file.getName()).toString());
            entities.add(entity);
        }
        Logger.i(TAG, "imageInfo： " + new Gson().toJson(entities));
        return new Gson().toJson(new BaseResp<>(new BaseInfo("200", "成功"), entities));
    }


    @GetMapping(path = "/videoInfo/{isLock}/{imei}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    String getVideoInfo(@PathVariable(name = "imei") String imei, @PathVariable(name = "isLock") int isLock, @RequestParam("offset") int offset, @RequestParam("limit") int limit) {
        Logger.i(TAG, "getVideoInfo  imei：" + imei + "，isLock：" + isLock + "，offset：" + offset + "，limit：" + limit);
        if (!DeviceUtils.hasTFCard(App.getInstance())) {
            return new Gson().toJson(new BaseResp<>(new BaseInfo("5001", "请先将TF卡插入设备后重试")));
        }
        List<File> files;
        String path;
        if (isLock == LOCK_VEDIO) {
            path = COLLISION_VIDEO_DIR;
        } else {
            path = FONT_VIDEO_DIR;
        }
        files = FileUtils.getVideoFileList(path);
        if (files.isEmpty() || files.size() == 0) {
            return new Gson().toJson(new BaseResp<>(new BaseInfo("200", "成功"), new ArrayList<VideoEntity>()));
        }

        files = fenye(files, offset, limit);

        List<VideoEntity> entities = new ArrayList<>();
        String rootUrl = PreferenceUtils.getPrefString(App.getInstance(), PreferenceConstants.ROOT_URL, "");
        for (File file : files) {
            VideoEntity entity = new VideoEntity();
            entity.setName(file.getName());
            entity.setLength(VideoUtils.getVideoTime(file.getAbsolutePath()));

            String imageFold = TextUtils.concat(rootUrl, "mirror/get/image/1/").toString();
            String imageName = file.getName().replace("mp4", "jpg");
            entity.setImagePath(imageFold + imageName);
            //获取视频缩略图
            getThumb(file.getAbsolutePath(), imageName);

            String videoPath = TextUtils.concat(rootUrl, "mirror/get/video/", String.valueOf(isLock), File.separator, file.getName()).toString();
            //设置视频地址
            entity.setVideoPath(videoPath);

            entities.add(entity);
        }

        Logger.i(TAG, "videoInfo： " + new Gson().toJson(entities));
        return new Gson().toJson(new BaseResp<>(new BaseInfo("200", "成功"), entities));
    }

    void getThumb(final String foldPath, final String fileName) {
        EXECUTOR_SERVICE.execute(new Runnable() {
            @Override
            public void run() {
                Bitmap bitmap = ImageUtils.getVideoThumbnail(foldPath, 267, 201, MediaStore.Images.Thumbnails.MINI_KIND);
                ImageUtils.saveBitmap(App.getInstance(), bitmap, App.getInstance().getAppCacheDir() + "thumb" + File.separator, fileName);
            }
        });
    }

    @GetMapping("/get/video/{isLock}/{fileName}")
    public ResponseBody getVideo(@NonNull HttpRequest request, @NonNull HttpResponse response, @PathVariable(name = "isLock") int isLock, @PathVariable(name = "fileName") String fileName) throws IOException {
        Logger.i(TAG, "getVideo isLock：" + isLock + ", fileName：" + fileName);
        if (request == null || response == null) {
            Logger.e(TAG, "Eroor，HttpRequest can not be null Or HttpResponse can not be null");
        }
        String path;
        if (isLock == LOCK_VEDIO) {
            path = COLLISION_VIDEO_DIR;
        } else {
            path = FONT_VIDEO_DIR;
        }
        String filePath = TextUtils.concat(path, fileName).toString();
        File file = new File(filePath);
        if (file.exists()) {
            Logger.e(TAG, "return FileBody");
            return new FileBody(file);
        }

        RandomAccessFile randomFile = new RandomAccessFile(file, "r");//只读模式
        long contentLength = randomFile.length();
        String range = request.getHeader("Range");
        int start = 0, end = 0;
        if (range != null && range.startsWith("bytes=")) {
            String[] values = range.split("=")[1].split("-");
            start = Integer.parseInt(values[0]);
            if (values.length > 1) {
                end = Integer.parseInt(values[1]);
            }
        }
        int requestSize = 0;
        if (end != 0 && end > start) {
            requestSize = end - start + 1;
        } else {
            requestSize = Integer.MAX_VALUE;
        }
//        byte[] buffer = new byte[4096];
        response.setHeader("Content-Type", "video/mp4");
        response.setHeader("Accept-Ranges", "bytes");
        response.setHeader("ETag", fileName);
        response.setDateHeader("Last-Modified", new Date().getTime());

        //第一次请求只返回content length来让客户端请求多次实际数据
        if (range == null) {
            response.setHeader("Content-length", contentLength + "");
        } else {
            //以后的多次以断点续传的方式来返回视频数据
            response.setStatus(HttpResponse.SC_PARTIAL_CONTENT);//206
            long requestStart = 0, requestEnd = 0;
            String[] ranges = range.split("=");
            if (ranges.length > 1) {
                String[] rangeDatas = ranges[1].split("-");
                requestStart = Integer.parseInt(rangeDatas[0]);
                if (rangeDatas.length > 1) {
                    requestEnd = Integer.parseInt(rangeDatas[1]);
                }
            }
            long length = 0;
            if (requestEnd > 0) {
                length = requestEnd - requestStart + 1;
                response.setHeader("Content-length", "" + length);
                response.setHeader("Content-Range", "bytes " + requestStart + "-" + requestEnd + "/" + contentLength);
            } else {
                length = contentLength - requestStart;
                response.setHeader("Content-length", "" + length);
                response.setHeader("Content-Range", "bytes " + requestStart + "-" + (contentLength - 1) + "/" + contentLength);
            }
        }
/*        OutputStream out = new FileOutputStream(file);
        int needSize = requestSize;
        randomFile.seek(start);
        while(needSize > 0){
            int len = randomFile.read(buffer);
            if(needSize < buffer.length){
                out.write(buffer, 0, needSize);
            } else {
                out.write(buffer, 0, len);
                if(len < buffer.length){
                    break;
                }
            }
            needSize -= buffer.length;
        }
        randomFile.close();
        out.close();*/
        Logger.i(TAG, "VideoBody  start：" + start + "，requestSize：" + requestSize);
        return new VideoBody(file, randomFile, start, requestSize);
    }

    @GetMapping("/get/image/{type}/{fileName}")
    public ResponseBody getImage(@PathVariable(name = "type") int type, @PathVariable(name = "fileName") String fileName) {
//        Logger.i(TAG, "getImage  fileName：" + fileName);
        String path;
        if (type == DEFAULT_IMAGE) {
            path = IMAGE_DIR;
        } else {
            path = TextUtils.concat(App.getInstance().getAppCacheDir(), "thumb", File.separator).toString();
        }
        String filePath = TextUtils.concat(path, fileName).toString();
        File file = new File(filePath);
        return new FileBody(file);
    }

    /**
     * 分页函数
     *
     * @param list
     * @param offset 第几页
     * @param limit  每页条数
     * @return
     */
    public List fenye(List<File> list, int offset, int limit) {
        int fromIndex = limit * offset;
        int toIndex = limit * (offset + 1);
        if (fromIndex > toIndex) {
            fromIndex = toIndex;
        }
        if (toIndex > list.size()) {
            toIndex = list.size();
        }
        return list.subList(fromIndex, toIndex);
    }

    public static void main(String[] args) {
        List<VideoEntity> entities = new ArrayList<>();
        VideoEntity entity = new VideoEntity();
        entity.setName("a.jpg");
//        entity.setLength("1分30秒");
        entity.setImagePath("http://192.168.43.1:8080/mirror/get/image/0/a.jpg");
//        entity.setVideoPath("http://192.168.43.1:8080/mirror/get/video/0/a.mp4");
        entities.add(entity);

        System.out.println("imageInfo：" + new Gson().toJson(new BaseResp<>(new BaseInfo("200", "成功"), entities)));

        System.out.println("imageInfo：" + new Gson().toJson(new BaseResp<>(new BaseInfo("5001", "no video file"), null)));

        System.out.println("http://192.168.43.1:8080/mirror/imageInfo/12312313?offset=0&limit=20");
    }
}
