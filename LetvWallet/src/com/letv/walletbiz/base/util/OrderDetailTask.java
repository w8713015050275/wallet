package com.letv.walletbiz.base.util;

import android.content.Context;

import com.google.gson.reflect.TypeToken;
import com.letv.wallet.common.http.beans.BaseResponse;
import com.letv.wallet.common.util.CommonCallback;
import com.letv.wallet.common.util.NetworkHelper;
import com.letv.walletbiz.base.http.beans.order.OrderBaseBean;
import com.letv.walletbiz.base.http.client.BaseRequestParams;

import org.xutils.xmain;

/**
 * Created by changjiajie on 16-4-6.
 */
public class OrderDetailTask implements Runnable {

    private Context mContext;
    private CommonCallback<OrderBaseBean> mCallback;
    private BaseRequestParams mRequestParams;
    private TypeToken mTypeToken;

    private int mErrorCode = CommonCallback.NO_ERROR;

    public OrderDetailTask(Context context, BaseRequestParams requestParams, TypeToken typeToken, CommonCallback<OrderBaseBean> callback) {
        mContext = context;
        mRequestParams = requestParams;
        mTypeToken = typeToken;
        mCallback = callback;

    }

    @Override
    public void run() {
        BaseResponse<OrderBaseBean> response = getOrderFromNetwork();
        if (response == null) {
            if (mCallback != null) {
                mCallback.onLoadFinished(null, mErrorCode);
            }
            return;
        }
        OrderBaseBean order = response.data;
        if (order == null) {
            mErrorCode = CommonCallback.ERROR_NETWORK;
        }
        if (mCallback != null) {
            mCallback.onLoadFinished(order, mErrorCode);
        }
    }

    private BaseResponse<OrderBaseBean> getOrderFromNetwork() {
        if (mContext == null || mRequestParams == null) {
            mErrorCode = CommonCallback.ERROR_PARAM;
            return null;
        }
        if (!NetworkHelper.isNetworkAvailable()) {
            mErrorCode = CommonCallback.ERROR_NO_NETWORK;
            return null;
        }
        BaseRequestParams params = mRequestParams;
        BaseResponse<OrderBaseBean> response = null;
        try {
            if (mTypeToken != null) {
                response = xmain.http().getSync(params, mTypeToken.getType());
            }
        } catch (Throwable throwable) {
            mErrorCode = CommonCallback.ERROR_NETWORK;
            response = null;
        }
        return response;
    }
}
