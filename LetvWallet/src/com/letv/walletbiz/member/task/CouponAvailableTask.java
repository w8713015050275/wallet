package com.letv.walletbiz.member.task;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.google.gson.reflect.TypeToken;
import com.letv.wallet.common.http.beans.BaseResponse;
import com.letv.walletbiz.base.http.client.BaseRequestParams;
import com.letv.walletbiz.member.MemberConstant;
import com.letv.walletbiz.member.beans.CouponAvailableListBean;
import com.letv.walletbiz.member.beans.CouponBean;
import com.letv.walletbiz.member.util.MemberCommonCallback;

import org.xutils.xmain;

/**
 * Created by zhangzhiwei1 on 16-11-22.
 */

public class CouponAvailableTask implements Runnable {

    private final String mSkus;
    private String mToken;
    private MemberCommonCallback<CouponBean[]> mCallback;

    private int mErrorCode = MemberCommonCallback.NO_ERROR;

    private static final int MSG_LOAD_FINISHED = 1;
    private Handler mHandler = new Handler(Looper.getMainLooper()) {

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_LOAD_FINISHED:
                    if (mCallback != null) {
                        CouponAvailableListBean listBean = (CouponAvailableListBean) msg.obj;
                        mCallback.onLoadFinished(listBean != null ? listBean.list : null, msg.arg1,true);
                    }
                    break;
            }
        }
    };

    public CouponAvailableTask(String token, String skus, MemberCommonCallback<CouponBean[]> callback) {
        mToken = token;
        mSkus = skus;
        mCallback = callback;
    }

    @Override
    public void run() {
        CouponAvailableListBean listBean = getCouponAvailableListFromNetwork();
        sendMessage(MSG_LOAD_FINISHED,listBean,mErrorCode);
    }

    private CouponAvailableListBean getCouponAvailableListFromNetwork() {
        BaseRequestParams params = new BaseRequestParams(MemberConstant.MEMBER_GET_AVAILABLE_COUPON_LIST);
        params.addQueryStringParameter(MemberConstant.MEMBER_TOKEN, mToken);
        params.addQueryStringParameter(MemberConstant.PARAM.MEMBER_SKUS, mSkus);

        BaseResponse<CouponAvailableListBean> response = null;
        try {
            TypeToken typeToken = new TypeToken<BaseResponse<CouponAvailableListBean>>() {
            };
            response = xmain.http().getSync(params, typeToken.getType());
        } catch (Throwable throwable) {
            mErrorCode = MemberCommonCallback.ERROR_NETWORK;
            response = null;
        }

        if (response == null) {
            return null;
        }

        return response.data;
    }

    private void sendMessage(int what, CouponAvailableListBean listBean, int erroCode) {
        Message msg = mHandler.obtainMessage(what);
        msg.obj = listBean;
        msg.arg1 = erroCode;
        mHandler.sendMessage(msg);
    }
}
