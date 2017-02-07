package com.letv.wallet.account;

import android.content.ComponentName;
import android.content.pm.PackageManager;
import android.os.Looper;

import com.letv.wallet.account.aidl.v1.AccountConstant;
import com.letv.wallet.common.BaseApplication;
import com.letv.wallet.common.util.LogHelper;

import java.util.ArrayList;

/**
 * Created by lijunying on 17-1-17.
 */

public class LePayUtils {

    private static final int BLOCKING_QUEUE_SIZE = 5;

    private static ArrayList<Runnable> blockingQueue = new ArrayList<Runnable>();

    private static ComponentName sComp;

    public static ComponentName getLePayComp() {
        if (null == sComp) {
            sComp = new ComponentName(AccountConstant.LEPAY_PKG, AccountConstant.LEPAY_SERVICE_CLASS);
        }
        return sComp;
    }

    public static boolean isExistPayApp(String packageName) {
        try {
            BaseApplication.getApplication().getPackageManager().getApplicationInfo(
                    packageName, PackageManager.MATCH_UNINSTALLED_PACKAGES);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            LogHelper.e("not install " + packageName);
            return false;
        }
    }

    public static boolean checkRunBefore() {
        if (LePayUtils.isExistPayApp(AccountConstant.LEPAY_PKG) && Looper.myLooper() == Looper.getMainLooper()) {
            return true;
        }
        return false;
    }

    public static void putBlockingOper(Runnable runnable) {
        if (runnable == null) {
            return;
        }
        LogHelper.d("service not bind ,put on blockingQueue");
        if (blockingQueue.size() >= BLOCKING_QUEUE_SIZE) {
            LogHelper.e("put blockingQueue ERROR, Max size has been reached!");
            blockingQueue.remove(0);
        }
        blockingQueue.add(runnable);
    }

    public static void clearBlockingOper() {
        for (Runnable r : blockingQueue) {
            r.run();
        }
        blockingQueue.clear();
    }

}
