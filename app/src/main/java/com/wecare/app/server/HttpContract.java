package com.wecare.app.server;

/**
 * Created by Administrator Chengzj
 *
 * @date 2018/10/30 15:46
 */
public interface HttpContract {
    void onServerStart(String ip);

    void onServerError(String message);

    void onServerStop();
}
