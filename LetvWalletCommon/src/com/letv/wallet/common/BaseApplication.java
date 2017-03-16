package com.letv.wallet.common;

import android.app.Application;
import android.content.Context;

import com.letv.wallet.common.util.AppUtils;

/**
 * Created by liuliang on 16-7-4.
 */
public class BaseApplication extends Application {

    private static BaseApplication _instance;

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        _instance = this;
    }

    public static BaseApplication getApplication() {
        return _instance;
    }

    public String getAppUA() {
        return "";
    }

    public String getAppVersion() {
        return AppUtils.getAppFullVersionName(this);
    }
}
