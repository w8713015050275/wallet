package com.letv.walletbiz.member.provider;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.letv.walletbiz.member.provider.MemberDBConstant.BannerTable;
import com.letv.walletbiz.member.provider.MemberDBConstant.MemberTypeTable;
import com.letv.walletbiz.member.provider.MemberDBConstant.ProductTable;

/**
 * Created by zhangzhiwei1 on 16-11-21.
 */

public class MemberDatabaseHelper extends SQLiteOpenHelper {

    static final String DATABASE_NAME = "member.db";
    static final int DATABASE_VERSION = 2;

    private static MemberDatabaseHelper sInstance = null;

    static synchronized MemberDatabaseHelper getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new MemberDatabaseHelper(context);
        }
        return sInstance;
    }

    public MemberDatabaseHelper(Context context) {
        super(context,DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        createTables(db);
    }

    private void createTables(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS " + BannerTable.TABLE_NAME +
                "(" + BannerTable.BANNER_ID + " INTEGER," +
                BannerTable.MEMBER_TYPE + " TEXT," +
                BannerTable.BANNER_NAME + " TEXT," +
                BannerTable.POSITION_ID + " INTEGER," +
                BannerTable.RANK + " INTEGER," +
                BannerTable.BANNER_TYPE + " INTEGER," +
                BannerTable.BANNER_POST + " TEXT," +
                BannerTable.BANNER_LINK + " TEXT," +
                BannerTable.BANNER_TOKEN + " INTEGER," +
                BannerTable.JUMP_PARA + " TEXT," +
                BannerTable.PACKAGE_NAME + " TEXT," +
                BannerTable.UPDATE_TIME + " INTEGER," +
                BannerTable.VERSION + " INTEGER," +
                "PRIMARY KEY(" + BannerTable.BANNER_ID + "));");

        db.execSQL("CREATE TABLE IF NOT EXISTS " + MemberTypeTable.TABLE_NAME +
                "(" + MemberTypeTable.ID + " INTEGER," +
                MemberTypeTable.NAME + " TEXT," +
                MemberTypeTable.TYPE + " TEXT," +
                MemberTypeTable.GOODS_ID + " TEXT," +
                MemberTypeTable.STATE + " TEXT," +
                MemberTypeTable.RANK + " TEXT," +
                MemberTypeTable.PROTOCOL_LINK + " TEXT," +
                MemberTypeTable.IMG_URL + " TEXT," +
                MemberTypeTable.DESCRIPTION + " TEXT," +
                MemberTypeTable.GOODS_JSON + " BLOB," +
                MemberTypeTable.UPDATE_TIME + " TEXT," +
                MemberTypeTable.ADD_TIME + " TEXT," +
                MemberTypeTable.OPERATOR + " TEXT," +
                "PRIMARY KEY(" + MemberTypeTable.ID + "));");

        db.execSQL("CREATE TABLE IF NOT EXISTS " + ProductTable.TABLE_NAME +
                "(" + ProductTable.ID + " INTEGER," +
                ProductTable.MEMBER_TYPE + " TEXT," +
                ProductTable.SKU_NO + " TEXT," +
                ProductTable.NAME + " TEXT," +
                ProductTable.PRICE + " TEXT," +
                ProductTable.KIND + " TEXT," +
                ProductTable.MONTH_PRICE + " TEXT," +
                ProductTable.TAG + " TEXT," +
                ProductTable.DESCRIPTION + " TEXT," +
                ProductTable.DURATION + " TEXT," +
                ProductTable.SPU_NAME + " TEXT," +
                ProductTable.PROTOCOL_URL + " TEXT," +
                "PRIMARY KEY(" + ProductTable.ID + "));");
    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
