package com.letv.wallet.account.task;


import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Parcelable;

import com.letv.tracker2.enums.Key;
import com.letv.wallet.account.aidl.v1.AccountConstant;
import com.letv.wallet.account.base.AccountGateway;
import com.letv.wallet.account.utils.AccountUtils;
import com.letv.wallet.base.util.Action;
import com.letv.wallet.common.http.beans.BaseResponse;
import com.letv.wallet.common.util.NetworkHelper;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by lijunying on 16-12-26.
 * 账户开户接口
 */

public class AccountCreateTask implements Runnable  {

    private Handler mMainHandler = null; //本地调用主线程返回

    public static final int SUCCESS = 1;
    public static final int ERROR = 2;
    public static final int NONET = 3;

    public static final String FAIL_NO_PHONE = "no_phone";
    public static final String FAIL_OTHER = "other";

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

    private AccountCommonCallback localCallback;
    private AccountCreateRemoteCallBack remoteCallback;

    public AccountCreateTask(AccountCommonCallback callback) {
        localCallback = callback;
    }

    public AccountCreateTask(AccountCreateRemoteCallBack callback) {
        remoteCallback = callback;
    }

    @Override
    public void run() {

        if (!NetworkHelper.isNetworkAvailable()) {
            onNoNetWork();
            return;
        }

        handleResult(AccountGateway.createAcount());
    }

    protected void handleResult(BaseResponse response) {
        if (response == null) {
            onError(AccountConstant.RspCode.ERROR_NETWORK, null);
            return;
        }

        if (response.errno == AccountConstant.RspCode.SUCCESS) {
            AccountUtils.updateCreatedAccountStatus();
            onSuccess(response.data);
            return;
        }

        if (response.errno == AccountConstant.RspCode.ERRNO_MOBILE_EMPTY) {
            uploadData(FAIL_NO_PHONE);
        }else {
            uploadData(FAIL_OTHER);
        }
        onError(response.errno, response.errmsg);
    }

    protected void onNoNetWork() {
        if (localCallback != null) {
            getHandler().obtainMessage(NONET).sendToTarget();
        }
        if (remoteCallback != null) {
            remoteCallback.onFailure(AccountConstant.RspCode.ERRNO_NO_NETWORK, null);
        }
    }

    protected void onSuccess(Object data) {
        if (localCallback != null) {
            Message msg = getHandler().obtainMessage(SUCCESS);
            msg.obj = data;
            msg.sendToTarget();
        }
        if (remoteCallback != null) {
            Bundle bundle = null;
            if (data instanceof Parcelable) {
                bundle = new Bundle();
                bundle.putParcelable(AccountConstant.KEY_LEPAY_RESPONSE, (Parcelable) data);
            }
            remoteCallback.onSuccess(bundle);
        }
    }

    protected void onError(int erro, String msg) {
        if (localCallback != null) {
            errorCode = erro;
            errorMsg = msg;
            getHandler().obtainMessage(ERROR).sendToTarget();
        }
        if (remoteCallback != null) {
            remoteCallback.onFailure(erro, msg);
        }
    }

    public interface AccountCreateRemoteCallBack{
        void onSuccess(Bundle bundle);
        void onFailure(int errorCode, String errorMsg);
    }

    private void uploadData(String reason){
        Map<String, Object> props = new HashMap<String, Object>();
        props.put(Key.Content.getKeyId(), reason);
        Action.uploadCustom(Action.EVENT_TYPE_FAIL, Action.ACCOUNT_CREATE_FAIL, props);
    }
}
