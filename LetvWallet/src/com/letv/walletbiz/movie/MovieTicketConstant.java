package com.letv.walletbiz.movie;

/**
 * Created by liuliang on 16-1-4.
 */
public class MovieTicketConstant {

    public static final String MOVIE_BASE_PATH = "movieticket/";

    /**
     * 城市列表
     * param:{@link #MOVIE_PARAM_CITY_NAME}
     */
    public static final String MOVIE_PATH_CITY_LIST = MOVIE_BASE_PATH + "api/v1/city";

    /**
     *城市的热映影片列表
     * param:{@link #MOVIE_PARAM_CITY_ID}
     */
    public static final String MOVIE_PATH_MOVIE_LIST = MOVIE_BASE_PATH + "api/v1/city/movie";

    /**
     *城市即将上映电影列表
     *
     */
    public static final String MOVIE_PATH_MOVIE_WILL_LIST = MOVIE_BASE_PATH + "api/v1/city/moviewill";

    /**
     *城市的影院列表
     * param: {@link #MOVIE_PARAM_CITY_ID};
     *        {@link #MOVIE_PARAM_LATITUDE}(可选);
     *        {@link #MOVIE_PARAM_LONGITUDE}(可选)
     */
    public static final String MOVIE_PATH_CINEMA_LIST = MOVIE_BASE_PATH + "api/v1/city/cinema";

    /**
     * 指定城市、日期、影片下可用的影院列表
     * param: {@link #MOVIE_PARAM_CITY_ID};
     *        {@link #MOVIE_PARAM_MOVIE_ID};
     *        {@link #MOVIE_PARAM_DATE};
     *        {@link #MOVIE_PARAM_LATITUDE}(可选);
     *        {@link #MOVIE_PARAM_LONGITUDE}(可选)
     */
    public static final String MOVIE_PATH_MOVIE_CINEMA_LIST = MOVIE_BASE_PATH + "api/v1/movie/cinema";

    /**
     * 收藏、取消收藏影院接口
     */
    public static final String MOVIE_PATH_CINEMA_SCHEDULE_FAVORITE = MOVIE_BASE_PATH + "api/v1/favorite/cinema";

    /**
     * 影院详情
     * param: {@link #MOVIE_PARAM_CINEMA_ID};
     *        {@link #MOVIE_PARAM_CITY_ID}
     */
    public static final String MOVIE_PATH_CINEMA_SCHEDULE = MOVIE_BASE_PATH + "api/v1/cinema/schedule";

    /**
     * 影厅座位静态数据
     * param: {@link #MOVIE_PARAM_CINEMA_ID};
     *        {@link #MOVIE_PARAM_ROOMID}
     */
    public static final String MOVIE_PATH_CINEMA_ROOM_MAP = MOVIE_BASE_PATH + "api/v1/cinema/room/map";

    /**
     * 影厅已售座位
     * param: {@link #MOVIE_PARAM_SSO_TK};
     *        {@link #MOVIE_PARAM_MPID}
     */
    public static final String MOVIE_PATH_CINEMA_ROOM_SOLD = MOVIE_BASE_PATH + "api/v1/cinema/room/disabled";

    /**
     * 下单（锁座）接口
     * param: {@link #MOVIE_PARAM_SSO_TK};
     *        {@link #MOVIE_PARAM_MPID};
     *        {@link #MOVIE_PARAM_SEAT}
     */
    public static final String MOVIE_PATH_ORDER_ADD = MOVIE_BASE_PATH + "api/v1/order/add";

    /**
     * 调起支付
     * param: {@link #MOVIE_PARAM_SSO_TK};
     *        {@link #MOVIE_PARAM_PHONE};
     *        {@link #MOVIE_PARAM_ORDER_NO};
     *        {@link #MOVIE_PARAM_PLATFORM}
     */
    public static final String MOVIE_PATH_ORDER_PAY = MOVIE_BASE_PATH + "api/v1/order/pay";

    /**
     * 支付结果查询
     * param: {@link #MOVIE_PARAM_SSO_TK};
     *        {@link #MOVIE_PARAM_ORDER_NO}
     */
    public static final String MOVIE_PATH_ORDER_PAYSTAT = MOVIE_BASE_PATH + "api/v1/order/paystat";

    /**
     * 订单列表接口
     * param: {@link #MOVIE_PARAM_SSO_TK};
     *        {@link #MOVIE_PARAM_PROGRESS}(可选):默认全部;
     *        {@link #MOVIE_PARAM_LAST_ID}(可选):默认最新的一条记录的id
     *        {@link #MOVIE_PARAM_MODEL}(可选):默认1，向后取数据
     *        {@link #MOVIE_PARAM_LIMIT}(可选):默认10,取值范围：[1, 20]
     */
    public static final String MOVIE_PATH_ORDER_LIST = MOVIE_BASE_PATH + "api/v1/order/list";

    /**
     * 订单详情
     * param: {@link #MOVIE_PARAM_SSO_TK};
     *        {@link #MOVIE_PARAM_ORDER_NO}
     */
    public static final String MOVIE_PATH_ORDER_DETAIL = MOVIE_BASE_PATH + "api/v1/order/detail";

    /**
     * 取消订单
     * param: {@link #MOVIE_PARAM_SSO_TK};
     *        {@link #MOVIE_PARAM_ORDER_NO}
     */
    public static final String MOVIE_PATH_ORDER_CANCEL = MOVIE_BASE_PATH + "api/v1/order/cancel";

    /**
     * 搜索
     * param: {@link #MOVIE_PARAM_SEARCH_NAME};
     *        {@link #MOVIE_PARAM_SEARCH_NUM}(可选):默认10,范围：[1, 100]
     */
    public static final String MOVIE_PATH_SEARCH = MOVIE_BASE_PATH + "api/v1/search";

    /**
     * 电影详情“资料”tab页数据
     * param: {@link #MOVIE_PARAM_MOVIE_ID};
     *        {@link #MOVIE_PARAM_CITY_ID}
     */
    public static final String MOVIE_PATH_DETAIL = MOVIE_BASE_PATH + "api/v1/movie/detail";

    /**
     * 获取电影剧照列表
     * param: {@link #MOVIE_PARAM_MOVIE_ID}
     */
    public static final String MOVIE_PATH_STILL = MOVIE_BASE_PATH + "api/v1/movie/still";

    /////////////////////////////////////////////////////////////////////////////////////////////////
    /**
     * 城市名称
     * TYPE:String
     */
    public static final String MOVIE_PARAM_CITY_NAME = "city_name";

    /**
     * 城市ID
     * TYPE:int
     */
    public static final String MOVIE_PARAM_CITY_ID = "city_id";

    /**
     * 城市列表版本
     * TYPE:intSeSe
     */
    public static final String MOVIE_PARAM_CITY_VERSION = "version";

    /**
     * 纬度
     * TYPE:float
     */
    public static final String MOVIE_PARAM_LATITUDE = "latitude";

    /**
     * 经度
     * TYPE:float
     */
    public static final String MOVIE_PARAM_LONGITUDE = "longitude";

    /**
     * 电影ID
     * TYPE:int
     */
    public static final String MOVIE_PARAM_MOVIE_ID = "movie_id";

    /**
     * 时间
     * TYPE:string，格式：“YYYYMMDD”
     */
    public static final String MOVIE_PARAM_DATE = "date";

    /**
     * 电影院ID
     * TYPE:int
     */
    public static final String MOVIE_PARAM_CINEMA_ID = "cinema_id";

    /**
     * 操作类型
     * TYPE:int 1：添加收藏;  2：取消收藏
     */
    public static final String MOVIE_PARAM_FAVORITES_ACTION = "action";
    public static final int MOVIE_PARAM_FAVORITES_ACTION_ADD = 1;
    public static final int MOVIE_PARAM_FAVORITES_ACTION_CANCLE = 2;

    /**
     * 影厅ID
     * Type:string
     */
    public static final String MOVIE_PARAM_ROOMID = "roomid";

    /**
     * 用户单点登录的票据
     * Type:string
     */
    public static final String MOVIE_PARAM_SSO_TK = "sso_tk";

    /**
     * 排期id
     * TYPE:string
     */
    public static final String MOVIE_PARAM_MPID = "mpid";

    /**
     * 座位信息
     * TYPE:string
     */
    public static final String MOVIE_PARAM_SEAT = "seat";

    /**
     * 用户确认的手机号
     * TYPE:string
     */
    public static final String MOVIE_PARAM_PHONE = "phone";

    /**
     * 订单编号
     * TYPE:int
     */
    public static final String MOVIE_PARAM_ORDER_NO = "order_no";

    /**
     * 支付平台
     * TYPE:int
     */
    public static final String MOVIE_PARAM_PLATFORM = "platform";

    /**
     * 订单状态
     * TYPE:int
     */
    public static final String MOVIE_PARAM_PROGRESS = "progress";

    /**
     * 默认最新的一条记录的id
     * 向后取数据：上次获取的最后一条记录的order_no, 向前取数据：获取到的记录的最前面的一条记录order_no
     * TYPE:int
     */
    public static final String MOVIE_PARAM_LAST_ID = "last_id";

    /**
     * 默认1，向后取数据
     * 1：标示向后取数据，-1：标识向前取数据
     * TYPE:int
     */
    public static final String MOVIE_PARAM_MODEL = "model";

    /**
     * 单次获取条数
     * TYPE:int
     */
    public static final String MOVIE_PARAM_LIMIT = "limit";

    /**
     * 用户搜索的关键字
     * TYPE:string
     */
    public static final String MOVIE_PARAM_SEARCH_NAME = "name";

    /**
     * 欲搜索的记录总数
     * TYPE:int
     * 范围：[1, 100]
     */
    public static final String MOVIE_PARAM_SEARCH_NUM = "num";

    ///////////////////////////////////sharedpreferences////////////////////////////////////////////

    public static final String PREFERENCES_CITY_LIST_VERSION = "city_list_version";

    public static final String PREFERENCES_CURRENT_CITY = "current_city";

    public static final String PREFERENCES_CURRENT_CITY_ID = "current_city_id";

    public static final String PREFERENCES_LAST_CHECK_CITYLIST = "last_check_cityList";

    //////////////////////////////////////////Bundle Extra//////////////////////////////////////////

    /**
     * int 跳转到电影票指定tab
     */
    public static final String EXTRA_MOVIE_TICKET_TAB_ID = "tag_id";
    /**
     * String
     */
    public static final String EXTRA_CINEMA_NAME = "cinema_name";

    /**
     * int
     */
    public static final String EXTRA_CINEMA_ID = "cinema_id";

    /**
     * String
     */
    public static final String EXTRA_CINEMA_ADDRESS = "cinema_address";

    /**
     * int
     */
    public static final String EXTRA_CITY_ID = "city_id";

    /**
     * long
     */
    public static final String EXTRA_MOVIE_ID = "movie_id";

    /**
     * String
     */
    public static final String EXTRA_MOVIE_NAME = "movie_name";

    /**
     * int
     */
    public static final String EXTRA_MOVIE_LONGS = "movie_longs";

    /**
     * String
     */
    public static final String EXTRA_DATE = "date";

    /**
     * 是否收藏，1：已收藏，0：未收藏
     */
    public static final String MOVIE_PARAM_FAVORITES = "is_favorite";
    public static final int MOVIE_PARAM_FAVORITED = 1;
    public static final int MOVIE_PARAM_UNFAVORITED = 0;

    /**
     * {@link com.letv.wallet.movie.beans.CinemaSchedule.Schedule}
     */
    public static final String EXTRA_MOVIE_SCHEDULE = "movie_schedule";

    /**
     *{@link com.letv.wallet.movie.beans.CinemaSchedule.DiscountInfo}
     */
    public static final String EXTRA_MOVIE_DISCOUNT_INFO = "movie_discount_info";

    /**
     * String
     */
    public static final String EXTRA_MOVIE_ORDER_NUM = "order_number";

    /**
     * String[]
     */
    public static final String EXTRA_SCHE_DATE = "schedule_date";

    /**
     * {@link com.letv.wallet.movie.beans.MovieDetail.MovieAllSizePhoto}
     */
    public static final String EXTRA_MOVIE_PHOTO_ARRAY = "photo_array";

    /**
     * int
     */
    public static final String EXTRA_MOVIE_PHOTO_INDEX = "photo_index";

    /**
     *movie list fragment type
     */
    public static final String EXTRA_MOVIE_LIST_FRAGMENT_TYPE = "movie_list_type";
    /**
     *movie list fragment  currrnt city id
     */
    public static final String EXTRA_MOVIE_LIST_FRAGMENT_CITY_ID = "movie_list_city_id";
}
