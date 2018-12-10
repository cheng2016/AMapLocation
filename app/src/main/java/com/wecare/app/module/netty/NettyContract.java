package com.wecare.app.module.netty;

import android.content.Context;

import com.wecare.app.module.BasePresenter;
import com.wecare.app.module.BaseView;

/**
 * Created by Administrator Chengzj
 *
 * @date 2018/10/18 18:21
 */
public interface NettyContract {
    interface View extends BaseView<Presenter> {

    }

    interface Presenter extends BasePresenter {
        void takePicture(Context context, long createtime, int cameraid, String key);

        void takeMicroRecord(Context context, long createtime, int cameraid, String key);
    }
}
