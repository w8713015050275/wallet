package com.letv.walletbiz.movie.provider;

import android.net.Uri;

/**
 * Created by liuliang on 15-12-28.
 */
public class MovieTicketContract {

    public static final String AUTHORITY = "com.letv.movie.ticket";

    public static final Uri AUTHORITY_URI = Uri.parse("content://" + AUTHORITY);

    /**
     * 某个城市的电影信息
     */
    public static final class CityMovieTable {

        private CityMovieTable() {
        }

        public static final Uri CONTENT_URI = Uri.withAppendedPath(AUTHORITY_URI, "city_movie");

        public static final String TABLE_NAME = "city_movie";

        /**
         *城市ID
         * <p>TYPE:INTEGER</p>
         */
        public static final String CITY_ID = "cityId";

        /**
         * 电影信息
         * <p>TYPE:BLOB</p>
         */
        public static final String DATA = "data";

        /**
         * 是否即将上映 0为正在上映, 1为即将上映
         * <p>TYPE:INTEGER</p>
         */
        public static final String IS_WILL = "isWill";

        /**
         * 最新更改时间
         * <p>TYPE:LONG</p>
         */
        public static final String LAST_MODIFIED = "last_modified";

    }

    public static final class CinemaTable {

        private CinemaTable() {
        }

        public static final Uri CONTENT_URI = Uri.withAppendedPath(AUTHORITY_URI, "cinema");

        public static final String TABLE_NAME = "cinema";

        /**
         * 城市ID
         * <p>TYPE:INTEGER</p>
         */
        public static final String CITY_ID = "city_id";

        /**
         * 电影ID 默认:-1
         * <p>TYPE:LONG</p>
         */
        public static final String MOVIE_ID = "movie_id";

        /**
         * 日期 "yyyyMMdd"
         * <p>TYPE:TEXT</p>
         */
        public static final String SCHEDULE_DATE = "sche_date";

        /**
         * 影院列表
         * <p>TYPE:BLOB</p>
         */
        public static final String DATA = "data";

        /**
         * 最后修改时间
         * <p>TYPE:INTEGER</p>
         */
        public static final String LAST_MODIFIED = "last_modified";
    }

    public static final class SeatMapTable {

        private SeatMapTable() {
        }

        public static final Uri CONTENT_URI = Uri.withAppendedPath(AUTHORITY_URI, "seat_map");

        public static final String TABLE_NAME = "seat_map";

        public static final String _ID = "_id";

        /**
         * <p>TYPE:INTEGER</p>
         */
        public static final String ROOM_ID = "room_id";

        /**
         * <p>TYPE:BLOB</p>
         */
        public static final String SEAT_INFO = "seat_info";
    }

    public static abstract class City {
        /**
         * <p>TYPE:INTEGER</p>
         */
        public static final String _ID = "_id";

        /**
         * 城市名
         * <p>TYPE:TEXT</p>
         */
        public static final String NAME = "name";

        /**
         * 城市拼音
         * <p>TYPE:TEXT</p>
         */
        public static final String PINYIN = "pinyin";
    }

    public static final class CityListTable extends City {

        private CityListTable() {
        }

        public static final Uri CONTENT_URI = Uri.withAppendedPath(AUTHORITY_URI, "city_list");

        public static final String TABLE_NAME = "city_list";

        /**
         * 定位时获得的城市名
         * <p>TYPE:TEXT</p>
         */
        public static final String LOCATION_NAME = "location_name";

        /**
         * 最后选择时间
         *
         * <p>TYPE:LONG</p>
         */
        public static final String LAST_SELECTED = "last_selected";

    }

    public static final class HotCityListTable extends City {

        private HotCityListTable() {
        }

        public static final Uri CONTENT_URI = Uri.withAppendedPath(AUTHORITY_URI, "hot_city_list");

        public static final String TABLE_NAME = "hot_city_list";

        public static final String CITY_ID = "city_id";
    }

}
