package com.letv.walletbiz.member.provider;

import android.net.Uri;

import com.letv.wallet.common.http.beans.LetvBaseBean;

/**
 * Created by zhangzhiwei1 on 16-11-21.
 */

public class MemberDBConstant {
    public static final String AUTHORITY = "com.letv.wallet.member";

    public static final Uri AUTHORITY_URI = Uri.parse("content://" + AUTHORITY);

    public static final class MemberTypeTable implements LetvBaseBean {

        private MemberTypeTable() {}

        public static final Uri CONTENT_URI = Uri.withAppendedPath(AUTHORITY_URI, "member_type");

        public static final String TABLE_NAME = "member_type_table";

        public static final String ID = "id";

        public static final String NAME = "name";

        public static final String TYPE = "type";

        public static final String GOODS_ID = "goods_id";

        public static final String STATE = "state";

        public static final String RANK = "rank";

        public static final String PROTOCOL_LINK = "protocol_link";

        public static final String IMG_URL = "img_url";

        public static final String DESCRIPTION = "description";

        public static final String GOODS_JSON = "goods_json";

        public static final String UPDATE_TIME = "update_time";

        public static final String ADD_TIME = "add_time";

        public static final String OPERATOR = "operator";
    }

    public static final class BannerTable implements LetvBaseBean {

        private BannerTable() {}

        public static final Uri CONTENT_URI = Uri.withAppendedPath(AUTHORITY_URI, "banner");

        public static final String TABLE_NAME = "banner_table";

        public static final String MEMBER_TYPE = "member_type";

        public static final String BANNER_ID = "banner_id";

        public static final String BANNER_NAME = "banner_name";

        public static final String POSITION_ID = "position_id";

        public static final String RANK = "rank";

        public static final String BANNER_TYPE = "banner_type";

        public static final String BANNER_POST = "banner_post";

        public static final String BANNER_LINK = "banner_link";

        public static final String BANNER_TOKEN = "need_token";

        public static final String JUMP_PARA = "jump_param";

        public static final String PACKAGE_NAME = "package_name";

        public static final String UPDATE_TIME = "update_time";

        public static final String VERSION = "version";
    }

    public static final class ProductTable implements LetvBaseBean {

        private ProductTable() {}

        public static final Uri CONTENT_URI = Uri.withAppendedPath(AUTHORITY_URI, "product");

        public static final String TABLE_NAME = "product_table";

        public static final String MEMBER_TYPE = "member_type";

        public static final String ID = "id";

        public static final String SKU_NO = "sku_no";

        public static final String NAME = "name";

        public static final String PRICE = "price";

        public static final String KIND = "kind";

        public static final String MONTH_PRICE = "month_price";

        public static final String TAG = "tag";

        public static final String DESCRIPTION = "description";

        public static final String DURATION = "duration";

        public static final String SPU_NAME = "spu_name";

        public static final String PROTOCOL_URL = "protocol_url";
    }
}
