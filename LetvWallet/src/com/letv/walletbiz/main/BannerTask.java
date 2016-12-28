package com.letv.walletbiz.main;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.letv.wallet.common.http.beans.BaseResponse;
import com.letv.wallet.common.util.NetworkHelper;
import com.letv.walletbiz.main.bean.WalletBannerListBean;

import java.util.Arrays;

/**
 * Created by liuliang on 16-4-14.
 */
public class BannerTask implements Runnable {

    private Context mContext;

    private int mErroCode = MainPanelHelper.NO_ERROR;
    private MainPanelHelper.Callback<WalletBannerListBean> mCallback;

    private int mPositionId = 0;
    private static final int MSG_LOAD_FROM_LOCAL_FINISHED = 1;
    private static final int MSG_LOAD_FROM_NETWORK_FINISHED = 2;
    private Handler mHandler = new Handler(Looper.getMainLooper()) {

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_LOAD_FROM_LOCAL_FINISHED:
                    if (mCallback != null) {
                        mCallback.onLoadFromLocalFinished((WalletBannerListBean) msg.obj, msg.arg1);
                    }
                    break;
                case MSG_LOAD_FROM_NETWORK_FINISHED:
                    if (mCallback != null) {
                        mCallback.onLoadFromNetworkFinished((WalletBannerListBean) msg.obj, msg.arg1, msg.arg2 == 1);
                    }
                    break;
            }
        }
    };

    public BannerTask(Context context, MainPanelHelper.Callback<WalletBannerListBean> callback, int positionId) {
        mContext = context;
        mCallback = callback;
        mPositionId = positionId;
    }

    @Override
    public void run() {
        WalletBannerListBean listBean = MainPanelHelper.getBannerListFromDb(mContext, mPositionId);
        if (listBean != null) {
            sendMessage(MSG_LOAD_FROM_LOCAL_FINISHED, listBean, mErroCode, true);
        }
        WalletBannerListBean newList = getBannerListFromNetwork(mContext, mPositionId);
        boolean needUpdate = false;
        int length = (listBean != null && listBean.list != null) ? listBean.list.length : 0;
        int newLength = (newList != null && newList.list != null) ? newList.list.length : 0;
        if (listBean == null || (newList != null && (newList.version > listBean.version || newLength != length))) {
            needUpdate = true;
            if (newList != null) {
                MainPanelHelper.syncBannerListToDb(mContext, newList, mPositionId);
            }
        }
        sendMessage(MSG_LOAD_FROM_NETWORK_FINISHED, newList, mErroCode, needUpdate);
    }

    private WalletBannerListBean getBannerListFromNetwork(Context context, int positionId) {
        if (!NetworkHelper.isNetworkAvailable()) {
            mErroCode = MainPanelHelper.ERROR_NO_NETWORK;
            return null;
        }
        BaseResponse<WalletBannerListBean> response = MainPanelHelper.getBannerListFromNetwork(positionId);
        if (response == null || response.errno != 10000) {
            mErroCode = MainPanelHelper.ERROR_NETWORK;
        } else {
            mErroCode = MainPanelHelper.NO_ERROR;
        }
        WalletBannerListBean result = response == null ? null : response.data;
        if (result != null && result.list != null) {
            Arrays.sort(result.list);
        }
        return result;
    }

    private void sendMessage(int what, WalletBannerListBean listBean, int erroCode, boolean needUpdate) {
        Message msg = mHandler.obtainMessage(what);
        msg.obj = listBean;
        msg.arg1 = erroCode;
        msg.arg2 = needUpdate ? 1 : 0;
        mHandler.sendMessage(msg);
    }
}
