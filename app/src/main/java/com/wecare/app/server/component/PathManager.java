package com.wecare.app.server.component;

import android.os.Environment;

import com.wecare.app.App;
import com.wecare.app.util.FileUtils;
import com.yanzhenjie.andserver.util.IOUtils;

import java.io.File;

/**
 * @Author: chengzj
 * @CreateDate: 2018/11/30 15:42
 * @Version: 3.0.0
 */
public class PathManager {
    private static PathManager sInstance;

    public static PathManager getInstance() {
        if(sInstance == null) {
            synchronized (PathManager.class) {
                if(sInstance == null) {
                    sInstance = new PathManager();
                }
            }
        }
        return sInstance;
    }

    private File mRootDir;

    private PathManager() {
        if (FileUtils.storageAvailable()) {
            mRootDir = Environment.getExternalStorageDirectory();
        } else {
            mRootDir = App.getInstance().getFilesDir();
        }
        mRootDir = new File(mRootDir, "AndServer");
        IOUtils.createFolder(mRootDir);
    }

    public String getRootDir() {
        return mRootDir.getAbsolutePath();
    }

    public String getWebDir() {
        return new File(mRootDir, "web").getAbsolutePath();
    }


    public String getWebDir(String path) {
        return new File(path, "web").getAbsolutePath();
    }
}
