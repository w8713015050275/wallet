package com.letv.walletbiz.movie.utils;

import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.RemoteException;
import android.text.TextUtils;

import com.google.gson.reflect.TypeToken;
import com.letv.wallet.common.http.beans.BaseResponse;
import com.letv.wallet.common.util.IOUtils;
import com.letv.wallet.common.util.LogHelper;
import com.letv.wallet.common.util.SharedPreferencesHelper;
import com.letv.walletbiz.WalletApplication;
import com.letv.walletbiz.base.http.client.BaseRequestParams;
import com.letv.walletbiz.movie.MovieTicketConstant;
import com.letv.walletbiz.movie.beans.CityList;
import com.letv.walletbiz.movie.provider.MovieTicketContract;
import com.letv.walletbiz.movie.provider.MovieTicketContract.CityListTable;
import com.letv.walletbiz.movie.provider.MovieTicketContract.HotCityListTable;

import org.xutils.xmain;

import java.util.ArrayList;

/**
 * Created by liuliang on 16-1-4.
 */
public class CityListHelper {

    public static final long DAY = 1000 * 60 * 60 * 24;

    public static boolean isCacheExpire() {
        long time = SharedPreferencesHelper.getLong(MovieTicketConstant.PREFERENCES_LAST_CHECK_CITYLIST, -1);
        if ((System.currentTimeMillis() - time) > DAY) {
            return true;
        }
        return false;
    }

    public static boolean syncCityListToDB(Context context, CityList cityList) {
        if (cityList == null) {
            return false;
        }
        int version = SharedPreferencesHelper.getInt(MovieTicketConstant.PREFERENCES_CITY_LIST_VERSION, -1);
        if (version != -1 && version == cityList.version) {
            return true;
        }
        CityList.City[] list = cityList.list;
        if (list == null || list.length <= 0) {
            return false;
        }

        ArrayList<ContentProviderOperation> operationList = new ArrayList<ContentProviderOperation>();

        operationList.add(ContentProviderOperation.newDelete(CityListTable.CONTENT_URI).build());
        operationList.add(ContentProviderOperation.newDelete(HotCityListTable.CONTENT_URI).build());

        for (CityList.City city : list) {
            ContentValues values = new ContentValues();
            values.put(CityListTable._ID, city.id);
            values.put(CityListTable.NAME, city.name);
            values.put(CityListTable.PINYIN, city.pinyin);
            operationList.add(ContentProviderOperation
                    .newInsert(CityListTable.CONTENT_URI)
                    .withValues(values)
                    .build());
        }

        list = cityList.hot;
        if (list != null && list.length > 0) {
            for (CityList.City city : list) {
                ContentValues values = new ContentValues();
                values.put(HotCityListTable.CITY_ID, city.id);
                values.put(HotCityListTable.NAME, city.name);
                values.put(HotCityListTable.PINYIN, city.pinyin);

                operationList.add(ContentProviderOperation
                        .newInsert(HotCityListTable.CONTENT_URI)
                        .withValues(values)
                        .build());
            }
        }

        ContentResolver resolver = context.getContentResolver();
        try {
            resolver.applyBatch(MovieTicketContract.AUTHORITY, operationList);
            SharedPreferencesHelper.putInt(MovieTicketConstant.PREFERENCES_CITY_LIST_VERSION, cityList.version);
            SharedPreferencesHelper.putLong(MovieTicketConstant.PREFERENCES_LAST_CHECK_CITYLIST, System.currentTimeMillis());
            return true;
        } catch (RemoteException e) {
            LogHelper.e(e);
        } catch (OperationApplicationException e) {
            LogHelper.e(e);
        }
        SharedPreferencesHelper.putLong(MovieTicketConstant.PREFERENCES_LAST_CHECK_CITYLIST, -1);
        SharedPreferencesHelper.putInt(MovieTicketConstant.PREFERENCES_CITY_LIST_VERSION, -1);
        return false;
    }

    public static void saveCityListToDBAsync(CityList cityList) {
        AsyncTask<CityList, Void, Boolean> task = new AsyncTask<CityList, Void, Boolean>() {

            @Override
            protected Boolean doInBackground(CityList[] params) {
                CityList list = params[0];
                return syncCityListToDB(WalletApplication.getApplication(), list);
            }
        };
        task.execute(cityList);
    }

    public static BaseResponse<CityList> getCityListOnLineSync(Context context, String cityName, int version) {
        BaseRequestParams params = new BaseRequestParams(MovieTicketConstant.MOVIE_PATH_CITY_LIST);
        if (!TextUtils.isEmpty(cityName)) {
            params.addParameter(MovieTicketConstant.MOVIE_PARAM_CITY_NAME, cityName);
        }
        if (version > 0) {
            params.addParameter(MovieTicketConstant.MOVIE_PARAM_CITY_VERSION, version);
        }

        BaseResponse<CityList> response = null;
        try {
            TypeToken typeToken = new TypeToken<BaseResponse<CityList>>() {};
            response = xmain.http().getSync(params, typeToken.getType());
        } catch (Throwable throwable) {
            response = null;
        }
        return response;
    }

    public static CityList getCityListFromLocal(Context context) {
        if (context == null) {
            return null;
        }
        CityList cityList = new CityList();
        String[] projection = new String[] {
                CityListTable._ID,
                CityListTable.NAME,
                CityListTable.PINYIN,
                CityListTable.LAST_SELECTED
        };
        Cursor cursor = context.getContentResolver().query(CityListTable.CONTENT_URI, projection, null, null, CityListTable.PINYIN);
        if (cursor == null || cursor.getCount() <= 0) {
            return null;
        }
        cursor.moveToFirst();
        CityList.City[] cities = new CityList.City[cursor.getCount()];
        int i = 0;
        do {
            CityList.City city = new CityList.City();
            city.id = cursor.getInt(0);
            city.name = cursor.getString(1);
            city.pinyin = cursor.getString(2);
            city.last_selected = cursor.getLong(3);
            cities[i++] = city;
        } while (cursor.moveToNext());

        cityList.list = cities;
        IOUtils.closeQuietly(cursor);

        projection = new String[] {
            HotCityListTable.CITY_ID,
            HotCityListTable.NAME,
            HotCityListTable.PINYIN
        };
        cursor = context.getContentResolver().query(HotCityListTable.CONTENT_URI, projection, null, null, HotCityListTable._ID);
        if (cursor != null && cursor.moveToFirst()) {
            cities = new CityList.City[cursor.getCount()];
            i = 0;
            do {
                CityList.City city = new CityList.City();
                city.id = cursor.getInt(0);
                city.name = cursor.getString(1);
                city.pinyin = cursor.getString(2);
                cities[i++] = city;
            } while (cursor.moveToNext());
            IOUtils.closeQuietly(cursor);
            cityList.hot = cities;
        }

        int currentId = SharedPreferencesHelper.getInt(MovieTicketConstant.PREFERENCES_CURRENT_CITY_ID, -1);
        if (currentId != -1) {
            cityList.curr_city_id = currentId;
        }

        cityList.version = SharedPreferencesHelper.getInt(MovieTicketConstant.PREFERENCES_CITY_LIST_VERSION, -1);

        return cityList;
    }

    public static CityList.City getLocationCityIdByCityName(Context context, String cityName) {
        if (context == null || TextUtils.isEmpty(cityName)) {
            return null;
        }
        CityList.City city = getLocationCityIdByCityNameFromDb(context, cityName);
        if (city != null) {
            return city;
        }
        int version = SharedPreferencesHelper.getInt(MovieTicketConstant.PREFERENCES_CITY_LIST_VERSION, -1);
        BaseResponse<CityList> response = getCityListOnLineSync(context, cityName, version);
        if (response != null && response.data != null) {
            CityList cityList = response.data;
            if (cityList.curr_city_id >= 0) {
                saveLocationCityIdToDb(context, cityList.curr_city_id, cityName);
                city = getLocationCityIdByCityNameFromDb(context, cityName);
                return city;
            }
        }
        return null;
    }

    private static CityList.City getLocationCityIdByCityNameFromDb(Context context, String cityName) {
        if (context == null || TextUtils.isEmpty(cityName)) {
            return null;
        }
        String selection = CityListTable.LOCATION_NAME + "=? OR " + CityListTable.NAME + "=?";
        Cursor cursor = context.getContentResolver().query(CityListTable.CONTENT_URI,
                new String[]{CityListTable._ID, CityListTable.NAME}, selection, new String[]{cityName, cityName}, null);
        CityList.City city = new CityList.City();
        try {
            if (cursor != null && cursor.moveToFirst()) {
                city.id = cursor.getInt(0);
                city.name = cursor.getString(1);
                return city;
            }
        } finally {
            IOUtils.closeQuietly(cursor);
        }
        return null;
    }

    private static void saveLocationCityIdToDb(Context context, int id, String locationName) {
        if (context == null || id < 0 || TextUtils.isEmpty(locationName)) {
            return;
        }
        ContentValues values = new ContentValues();
        values.put(CityListTable.LOCATION_NAME, locationName);
        String where = CityListTable._ID + "=" + id;
        context.getContentResolver().update(CityListTable.CONTENT_URI, values, where, null);
    }
}
