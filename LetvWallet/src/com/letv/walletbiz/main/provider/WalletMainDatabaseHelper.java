package com.letv.walletbiz.main.provider;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import static com.letv.walletbiz.main.provider.WalletContract.BannerTable;
import static com.letv.walletbiz.main.provider.WalletContract.ServiceTable;

/**
 * Created by liuliang on 16-4-11.
 */
public class WalletMainDatabaseHelper extends SQLiteOpenHelper {

    static final String DATABASE_NAME = "wallet_main.db";

    static final int DATABASE_VERSION = 2;

    private Context mContext;

    private static WalletMainDatabaseHelper sInstance = null;

    static synchronized WalletMainDatabaseHelper getsInstance(Context context) {
        if (sInstance == null) {
            sInstance = new WalletMainDatabaseHelper(context);
        }
        return sInstance;
    }

    public WalletMainDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        mContext = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        createTables(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            updateToVersion2(db);
        }
    }

    private void createTables(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS " + ServiceTable.TABLE_NAME +
                "(" + ServiceTable.SERVICE_ID + " INTEGER PRIMARY KEY," +
                ServiceTable.SERVICE_NAME + " TEXT," +
                ServiceTable.ICON + " TEXT," +
                ServiceTable.JUMP_TYPE + " INTEGER," +
                ServiceTable.JUMP_LINK + " TEXT," +
                ServiceTable.NEED_TOKEN + " INTEGER," +
                ServiceTable.JUMP_PARAM + " TEXT," +
                ServiceTable.PACKAGE_NAME + " TEXT," +
                ServiceTable.STATE + " INTEGER," +
                ServiceTable.RANK + " INTEGER," +
                ServiceTable.UPDATE_TIME + " INTEGER);");
        db.execSQL("CREATE TABLE IF NOT EXISTS " + BannerTable.TABLE_NAME +
                "(" + BannerTable.BANNER_ID + " INTEGER PRIMARY KEY," +
                BannerTable.BANNER_NAME + " TEXT," +
                BannerTable.POSITION_ID + " INTEGER," +
                BannerTable.RANK + " INTEGER," +
                BannerTable.BANNER_TYPE + " INTEGER," +
                BannerTable.BANNER_POST + " TEXT," +
                BannerTable.BANNER_LINK + " TEXT," +
                BannerTable.NEED_TOKEN + " INTEGER," +
                BannerTable.UPDATE_TIME + " INTEGER);");
    }

    private void updateToVersion2(SQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS " + ServiceTable.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + BannerTable.TABLE_NAME);
        createTables(db);
    }
}
