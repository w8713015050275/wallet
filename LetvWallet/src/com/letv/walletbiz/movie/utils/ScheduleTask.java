package com.letv.walletbiz.movie.utils;

import android.os.AsyncTask;
import android.text.TextUtils;

import com.google.gson.reflect.TypeToken;
import com.letv.wallet.common.http.beans.BaseResponse;
import com.letv.wallet.common.util.AccountHelper;
import com.letv.wallet.common.util.ParseHelper;
import com.letv.walletbiz.WalletApplication;
import com.letv.walletbiz.base.http.client.BaseRequestParams;
import com.letv.walletbiz.movie.MovieTicketConstant;
import com.letv.walletbiz.movie.beans.CinemaSchedule;

import org.xutils.xmain;

/**
 * Created by liuliang on 16-1-26.
 */
public class ScheduleTask extends AsyncTask<Object, Void, CinemaSchedule> {

    private int mErrorCode = MovieCommonCallback.NO_ERROR;
    private MovieCommonCallback<CinemaSchedule> mCallback;

    public ScheduleTask(MovieCommonCallback<CinemaSchedule> callback) {
        mCallback = callback;
    }

    @Override
    protected CinemaSchedule doInBackground(Object... params) {
        if (params == null || params.length != 2) {
            mErrorCode = MovieCommonCallback.ERROR_PARAM;
            return null;
        }
        int cityId = (int) params[0];
        int cinemaId = (int) params[1];
        return getCinemaScheduleFromNet(cityId, cinemaId);
    }

    @Override
    protected void onPostExecute(CinemaSchedule cinemaSchedule) {
        super.onPostExecute(cinemaSchedule);
        if (mCallback != null) {
            mCallback.onLoadFinished(cinemaSchedule, mErrorCode);
        }

    }

    private CinemaSchedule getCinemaScheduleFromNet(int cityId, int cinemaId) {
        BaseRequestParams params = new BaseRequestParams(MovieTicketConstant.MOVIE_PATH_CINEMA_SCHEDULE);
        params.addParameter(MovieTicketConstant.MOVIE_PARAM_CITY_ID, cityId);
        params.addParameter(MovieTicketConstant.MOVIE_PARAM_CINEMA_ID, cinemaId);
        String token = AccountHelper.getInstance().getToken(WalletApplication.getApplication());
        params.addParameter(MovieTicketConstant.MOVIE_PARAM_SSO_TK, token);
        String responseJson = null;
        try {
            responseJson = xmain.http().getSync(params, String.class);
        } catch (Throwable throwable) {
            responseJson = null;
        }

        if (TextUtils.isEmpty(responseJson)) {
            mErrorCode = MovieCommonCallback.ERROR_NETWORK;
            return null;
        }

        TypeToken typeToken = new TypeToken<BaseResponse<CinemaSchedule>>() {};
        BaseResponse<CinemaSchedule> response = ParseHelper.parseByGson(responseJson, typeToken.getType());
        if (response == null) {
            mErrorCode = MovieCommonCallback.ERROR_NETWORK;
            return null;
        }
        if (response.data == null) {
            mErrorCode = MovieCommonCallback.ERROR_NETWORK;
            return null;
        }
        return response.data;
    }
}
