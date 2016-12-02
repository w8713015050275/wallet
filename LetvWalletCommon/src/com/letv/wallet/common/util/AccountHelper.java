package com.letv.wallet.common.util;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;

import com.letv.wallet.common.BaseApplication;
import com.letv.wallet.common.receiver.AccountChangedReceiver;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by linquan on 15-11-18.
 */
public class AccountHelper {
    public final static String TAG = "AccountHelper";
    public final static String ACCOUNT_TYPE = "com.letv";
    public final static String AUTH_TOKEN_TYPE_LETV = "tokenTypeLetv";
    private static AccountHelper instance;
    private static String uToken ;

    static {
        instance = new AccountHelper();
    }

    public static AccountHelper getInstance() {
        return instance;
    }


    public boolean isLogin(Context context) {

        AccountManager am = AccountManager.get(context);
        boolean isLogin = false;
        final Account[] accounts = am.getAccountsByType(ACCOUNT_TYPE);
        if (accounts != null && accounts.length > 0) {
            isLogin = true;
        }
        return isLogin;

    }

    public void getTokenASync(Context context) {

    AccountThread aThread =new AccountThread(context);
    new Thread(aThread).start();
    }


class AccountThread implements Runnable{
    Context mContext;
    public  AccountThread(Context context){
        mContext = context;
    }

    @Override
    public void run() {
        getTokenSync(mContext);
    }
};


public String getTokenSync(Context context) {
        LogHelper.w("[%s] getTokenSync " , TAG);

        AccountManager manager = AccountManager.get(context);
        Account[] accounts = manager
                .getAccountsByType(ACCOUNT_TYPE);
        if (accounts != null && accounts.length > 0) {
            try {
                uToken = manager.blockingGetAuthToken(accounts[0],
                        AUTH_TOKEN_TYPE_LETV, true);

                LogHelper.v(TAG + "getTokenSync, authToken: " + uToken);
                if (TextUtils.isEmpty(uToken)) {
                    LogHelper.e(TAG + "getTokenSync , authToken is null");
                }
                return uToken;
            } catch (OperationCanceledException e) {
                LogHelper.v(TAG + "getTokenSync, e: " + e.getMessage());
                e.printStackTrace();
            } catch (AuthenticatorException e) {
                LogHelper.v(TAG + "getTokenSync, e: " + e.getMessage());
                e.printStackTrace();
            } catch (IOException e) {
                LogHelper.v(TAG + "getTokenSync, e: " + e.getMessage());
                e.printStackTrace();
            }
        }
        return null;
    }

    public String getToken(Context context) {
        if(isLogin(context)) {
            return getToken();
        }
        return null;
    }


    private String getToken() {
        LogHelper.w("[%s] getToken " + uToken, TAG);
        if (TextUtils.isEmpty(uToken)) {
            LogHelper.e(TAG + "getToken , uToken is null");
        }
        return uToken;
    }

    public void  clearToken() {
        LogHelper.w("[%s] clearToken ", TAG);

        uToken = null;
    }

    public synchronized static void addAccount(Activity activity,
                                               AccountManagerCallback<Bundle> callback) {

        AccountManager am = AccountManager.get(activity);
        Bundle options = new Bundle();
        options.putBoolean("loginFinish", true);
        am.addAccount(ACCOUNT_TYPE, AUTH_TOKEN_TYPE_LETV, null, options, activity, callback, null);
    }

    private static final String ACCOUNT_UID = "UID";

    public String getUid() {
        return getUserData(ACCOUNT_UID);
    }

    public String getUserData(String dataKey) {
        if (TextUtils.isEmpty(dataKey))
            throw new IllegalArgumentException();
        AccountManager manager = AccountManager.get(BaseApplication.getApplication());

        final Account[] accountList = manager.getAccountsByType(ACCOUNT_TYPE);
        String userdata = "";
        if (accountList != null && accountList.length > 0) {
            userdata = manager.getUserData(accountList[0], dataKey);
            LogHelper.d("[%s] account : " + accountList[0].toString(), TAG );
        }
        return userdata;
    }

    public boolean loginLetvAccountIfNot(Activity activity,
                AccountManagerCallback<Bundle> callback) {
        if (isLogin(activity)) {
            return true;
        }
        addAccount(activity,callback);
        return false;
    }

    public String getUserMobile() {
        String mobile = getUserData("mobile");
        if (TextUtils.isEmpty(mobile) || "null".equals(mobile)) {
            mobile = "";
        }
        return mobile;
    }

    private  Handler mMainHandler = new Handler(Looper.getMainLooper()){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_ACCOUNT_CHANGE:
                    String action = (String) msg.obj;
                    synchronized (mAccountChangeListeners) {
                        for (OnAccountChangedListener listener : mAccountChangeListeners) {
                            if (AccountChangedReceiver.ACTION_ACCOUNT_LOGIN.equals(action) || AccountChangedReceiver.ACTION_ACCOUNT_TOKEN_UPDATE.equals(action)) {
                                listener.onAccountLogin();

                            } else if (AccountChangedReceiver.ACTION_ACCOUNT_LOGOUT.equals(action) || AccountChangedReceiver.ACTION_ACCOUNT_LOGOUT_SAVE.equals(action)) {
                                listener.onAccountLogout();
                            }
                        }
                    }
                    break;

            }
        }
    };

    private static final int MSG_ACCOUNT_CHANGE = 1;

    /**
     * 注册乐视账号变更监听
     * @param listener
     */
    public void registerOnAccountChangeListener(final OnAccountChangedListener listener) {
        if (listener == null) return;

        synchronized (mAccountChangeListeners) {
            if (mAccountChangeListeners.contains(listener)) {
                return;
            }
            mAccountChangeListeners.add(listener);
        }
    }

    /**
     * 注销监听
     * @param listener
     */
    public void unregisterOnAccountChangeListener(OnAccountChangedListener listener) {
        if (listener == null || mAccountChangeListeners.isEmpty()) return;

        synchronized (mAccountChangeListeners) {
            if (!mAccountChangeListeners.contains(listener)) {
                return;
            }
            mAccountChangeListeners.remove(listener);
        }
    }

    public void onAccountChange(String action) {
        if(mAccountChangeListeners.isEmpty() || mMainHandler == null ) return;

        Message msg = mMainHandler.obtainMessage(MSG_ACCOUNT_CHANGE);
        msg.obj = action;
        mMainHandler.sendMessage(msg);
    }

    private final ArrayList<OnAccountChangedListener> mAccountChangeListeners = new ArrayList<>();

    public interface OnAccountChangedListener {
        void onAccountLogin();

        void onAccountLogout();
    }
}
