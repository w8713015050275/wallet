package com.letv.wallet.account;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;

import com.letv.wallet.account.aidl.v1.AccountConstant;
import com.letv.wallet.account.aidl.v1.IAccountCallback;

/**
 * Created by lijunying on 17-1-20.
 */

public abstract class LePayCommonCallback<T> extends IAccountCallback.Stub {

    public static final int SUCCESS = 1;
    public static final int ERROR = 2;

    private int errorCode ;

    private String errorMsg = null;

    private T t = null;

    private Handler mMainHandler = new Handler(Looper.getMainLooper()){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {

                case SUCCESS:
                    onSuccess(t);
                    break;

                case ERROR:
                    onError(errorCode, errorMsg);
                    break;
            }
            LePayUtils.unRegisterCallback(LePayCommonCallback.this); //反注册 服务被杀死监听
        }
    };

    @Override
    public void onSuccess(Bundle bundle) throws RemoteException {
        clearData();
        if(bundle != null){
            try {
                bundle.setClassLoader(getClass().getClassLoader());
                t = (T) bundle.getParcelable(AccountConstant.KEY_LEPAY_RESPONSE);
            }catch (Exception e){
               e.printStackTrace();
            }
        }
        mMainHandler.obtainMessage(SUCCESS).sendToTarget();

    }

    @Override
    public void onFailure(int errorCode, String errorMsg) throws RemoteException {
        clearData();
        this.errorCode = errorCode;
        this.errorMsg = errorMsg;
        mMainHandler.obtainMessage(ERROR).sendToTarget();
    }

    private void clearData(){
        t = null;
        errorCode = 0;
        errorMsg = null;
    }

    public abstract void onSuccess(T t);
    public abstract void onError(int errorCode, String errorMsg);

}
