package com.letv.walletbiz.mobile.provider;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by changjiajie on 16-1-13.
 */
public class MobileContactDatabaseHelper extends SQLiteOpenHelper {


    static final String DATABASE_NAME = "mobilecache.db";

    static final int DATABASE_VERSION = 1;

    private Context mContext;

    private static MobileContactDatabaseHelper sInstance = null;

    static synchronized MobileContactDatabaseHelper getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new MobileContactDatabaseHelper(context);
        }
        return sInstance;
    }

    protected MobileContactDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        mContext = context;
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + MobileContact.ContactNumberCacheTable.TABLE_NAME +
                "(" + MobileContact.ContactNumberCacheTable._ID + " INTEGER," +
                MobileContact.ContactNumberCacheTable.PHONE_NUMBER + " TEXT PRIMARY KEY,"
                + MobileContact.ContactNumberCacheTable._TIME + " LONG);");
    }
}
