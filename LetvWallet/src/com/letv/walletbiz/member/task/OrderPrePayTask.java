package com.letv.walletbiz.member.task;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.google.gson.reflect.TypeToken;
import com.letv.wallet.common.http.beans.BaseResponse;
import com.letv.walletbiz.base.http.client.BaseRequestParams;
import com.letv.walletbiz.member.MemberConstant;
import com.letv.walletbiz.member.beans.OrderPayBean;
import com.letv.walletbiz.member.util.MemberCommonCallback;

import org.xutils.xmain;

/**
 * Created by zhangzhiwei1 on 16-11-22.
 */

public class OrderPrePayTask implements Runnable {
    private String mToken;
    private int mClientType;
    private String mOrderSN;
    private int mPlatform;
    private boolean mDBG;
    private MemberCommonCallback<OrderPayBean> mCallback;

    private int mErrorCode = MemberCommonCallback.NO_ERROR;

    private static final int MSG_LOAD_FINISHED = 1;
    private Handler mHandler = new Handler(Looper.getMainLooper()) {

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_LOAD_FINISHED :
                    if (mCallback != null) {
                        mCallback.onLoadFinished((OrderPayBean) msg.obj, mErrorCode,true);
                    }
                    break;
            }
        }
    };

    public OrderPrePayTask(String token, int client_type, String order_sn, int platform, boolean dbg, MemberCommonCallback<OrderPayBean> callback) {
        mToken = token;
        mClientType = client_type;
        mOrderSN = order_sn;
        mPlatform = platform;
        mDBG = dbg;
        mCallback = callback;
    }

    @Override
    public void run() {
        BaseRequestParams params = new BaseRequestParams(MemberConstant.MEMBER_PAY_ORDER);
        params.addBodyParameter(MemberConstant.MEMBER_TOKEN,mToken);
        params.addBodyParameter(MemberConstant.MEMBER_CLIENT_TYPE, "" + mClientType);
        params.addBodyParameter(MemberConstant.MEMBER_ORDER_SN,mOrderSN);
        params.addBodyParameter(MemberConstant.MEMBER_ORDER_PLATFORM,"" + mPlatform);
        params.addBodyParameter(MemberConstant.MEMBER_ORDER_DBG,"" + mDBG);

        BaseResponse<OrderPayBean> response = null;
        try {
            TypeToken typeToken = new TypeToken<BaseResponse<OrderPayBean>>() {
            };
            response = xmain.http().postSync(params, typeToken.getType());
        } catch (Throwable throwable) {
            mErrorCode = MemberCommonCallback.ERROR_NETWORK;
            response = null;
        }

        if (response == null) {
            if (mCallback != null) {
                Message msg = mHandler.obtainMessage(MSG_LOAD_FINISHED);
                msg.obj = null;
                mHandler.sendMessage(msg);
            }
            return;
        }

        OrderPayBean orderPayBean = null;
        orderPayBean = response.data;
        if (orderPayBean == null) {
            mErrorCode = MemberCommonCallback.ERROR_NETWORK;
        }
        if (mCallback != null) {
            Message msg = mHandler.obtainMessage(MSG_LOAD_FINISHED);
            msg.obj = orderPayBean;
            mHandler.sendMessage(msg);
        }
    }
}
