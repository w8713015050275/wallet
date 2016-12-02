package com.letv.walletbiz.coupon.utils;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.google.gson.reflect.TypeToken;
import com.letv.wallet.common.http.beans.BaseResponse;
import com.letv.wallet.common.util.AccountHelper;
import com.letv.walletbiz.base.http.client.BaseRequestParams;
import com.letv.walletbiz.coupon.beans.CardCouponList;
import com.letv.walletbiz.movie.MovieTicketConstant;
import com.letv.walletbiz.movie.beans.MovieOrder;

import org.xutils.xmain;

/**
 * Created by liuliang on 16-4-19.
 */
public class CardCouponListTask implements Runnable {

    private Context mContext;
    private CouponCommonCallback<CardCouponList> mCallback;

    private int mProgress = MovieOrder.MOVIE_TICKET_PROGRESS_UNCONSUMED;
    private long mLastId = -1;
    private int mModel = -1;
    private int mLimit = -1;

    private int mErrorCode = CouponCommonCallback.NO_ERROR;

    private static final int MSG_LOAD_FINISHED = 1;
    private Handler mHandler = new Handler(Looper.getMainLooper()) {

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_LOAD_FINISHED :
                    if (mCallback != null) {
                        mCallback.onLoadFinished((CardCouponList) msg.obj, mErrorCode);
                    }
                    break;
            }
        }
    };

    public CardCouponListTask(Context context, CouponCommonCallback<CardCouponList> callback) {
        mContext = context;
        mCallback = callback;
    }

    public void setParams(int progress, int limit) {
        setParams(progress, -1, -1, limit);
    }

    public void setParams(int progress, long lastId, int limit) {
        setParams(progress, lastId, -1, limit);
    }

    public void setParams(int progress, long lastId, int model, int limit) {
        mProgress = progress;
        mLastId = lastId;
        mModel = model;
        mLimit = limit;
    }

    @Override
    public void run() {
        CardCouponList cardCouponList = getCardCouponListFromNetwork();
        Message msg = mHandler.obtainMessage(MSG_LOAD_FINISHED);
        msg.obj = cardCouponList;
         mHandler.sendMessage(msg);
    }

    private CardCouponList getCardCouponListFromNetwork() {
        BaseRequestParams params = new BaseRequestParams(MovieTicketConstant.MOVIE_PATH_ORDER_LIST);
        String token = AccountHelper.getInstance().getToken(mContext);
        params.addParameter(MovieTicketConstant.MOVIE_PARAM_SSO_TK, token);
        params.addParameter(MovieTicketConstant.MOVIE_PARAM_PROGRESS, mProgress);
        if (mLastId != -1) {
            params.addParameter(MovieTicketConstant.MOVIE_PARAM_LAST_ID, mLastId);
        }
        if (mModel != -1) {
            params.addParameter(MovieTicketConstant.MOVIE_PARAM_MODEL, mModel);
        }
        if (mLimit != -1) {
            params.addParameter(MovieTicketConstant.MOVIE_PARAM_LIMIT, mLimit);
        }
        BaseResponse<CardCouponList> response = null;
        try {
            TypeToken typeToken = new TypeToken<BaseResponse<CardCouponList>>() {};
            response = xmain.http().getSync(params, typeToken.getType());
        } catch (Throwable throwable) {
            response = null;
        }
        if (response == null || response.errno != 10000) {
            mErrorCode = CouponCommonCallback.ERROR_NETWORK;
        }

        return response == null ? null : response.data;
    }
}
