package com.letv.wallet.account;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;

import com.letv.wallet.account.aidl.v1.AccountConstant;
import com.letv.wallet.account.aidl.v1.IAccountServiceV1;
import com.letv.wallet.common.BaseApplication;
import com.letv.wallet.common.util.LogHelper;

/**
 * Created by lijunying on 17-1-17.
 */

public class LePayEngine {

    private static final int VERSION = 1;

    private final Object mLock = new Object();

    private IAccountServiceV1 mBinderV1;
    private ServiceConnection connection = new MyServiceConnectionV1();

    private CallBack mCallback;

    public interface CallBack {

        void onServiceReady(LePayEngine service);

        void onServiceLost();
    }

    private class MyServiceConnectionV1 implements ServiceConnection {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            LogHelper.e("onServiceConnected");
            mBinderV1 = IAccountServiceV1.Stub.asInterface(iBinder);
            if (mCallback != null) {
                mCallback.onServiceReady(LePayEngine.this);
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            LogHelper.e("onServiceDisconnected");
            mBinderV1 = null;
            if (mCallback != null) {
                mCallback.onServiceLost();
            }
        }
    }

    public void bindService() {
        synchronized (mLock) {
            Intent intent = new Intent(AccountConstant.ACTION_SERVICE_LEPAY);
            intent.setComponent(LePayUtils.getLePayComp());
            intent.putExtra(AccountConstant.EXTRA_AIDL_VERSION, getVersion());
            BaseApplication.getApplication().bindService(intent, connection, Context.BIND_AUTO_CREATE);
        }
    }

    private void unbindService() {
        if (mBinderV1 != null) {
            try {
                BaseApplication.getApplication().unbindService(connection);
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            }
            mBinderV1 = null;
        }
    }

    public LePayEngine(CallBack listener) {
        mCallback = listener;
        bindService();
    }


    public boolean isConnected(){
        synchronized (mLock) {
            return mBinderV1 != null ;
        }
    }

    public int getVersion() {
        return VERSION;
    }

    public void createAccount(final LePayCommonCallback callback) {
        if (mBinderV1 == null) {
            callback.onError(AccountConstant.RspCode.ERROR_REMOTE_SERVICE_DISCONNECTE, null);
            return;
        }

        try {
            LogHelper.e("createAccount start ... ");
            mBinderV1.createAccount(callback);
        } catch (RemoteException e) {
            e.printStackTrace();
        }

    }

    public void queryAccount(String qType, final LePayCommonCallback callback) {
        if (mBinderV1 == null) {
            callback.onError(AccountConstant.RspCode.ERROR_REMOTE_SERVICE_DISCONNECTE, null);
            return;
        }

        try {
            LogHelper.e("queryAccount start ... ");
            mBinderV1.queryAccount(qType, callback);
        } catch (RemoteException e) {
            e.printStackTrace();
            LogHelper.e("exception = " + e.getMessage());
        }

    }

    public void redirect(final String[] jTypes, final LePayCommonCallback callback) {
        if (mBinderV1 == null) {
            callback.onError(AccountConstant.RspCode.ERROR_REMOTE_SERVICE_DISCONNECTE, null);
            return;
        }

        try {
            LogHelper.e("redirect start ... ");
            mBinderV1.redirect(jTypes , callback);
        } catch (RemoteException e) {
            e.printStackTrace();
            LogHelper.e("exception = " + e.getMessage());
        }
    }


}
