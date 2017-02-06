package com.letv.walletbiz.main.provider;

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

import com.letv.walletbiz.main.provider.WalletContract.BannerTable;
import com.letv.walletbiz.main.provider.WalletContract.ServiceTable;
import com.letv.walletbiz.main.provider.WalletContract.MainTopTable;

import java.util.ArrayList;

/**
 * Created by liuliang on 16-4-12.
 */
public class WalletMainProvider extends ContentProvider {

    private static final String TAG = "wallet_main";

    private static final int SERVICE_LIST = 0;
    private static final int SERVICE_ID = 1;

    private static final int BANNER_LIST = 10;
    private static final int BANNER_ID = 11;

    private static final int TOP_LIST = 20;
    private static final int TOP_ID = 21;

    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        sUriMatcher.addURI(WalletContract.AUTHORITY, "service", SERVICE_LIST);
        sUriMatcher.addURI(WalletContract.AUTHORITY, "service/#", SERVICE_ID);
        sUriMatcher.addURI(WalletContract.AUTHORITY, "banner", BANNER_LIST);
        sUriMatcher.addURI(WalletContract.AUTHORITY, "banner/#", BANNER_ID);

        sUriMatcher.addURI(WalletContract.AUTHORITY, "top", TOP_LIST);
        sUriMatcher.addURI(WalletContract.AUTHORITY, "top/#", TOP_ID);
    }

    private WalletMainDatabaseHelper mOpenHelper;

    @Override
    public boolean onCreate() {
        mOpenHelper = WalletMainDatabaseHelper.getsInstance(getContext());
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
            case SERVICE_ID:
                qb.appendWhere(ServiceTable.SERVICE_ID + "=" + uri.getPathSegments().get(1));
            case SERVICE_LIST:
                qb.setTables(ServiceTable.TABLE_NAME);
                break;
            case BANNER_ID:
                qb.appendWhere(BannerTable.BANNER_ID + "=" + uri.getPathSegments().get(1));
            case BANNER_LIST:
                qb.setTables(BannerTable.TABLE_NAME);
                break;
            case TOP_ID:
                qb.appendWhere(MainTopTable.TOP_NAME + "=" + uri.getPathSegments().get(1));
            case TOP_LIST:
                qb.setTables(MainTopTable.TABLE_NAME);
                break;
            default:{
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
            case SERVICE_LIST:
                rowID = db.insert(ServiceTable.TABLE_NAME, null, values);
                break;
            case BANNER_LIST:
                rowID = db.insert(BannerTable.TABLE_NAME, null, values);
                break;
            case TOP_LIST:
                rowID = db.insert(MainTopTable.TABLE_NAME, null, values);
                break;
        }
        if (rowID >= 0) {
            result = ContentUris.withAppendedId(uri, rowID);
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
            case SERVICE_ID:
                id = ContentUris.parseId(uri);
                count = db.delete(ServiceTable.TABLE_NAME, appendSelection(selection, ServiceTable.SERVICE_ID + "=" + id), selectionArgs);
                break;
            case SERVICE_LIST:
                count = db.delete(ServiceTable.TABLE_NAME, selection, selectionArgs);
                break;
            case BANNER_ID:
                id = ContentUris.parseId(uri);
                count = db.delete(BannerTable.TABLE_NAME, appendSelection(selection, BannerTable.BANNER_ID + "=" + id), selectionArgs);
                break;
            case BANNER_LIST:
                count = db.delete(BannerTable.TABLE_NAME, selection, selectionArgs);
                break;
            case TOP_ID:
                id = ContentUris.parseId(uri);
                count = db.delete(MainTopTable.TABLE_NAME, appendSelection(selection, MainTopTable.TOP_NAME + "=" + id), selectionArgs);
                break;
            case TOP_LIST:
                count = db.delete(MainTopTable.TABLE_NAME, selection, selectionArgs);
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
            case SERVICE_ID:
                id = ContentUris.parseId(uri);
                count = db.update(ServiceTable.TABLE_NAME, values, appendSelection(selection, ServiceTable.SERVICE_ID + "=" + id), selectionArgs);
                break;
            case SERVICE_LIST:
                count = db.update(ServiceTable.TABLE_NAME, values, selection, selectionArgs);
                break;
            case BANNER_ID:
                id = ContentUris.parseId(uri);
                count = db.update(BannerTable.TABLE_NAME, values, appendSelection(selection, BannerTable.BANNER_ID + "=" + id), selectionArgs);
                break;
            case BANNER_LIST:
                count = db.update(BannerTable.TABLE_NAME, values, selection, selectionArgs);
                break;
            case TOP_ID:
                id = ContentUris.parseId(uri);
                count = db.update(MainTopTable.TABLE_NAME, values, appendSelection(selection, MainTopTable.TOP_NAME + "=" + id), selectionArgs);
                break;
            case TOP_LIST:
                count = db.update(MainTopTable.TABLE_NAME, values, selection, selectionArgs);
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
}
