package com.letv.walletbiz.me.utils;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import com.letv.wallet.account.LePayAccountManager;
import com.letv.wallet.account.LePayCommonCallback;
import com.letv.wallet.account.aidl.v1.AccountConstant;
import com.letv.wallet.account.aidl.v1.RedirectURL;
import com.letv.wallet.common.util.CommonConstants;
import com.letv.wallet.common.util.LogHelper;
import com.letv.walletbiz.me.activity.AccountWebActivity;

/**
 * Created by lijunying on 17-3-8.
 */

public class AccountUtils {

    public static final int LELEHUA_HOME = 0, LELEHUA_BILL = 1;

    static RedirectURL redirectURL;

    public static  String getLeLeHuaJtype(int type, int leleHuaStatus) {
        switch (leleHuaStatus) {
            case AccountConstant.LELEHUA_ACCOUNT_STATE_NOACTIVATED_FROZEN:
                return AccountConstant.JTYPE_LELEHUA_NOACTIVE; // 跳转到乐乐花不可用页面

            case AccountConstant.LELEHUA_ACCOUNT_STATE_NOACTIVATED:
                return AccountConstant.JTYPE_LELEHUA_ACTIVE; // 跳转到乐乐花激活页面

            case AccountConstant.LELEHUA_ACCOUNT_STATE_ACTIVING:
                return AccountConstant.JTYPE_LELEHUA_ACTIVING; // 跳转至乐乐花激活中页面

            case AccountConstant.LELEHUA_ACCOUNT_STATE_ACTIVATED:
            case AccountConstant.LELEHUA_ACCOUNT_STATE_ACTIVATED_FROZEN:
                if (LELEHUA_BILL == type) {
                    return AccountConstant.JTYPE_LELEHUA_BILL_LIST; //跳转到乐乐花账单页面
                }
                return AccountConstant.JTYPE_LELEHUA_HOME; // 跳转到乐乐花首页

            default:
                LogHelper.e("unkown leleHuaStatus = " + leleHuaStatus);
                return null;
        }
    }

    //金融统一跳转H5接口
    public static void goToAccountWeb(Context context, String jType) {
        if (context == null || TextUtils.isEmpty(jType)) {
            return;
        }
        Intent intent = new Intent(context, AccountWebActivity.class);
        intent.putExtra(AccountWebActivity.EXTRA_KEY_JTYPE, jType);
        intent.putExtra(CommonConstants.EXTRA_URL, getRedirectUrl(jType));
        context.startActivity(intent);
    }

    //有π首页
    public static void goToLeLeHuaHome(Context context, int leleHuaStatus){
        goToAccountWeb(context, getLeLeHuaJtype(LELEHUA_HOME, leleHuaStatus));
    }

    //有π账单页面
    public static void goToLeLeHuaBill(Context context, int leleHuaStatus){
        goToAccountWeb(context, getLeLeHuaJtype(LELEHUA_BILL, leleHuaStatus));
    }

    //跳转到绑定手机号H5
    public static void goToBindMobile(Context context){
        goToAccountWeb(context, AccountConstant.JTYPE_SSO_BIND_MOBILE);
    }


    private static long lastRedirect = 0;
    public static final int REDIRECT_CACHE_EXPIRE =  1000 * 60 * 5; // 5min

    public static boolean checkRedirectExpired() {
        if ((redirectURL != null) && (System.currentTimeMillis() - lastRedirect) <= REDIRECT_CACHE_EXPIRE) {
            return false;
        }
        redirect(new String[]{AccountConstant.JTYPE_LELEHUA_ACTIVE, AccountConstant.JTYPE_LELEHUA_HOME, AccountConstant.JTYPE_LELEHUA_NOACTIVE,
                AccountConstant.JTYPE_LELEHUA_BILL_LIST, AccountConstant.JTYPE_LELEHUA_ACTIVING, AccountConstant.JTYPE_SSO_BIND_MOBILE});
        return true;
    }

    public static String getRedirectUrl(String jType){
        if (checkRedirectExpired()) {
            return null;
        }
        return redirectURL.getUrl(jType);
    }

    private static void redirect(String[] jTypes){
        LePayAccountManager.getInstance().redirect(jTypes, new LePayCommonCallback<RedirectURL>() {
            @Override
            public void onSuccess(RedirectURL result) {
                if (result != null) {
                    redirectURL = result;
                    lastRedirect = System.currentTimeMillis();
                }
            }

            @Override
            public void onError(int errorCode, String errorMsg) {

            }
        });
    }



}
