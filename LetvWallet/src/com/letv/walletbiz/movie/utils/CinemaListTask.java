package com.letv.walletbiz.movie.utils;

import android.content.Context;
import android.text.TextUtils;

import com.letv.wallet.common.http.beans.BaseResponse;
import com.letv.wallet.common.util.NetworkHelper;
import com.letv.walletbiz.movie.beans.CinemaList;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by liuliang on 16-1-20.
 */
public class CinemaListTask implements Runnable {

    private Context mContext;
    private MovieCommonCallback mCallback;
    private boolean isQueryFromNet = false;
    private int mQueryType = -1;
    private int mErrorCode = MovieCommonCallback.NO_ERROR;

    private Object[] mParams;

    public CinemaListTask(Context context, MovieCommonCallback callback) {
        mContext = context;
        mCallback = callback;
    }

    public void setParam(int queryType, boolean queryFromNet, Object... params) {
        mQueryType = queryType;
        isQueryFromNet = queryFromNet;
        mParams = params;
        mErrorCode = MovieCommonCallback.NO_ERROR;
    }

    @Override
    public void run() {
        CinemaList cinemaList = doInBackground(mParams);
        if (mCallback != null) {
            mCallback.onLoadFinished(cinemaList, mErrorCode);
        }
    }

    /**
     *
     * @param params params index:
     *               TYPE_CINEMA_BY_CITY:
     *               0:cityid
     *               1:latitude
     *               2:longitude
     *               TYPE_CINEMA_BY_CITY_MOVIE:
     *               0:cityid
     *               1:movieid
     *               2:date "YYYYMMDD"
     *               3:latitude
     *               4:longitude
     * @return
     */
    protected CinemaList doInBackground(Object... params) {
        if (mQueryType <= 0 || params == null || params.length <= 0) {
            mErrorCode = MovieCommonCallback.ERROR_PARAM;
            return null;
        }

        int cityId = (int) params[0];
        long movieId = -1;
        CinemaList cinemaList = null;
        String date = null;
        if (mQueryType == CinemaListHelper.TYPE_CINEMA_BY_CITY) {
            double latitude = -1;
            double longitude = -1;
            if (params.length == 3) {
                latitude = (double) params[1];
                latitude = (double) params[2];
            }
            if (isQueryFromNet || CinemaListHelper.isCacheExpire(mContext, cityId, movieId, date)) {
                cinemaList = getCinemaListByCityFromNetwork(cityId, latitude, longitude);
            }
        } else if (mQueryType == CinemaListHelper.TYPE_CINEMA_BY_MOVIE) {
            if (params.length < 3) {
                mErrorCode = MovieCommonCallback.ERROR_PARAM;
                return null;
            }
            movieId = (long) params[1];
            date = (String) params[2];
            double latitude = -1;
            double longitude = -1;
            if (params.length == 5) {
                latitude = (double) params[3];
                latitude = (double) params[4];
            }
            if (isQueryFromNet || CinemaListHelper.isCacheExpire(mContext, cityId, movieId, date)) {
                cinemaList = getCinemaListByMovieFromNetwork(cityId, movieId, date, latitude, longitude);
            }
        }
        if (cinemaList == null) {
            cinemaList = getCinemaListFromDb(cityId, movieId, date);
        }
        if (cinemaList != null) {
            mErrorCode = MovieCommonCallback.NO_ERROR;
        }
        return cinemaList;
    }

    private CinemaList getCinemaListByCityFromNetwork(int cityId, double latitude, double longitude) {
        if (!NetworkHelper.isNetworkAvailable()) {
            mErrorCode = MovieCommonCallback.ERROR_NO_NETWORK;
            return null;
        }
        String responseJSON = CinemaListHelper.getCinemaListByCityFromNetwork(mContext, cityId, latitude, longitude);
        if (TextUtils.isEmpty(responseJSON)) {
            mErrorCode = MovieCommonCallback.ERROR_NETWORK;
        }
        BaseResponse<CinemaList> response = CinemaListHelper.parseJSONFromResponse(responseJSON);
        CinemaList result = null;
        if (response == null || response.errno != 10000) {
            mErrorCode = MovieCommonCallback.ERROR_NETWORK;
            result = null;
        } else {
            result = response.data;
            CinemaListHelper.saveDataToDb(mContext, cityId, -1, null, getResponseData(responseJSON));
        }
        return result;
    }

    private CinemaList getCinemaListByMovieFromNetwork(int cityId, long movieId, String date, double latitude, double longitude) {
        if (!NetworkHelper.isNetworkAvailable()) {
            mErrorCode = MovieCommonCallback.ERROR_NO_NETWORK;
            return null;
        }
        String responseJSON = CinemaListHelper.getCinemaListByMovieFromNetwork(mContext, cityId, movieId, date, latitude, longitude);
        if (TextUtils.isEmpty(responseJSON)) {
            mErrorCode = MovieCommonCallback.ERROR_NETWORK;
        }
        BaseResponse<CinemaList> response = CinemaListHelper.parseJSONFromResponse(responseJSON);
        CinemaList result = null;
        if (response == null || response.errno != 10000) {
            mErrorCode = MovieCommonCallback.ERROR_NETWORK;
            result = null;
        } else {
            result = response.data;
            CinemaListHelper.saveDataToDb(mContext, cityId, movieId, date, getResponseData(responseJSON));
        }
        return result;
    }

    private CinemaList getCinemaListFromDb(int cityId, long movieId, String date) {
        CinemaList cinemaList = CinemaListHelper.getCinemaListFromLocal(mContext, cityId, movieId, date);
        return cinemaList;
    }

    private String getResponseData(String response) {
        String data = null;
        try {
            JSONObject jsonObject = new JSONObject(response);
            data = jsonObject.getString("data");
        } catch (JSONException e) {
            data = null;
        }
        return data;
    }
}
