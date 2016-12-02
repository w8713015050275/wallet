package com.letv.walletbiz.main;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.letv.wallet.common.http.beans.BaseResponse;
import com.letv.wallet.common.util.NetworkHelper;
import com.letv.walletbiz.main.bean.WalletServiceListBean;

import java.util.Arrays;

/**
 * Created by liuliang on 16-4-11.
 */
public class MainServiceTask implements Runnable {

    private Context mContext;

    private int mErroCode = MainPanelHelper.NO_ERROR;
    private MainPanelHelper.Callback<WalletServiceListBean> mCallback;

    private static final int MSG_LOAD_FROM_LOCAL_FINISHED = 1;
    private static final int MSG_LOAD_FROM_NETWORK_FINISHED = 2;
    private Handler mHandler = new Handler(Looper.getMainLooper()) {

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_LOAD_FROM_LOCAL_FINISHED:
                    if (mCallback != null) {
                        mCallback.onLoadFromLocalFinished((WalletServiceListBean) msg.obj, msg.arg1);
                    }
                    break;
                case MSG_LOAD_FROM_NETWORK_FINISHED:
                    if (mCallback != null) {
                        mCallback.onLoadFromNetworkFinished((WalletServiceListBean) msg.obj, msg.arg1, msg.arg2 == 1);
                    }
                    break;
            }
        }
    };

    public MainServiceTask(Context context, MainPanelHelper.Callback<WalletServiceListBean> callback) {
        mContext = context;
        mCallback = callback;
    }

    @Override
    public void run() {
        WalletServiceListBean listBean = MainPanelHelper.getServiceListFromDb(mContext);
        if (listBean != null) {
            sendMessage(MSG_LOAD_FROM_LOCAL_FINISHED, listBean, mErroCode, true);
        }
        WalletServiceListBean newList = getServiceListFromNetwork(mContext);
        boolean needUpdate = false;

        if (listBean == null || (newList != null && (newList.version > listBean.version))) {
            needUpdate = true;
            if (newList != null) {
                MainPanelHelper.syncServiceListToDb(mContext, newList);
            }
        }
        sendMessage(MSG_LOAD_FROM_NETWORK_FINISHED, newList, mErroCode, needUpdate);
    }

    private WalletServiceListBean getServiceListFromNetwork(Context context) {
        if (!NetworkHelper.isNetworkAvailable()) {
            mErroCode = MainPanelHelper.ERROR_NO_NETWORK;
            return null;
        }
        BaseResponse<WalletServiceListBean> response = MainPanelHelper.getServiceListFromNetwork();
        if (response == null || response.errno != 10000) {
            mErroCode = MainPanelHelper.ERROR_NETWORK;
        } else {
            mErroCode = MainPanelHelper.NO_ERROR;
        }
        WalletServiceListBean result = response == null ? null : response.data;
        if (result != null && result.list != null) {
            Arrays.sort(result.list);
        }
        return result;
    }

    private void sendMessage(int what, WalletServiceListBean listBean, int erroCode, boolean needUpdate) {
        Message msg = mHandler.obtainMessage(what);
        msg.obj = listBean;
        msg.arg1 = erroCode;
        msg.arg2 = needUpdate ? 1 : 0;
        mHandler.sendMessage(msg);
    }
}
