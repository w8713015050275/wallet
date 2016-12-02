package com.letv.walletbiz.main.provider;

import android.net.Uri;

import com.letv.wallet.common.http.beans.LetvBaseBean;

/**
 * Created by liuliang on 16-4-12.
 */
public class WalletContract {

    public static final String AUTHORITY = "com.letv.wallet.main";

    public static final Uri AUTHORITY_URI = Uri.parse("content://" + AUTHORITY);

    public static final class ServiceTable {

        private ServiceTable() {}

        public static final Uri CONTENT_URI = Uri.withAppendedPath(AUTHORITY_URI, "service");

        public static final String TABLE_NAME = "service";

        /**
         * 服务id
         * <p>TYPE:INTEGER</p>
         */
        public static final String SERVICE_ID = "service_id";

        /**
         * 服务名称
         * <p>TYPE:TEXT</p>
         */
        public static final String SERVICE_NAME = "service_name";

        /**
         * icon图标
         * <p>TYPE:TEXT</p>
         */
        public static final String ICON = "icon";

        /**
         * 跳转类型:1-APP跳转，2-网页跳转
         * <p>TYPE:INTEGER</p>
         */
        public static final String JUMP_TYPE = "jump_type";

        /**
         * 跳转链接，当jump_type=2时候有效，否则为空字符串
         * <p>TYPE:TEXT</p>
         */
        public static final String JUMP_LINK = "jump_link";

        /**
         * 是否需要登陆：1-加token，0-不加token
         * <p>TYPE:INTEGER</p>
         */
        public static final String NEED_TOKEN = "need_token";

        /**
         * 跳转参数，当jump_type=1有效，否则为空字符串
         * <p>TYPE:TEXT</p>
         */
        public static final String JUMP_PARAM = "jump_param";

        /**
         * 包名，当jump_type=1有效，否则为空字符串
         * <p>TYPE:TEXT</p>
         */
        public static final String PACKAGE_NAME = "package_name";

        /**
         * 状态：1-在线，2-待上线，3-下线，4-已删除（客户端只能获取到在线状态的数据）
         * <p>TYPE:INTEGER</p>
         */
        public static final String STATE = "state";

        /**
         * 排序值，值越小，越靠前
         * <p>TYPE:INTEGER</p>
         */
        public static final String RANK = "rank";

        /**
         * 更新时间
         * <p>TYPE:LONG</p>
         */
        public static final String UPDATE_TIME = "update_time";
    }

    public static final class BannerTable implements LetvBaseBean {

        private BannerTable() {}

        public static final Uri CONTENT_URI = Uri.withAppendedPath(AUTHORITY_URI, "banner");

        public static final String TABLE_NAME = "banner";

        /**
         * banner id
         * <p>TYPE:LONG</p>
         */
        public static final String BANNER_ID = "banner_id";

        /**
         * <p>TYPE:TEXT</p>
         */
        public static final String BANNER_NAME = "banner_name";

        /**
         * Banner位置ID
         * <p>TYPE:INTEGER</p>
         */
        public static final String POSITION_ID = "position_id";

        /**
         * Banner顺序位置 排序值，值越小，越靠前
         * <p>TYPE:INTEGER</p>
         */
        public static final String RANK = "rank";

        /**
         * Banner类型，1为图片,2为链接
         * <p>TYPE:INTEGER</p>
         */
        public static final String BANNER_TYPE = "banner_type";

        /**
         * 图片地址
         * <p>TYPE:TEXT</p>
         */
        public static final String BANNER_POST = "banner_post";

        /**
         * 链接地址
         * <p>TYPE:TEXT</p>
         */
        public static final String BANNER_LINK = "banner_link";

        /**
         * 是否需要登陆：1-加token，0-不加token
         * <p>TYPE:INTEGER</p>
         */
        public static final String NEED_TOKEN = "need_token";

        /**
         * 更新时间
         * <p>TYPE:LONG</p>
         */
        public static final String UPDATE_TIME = "update_time";
    }
}
