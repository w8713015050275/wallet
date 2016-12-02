package com.letv.walletbiz.movie.utils;

import android.content.Context;

import com.google.gson.reflect.TypeToken;
import com.letv.wallet.common.http.beans.BaseResponse;
import com.letv.walletbiz.base.http.client.BaseRequestParams;
import com.letv.walletbiz.movie.MovieTicketConstant;
import com.letv.walletbiz.movie.beans.MovieDetail;

import org.xutils.xmain;

/**
 * Created by liuliang on 16-3-17.
 */
public class MovieDetailTask implements Runnable {

    private Context mContext;
    private long mMovieId;
    private int mCityId;
    private MovieCommonCallback<MovieDetail> mCallback;

    private int mErrorCode = MovieCommonCallback.NO_ERROR;

    public MovieDetailTask(Context context, long movieId, int cityId, MovieCommonCallback<MovieDetail> callback) {
        mContext = context;
        mMovieId = movieId;
        mCityId = cityId;
        mCallback = callback;
    }

    @Override
    public void run() {
        BaseResponse<MovieDetail> response = getMovieDetail(mCityId);
        MovieDetail result;
        if (response == null) {
            mErrorCode = MovieCommonCallback.ERROR_NETWORK;
            result = null;
        } else {
            if (response.errno == 10000) {
                mErrorCode = MovieCommonCallback.NO_ERROR;
                result = response.data;
            } else {
                mErrorCode = MovieCommonCallback.ERROR_NETWORK;
                result = null;
            }
        }
        if (mCallback != null) {
            mCallback.onLoadFinished(result, mErrorCode);
        }
    }

    private BaseResponse<MovieDetail> getMovieDetail(int cityId) {
        if (mContext == null || mMovieId < 0) {
            mErrorCode = MovieCommonCallback.ERROR_PARAM;
            return null;
        }
        if (cityId == -1) {
            mErrorCode = MovieCommonCallback.ERROR_PARAM;
            return null;
        }
        BaseRequestParams params = new BaseRequestParams(MovieTicketConstant.MOVIE_PATH_DETAIL);
        params.addParameter(MovieTicketConstant.MOVIE_PARAM_MOVIE_ID, mMovieId);
        params.addParameter(MovieTicketConstant.MOVIE_PARAM_CITY_ID, cityId);
        BaseResponse<MovieDetail> response;
        try {
            TypeToken typeToken = new TypeToken<BaseResponse<MovieDetail>>() {};
            response = xmain.http().getSync(params, typeToken.getType());
        } catch (Throwable throwable) {
            mErrorCode = MovieCommonCallback.ERROR_NETWORK;
            response = null;
        }
        return response;
    }
}
