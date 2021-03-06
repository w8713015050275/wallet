package com.letv.wallet.account.task;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Parcelable;
import android.os.RemoteException;

import com.letv.wallet.account.aidl.v1.AccountConstant;
import com.letv.wallet.account.aidl.v1.IAccountCallback;
import com.letv.wallet.common.http.beans.BaseResponse;
import com.letv.wallet.common.util.NetworkHelper;

/**
 * Created by lijunying on 17-1-10.
 */

public abstract class AccountBaseTask implements Runnable {

    private Handler mMainHandler = null; //本地调用主线程返回

    public static final int SUCCESS = 1;
    public static final int ERROR = 2;
    public static final int NONET = 3;

    private int errorCode ;

    private String errorMsg = null;

    public Handler getHandler() {
        if (mMainHandler == null) {
            mMainHandler = new Handler(Looper.getMainLooper()) {
                @Override
                public void handleMessage(Message msg) {
                    switch (msg.what) {
                        case SUCCESS:
                            if (localCallback != null) {
                                localCallback.onSuccess(msg.obj);
                            }
                            break;
                        case ERROR:
                            if (localCallback != null) {
                                localCallback.onError(errorCode, errorMsg);
                            }
                            break;
                        case NONET:
                            if (localCallback != null) {
                                localCallback.onNoNet();
                            }
                            break;
                    }
                }
            };
        }
        return mMainHandler;
    }

    protected AccountCommonCallback localCallback;
    protected IAccountCallback remoteCallback;

    protected AccountBaseTask(AccountCommonCallback callback) {
        localCallback = callback;
    }

    protected AccountBaseTask(IAccountCallback callback) {
        remoteCallback = callback;
    }

    @Override
    public void run() {
        if (!NetworkHelper.isNetworkAvailable()) {
            onNoNetWork();
            return;
        }
        if (checkParamsValidate()) {
            handleResult(onExecute());
        } else {
            onError(AccountConstant.RspCode.ERRNO_PARAM, null);
        }

    }

    protected void handleResult(BaseResponse response) {
        if (response == null) {
            onError(AccountConstant.RspCode.ERROR_NETWORK, null);
            return;
        }

        if (response.errno == AccountConstant.RspCode.SUCCESS) {
            onSuccess(response.data);
        } else {
            onError(response.errno, response.errmsg);
        }
    }

    protected void onNoNetWork() {
        if (localCallback != null) {
            getHandler().obtainMessage(NONET).sendToTarget();
        }
        if (remoteCallback != null) {
            try {
                remoteCallback.onFailure(AccountConstant.RspCode.ERRNO_NO_NETWORK, null);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    protected void onSuccess(Object data) {
        if (localCallback != null) {
            Message msg = getHandler().obtainMessage(SUCCESS);
            msg.obj = data;
            msg.sendToTarget();
        }
        if (remoteCallback != null) {
            try {
                Bundle bundle = null;
                if (data instanceof Parcelable) {
                    bundle = new Bundle();
                    bundle.putParcelable(AccountConstant.KEY_LEPAY_RESPONSE, (Parcelable) data);
                }
                remoteCallback.onSuccess(bundle);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    protected void onError(int erro, String msg) {
        if (localCallback != null) {
            errorCode = erro;
            errorMsg = msg;
            getHandler().obtainMessage(ERROR).sendToTarget();
        }
        if (remoteCallback != null) {
            try {
                remoteCallback.onFailure(erro, msg);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    public abstract BaseResponse onExecute();

    public abstract boolean checkParamsValidate();

}
