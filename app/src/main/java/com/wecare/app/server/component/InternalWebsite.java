package com.wecare.app.server.component;

import com.yanzhenjie.andserver.annotation.Website;
import com.yanzhenjie.andserver.framework.website.AssetsWebsite;
import com.yanzhenjie.andserver.framework.website.FileBrowser;
import com.yanzhenjie.andserver.framework.website.StorageWebsite;

/**
 * @Author: chengzj
 * @CreateDate: 2018/11/30 15:44
 * @Version: 3.0.0
 */
@Website
public class InternalWebsite extends AssetsWebsite {

    public InternalWebsite() {
        super("/storage/sdcard1");
    }
}