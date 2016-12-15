package com.letv.walletbiz.member.task;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.google.gson.reflect.TypeToken;
import com.letv.wallet.common.http.beans.BaseResponse;
import com.letv.walletbiz.base.http.client.BaseRequestParams;
import com.letv.walletbiz.member.MemberConstant;
import com.letv.walletbiz.member.beans.OrderCreateBean;
import com.letv.walletbiz.member.util.MemberCommonCallback;

import org.xutils.xmain;

/**
 * Created by zhangzhiwei1 on 16-11-22.
 */

public class OrderCreateTask implements Runnable {
    private String mToken;
    private String mSkuNo;
    private String mCoupon;

    private MemberCommonCallback<OrderCreateBean> mCallback;

    private int mErrorCode = MemberCommonCallback.NO_ERROR;

    private static final int MSG_LOAD_FINISHED = 1;
    private Handler mHandler = new Handler(Looper.getMainLooper()) {

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_LOAD_FINISHED:
                    if (mCallback != null) {
                        OrderCreateBean listBean = (OrderCreateBean) msg.obj;
                        mCallback.onLoadFinished(listBean, msg.arg1,true);
                    }
                    break;
            }
        }
    };
    public OrderCreateTask(String token, String sku_no, String coupon, MemberCommonCallback<OrderCreateBean> callback) {
        mToken = token;
        mSkuNo = sku_no;
        mCoupon = coupon;
        mCallback = callback;
    }

    @Override
    public void run() {
        OrderCreateBean orderCreateBean = createOrder();
        sendMessage(MSG_LOAD_FINISHED,orderCreateBean,mErrorCode);
    }

    private OrderCreateBean createOrder() {
        BaseRequestParams params = new BaseRequestParams(MemberConstant.MEMBER_CREATE_ORDER);
        params.addBodyParameter(MemberConstant.MEMBER_TOKEN, mToken);
        params.addBodyParameter(MemberConstant.MEMBER_SKU_NO, mSkuNo);
        params.addBodyParameter(MemberConstant.MEMBER_COUPON, mCoupon);

        BaseResponse<OrderCreateBean> response = null;
        try {
            TypeToken typeToken = new TypeToken<BaseResponse<OrderCreateBean>>() {
            };
            response = xmain.http().postSync(params, typeToken.getType());

        } catch (Throwable throwable) {
            mErrorCode = MemberCommonCallback.ERROR_NETWORK;
            response = null;
        }

        if (response == null) {
            return null;
        }

        return response.data;
    }

    private void sendMessage(int what, OrderCreateBean listBean, int erroCode) {
        Message msg = mHandler.obtainMessage(what);
        msg.obj = listBean;
        msg.arg1 = erroCode;
        mHandler.sendMessage(msg);
    }
}
