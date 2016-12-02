package com.letv.walletbiz.mobile.provider;

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
import android.util.Log;

import com.letv.walletbiz.mobile.dbhelper.HistoryRecordHelper;

import java.util.ArrayList;

/**
 * Created by changjiajie on 16-1-13.
 */
public class MobileContactProvider extends ContentProvider {

    private static final String TAG = MobileContactProvider.class.getSimpleName();
    private static final int PAYCACHE = 1;


    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        sUriMatcher.addURI(MobileContact.AUTHORITY, "mobilecache", PAYCACHE);
    }

    private MobileContactDatabaseHelper mOpenHelper;

    @Override
    public boolean onCreate() {
        mOpenHelper = MobileContactDatabaseHelper.getInstance(getContext());
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
            case PAYCACHE:
                qb.setTables(MobileContact.ContactNumberCacheTable.TABLE_NAME);
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
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        Uri result = null;
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int match = sUriMatcher.match(uri);
        long rowID = 0;
        switch (match) {
            case PAYCACHE:
                rowID = insertContractInfo(db, values);
                if (rowID >= 0) {
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
        switch (match) {
            case PAYCACHE:
                count = db.delete(MobileContact.ContactNumberCacheTable.TABLE_NAME, selection, selectionArgs);
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
        switch (match) {
            case PAYCACHE:
                count = db.update(MobileContact.ContactNumberCacheTable.TABLE_NAME, values, selection, selectionArgs);
                break;
            default: {
                throw new UnsupportedOperationException("Cannot update that URL: " + uri);
            }
        }
        return count;
    }


    private long insertContractInfo(SQLiteDatabase db, ContentValues values) {
        long rowID = db.replace(MobileContact.ContactNumberCacheTable.TABLE_NAME, null, values);
        String[] projection = new String[]{
                MobileContact.ContactNumberCacheTable.PHONE_NUMBER,
                MobileContact.ContactNumberCacheTable._TIME,
        };
        Cursor cursor = query(MobileContact.ContactNumberCacheTable.CONTENT_URI, projection, null, null, MobileContact.ContactNumberCacheTable._TIME + " asc");
        int count = cursor.getCount();
        if (count > HistoryRecordHelper.MAXCOUNT) {
            cursor.moveToFirst();
            String[] selectionArgs = new String[]{cursor.getString(0)};
            delete(MobileContact.ContactNumberCacheTable.CONTENT_URI, MobileContact.ContactNumberCacheTable.PHONE_NUMBER + "=?", selectionArgs);
        }
        return rowID;
    }

    @Override
    public String getType(@NonNull Uri uri) {
        int match = sUriMatcher.match(uri);
        switch (match) {
            case PAYCACHE:
                return MobileContact.AUTHORITY + "/mobilecache";
        }
        return null;
    }
}
