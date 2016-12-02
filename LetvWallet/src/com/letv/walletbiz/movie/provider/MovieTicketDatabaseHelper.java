package com.letv.walletbiz.movie.provider;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import static com.letv.walletbiz.movie.provider.MovieTicketContract.CinemaTable;
import static com.letv.walletbiz.movie.provider.MovieTicketContract.CityListTable;
import static com.letv.walletbiz.movie.provider.MovieTicketContract.CityMovieTable;
import static com.letv.walletbiz.movie.provider.MovieTicketContract.HotCityListTable;

/**
 * Created by liuliang on 15-12-28.
 */
public class MovieTicketDatabaseHelper extends SQLiteOpenHelper {

    static final String DATABASE_NAME = "movie_ticket.db";

    static final int DATABASE_VERSION = 3;

    private Context mContext;

    private static MovieTicketDatabaseHelper sInstance = null;

    static synchronized MovieTicketDatabaseHelper getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new MovieTicketDatabaseHelper(context);
        }
        return sInstance;
    }

    protected MovieTicketDatabaseHelper(Context context) {
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
            oldVersion = 2;
        }
        if(oldVersion < 3){
            updateToVersion3(db);
            oldVersion += 1;
        }
    }

    private void createTables(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS " + CityMovieTable.TABLE_NAME +
                "(" + CityMovieTable.CITY_ID + " INTEGER," +
                CityMovieTable.DATA + " BLOB," +
                CityMovieTable.IS_WILL+ " INTEGER DEFAULT 0," +
                CityMovieTable.LAST_MODIFIED + " INTEGER,"+
                "PRIMARY KEY(" + CityMovieTable.CITY_ID + "," + CityMovieTable.IS_WILL + "));") ;
        db.execSQL("CREATE TABLE IF NOT EXISTS " + CinemaTable.TABLE_NAME +
                "(" + CinemaTable.CITY_ID + " INTEGER," +
                CinemaTable.MOVIE_ID + " INTEGER DEFAULT -1," +
                CinemaTable.SCHEDULE_DATE + " TEXT DEFAULT 'NULL'," +
                CinemaTable.DATA + " BLOB," +
                CinemaTable.LAST_MODIFIED + " INTEGER," +
                "PRIMARY KEY(" + CinemaTable.CITY_ID + "," + CinemaTable.MOVIE_ID + "," + CinemaTable.SCHEDULE_DATE + "));") ;
        db.execSQL("CREATE TABLE IF NOT EXISTS " + CityListTable.TABLE_NAME +
                "(" + CityListTable._ID + " INTEGER PRIMARY KEY," +
                CityListTable.NAME + " TEXT," +
                CityListTable.PINYIN + " TEXT," +
                CityListTable.LOCATION_NAME + " TEXT," +
                CityListTable.LAST_SELECTED + " INTEGER DEFAULT 0);");
        db.execSQL("CREATE TABLE IF NOT EXISTS " + HotCityListTable.TABLE_NAME +
                "(" + HotCityListTable._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                HotCityListTable.CITY_ID + " INTEGER UNIQUE," +
                HotCityListTable.NAME + " TEXT ," +
                HotCityListTable.PINYIN + " TEXT);");
    }

    private void updateToVersion2(SQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS " + CityMovieTable.TABLE_NAME);
        db.execSQL("CREATE TABLE IF NOT EXISTS " + CityMovieTable.TABLE_NAME +
                "(" + CityMovieTable.CITY_ID + " INTEGER PRIMARY KEY," +
                CityMovieTable.DATA + " BLOB," +
                CityMovieTable.IS_WILL+ " INTEGER DEFAULT 0," +
                CityMovieTable.LAST_MODIFIED + " INTEGER);");
    }

    private void updateToVersion3(SQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS " + CinemaTable.TABLE_NAME);
        db.execSQL("CREATE TABLE IF NOT EXISTS " + CinemaTable.TABLE_NAME +
                "(" + CinemaTable.CITY_ID + " INTEGER," +
                CinemaTable.MOVIE_ID + " INTEGER DEFAULT -1," +
                CinemaTable.SCHEDULE_DATE + " TEXT DEFAULT 'NULL'," +
                CinemaTable.DATA + " BLOB," +
                CinemaTable.LAST_MODIFIED + " INTEGER," +
                "PRIMARY KEY(" + CinemaTable.CITY_ID + "," + CinemaTable.MOVIE_ID + "," + CinemaTable.SCHEDULE_DATE + "));") ;
    }
}
