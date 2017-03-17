package com.letv.wallet.online.utils;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.letv.wallet.account.base.AccountGateway;
import com.letv.wallet.common.http.beans.BaseResponse;
import com.letv.wallet.common.util.LogHelper;
import com.letv.wallet.common.util.NetworkHelper;
import com.letv.wallet.online.activity.LePayEntryActivity;
import com.letv.wallet.online.bean.LePayChannelListBean;

/**
 * Created by changjiajie on 17-1-9.
 */

public class LePayChannelListLoadTask implements Runnable {

    private static final String TAG = LePayChannelListLoadTask.class.getSimpleName();

    private LePayOnlineCallback mCallback;
    private String mPayInfo;
    private int ERROR_CODE;

    public LePayChannelListLoadTask(String payInfo) {
        this.mPayInfo = payInfo;
    }

    public void setCallback(LePayOnlineCallback callback) {
        this.mCallback = callback;
    }

    private static final int MSG_LOAD_FINISHED = 100;
    private static final int MSG_LOAD_ERROR = 101;

    protected Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_LOAD_FINISHED:
                    mCallback.onSuccess(msg.obj);
                    break;
                case MSG_LOAD_ERROR:
                    mCallback.onError(msg.obj, msg.arg1);
                    break;
            }
        }
    };

    @Override
    public void run() {
        BaseResponse<LePayChannelListBean> response = null;
        try {
            response = AccountGateway.payChannel(mPayInfo);
            if (response != null) {
                if (response.data != null) {
                    Message msg = mHandler.obtainMessage(MSG_LOAD_FINISHED);
                    msg.obj = response;
                    mHandler.sendMessage(msg);
                    return;
                }
                LogHelper.e("[%S] errno = [%s] | errmsg = [%s]", TAG, response.errno, response.errmsg);
                ERROR_CODE = LePayOnlineCallback.ERROR_OTHER;
            } else {
                LogHelper.e("[%S] channel data error", TAG);
                ERROR_CODE = LePayOnlineCallback.ERROR_DATA;
                if (!NetworkHelper.isNetworkAvailable()) {
                    ERROR_CODE = LePayOnlineCallback.ERROR_NETWORK;
                    LogHelper.e("[%S] no network", TAG);
                }
            }
            Message msg = mHandler.obtainMessage(MSG_LOAD_ERROR);
            msg.obj = response;
            msg.arg1 = ERROR_CODE;
            mHandler.sendMessage(msg);
        } catch (Exception e) {
            e.printStackTrace();
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }

}
