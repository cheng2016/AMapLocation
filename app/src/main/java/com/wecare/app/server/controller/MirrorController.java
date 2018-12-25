package com.wecare.app.server.controller;

import android.graphics.Bitmap;
import android.provider.MediaStore;
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
import com.yanzhenjie.andserver.framework.body.FileBody;
import com.yanzhenjie.andserver.http.ResponseBody;
import com.yanzhenjie.andserver.util.MediaType;

import java.io.File;
import java.util.ArrayList;
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
    public ResponseBody getVideo(@PathVariable(name = "isLock") int isLock, @PathVariable(name = "fileName") String fileName) {
        Logger.i(TAG, "getVideo isLock：" + isLock + ", fileName：" + fileName);
        String path;
        if (isLock == LOCK_VEDIO) {
            path = COLLISION_VIDEO_DIR;
        } else {
            path = FONT_VIDEO_DIR;
        }
        String filePath = TextUtils.concat(path, fileName).toString();
        File file = new File(filePath);
        return new FileBody(file);
    }

    @GetMapping("/get/image/{type}/{fileName}")
    public ResponseBody getImage(@PathVariable(name = "type") int type, @PathVariable(name = "fileName") String fileName) {
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
