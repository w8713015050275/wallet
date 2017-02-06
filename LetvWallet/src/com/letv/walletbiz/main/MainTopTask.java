package com.letv.walletbiz.main;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.letv.wallet.common.http.beans.BaseResponse;
import com.letv.wallet.common.util.NetworkHelper;
import com.letv.walletbiz.main.bean.WalletTopListBean;

import java.util.Arrays;

/**
 * Created by zhuchuntao on 17-1-20.
 */
public class MainTopTask implements Runnable {

    private Context mContext;

    private int mErroCode = MainPanelHelper.NO_ERROR;
    private MainPanelHelper.Callback<WalletTopListBean> mCallback;

    private static final int MSG_LOAD_FROM_LOCAL_FINISHED = 1;
    private static final int MSG_LOAD_FROM_NETWORK_FINISHED = 2;
    private Handler mHandler = new Handler(Looper.getMainLooper()) {

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_LOAD_FROM_LOCAL_FINISHED:
                    if (mCallback != null) {
                        mCallback.onLoadFromLocalFinished((WalletTopListBean) msg.obj, msg.arg1);
                    }
                    break;
                case MSG_LOAD_FROM_NETWORK_FINISHED:
                    if (mCallback != null) {
                        mCallback.onLoadFromNetworkFinished((WalletTopListBean) msg.obj, msg.arg1, msg.arg2 == 1);
                    }
                    break;
            }
        }
    };

    public MainTopTask(Context context, MainPanelHelper.Callback<WalletTopListBean> callback) {
        mContext = context;
        mCallback = callback;
    }

    @Override
    public void run() {
        WalletTopListBean listBean = MainPanelHelper.getTopListFromDb(mContext);
        if (listBean != null) {
            sendMessage(MSG_LOAD_FROM_LOCAL_FINISHED, listBean, mErroCode, true);
        }
        WalletTopListBean newList = getTopListFromNetwork(mContext);
        boolean needUpdate = false;

        int length = (listBean != null && listBean.list != null) ? listBean.list.length : 0;
        int newLength = (newList != null && newList.list != null) ? newList.list.length : 0;
        if (listBean == null || (newList != null && (newList.version > listBean.version || newLength != length))) {
            needUpdate = true;
            if (newList != null) {
                MainPanelHelper.syncTopListToDb(mContext, newList);
            }
        }
        sendMessage(MSG_LOAD_FROM_NETWORK_FINISHED, newList, mErroCode, needUpdate);
    }

    private WalletTopListBean getTopListFromNetwork(Context context) {
        if (!NetworkHelper.isNetworkAvailable()) {
            mErroCode = MainPanelHelper.ERROR_NO_NETWORK;
            return null;
        }
        BaseResponse<WalletTopListBean> response = MainPanelHelper.getTopListFromNetwork();
        if (response == null || response.errno != 10000) {
            mErroCode = MainPanelHelper.ERROR_NETWORK;
        } else {
            mErroCode = MainPanelHelper.NO_ERROR;
        }
        WalletTopListBean result = response == null ? null : response.data;
        if (result != null && result.list != null && result.list.length > 1) {
            Arrays.sort(result.list);
        }
        return result;
    }

    private void sendMessage(int what, WalletTopListBean listBean, int erroCode, boolean needUpdate) {
        Message msg = mHandler.obtainMessage(what);
        msg.obj = listBean;
        msg.arg1 = erroCode;
        msg.arg2 = needUpdate ? 1 : 0;
        mHandler.sendMessage(msg);
    }

}
