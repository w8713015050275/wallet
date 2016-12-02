package com.letv.walletbiz.coupon.utils;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.google.gson.reflect.TypeToken;
import com.letv.wallet.common.http.beans.BaseResponse;
import com.letv.wallet.common.util.AccountHelper;
import com.letv.walletbiz.base.http.client.BaseRequestParams;
import com.letv.walletbiz.coupon.CouponConstant;
import com.letv.walletbiz.coupon.beans.BaseCoupon;
import com.letv.walletbiz.coupon.beans.CardCouponList;
import com.letv.walletbiz.movie.MovieTicketConstant;

import org.xutils.xmain;

import java.util.List;

/**
 * Created by lijujying on 16-5-26.
 */
public class CouponCheckNewTask implements Runnable  {
    private CouponCommonCallback mCallback;
    private Context mContext;
    private static final int MSG_LOAD_FINISHED = 1;
    private int mErrorCode = CouponCommonCallback.NO_ERROR;
    private Handler mHandler = new Handler(Looper.getMainLooper()) {

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_LOAD_FINISHED :
                    if (mCallback != null) {
                        mCallback.onLoadFinished(msg.obj, mErrorCode);
                    }
                    break;
            }
        }
    };

    public CouponCheckNewTask(Context context, CouponCommonCallback callback) {
        mContext = context;
        mCallback = callback;
    }

    @Override
    public void run() {
        checkNewCouponFromNetwork();
        Message msg = mHandler.obtainMessage(MSG_LOAD_FINISHED);
        mHandler.sendMessage(msg);
    }

    private void checkNewCouponFromNetwork() {
        BaseRequestParams params = new BaseRequestParams(CouponConstant.COUPON_CHECK_NEW_PATH);
        String token = AccountHelper.getInstance().getToken(mContext);
        params.addParameter(MovieTicketConstant.MOVIE_PARAM_SSO_TK, token);
        BaseResponse<CardCouponList> response = null;
        try {
            TypeToken typeToken = new TypeToken<BaseResponse<List<BaseCoupon>>>() {};
            response = xmain.http().postSync(params, typeToken.getType());
        } catch (Throwable throwable) {
            response = null;
        }
        if (response == null || response.errno != 10000) {
            mErrorCode = CouponCommonCallback.ERROR_NETWORK;
        }
        return ;
    }
}
