package com.letv.walletbiz.movie.utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;

import com.google.gson.reflect.TypeToken;
import com.letv.wallet.common.http.beans.BaseResponse;
import com.letv.wallet.common.util.IOUtils;
import com.letv.wallet.common.util.ParseHelper;
import com.letv.walletbiz.base.http.client.BaseRequestParams;
import com.letv.walletbiz.movie.MovieTicketConstant;
import com.letv.walletbiz.movie.beans.Movie;

import org.xutils.xmain;

import java.nio.charset.Charset;
import java.util.List;

import static com.letv.walletbiz.movie.provider.MovieTicketContract.CityMovieTable;

/**
 * Created by liuliang on 16-1-13.
 */
public class MovieListHelper {


    public static final long MOVIE_LIST_CACHE_EXPIRE = 1000 * 60 * 5;

    public static String getMovieListOnlineSync(Context context, int cityId, String reqPath) {
        BaseRequestParams params = new BaseRequestParams(reqPath);
        params.addParameter(MovieTicketConstant.MOVIE_PARAM_CITY_ID, cityId);

        String response = null;
        try {
            response = xmain.http().getSync(params, String.class);
        } catch (Throwable throwable) {
            response = null;
        }
        return response;
    }

    public static List<Movie> getMovieListFromDbSync(Context context, int cityId, int isWill) {
        String[] projection = new String[] {
                CityMovieTable.CITY_ID,
                CityMovieTable.DATA,
                CityMovieTable.LAST_MODIFIED,
                CityMovieTable.IS_WILL
        };

        final String SELECTION_MOVIE_LIST_CLAUSE = CityMovieTable.CITY_ID + "=" + cityId + " AND " +
                CityMovieTable.IS_WILL + "=" + isWill;
        Cursor cursor = context.getContentResolver().query(CityMovieTable.CONTENT_URI,
                projection, SELECTION_MOVIE_LIST_CLAUSE, null, null);
        String result = null;
        try {
            if (cursor != null && cursor.moveToFirst()) {
                result = new String(cursor.getBlob(1), Charset.forName("UTF-8"));
            }
        } catch (Exception e) {
        } finally {
            IOUtils.closeQuietly(cursor);
        }

        return parseJSON(result);
    }

    public static boolean isCacheExpire(Context context, int cityId, int isWill) {
        if (context == null || cityId < 0) {
            return true;
        }
        final String SELECTION_MOVIE_LIST_CLAUSE = CityMovieTable.CITY_ID + "=" + cityId + " AND " +
                CityMovieTable.IS_WILL + "=" + isWill;
        Cursor cursor = context.getContentResolver().query(CityMovieTable.CONTENT_URI,
                new String[]{CityMovieTable.LAST_MODIFIED}, SELECTION_MOVIE_LIST_CLAUSE, null, null);
        long lastModified = -1;
        if (cursor != null && cursor.moveToFirst()) {
            lastModified = cursor.getLong(0);
        }
        IOUtils.closeQuietly(cursor);
        if ((System.currentTimeMillis() - lastModified) > MOVIE_LIST_CACHE_EXPIRE) {
            return true;
        }
        return false;
    }

    public static BaseResponse<List<Movie>> parseJSONFromResponse(String response) {
        if (response == null) {
            return null;
        }
        TypeToken typeToken = new TypeToken<BaseResponse<List<Movie>>>() {};
        return ParseHelper.parseByGson(response, typeToken.getType());
    }

    public static List<Movie> parseJSON(String json) {
        if (json == null) {
            return null;
        }
        TypeToken typeToken = new TypeToken<List<Movie>>() {};
        return ParseHelper.parseByGson(json, typeToken.getType());
    }

    public static void saveDataToDb(Context context, int cityId, int isWill, String data) {
        if (cityId < 0 || TextUtils.isEmpty(data)) {
            return;
        }
        ContentValues values = new ContentValues();
        values.put(CityMovieTable.CITY_ID, cityId);
        values.put(CityMovieTable.IS_WILL, isWill);
        values.put(CityMovieTable.DATA, data.getBytes());
        values.put(CityMovieTable.LAST_MODIFIED, System.currentTimeMillis());
        context.getContentResolver().insert(CityMovieTable.CONTENT_URI, values);
    }
}
