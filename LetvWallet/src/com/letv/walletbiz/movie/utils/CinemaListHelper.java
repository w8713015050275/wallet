package com.letv.walletbiz.movie.utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.location.Address;
import android.text.TextUtils;

import com.google.gson.reflect.TypeToken;
import com.letv.wallet.common.http.beans.BaseResponse;
import com.letv.wallet.common.util.AccountHelper;
import com.letv.wallet.common.util.IOUtils;
import com.letv.wallet.common.util.LogHelper;
import com.letv.wallet.common.util.ParseHelper;
import com.letv.walletbiz.R;
import com.letv.walletbiz.base.http.client.BaseRequestParams;
import com.letv.walletbiz.movie.MovieTicketConstant;
import com.letv.walletbiz.movie.beans.CinemaFilterBean;
import com.letv.walletbiz.movie.beans.CinemaList;
import com.letv.walletbiz.movie.provider.MovieTicketContract.CinemaTable;

import org.xutils.xmain;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * Created by liuliang on 16-1-19.
 */
public class CinemaListHelper {

    public static final int TYPE_CINEMA_BY_CITY = 1;
    public static final int TYPE_CINEMA_BY_MOVIE = 2;

    public static final long CINEMA_EXPIRE_TIME = 1000 * 60 * 5;

    public static boolean isCacheExpire(Context context, int cityId, long movieId, String date) {
        if (context == null || cityId < 0) {
            return true;
        }
        String selection = CinemaTable.CITY_ID + "=" + cityId + " AND " + CinemaTable.MOVIE_ID + "=" + movieId;
        if (!TextUtils.isEmpty(date)) {
            selection += " AND " + CinemaTable.SCHEDULE_DATE + "='" + date + "'";
        }
        Cursor cursor = null;
        long lastModified = -1;
        try {
            cursor = context.getContentResolver().query(CinemaTable.CONTENT_URI,
                    new String[]{CinemaTable.LAST_MODIFIED}, selection, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                lastModified = cursor.getLong(0);
            }
        } finally {
            IOUtils.closeQuietly(cursor);
        }
        if ((System.currentTimeMillis() - lastModified) > CINEMA_EXPIRE_TIME) {
            return true;
        }
        return false;
    }

    public static String getCinemaListByCityFromNetwork(Context context, int cityid, double latitude, double longitude) {
        BaseRequestParams params = new BaseRequestParams(MovieTicketConstant.MOVIE_PATH_CINEMA_LIST);
        params.addParameter(MovieTicketConstant.MOVIE_PARAM_CITY_ID, cityid);
        String token = AccountHelper.getInstance().getToken(context);
        params.addParameter(MovieTicketConstant.MOVIE_PARAM_SSO_TK, token);
        if (latitude > 0 && longitude > 0) {
            params.addParameter(MovieTicketConstant.MOVIE_PARAM_LATITUDE, latitude);
            params.addParameter(MovieTicketConstant.MOVIE_PARAM_LONGITUDE, longitude);
        }
        String response = null;
        try {
            response = xmain.http().getSync(params, String.class);
        } catch (Throwable throwable) {
            response = null;
        }
        return response;
    }

    public static String getCinemaListByMovieFromNetwork(Context context, int cityId, long movieId,
                                                         String date, double latitude, double longitude) {
        BaseRequestParams params = new BaseRequestParams(MovieTicketConstant.MOVIE_PATH_MOVIE_CINEMA_LIST);
        params.addParameter(MovieTicketConstant.MOVIE_PARAM_CITY_ID, cityId);
        params.addParameter(MovieTicketConstant.MOVIE_PARAM_MOVIE_ID, movieId);
        params.addParameter(MovieTicketConstant.MOVIE_PARAM_DATE, date);
        String token = AccountHelper.getInstance().getToken(context);
        params.addParameter(MovieTicketConstant.MOVIE_PARAM_SSO_TK, token);

        if (latitude > 0 && longitude > 0) {
            params.addParameter(MovieTicketConstant.MOVIE_PARAM_LATITUDE, latitude);
            params.addParameter(MovieTicketConstant.MOVIE_PARAM_LONGITUDE, longitude);
        }

        String response = null;
        try {
            response = xmain.http().getSync(params, String.class);
        } catch (Throwable throwable) {
            response = null;
        }
        return response;
    }

    public static CinemaList getCinemaListFromLocal(Context context, int cityId, long movieId, String date) {
        String selection = CinemaTable.CITY_ID + "=" + cityId + " AND " + CinemaTable.MOVIE_ID + "=" + movieId;
        if (!TextUtils.isEmpty(date)) {
            selection += " AND " + CinemaTable.SCHEDULE_DATE + "='" + date + "'";
        }
        Cursor cursor = context.getContentResolver().query(CinemaTable.CONTENT_URI, new String[]{CinemaTable.DATA}, selection, null, null);
        String result = null;
        try {
            if (cursor != null && cursor.moveToFirst()) {
                result = new String(cursor.getBlob(0), Charset.forName("UTF-8"));
            }
        } catch (Exception e) {
        }
        return parseJSONFromDb(result);
    }

    public static void deleteCinemaListFromLocal(Context context, int cityId, long movieId, String date) {
        String whereClause = CinemaTable.CITY_ID + "=" + cityId ;

        if(movieId > 0){
            whereClause += " AND " + CinemaTable.MOVIE_ID + "=" + movieId;
        }

        if (!TextUtils.isEmpty(date)) {
            whereClause += " AND " + CinemaTable.SCHEDULE_DATE + "='" + date + "'";
        }
        int rows = context.getContentResolver().delete(CinemaTable.CONTENT_URI, whereClause, null);
        LogHelper.d("delete rows == " + rows);
    }

    public static BaseResponse<CinemaList> parseJSONFromResponse(String responseJSON) {
        if (TextUtils.isEmpty(responseJSON)) {
            return null;
        }
        TypeToken typeToken = new TypeToken<BaseResponse<CinemaList>>() {};
        return ParseHelper.parseByGson(responseJSON, typeToken.getType());
    }

    public static CinemaList parseJSONFromDb(String data) {
        if (TextUtils.isEmpty(data)) {
            return null;
        }
        TypeToken typeToken = new TypeToken<CinemaList>() {};
        return ParseHelper.parseByGson(data, typeToken.getType());
    }

    public static void saveDataToDb(Context context, int cityId, long movieId, String date, String data) {
        if (context == null || cityId < 0 || TextUtils.isEmpty(data)) {
            return;
        }
        ContentValues values = new ContentValues();
        values.put(CinemaTable.CITY_ID, cityId);
        values.put(CinemaTable.MOVIE_ID, movieId);
        if (!TextUtils.isEmpty(date)) {
            values.put(CinemaTable.SCHEDULE_DATE, date);
        }
        values.put(CinemaTable.DATA, data.getBytes(Charset.forName("UTF-8")));
        values.put(CinemaTable.LAST_MODIFIED, System.currentTimeMillis());
        context.getContentResolver().insert(CinemaTable.CONTENT_URI, values);
    }

    public static void updateDistanceForCinemaList(CinemaList.Cinema[] cinemaArray, Address address) {
        if (cinemaArray == null || address == null) {
            return;
        }
        if (cinemaArray == null || cinemaArray.length <= 0) {
            return;
        }
        double latitude1 = address.getLatitude();
        double longitude1 = address.getLongitude();
        for (CinemaList.Cinema cinema : cinemaArray) {
            cinema.updateDistance(latitude1, longitude1);
        }
        Arrays.sort(cinemaArray);
    }

    public static CinemaFilterBean getCinemaFilterData(Context context, CinemaList cinemaList) {
        if (context == null || cinemaList == null) {
            return null;
        }
        if (cinemaList.cinema_list == null || cinemaList.cinema_list.length <= 0) {
            return null;
        }
        String categoryAll = context.getString(R.string.movie_cinema_filter_category_all);
        List<CinemaList.Cinema> allList = Arrays.asList(cinemaList.cinema_list);

        CinemaFilterBean filterBean = new CinemaFilterBean();

        CinemaList.Area[] areaArray = cinemaList.area_list;
        HashMap<String, List<CinemaList.Cinema>> areaCinemaMap = new HashMap<>();
        if (areaArray != null && areaArray.length > 0) {
            areaCinemaMap.put(categoryAll, allList);
            ArrayList<String> areaNameArray = new ArrayList<>();
            areaNameArray.add(categoryAll);
            for (CinemaList.Area area : areaArray) {
                if (area != null && !TextUtils.isEmpty(area.name)) {
                    if (!areaNameArray.contains(area.name)) {
                        areaNameArray.add(area.name);
                    }
                    if (!areaCinemaMap.containsKey(area.name)) {
                        areaCinemaMap.put(area.name, new ArrayList<CinemaList.Cinema>());
                    }
                }
            }
            filterBean.addName(CinemaFilterBean.CINEMA_CATEGORY_AREA, areaNameArray);
            filterBean.addCinemaList(CinemaFilterBean.CINEMA_CATEGORY_AREA, areaCinemaMap);
        }

        String[] brandArray = cinemaList.brand_list;
        HashMap<String, List<CinemaList.Cinema>> brandCinemaMap = new HashMap<>();
        if (brandArray != null && brandArray.length > 0) {
            brandCinemaMap.put(categoryAll, allList);
            ArrayList<String> brandNameArray = new ArrayList<String>();
            brandNameArray.add(categoryAll);
            for (String brand : brandArray) {
                if (!TextUtils.isEmpty(brand)) {
                    if (!brandNameArray.contains(brand)) {
                        brandNameArray.add(brand);
                    }
                    if (!brandCinemaMap.containsKey(brand)) {
                        brandCinemaMap.put(brand, new ArrayList<CinemaList.Cinema>());
                    }
                }
            }
            filterBean.addName(CinemaFilterBean.CINEMA_CATEGORY_BRAND, brandNameArray);
            filterBean.addCinemaList(CinemaFilterBean.CINEMA_CATEGORY_BRAND, brandCinemaMap);
        }

        String[] specialArray = cinemaList.special_list;
        HashMap<String, List<CinemaList.Cinema>> specialCinemaMap = new HashMap<>();
        if (specialArray != null && specialArray.length > 0) {
            specialCinemaMap.put(categoryAll, allList);
            ArrayList<String> specialNameArray = new ArrayList<String>();
            specialNameArray.add(categoryAll);
            for (String special : specialArray) {
                if (!TextUtils.isEmpty(special)) {
                    if (!specialNameArray.contains(special)) {
                        specialNameArray.add(special);
                    }
                    if (!specialCinemaMap.containsKey(special)) {
                        specialCinemaMap.put(special, new ArrayList<CinemaList.Cinema>());
                    }
                }
            }
            filterBean.addName(CinemaFilterBean.CINEMA_CATEGORY_SPECIAL, specialNameArray);
            filterBean.addCinemaList(CinemaFilterBean.CINEMA_CATEGORY_SPECIAL, specialCinemaMap);
        }

        List<CinemaList.Cinema> temp;
        for (CinemaList.Cinema cinema : cinemaList.cinema_list) {
            if (cinema == null) {
                continue;
            }
            temp = areaCinemaMap.get(cinema.area_name);
            if (temp != null) {
                temp.add(cinema);
            }
            temp = brandCinemaMap.get(cinema.cinema_brand);
            if (temp != null) {
                temp.add(cinema);
            }
            for (String spec : cinema.special) {
                temp = specialCinemaMap.get(spec);
                if (temp != null) {
                    temp.add(cinema);
                }
            }
        }
        return filterBean;
    }

    public static CinemaList.Cinema[] getCinemaArray(Context context, CinemaList cinemaList, int category, String secCategoryName) {
        CinemaFilterBean filterBean = getCinemaFilterData(context, cinemaList);
        List<CinemaList.Cinema> list = filterBean.getCinemaList(category, secCategoryName);
        return list.toArray(new CinemaList.Cinema[0]);
    }
}
