package com.letv.walletbiz.movie.utils;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;

import com.google.gson.reflect.TypeToken;
import com.letv.wallet.common.http.beans.BaseResponse;
import com.letv.wallet.common.util.AccountHelper;
import com.letv.wallet.common.util.ParseHelper;
import com.letv.walletbiz.base.http.client.BaseRequestParams;
import com.letv.walletbiz.movie.MovieTicketConstant;
import com.letv.walletbiz.movie.beans.CinemaSchedule;

import org.xutils.xmain;

/**
 * Created by lijujying on 16-6-22.
 */
public class MovieScheduleFavoritesTask implements Runnable {
    private Context mContext;
    private MovieCommonCallback mCallback;
    private static final int MSG_LOAD_FINISHED = 1;
    private int mErrorCode = MovieCommonCallback.NO_ERROR;
    private int cinema_id = -1;
    private int action = -1; // 1：添加收藏;  2：取消收藏

    private Handler mHandler = new Handler(Looper.getMainLooper()) {

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_LOAD_FINISHED:
                    if (mCallback != null) {
                        mCallback.onLoadFinished(msg.obj, mErrorCode);
                    }
                    break;
            }
        }
    };

    public MovieScheduleFavoritesTask(Context mContext, MovieCommonCallback mCallback) {
        this.mContext = mContext;
        this.mCallback = mCallback;
    }

    public void setParams(int cinema_id, boolean addAction) {
        this.cinema_id = cinema_id;
        this.action = addAction ? MovieTicketConstant.MOVIE_PARAM_FAVORITES_ACTION_ADD : MovieTicketConstant.MOVIE_PARAM_FAVORITES_ACTION_CANCLE;
    }

    @Override
    public void run() {
        getCinemaScheduleFavorites();
        Message msg = mHandler.obtainMessage(MSG_LOAD_FINISHED);
        mHandler.sendMessage(msg);
    }

    private void getCinemaScheduleFavorites() {
        BaseRequestParams params = new BaseRequestParams(MovieTicketConstant.MOVIE_PATH_CINEMA_SCHEDULE_FAVORITE);
        String token = AccountHelper.getInstance().getToken(mContext);
        params.addParameter(MovieTicketConstant.MOVIE_PARAM_SSO_TK, token);
        params.addParameter(MovieTicketConstant.MOVIE_PARAM_CINEMA_ID, cinema_id);
        params.addParameter(MovieTicketConstant.MOVIE_PARAM_FAVORITES_ACTION, action);
        String responseJson = null;
        try {
            responseJson = xmain.http().postSync(params, String.class);
        } catch (Throwable throwable) {
            responseJson = null;
        }
        if (TextUtils.isEmpty(responseJson)) {
            mErrorCode = MovieCommonCallback.ERROR_NETWORK;
            return;
        }

        TypeToken typeToken = new TypeToken<BaseResponse<CinemaSchedule>>() {};
        BaseResponse<CinemaSchedule> response = ParseHelper.parseByGson(responseJson, typeToken.getType());
        if (response == null || response.errno != 10000) {
            mErrorCode = MovieCommonCallback.ERROR_NETWORK;
            return ;
        }
    }
}
