package com.letv.walletbiz.update.util;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import com.letv.walletbiz.MainActivity;
import com.letv.walletbiz.update.UpdateConstant;
import com.letv.walletbiz.update.service.UpgradeService;

/**
 * Created by zhangzhiwei1 on 16-8-12.
 */
public class CrashHandler implements Thread.UncaughtExceptionHandler {
    private Context mContext;

    private CrashHandler() {
    }

    private static class CrashHolder {
        static final CrashHandler crashHandler = new CrashHandler();
    }

    public static CrashHandler getInstance() {
        return CrashHolder.crashHandler;
    }

    public void init(Context context) {
        Thread.setDefaultUncaughtExceptionHandler(this);
        mContext = context;
    }

    @Override
    public void uncaughtException(Thread thread, Throwable ex) {
        android.util.Log.i("CrashHandler","uncaughtException");
        ex.printStackTrace();
        restartUpgradeService();
        android.os.Process.killProcess(android.os.Process.myPid());
    }

    public void restartUpgradeService() {
        Intent intent = new Intent(mContext,UpgradeService.class);
        intent.putExtra(UpdateConstant.UPGRADE_AFTER_CRASHED,true);
        ((Application)mContext).startService(intent);
    }
}
