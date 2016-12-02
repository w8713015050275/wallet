package com.letv.wallet.common;

import android.app.Application;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import com.letv.wallet.common.util.LogHelper;

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

    protected String getAppVersion() {
        PackageInfo packageInfo = null;
        String version = "";
        try {
            packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            version = packageInfo.versionName;
        } catch (Exception e) {
            LogHelper.e(e);
        }
        return version;
    }
}
