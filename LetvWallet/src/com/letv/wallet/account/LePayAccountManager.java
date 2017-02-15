package com.letv.wallet.account;


import android.content.SharedPreferences;
import android.text.TextUtils;

import com.letv.wallet.account.aidl.v1.AccountConstant;
import com.letv.wallet.account.aidl.v1.RedirectURL;
import com.letv.wallet.common.util.AccountHelper;
import com.letv.wallet.common.util.DigestUtils;
import com.letv.wallet.common.util.SharedPreferencesHelper;

import java.util.ArrayList;

/**
 * Created by lijunying on 17-1-17.
 */

public final class LePayAccountManager implements LePayEngine.CallBack {
    private static LePayAccountManager sInstance;
    public LePayEngine lepayEngine;


    private static ArrayList<LePayCommonCallback> callbacks = new ArrayList<LePayCommonCallback>();

    private LePayAccountManager() {
    }

    public synchronized static LePayAccountManager getInstance() {
        if (sInstance == null) {
            sInstance = new LePayAccountManager();
        }
        return sInstance;
    }

    private void registerCallback(LePayCommonCallback callback){
        callbacks.add(callback);
    }

    public static void unRegisterCallback(LePayCommonCallback callback){
        callbacks.remove(callbacks);
    }

    private void checkEngine(){
        if (lepayEngine == null) {
            lepayEngine = new LePayEngine(this);
        }
        if (lepayEngine.isConnected()) {
            return;
        }
        lepayEngine.bindService();
    }

    public static boolean hasCreatedAccount() {
        SharedPreferences sharedPreferences = SharedPreferencesHelper.getUserIdPreferences(AccountConstant.LEPAY_PKG);
        if (sharedPreferences != null) {
           return sharedPreferences.getBoolean(DigestUtils.getMd5_30(AccountHelper.getInstance().getUid()+AccountConstant.SHAREDPREFERENCES_CREATE_ACCOUNT_SUFFIX), false);
        }
        return false;
    }



    public static boolean hasVerifyAccount(){
        SharedPreferences sharedPreferences = SharedPreferencesHelper.getUserIdPreferences(AccountConstant.LEPAY_PKG);
        if (sharedPreferences != null) {
            return sharedPreferences.getBoolean(DigestUtils.getMd5_30(AccountHelper.getInstance().getUid()+AccountConstant.SHAREDPREFERENCES_VERIFY_ACCOUNT_SUFFIX), false);
        }
        return false;
    }

    public void createAccount(final LePayCommonCallback callback) {
        if (!LePayUtils.checkRunBefore()) {
             return;
        }

        checkEngine();

        registerCallback(callback);

        if (lepayEngine.isConnected()) {
            lepayEngine.createAccount(callback);
        } else {
            LePayUtils.putBlockingOper(new Runnable() {
                @Override
                public void run() {
                    lepayEngine.createAccount(callback);
                }
            });
        }
    }

    public void queryAccount(final String qType, final LePayCommonCallback callback) {
        if (TextUtils.isEmpty(qType) || !LePayUtils.checkRunBefore())
            return;

        checkEngine();

        registerCallback(callback);

        if (lepayEngine.isConnected()) {
            lepayEngine.queryAccount(qType, callback);
        } else {
            LePayUtils.putBlockingOper(new Runnable() {
                @Override
                public void run() {
                    lepayEngine.queryAccount(qType, callback);
                }
            });
        }
    }

   public void redirect(final String[] jTypes , final LePayCommonCallback<RedirectURL> callback){
       if (jTypes == null || !LePayUtils.checkRunBefore()) {
           return;
       }

       checkEngine();

       registerCallback(callback);

       if (lepayEngine.isConnected()) {
           lepayEngine.redirect(jTypes, callback);
       } else {
           LePayUtils.putBlockingOper(new Runnable() {
               @Override
               public void run() {
                   lepayEngine.redirect(jTypes, callback);
               }
           });
       }
    }

    @Override
    public void onServiceReady(LePayEngine service) {
        LePayUtils.clearBlockingOper();
    }

    @Override
    public void onServiceLost() {
        for (LePayCommonCallback callback : callbacks) {
            callback.onError(AccountConstant.RspCode.ERROR_REMOTE_SERVICE_KILLED, null);
        }
    }
}
