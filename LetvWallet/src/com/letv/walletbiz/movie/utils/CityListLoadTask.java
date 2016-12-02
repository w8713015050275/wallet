package com.letv.walletbiz.movie.utils;

import android.content.Context;
import android.os.AsyncTask;

import com.letv.wallet.common.http.beans.BaseResponse;
import com.letv.wallet.common.util.NetworkHelper;
import com.letv.wallet.common.util.SharedPreferencesHelper;
import com.letv.walletbiz.movie.MovieTicketConstant;
import com.letv.walletbiz.movie.beans.CityList;

/**
 * Created by liuliang on 16-1-15.
 */
public class CityListLoadTask extends AsyncTask<Object, Void, CityList> {

    private Context mContext;
    private MovieCommonCallback<CityList> mCallback;

    private int mErrorCode = MovieCommonCallback.NO_ERROR;

    public CityListLoadTask(Context context, MovieCommonCallback<CityList> callback) {
        mContext = context;
        mCallback = callback;
    }

    @Override
    protected CityList doInBackground(Object... params) {
        if (params == null || params.length != 1) {
            mErrorCode = MovieCommonCallback.ERROR_PARAM;
            return null;
        }
        boolean queryFromNet = (boolean) params[0];
        CityList cityList = null;
        if (queryFromNet) {
            cityList = getCityListFromNetwork();
            return cityList;
        }
        if (CityListHelper.isCacheExpire()) {
            cityList = getCityListFromNetwork();
        }

        if (cityList == null) {
            cityList = getCityListFromDb();
        }
        return cityList;
    }

    @Override
    protected void onPostExecute(CityList cityList) {
        if (mCallback != null) {
            mCallback.onLoadFinished(cityList, mErrorCode);
        }
    }

    private CityList getCityListFromNetwork() {
        if (!NetworkHelper.isNetworkAvailable()) {
            mErrorCode = MovieCommonCallback.ERROR_NO_NETWORK;
            return null;
        }
        String cityName = SharedPreferencesHelper.getString(MovieTicketConstant.PREFERENCES_CURRENT_CITY, null);
        int version = SharedPreferencesHelper.getInt(MovieTicketConstant.PREFERENCES_CITY_LIST_VERSION, -1);

        BaseResponse<CityList> response = CityListHelper.getCityListOnLineSync(mContext, cityName, version);
        if (response == null || response.errno != 10000 || response.data == null) {
            mErrorCode = MovieCommonCallback.ERROR_NETWORK;
            return null;
        }

        CityList cityList = response.data;
        if (cityList.version != version) {
            if (CityListHelper.syncCityListToDB(mContext, cityList)) {
                cityList = getCityListFromDb();
            }
        } else {
            cityList = null;
            SharedPreferencesHelper.putLong(MovieTicketConstant.PREFERENCES_LAST_CHECK_CITYLIST, System.currentTimeMillis());
        }
        return cityList;
    }

    private CityList getCityListFromDb() {
        CityList cityList = CityListHelper.getCityListFromLocal(mContext);
        if (cityList != null) {
            mErrorCode = MovieCommonCallback.NO_ERROR;
        }
        return cityList;
    }
}
