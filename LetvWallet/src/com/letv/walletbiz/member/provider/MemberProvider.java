package com.letv.walletbiz.member.provider;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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
import android.text.TextUtils;
import java.util.ArrayList;

import com.letv.walletbiz.member.provider.MemberDBConstant.BannerTable;
import com.letv.walletbiz.member.provider.MemberDBConstant.MemberTypeTable;
import com.letv.walletbiz.member.provider.MemberDBConstant.ProductTable;

/**
 * Created by zhangzhiwei1 on 16-11-21.
 */

public class MemberProvider extends ContentProvider {

    private final String TAG = "MemberProvider";
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    private MemberDatabaseHelper mMemberHelper;

    private static final int MEMBER_TYPE = 0;
    private static final int MEMBER_TYPE_ID = 1;
    private static final int BANNER = 2;
    private static final int BANNER_ID = 3;
    private static final int PRODUCT = 4;
    private static final int PRODUCT_ID = 5;

    static {
        sUriMatcher.addURI(MemberDBConstant.AUTHORITY, "member_type", MEMBER_TYPE);
        sUriMatcher.addURI(MemberDBConstant.AUTHORITY, "member_type/#", MEMBER_TYPE_ID);
        sUriMatcher.addURI(MemberDBConstant.AUTHORITY, "banner", BANNER);
        sUriMatcher.addURI(MemberDBConstant.AUTHORITY, "banner/#", BANNER_ID);
        sUriMatcher.addURI(MemberDBConstant.AUTHORITY, "product", PRODUCT);
        sUriMatcher.addURI(MemberDBConstant.AUTHORITY, "product/#", PRODUCT_ID);
    }

    @Override
    public boolean onCreate() {
        mMemberHelper = MemberDatabaseHelper.getInstance(getContext());
        return true;
    }

    @Override
    public ContentProviderResult[] applyBatch(@NonNull ArrayList<ContentProviderOperation> operations) throws OperationApplicationException {
        SQLiteDatabase db = mMemberHelper.getWritableDatabase();
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
        SQLiteDatabase db = mMemberHelper.getReadableDatabase();
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();

        int match = sUriMatcher.match(uri);
        switch (match) {
            case MEMBER_TYPE_ID :
                qb.appendWhere(MemberTypeTable.ID + "=" + uri.getPathSegments().get(1));
            case MEMBER_TYPE :
                qb.setTables(MemberTypeTable.TABLE_NAME);
                break;
            case BANNER_ID :
                qb.appendWhere(BannerTable.BANNER_ID + "=" + uri.getPathSegments().get(1));
            case BANNER :
                qb.setTables(BannerTable.TABLE_NAME);
                break;
            case PRODUCT_ID:
                qb.appendWhere(ProductTable.ID + "=" + uri.getPathSegments().get(1));
            case PRODUCT :
                qb.setTables(ProductTable.TABLE_NAME);
                break;
            default: {
                return null;
            }
        }
        Cursor ret = null;
        try {
            ret = qb.query(db, projection, selection, selectionArgs, null, null, sortOrder);
        } catch (SQLException e) {
            android.util.Log.e(TAG, "got exception when querying: " + e);
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

        SQLiteDatabase db = mMemberHelper.getWritableDatabase();
        int match = sUriMatcher.match(uri);
        long rowID = 0;
        switch (match) {
            case MEMBER_TYPE:
                rowID = db.replace(MemberTypeTable.TABLE_NAME, null, values);
                if (rowID >= 0) {
                    result = ContentUris.withAppendedId(uri, rowID);
                }
                break;
            case BANNER :
                rowID = db.replace(BannerTable.TABLE_NAME, null, values);
                if (rowID >= 0) {
                    result = ContentUris.withAppendedId(uri, rowID);
                }
                break;
            case PRODUCT :
                rowID = db.replace(ProductTable.TABLE_NAME, null, values);
                if (rowID >= 0) {
                    result = ContentUris.withAppendedId(uri, rowID);
                }
                break;
        }
        return result;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        SQLiteDatabase db = mMemberHelper.getReadableDatabase();
        int match = sUriMatcher.match(uri);
        int count = 0;
        long id;
        switch (match) {
            case MEMBER_TYPE_ID :
                id = ContentUris.parseId(uri);
                count = db.delete(MemberTypeTable.TABLE_NAME, appendSelection(selection, MemberTypeTable.ID + "=" + id), selectionArgs);
                break;
            case MEMBER_TYPE :
                count = db.delete(MemberTypeTable.TABLE_NAME, selection, selectionArgs);
                break;
            case BANNER_ID :
                id = ContentUris.parseId(uri);
                count = db.delete(BannerTable.TABLE_NAME, appendSelection(selection, BannerTable.BANNER_ID + "=" + id), selectionArgs);
                break;
            case BANNER :
                count = db.delete(BannerTable.TABLE_NAME, selection, selectionArgs);
                break;
            case PRODUCT_ID:
                id = ContentUris.parseId(uri);
                count = db.delete(ProductTable.TABLE_NAME, appendSelection(selection, ProductTable.ID + "=" + id), selectionArgs);
                break;
            case PRODUCT :
                count = db.delete(ProductTable.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                break;
            }
        return count;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        SQLiteDatabase db = mMemberHelper.getReadableDatabase();
        int match = sUriMatcher.match(uri);
        int count = 0;
        long id;
        switch (match) {
            case MEMBER_TYPE_ID :
                id = ContentUris.parseId(uri);
                count = db.update(MemberTypeTable.TABLE_NAME, values, appendSelection(selection, MemberTypeTable.ID + "=" + id), selectionArgs);
                break;
            case MEMBER_TYPE :
                count = db.update(MemberTypeTable.TABLE_NAME, values, selection, selectionArgs);
                break;
            case BANNER_ID :
                id = ContentUris.parseId(uri);
                count = db.update(BannerTable.TABLE_NAME, values, appendSelection(selection, BannerTable.BANNER_ID + "=" + id), selectionArgs);
                break;
            case BANNER :
                count = db.update(BannerTable.TABLE_NAME, values, selection, selectionArgs);
                break;
            case PRODUCT_ID:
                id = ContentUris.parseId(uri);
                count = db.update(ProductTable.TABLE_NAME, values, appendSelection(selection, ProductTable.ID + "=" + id), selectionArgs);
                break;
            case PRODUCT :
                count = db.update(ProductTable.TABLE_NAME, values, selection, selectionArgs);
                break;
            default:
                break;
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
