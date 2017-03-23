package com.letv.wallet.account;

import android.content.ComponentName;
import android.content.pm.PackageManager;

import com.letv.wallet.account.aidl.v1.AccountConstant;
import com.letv.wallet.common.BaseApplication;
import com.letv.wallet.common.util.LogHelper;
import com.letv.walletbiz.WalletApplication;
import com.letv.walletbiz.update.util.UpdateUtil;

import java.util.ArrayList;
import java.util.Hashtable;

/**
 * Created by lijunying on 17-1-17.
 */

public class LePayUtils {

    private static final int BLOCKING_QUEUE_SIZE = 8;

    private static final int PAY_AIDL_VERSION_MINI = 10002;

    // service未连接时，待请求列表 ； 当列表size > maxSize 时，notifyServiceDisconnected
    public static ArrayList<Runnable> blockingQueue = new ArrayList<Runnable>();
    public static Hashtable<Integer, LePayCommonCallback> blockingHashtable = new Hashtable<Integer, LePayCommonCallback>();

    //正在执行的请求列表 callback ； 请求未返回remoteService被异常终止时，notifyRemoteException
    public static ArrayList<LePayCommonCallback> callbacks = new ArrayList<LePayCommonCallback>();

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

    public static void registerCallback(LePayCommonCallback callback){
        if (callback != null && !callbacks.contains(callback)) {
            callbacks.add(callback);
        }
    }

    public static void unRegisterCallback(LePayCommonCallback callback){
        if (callback != null && !callbacks.isEmpty()) {
            callbacks.remove(callback);
        }

    }

    public static void putBlockingOper(Runnable r, LePayCommonCallback callback) {
        if (r == null) {
            return;
        }
        checkQueueSize();
        addRunnable(r, callback);
    }

    private static void addRunnable(Runnable r, LePayCommonCallback callback) {
        if (r == null || blockingQueue.contains(r)) {
            return;
        }
        blockingQueue.add(r);
        if (callback != null) {
            blockingHashtable.put(r.hashCode(), callback);
        }
    }

    private static void checkQueueSize(){
        if (blockingQueue.size() >= BLOCKING_QUEUE_SIZE) {
            LogHelper.w("put blockingQueue ERROR, Max size has been reached!");
            Runnable r = blockingQueue.remove(0); // 移除最早的oper
            if (r != null) {
                LePayCommonCallback callback =  blockingHashtable.remove(r.hashCode());
                if (callback != null) {
                    callback.onError(AccountConstant.RspCode.ERROR_REMOTE_SERVICE_DISCONNECTE, null); // service长时间未连接上, 通知callback；
                }
            }

        }
    }

    public static void excuteBlockingOper() {
        for (Runnable r : blockingQueue) {
            r.run();
        }
        blockingQueue.clear();
        blockingHashtable.clear();
    }

}
