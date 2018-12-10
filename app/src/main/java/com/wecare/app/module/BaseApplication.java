package com.wecare.app.module;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.util.Log;

import com.wecare.app.util.Logger;

/**
 * Created by chengzj on 2017/6/17.
 */

public class BaseApplication extends Application {
    private final static String TAG = "Base %s";
    private Activity app_activity = null;

    @Override
    public void onCreate() {
        super.onCreate();
        registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(Activity activity, Bundle bundle) {
                app_activity = activity;
                Logger.d(activity.getClass().getSimpleName(),"-------------------- onActivityCreated------------------");
            }

            @Override
            public void onActivityStarted(Activity activity) {
                app_activity = activity;
                Logger.d(activity.getClass().getSimpleName(),"-------------------- onActivityStarted------------------");
            }

            @Override
            public void onActivityResumed(Activity activity) {
                app_activity = activity;
                Logger.d(activity.getClass().getSimpleName(),"-------------------- onActivityResumed------------------");
            }

            @Override
            public void onActivityPaused(Activity activity) {
                app_activity = activity;
                Logger.d(activity.getClass().getSimpleName(),"-------------------- onActivityPaused------------------");
            }

            @Override
            public void onActivityStopped(Activity activity) {
                app_activity = activity;
                Logger.d(activity.getClass().getSimpleName(),"-------------------- onActivityStopped------------------");
            }

            @Override
            public void onActivitySaveInstanceState(Activity activity, Bundle bundle) {
                Logger.d(activity.getClass().getSimpleName(),"-------------------- onActivitySaveInstanceState------------------");
            }

            @Override
            public void onActivityDestroyed(Activity activity) {
                Logger.d(activity.getClass().getSimpleName(),"-------------------- onActivityDestroyed------------------");
            }
        });
    }

    /**
     * 公开方法，外部可通过 MyApplication.getInstance().getCurrentActivity() 获取到当前最上层的activity
     */
    public Activity getCurrentActivity() {
        return app_activity;
    }
}
