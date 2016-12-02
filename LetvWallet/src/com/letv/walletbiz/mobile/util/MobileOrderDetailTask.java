package com.letv.walletbiz.mobile.util;

import android.content.Context;
import android.text.TextUtils;

import com.google.gson.reflect.TypeToken;
import com.letv.wallet.common.http.beans.BaseResponse;
import com.letv.wallet.common.util.AccountHelper;
import com.letv.wallet.common.util.NetworkHelper;
import com.letv.walletbiz.base.http.beans.order.OrderBaseBean;
import com.letv.walletbiz.base.http.client.BaseRequestParams;
import com.letv.walletbiz.mobile.MobileConstant;

import org.xutils.xmain;

/**
 * Created by changjiajie on 16-4-6.
 */
public class MobileOrderDetailTask implements Runnable {

    private Context mContext;
    private MobileCommonCallback<OrderBaseBean> mCallback;
    private String mOrderNum;

    private int mErrorCode = MobileCommonCallback.NO_ERROR;

    public MobileOrderDetailTask(Context context, String orderNum, MobileCommonCallback<OrderBaseBean> callback) {
        mContext = context;
        mOrderNum = orderNum;
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
            mErrorCode = MobileCommonCallback.ERROR_NETWORK;
        }
        if (mCallback != null) {
            mCallback.onLoadFinished(order, mErrorCode);
        }
    }

    private BaseResponse<OrderBaseBean> getOrderFromNetwork() {
        if (mContext == null || TextUtils.isEmpty(mOrderNum)) {
            mErrorCode = MobileCommonCallback.ERROR_PARAM;
            return null;
        }
        if (!NetworkHelper.isNetworkAvailable()) {
            mErrorCode = MobileCommonCallback.ERROR_NO_NETWORK;
            return null;
        }
        BaseRequestParams params = new BaseRequestParams(MobileConstant.PATH.ORDER_DETAIL_QUERY);
        String token = AccountHelper.getInstance().getToken(mContext);
        params.addParameter(MobileConstant.PARAM.TOKEN, token);
        params.addParameter(MobileConstant.PARAM.ORDER_SN, mOrderNum);
        BaseResponse<OrderBaseBean> response = null;
        try {
            TypeToken typeToken = new TypeToken<BaseResponse<OrderBaseBean>>() {
            };
            response = xmain.http().getSync(params, typeToken.getType());
        } catch (Throwable throwable) {
            mErrorCode = MobileCommonCallback.ERROR_NETWORK;
            response = null;
        }
        return response;
    }
}
