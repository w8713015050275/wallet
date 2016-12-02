package com.letv.walletbiz.movie.utils;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;

import com.letv.wallet.common.http.beans.BaseResponse;
import com.letv.wallet.common.util.NetworkHelper;
import com.letv.walletbiz.movie.MovieTicketConstant;
import com.letv.walletbiz.movie.beans.Movie;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

/**
 * Created by liuliang on 16-1-14.
 */
public class MovieListLoadTask implements Runnable{

    private Context mContext;
    private MovieCommonCallback mCallback;

    private int mErrorCode = -1;
    private int cityId = -1;
    private int isWill = 0;  // 0:正在上映, 1:即将上映
    private boolean queryFromNet = false;
    private String reqPath = MovieTicketConstant.MOVIE_PATH_MOVIE_LIST;

    private static final int MSG_LOAD_FINISHED = 1;
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

    public MovieListLoadTask(Context context, MovieCommonCallback<List<Movie>> callback) {
        mContext = context;
        mCallback = callback;
    }

    public void setParams(int cityId, boolean isWill,  boolean queryFromNet) {
        this.cityId = cityId;
        this.queryFromNet = queryFromNet;
        if (isWill) {
            this.isWill = 1;
            this.reqPath = MovieTicketConstant.MOVIE_PATH_MOVIE_WILL_LIST;
        } else {
            this.isWill = 0;
            this.reqPath = MovieTicketConstant.MOVIE_PATH_MOVIE_LIST;
        }
    }

    @Override
    public void run() {
        List<Movie> movieList  = null;
        if (queryFromNet) {
            movieList = getMovieListFromNetwork(cityId, reqPath);
        } else {
            if (!MovieListHelper.isCacheExpire(mContext, cityId, isWill)) {
                movieList = getMovieListFromDb(mContext, cityId, isWill);
            }
            if (movieList == null) { //cache is null
                movieList = getMovieListFromNetwork(cityId, reqPath);
            }
        }
        Message msg = mHandler.obtainMessage(MSG_LOAD_FINISHED);
        msg.obj = movieList;
        mHandler.sendMessage(msg);
    }

    private List<Movie> getMovieListFromNetwork(int cityId, String reqPath) {
        if (!NetworkHelper.isNetworkAvailable()) {
            mErrorCode = MovieCommonCallback.ERROR_NO_NETWORK;
            return null;
        }
        String responseJSON = MovieListHelper.getMovieListOnlineSync(mContext, cityId, reqPath);
        if (TextUtils.isEmpty(responseJSON)) {
            mErrorCode = MovieCommonCallback.ERROR_NETWORK;
        }
        BaseResponse<List<Movie>> response = MovieListHelper.parseJSONFromResponse(responseJSON);
        List<Movie> result = null;
        if (response == null || response.errno != 10000) {
            mErrorCode = MovieCommonCallback.ERROR_NETWORK;
        } else {
            result = response.data;
            MovieListHelper.saveDataToDb(mContext, cityId, isWill,  getResponseData(responseJSON));
        }
        return result;
    }

    private List<Movie> getMovieListFromDb(Context context, int cityId, int isWill) {
        return MovieListHelper.getMovieListFromDbSync(context, cityId, isWill);
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
