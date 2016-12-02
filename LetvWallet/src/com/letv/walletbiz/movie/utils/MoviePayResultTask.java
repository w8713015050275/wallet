package com.letv.walletbiz.movie.utils;

import android.content.Context;
import android.text.TextUtils;

import com.google.gson.reflect.TypeToken;
import com.letv.wallet.common.http.beans.BaseResponse;
import com.letv.wallet.common.util.AccountHelper;
import com.letv.wallet.common.util.NetworkHelper;
import com.letv.walletbiz.base.http.client.BaseRequestParams;
import com.letv.walletbiz.movie.MovieTicketConstant;
import com.letv.walletbiz.movie.beans.MoviePayResult;

import org.xutils.xmain;

/**
 * Created by liuliang on 16-3-13.
 */
public class MoviePayResultTask implements Runnable {

    private Context mContext;
    private MovieCommonCallback<MoviePayResult> mCallback;
    private int mErrorCode = MovieCommonCallback.NO_ERROR;

    private String mOrderNum = "";

    public MoviePayResultTask(Context context, String orderNum, MovieCommonCallback<MoviePayResult> callback) {
        mContext = context;
        mCallback = callback;
        mOrderNum = orderNum;
    }

    @Override
    public void run() {
        BaseResponse<MoviePayResult> response = getPayResult();
        MoviePayResult moviePayResult = null;
        if (response != null) {
            moviePayResult = response.data;
            if (moviePayResult == null) {
                mErrorCode = MovieCommonCallback.ERROR_NETWORK;
            }
        }
        if (mCallback != null) {
            mCallback.onLoadFinished(moviePayResult, mErrorCode);
        }
    }

    private BaseResponse<MoviePayResult> getPayResult() {
        if (mContext == null || TextUtils.isEmpty(mOrderNum)) {
            mErrorCode = MovieCommonCallback.ERROR_PARAM;
            return null;
        }
        if (!NetworkHelper.isNetworkAvailable()) {
            mErrorCode = MovieCommonCallback.ERROR_NO_NETWORK;
            return null;
        }

        BaseRequestParams params = new BaseRequestParams(MovieTicketConstant.MOVIE_PATH_ORDER_PAYSTAT);
        String token = AccountHelper.getInstance().getToken(mContext);
        params.addParameter(MovieTicketConstant.MOVIE_PARAM_SSO_TK, token);
        params.addParameter(MovieTicketConstant.MOVIE_PARAM_ORDER_NO, mOrderNum);
        BaseResponse<MoviePayResult> response = null;
        try {
            TypeToken typeToken = new TypeToken<BaseResponse<MoviePayResult>>() {};
            response = xmain.http().getSync(params, typeToken.getType());
        } catch (Throwable throwable) {
            mErrorCode = MovieCommonCallback.ERROR_NETWORK;
            response = null;
        }
        return response;
    }
}
