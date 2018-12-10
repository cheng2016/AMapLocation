package com.wecare.app.server.controller;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.yanzhenjie.andserver.http.ResponseBody;
import com.yanzhenjie.andserver.util.IOUtils;
import com.yanzhenjie.andserver.util.MediaType;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;

/**
 * @Author: chengzj
 * @CreateDate: 2018/12/5 10:49
 * @Version: 3.0.0
 */
public class VideoBody implements ResponseBody {
    private File mBody;
    RandomAccessFile randomFile;
    int start;
    int requestSize;

    public VideoBody(File mBody, RandomAccessFile randomFile, int start, int requestSize) {
        this.mBody = mBody;
        this.randomFile = randomFile;
        this.start = start;
        this.requestSize = requestSize;
    }

    @Override
    public long contentLength() {
        return requestSize;
    }

    @Nullable
    @Override
    public MediaType contentType() {
        return MediaType.getFileMediaType(mBody.getName());
    }

    @Override
    public void writeTo(@NonNull OutputStream out) throws IOException {
        byte[] buffer = new byte[2048];
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
        out.close();
    }
}
