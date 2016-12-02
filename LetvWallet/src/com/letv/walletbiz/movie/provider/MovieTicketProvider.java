package com.letv.walletbiz.movie.provider;

import android.content.ContentProvider;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.OperationApplicationException;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import java.util.ArrayList;

import static com.letv.walletbiz.movie.provider.MovieTicketContract.AUTHORITY;
import static com.letv.walletbiz.movie.provider.MovieTicketContract.CinemaTable;
import static com.letv.walletbiz.movie.provider.MovieTicketContract.CityListTable;
import static com.letv.walletbiz.movie.provider.MovieTicketContract.CityMovieTable;
import static com.letv.walletbiz.movie.provider.MovieTicketContract.HotCityListTable;

/**
 * Created by liuliang on 15-12-29.
 */
public class MovieTicketProvider extends ContentProvider {

    private static final String TAG = "MOVIE_TICKET";

    private static final int CITIES = 0;
    private static final int CITY_WITH_ID = 1;

    private static final int HOT_CITIES = 10;
    private static final int HOT_CITY_WITH_ID = 11;

    private static final int CITY_MOVIE_LIST = 20;
    private static final int CITY_MOVIE_LIST_WITH_CITYID = 21;

    private static final int CINEMA_LIST = 30;
    private static final int CINEMA_LIST_BY_CITY = 31;
    private static final int CINEMA_LIST_BY_CITY_MOVIE = 32;


    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        sUriMatcher.addURI(AUTHORITY, "city_list", CITIES);
        sUriMatcher.addURI(AUTHORITY, "city_list/#", CITY_WITH_ID);
        sUriMatcher.addURI(AUTHORITY, "hot_city_list", HOT_CITIES);
        sUriMatcher.addURI(AUTHORITY, "hot_city_list/#", HOT_CITY_WITH_ID);
        sUriMatcher.addURI(AUTHORITY, "city_movie", CITY_MOVIE_LIST);
        sUriMatcher.addURI(AUTHORITY, "city_movie/#", CITY_MOVIE_LIST_WITH_CITYID);
        sUriMatcher.addURI(AUTHORITY, "cinema", CINEMA_LIST);
        sUriMatcher.addURI(AUTHORITY, "cinema/#", CINEMA_LIST_BY_CITY);
        sUriMatcher.addURI(AUTHORITY, "cinema/#/#", CINEMA_LIST_BY_CITY_MOVIE);
    }

    private MovieTicketDatabaseHelper mOpenHelper;

    @Override
    public boolean onCreate() {
        mOpenHelper = MovieTicketDatabaseHelper.getInstance(getContext());
        return true;
    }

    @Override
    public ContentProviderResult[] applyBatch(@NonNull ArrayList<ContentProviderOperation> operations) throws OperationApplicationException {
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            ContentProviderResult[] results = super.applyBatch(operations);
            db.setTransactionSuccessful();
            return results;
        } finally {
            db.endTransaction();
        }
    }

    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        SQLiteDatabase db = mOpenHelper.getReadableDatabase();

        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();

        int match = sUriMatcher.match(uri);
        switch (match) {
            case CITY_WITH_ID :
                qb.appendWhere(CityListTable._ID + "=" + uri.getPathSegments().get(1));
            case CITIES :
                qb.setTables(CityListTable.TABLE_NAME);
                break;
            case HOT_CITIES :
                qb.setTables(HotCityListTable.TABLE_NAME);
                break;
            case CITY_MOVIE_LIST_WITH_CITYID :
                qb.appendWhere(CityMovieTable.CITY_ID + "=" + uri.getPathSegments().get(1));
            case CITY_MOVIE_LIST :
                qb.setTables(CityMovieTable.TABLE_NAME);
                break;
            case CINEMA_LIST_BY_CITY_MOVIE:
                qb.appendWhere(CinemaTable.MOVIE_ID + "=" + uri.getPathSegments().get(2));
            case CINEMA_LIST_BY_CITY:
                qb.appendWhere(CinemaTable.CITY_ID + "=" + uri.getPathSegments().get(1));
            case CINEMA_LIST:
                qb.setTables(CinemaTable.TABLE_NAME);
                break;

            default: {
                return null;
            }
        }
        Cursor ret = null;
        try {
            ret = qb.query(db, projection, selection, selectionArgs, null, null, sortOrder);
        } catch (SQLException e) {
            Log.e(TAG, "got exception when querying: " + e);
        }

        return ret;
    }

    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        Uri result = null;

        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int match = sUriMatcher.match(uri);
        long rowID = 0;
        switch (match) {
            case CITIES:
                rowID = insertOrUpdateCities(db, values);
                if (rowID >= 0) {
                    result = ContentUris.withAppendedId(uri, rowID);
                }
                break;
            case HOT_CITIES :
                rowID = insertOrUpdateHotCities(db, values);
                if (rowID >= 0) {
                    result = ContentUris.withAppendedId(uri, rowID);
                }
                break;
            case CITY_MOVIE_LIST :
                rowID = insertOrUpdateCityMovieList(db, values);
                if (rowID >= 0) {
                    result = ContentUris.withAppendedId(uri, rowID);
                }
                break;
            case CINEMA_LIST:
                rowID = insertOrUpdateCinemaList(db, values);
                if (rowID > 0) {
                    result = ContentUris.withAppendedId(uri, rowID);
                }
                break;
        }
        return result;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        int count = 0;
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int match = sUriMatcher.match(uri);

        long id;
        switch (match) {
            case CITY_WITH_ID:
                id = ContentUris.parseId(uri);
                count = db.delete(CityListTable.TABLE_NAME, appendSelection(selection, CityListTable._ID + "=" + id), selectionArgs);
                break;
            case CITIES:
                count = db.delete(CityListTable.TABLE_NAME, selection, selectionArgs);
                break;
            case HOT_CITIES :
                count = db.delete(HotCityListTable.TABLE_NAME, selection, selectionArgs);
                break;
            case CITY_MOVIE_LIST_WITH_CITYID :
                id = ContentUris.parseId(uri);
                count = db.delete(CityMovieTable.TABLE_NAME, appendSelection(selection, CityMovieTable.CITY_ID + "=" + id), selectionArgs);
                break;
            case CITY_MOVIE_LIST :
                count = db.delete(CityListTable.TABLE_NAME, selection, selectionArgs);
                break;
            case CINEMA_LIST:
                count = db.delete(CinemaTable.TABLE_NAME, selection, selectionArgs);
                break;
            case CINEMA_LIST_BY_CITY:
                count = db.delete(CinemaTable.TABLE_NAME, appendSelection(selection, CinemaTable.CITY_ID + "=" + uri.getPathSegments().get(1)), selectionArgs);
                break;
            case CINEMA_LIST_BY_CITY_MOVIE:
                String append = CinemaTable.CITY_ID + "=" + uri.getPathSegments().get(1) +
                        " AND " + CinemaTable.MOVIE_ID + "=" + uri.getPathSegments().get(2);
                count = db.delete(CinemaTable.TABLE_NAME, appendSelection(selection, append), selectionArgs);
                break;

            default: {
                throw new UnsupportedOperationException("Cannot delete that Uri: " + uri);
            }
        }
        return count;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        int count = 0;
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int match = sUriMatcher.match(uri);
        long id;
        switch (match) {
            case CITY_WITH_ID:
                id = ContentUris.parseId(uri);
                count = db.update(CityListTable.TABLE_NAME, values, appendSelection(selection, CityListTable._ID + "=" + id), selectionArgs);
                break;
            case CITIES:
                count = db.update(CityListTable.TABLE_NAME, values, selection, selectionArgs);
                break;
            case CITY_MOVIE_LIST_WITH_CITYID :
                id = ContentUris.parseId(uri);
                count = db.update(CityMovieTable.TABLE_NAME, values, appendSelection(selection, CityMovieTable.CITY_ID + "=" + id), selectionArgs);
                break;
            case CITY_MOVIE_LIST:
                count = db.update(CityMovieTable.TABLE_NAME, values, selection, selectionArgs);
                break;
            case CINEMA_LIST:
                count = db.update(CinemaTable.TABLE_NAME, values, selection, selectionArgs);
                break;
            case CINEMA_LIST_BY_CITY:
                id = ContentUris.parseId(uri);
                count = db.update(CinemaTable.TABLE_NAME, values, appendSelection(selection, CinemaTable.CITY_ID + "=" + id), selectionArgs);
                break;
            case CINEMA_LIST_BY_CITY_MOVIE:
                String append = CinemaTable.CITY_ID + "=" + uri.getPathSegments().get(1) + " AND " + CinemaTable.MOVIE_ID + "=" + uri.getPathSegments().get(2);
                count = db.update(CinemaTable.TABLE_NAME, values, appendSelection(selection, append), selectionArgs);
                break;

            default: {
                throw new UnsupportedOperationException("Cannot update that URL: " + uri);
            }
        }
        return count;
    }

    private String appendSelection(String selection, String append) {
        if (TextUtils.isEmpty(append)) {
            return selection;
        }
        if (selection == null) {
            selection = "";
        } else {
            selection += " AND ";
        }
        selection += append;
        return selection;
    }

    private long insertOrUpdateCities(SQLiteDatabase db, ContentValues values) {
        return db.replace(CityListTable.TABLE_NAME, null, values);
    }

    private long insertOrUpdateHotCities(SQLiteDatabase db, ContentValues values) {
        return db.replace(HotCityListTable.TABLE_NAME, null, values);
    }

    private long insertOrUpdateCityMovieList(SQLiteDatabase db, ContentValues values) {
        return db.replace(CityMovieTable.TABLE_NAME, null, values);
    }

    private long insertOrUpdateCinemaList(SQLiteDatabase db, ContentValues values) {
        return db.replace(CinemaTable.TABLE_NAME, null, values);
    }
}
