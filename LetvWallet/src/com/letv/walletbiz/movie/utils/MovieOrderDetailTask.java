package com.letv.walletbiz.movie.utils;

import android.content.Context;
import android.text.TextUtils;

import com.google.gson.reflect.TypeToken;
import com.letv.wallet.common.http.beans.BaseResponse;
import com.letv.wallet.common.util.AccountHelper;
import com.letv.wallet.common.util.NetworkHelper;
import com.letv.walletbiz.base.http.client.BaseRequestParams;
import com.letv.walletbiz.movie.MovieTicketConstant;
import com.letv.walletbiz.movie.beans.MovieOrder;

import org.xutils.xmain;

/**
 * Created by liuliang on 16-3-7.
 */
public class MovieOrderDetailTask implements Runnable {

    private Context mContext;
    private MovieCommonCallback<MovieOrder> mCallback;
    private String mOrderNum;

    private int mErrorCode = MovieCommonCallback.NO_ERROR;

    public MovieOrderDetailTask(Context context, String orderNum, MovieCommonCallback<MovieOrder> callback) {
        mContext = context;
        mOrderNum = orderNum;
        mCallback = callback;
    }

    @Override
    public void run() {
        BaseResponse<MovieOrder> response = getMovieOrderFromNetwork();
        if (response == null) {
            if (mCallback != null) {
                mCallback.onLoadFinished(null, mErrorCode);
            }
            return;
        }
        MovieOrder order = response.data;
        if (order == null) {
            mErrorCode = MovieCommonCallback.ERROR_NETWORK;
        }
        if (mCallback != null) {
            mCallback.onLoadFinished(order, mErrorCode);
        }
    }

    private BaseResponse<MovieOrder> getMovieOrderFromNetwork() {
        if (mContext == null || TextUtils.isEmpty(mOrderNum)) {
            mErrorCode = MovieCommonCallback.ERROR_PARAM;
            return null;
        }
        if (!NetworkHelper.isNetworkAvailable()) {
            mErrorCode = MovieCommonCallback.ERROR_NO_NETWORK;
            return null;
        }
        BaseRequestParams params = new BaseRequestParams(MovieTicketConstant.MOVIE_PATH_ORDER_DETAIL);
        String token = AccountHelper.getInstance().getToken(mContext);
        params.addParameter(MovieTicketConstant.MOVIE_PARAM_SSO_TK, token);
        params.addParameter(MovieTicketConstant.MOVIE_PARAM_ORDER_NO, mOrderNum);
        BaseResponse<MovieOrder> response = null;
        try {
            TypeToken typeToken = new TypeToken<BaseResponse<MovieOrder>>() {};
            response = xmain.http().getSync(params, typeToken.getType());
        } catch (Throwable throwable) {
            mErrorCode = MovieCommonCallback.ERROR_NETWORK;
            response = null;
        }
        return response;
    }
}
