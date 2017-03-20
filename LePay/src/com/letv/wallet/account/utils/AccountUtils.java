package com.letv.wallet.account.utils;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import com.letv.wallet.account.activity.AccountWebActivity;
import com.letv.wallet.account.aidl.v1.AccountConstant;
import com.letv.wallet.account.aidl.v1.RedirectURL;
import com.letv.wallet.account.task.AccountCommonCallback;
import com.letv.wallet.account.task.RedirectTask;
import com.letv.wallet.common.util.AccountHelper;
import com.letv.wallet.common.util.CommonConstants;
import com.letv.wallet.common.util.DigestUtils;
import com.letv.wallet.common.util.ExecutorHelper;
import com.letv.wallet.common.util.SharedPreferencesHelper;

/**
 * Created by lijunying on 17-2-15.
 * 检测本地缓存是否已开户， 实名 ，具体应以账户查询接口返回为准
 */

public class AccountUtils {
    public static boolean hasCreatedAccount() {
        return SharedPreferencesHelper.getBoolean(DigestUtils.getMd5_30( AccountHelper.getInstance().getUid() + AccountConstant.SHAREDPREFERENCES_CREATE_ACCOUNT_SUFFIX), false);
    }

    public static boolean hasVerifyAccount() {
        return SharedPreferencesHelper.getBoolean(DigestUtils.getMd5_30(AccountHelper.getInstance().getUid() + AccountConstant.SHAREDPREFERENCES_VERIFY_ACCOUNT_SUFFIX), false);
    }

    public static void updateCreatedAccountStatus() {
        if (hasCreatedAccount()) {
            return; // 开户已缓存返回
        }
        SharedPreferencesHelper.getSharePreferences().edit().putBoolean(DigestUtils.getMd5_30(AccountHelper.getInstance().getUid() +
                AccountConstant.SHAREDPREFERENCES_CREATE_ACCOUNT_SUFFIX), true).commit();

    }

    public static void updateVerifyAccountStatus() {
        if (hasVerifyAccount()) {
            return; // 认证已缓存返回
        }
        SharedPreferencesHelper.getSharePreferences().edit().putBoolean(DigestUtils.getMd5_30(AccountHelper.getInstance().getUid() +
                AccountConstant.SHAREDPREFERENCES_VERIFY_ACCOUNT_SUFFIX), true).commit();
    }

    //金融统一跳转H5接口
    static RedirectURL redirectURL;

    public static void goToAccountWeb(Context context, String jType) {
        if (context == null || TextUtils.isEmpty(jType)) {
            return;
        }
        Intent intent = new Intent(context, AccountWebActivity.class);
        intent.putExtra(AccountWebActivity.EXTRA_KEY_JTYPE, jType);
        intent.putExtra(CommonConstants.EXTRA_URL, getRedirectUrl(jType));
        context.startActivity(intent);
    }

    private static long lastRedirect = 0;
    public  static final int REDIRECT_CACHE_EXPIRE =  1000 * 60 * 5; // 5min
    private static  RedirectTask redirectTask = null;
    private static final Object taskHashLock = new Object();

    public static boolean checkRedirectExpired() {
        if ((redirectURL != null) && (System.currentTimeMillis() - lastRedirect) <= REDIRECT_CACHE_EXPIRE) {
            return false;
        }
        redirect(new String[]{AccountConstant.JTYPE_SSO_BIND_MOBILE, AccountConstant.JTYPE_ADD_CARD, AccountConstant.JTYPE_SET_PAY_PWD});
        return true;
    }

    public static String getRedirectUrl(String jType){
        if (checkRedirectExpired()) {
            return null;
        }
        return redirectURL.getUrl(jType);
    }

    private static void redirect(String[] jTypes){
        if (redirectTask == null) {
            synchronized (taskHashLock) {
                if (redirectTask == null) {
                    ExecutorHelper.getExecutor().runnableExecutor(redirectTask = new RedirectTask(jTypes, new AccountCommonCallback<RedirectURL>() {
                        @Override
                        public void onSuccess(RedirectURL result) {
                            synchronized (taskHashLock) {
                                redirectTask = null;
                            }
                            if (result != null) {
                                redirectURL = result;
                                lastRedirect = System.currentTimeMillis();
                            }
                        }

                        @Override
                        public void onError(int errorCode, String errorMsg) {
                            synchronized (taskHashLock) {
                                redirectTask = null;
                            }
                        }

                        @Override
                        public void onNoNet() {
                            synchronized (taskHashLock) {
                                redirectTask = null;
                            }
                        }
                    }));
                }
            }
        }
    }

}
