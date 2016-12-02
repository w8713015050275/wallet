package com.letv.wallet.common.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.letv.wallet.common.util.AccountHelper;
import com.letv.wallet.common.util.LogHelper;

/**
 * Created by linquan on 16-1-6.
 */
public class AccountChangedReceiver extends BroadcastReceiver {
    public final static String TAG = "AccountChangedReceiver";

    public static final String ACTION_ACCOUNT_LOGIN = "com.letv.android.account.ACTION_LOGIN";
    public static final String ACTION_ACCOUNT_LOGOUT = "com.letv.android.account.ACTION_LOGOUT";
    public static final String ACTION_ACCOUNT_LOGOUT_SAVE = "com.letv.android.account.ACTION_LOGOUT_SAVE";
    public static final String ACTION_ACCOUNT_TOKEN_UPDATE  = "com.letv.android.account.ACTION_TOKEN_UPDATE";
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (ACTION_ACCOUNT_LOGIN.equals(action) || ACTION_ACCOUNT_TOKEN_UPDATE.equals(action)) {
            LogHelper.w("[%s] action " + action, TAG);
            AccountHelper.getInstance().getTokenASync(context);
        } else if (ACTION_ACCOUNT_LOGOUT.equals(action) || ACTION_ACCOUNT_LOGOUT_SAVE.equals(action)) {
            LogHelper.w("[%s] action " + action, TAG);
            AccountHelper.getInstance().clearToken();
        }
        AccountHelper.getInstance().onAccountChange(action);
    }


}
