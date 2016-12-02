package com.letv.walletbiz.coupon.utils;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.letv.wallet.common.http.beans.BaseResponse;
import com.letv.wallet.common.util.NetworkHelper;
import com.letv.walletbiz.coupon.beans.CouponExpiredListResponseResult;
import com.letv.walletbiz.coupon.beans.CouponListRequestParams;
import com.letv.walletbiz.coupon.beans.CouponListResponseResult;

/**
 * Created by lijunying on 16-4-18.
 */
public class CouponListLoadTask implements Runnable {
    private static final String TAG = CouponListLoadTask.class.getSimpleName();
    private Context mContext;
    private CouponCommonCallback<CouponListResponseResult> mCallback;
    private CouponCommonCallback<CouponExpiredListResponseResult> mExpiredCallback;
    private int mErrorCode = CouponCommonCallback.NO_ERROR;
    private CouponListRequestParams reqParams;
    private Handler mhandler;
    private Looper mLooper;
    private static final int MSG_COUPON_LIST = 0, MSG_COUPON_LIST_EXPIRED = 1;
    private int requestType;


    public void setResponseCallback(CouponCommonCallback<CouponListResponseResult> mCallback) {
        this.mCallback = mCallback;
        requestType = MSG_COUPON_LIST;
    }

    public void setExpiredResponseCallback(CouponCommonCallback<CouponExpiredListResponseResult> mCallback) {
        this.mExpiredCallback = mCallback;
        requestType = MSG_COUPON_LIST_EXPIRED;
    }

    public CouponListLoadTask(Context mContext, long last_id, int limit) {
        this.mContext = mContext;
        reqParams = new CouponListRequestParams();
        reqParams.setLast_id(last_id);
        reqParams.setLimit(limit);
        setResponseHandler();
    }

    private void setResponseHandler() {
        this.mLooper = Looper.getMainLooper();
        if (this.mhandler == null) {
            this.mhandler = new CouponListLoadTask.ResponderHandler(this, this.mLooper);
        }
    }

    @Override
    public void run() {
        Object result = null;

        switch (requestType) {
            case MSG_COUPON_LIST:
                result = getCouponListFromNetwork();
                break;
            case MSG_COUPON_LIST_EXPIRED:
                result = getCouponExpiredListFromNetwork();
                break;
        }

        this.mhandler.sendMessage(this.obtainMessage(requestType, result));

    }

    /**
     * 获取用户优惠券列表
     *
     * @return
     */
    private CouponListResponseResult getCouponListFromNetwork() {
        if (mContext == null) {
            mErrorCode = CouponCommonCallback.ERROR_PARAM;
            return null;
        }
        if (!NetworkHelper.isNetworkAvailable()) {
            mErrorCode = CouponCommonCallback.ERROR_NO_NETWORK;
            return null;
        }
        if (reqParams == null) {
            reqParams = new CouponListRequestParams();
        }
        BaseResponse<CouponListResponseResult> response = CouponListHelper.getCouponListOnlineSync(mContext, reqParams);
        if (response == null || response.errno != 10000) {
            mErrorCode = CouponCommonCallback.ERROR_NETWORK;
        }
        return response == null ? null : response.data;
    }

    /**
     * 获取已失效的优惠券列表
     *
     * @return
     */
    private CouponExpiredListResponseResult getCouponExpiredListFromNetwork() {
        if (mContext == null) {
            mErrorCode = CouponCommonCallback.ERROR_PARAM;
            return null;
        }
        if (!NetworkHelper.isNetworkAvailable()) {
            mErrorCode = CouponCommonCallback.ERROR_NO_NETWORK;
            return null;
        }
        BaseResponse<CouponExpiredListResponseResult> response = CouponListHelper.getCouponExpiredListOnlineSync(mContext, reqParams);
        if (response == null) {
            mErrorCode = CouponCommonCallback.ERROR_NETWORK;

        }
        return response == null ? null : response.data;
    }

    protected void handleMessage(Message msg) {
        switch (msg.what) {
            case MSG_COUPON_LIST:
                if (mCallback != null) {
                    mCallback.onLoadFinished((CouponListResponseResult) msg.obj, mErrorCode);
                }
                break;
            case MSG_COUPON_LIST_EXPIRED:
                if (mExpiredCallback != null) {
                    mExpiredCallback.onLoadFinished((CouponExpiredListResponseResult) msg.obj, mErrorCode);
                }
                break;
        }
    }

    private static class ResponderHandler extends Handler {
        private final CouponListLoadTask mtask;

        ResponderHandler(CouponListLoadTask mtask, Looper looper) {
            super(looper);
            this.mtask = mtask;
        }

        public void handleMessage(Message msg) {
            this.mtask.handleMessage(msg);
        }
    }

    protected Message obtainMessage(int responseMessageId, Object responseMessageData) {
        return Message.obtain(this.mhandler, responseMessageId, responseMessageData);
    }

}
