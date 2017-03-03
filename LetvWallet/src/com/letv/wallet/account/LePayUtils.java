package com.letv.wallet.account;

import android.content.ComponentName;
import android.content.pm.PackageManager;

import com.letv.wallet.account.aidl.v1.AccountConstant;
import com.letv.wallet.common.BaseApplication;
import com.letv.wallet.common.util.LogHelper;
import com.letv.walletbiz.WalletApplication;
import com.letv.walletbiz.update.util.UpdateUtil;

import java.util.ArrayList;

/**
 * Created by lijunying on 17-1-17.
 */

public class LePayUtils {

    private static final int BLOCKING_QUEUE_SIZE = 5;

    private static final int PAY_AIDL_VERSION_MINI = 10002;

    private static ArrayList<Runnable> blockingQueue = new ArrayList<Runnable>();

    private static ComponentName sComp;

    public static ComponentName getLePayComp() {
        if (null == sComp) {
            sComp = new ComponentName(AccountConstant.LEPAY_PKG, AccountConstant.LEPAY_SERVICE_CLASS);
        }
        return sComp;
    }

    public static boolean isExistPayApp() {
        try {
            BaseApplication.getApplication().getPackageManager().getApplicationInfo(
                    AccountConstant.LEPAY_PKG, PackageManager.MATCH_UNINSTALLED_PACKAGES);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            LogHelper.e(AccountConstant.LEPAY_PKG + " not install ");
            return false;
        }
    }

    public static boolean isPayHasAidl(){
        int version = Integer.parseInt(UpdateUtil.getVersion(WalletApplication.getApplication(), AccountConstant.LEPAY_PKG));
        if (version >= PAY_AIDL_VERSION_MINI) {
            return true;
        }else {
            LogHelper.e(AccountConstant.LEPAY_PKG + " low version = " + version );
            return false;
        }
    }

    public static void putBlockingOper(Runnable runnable) {
        if (runnable == null) {
            return;
        }
        if (blockingQueue.size() >= BLOCKING_QUEUE_SIZE) {
            LogHelper.w("put blockingQueue ERROR, Max size has been reached!");
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
