package com.letv.walletbiz.member;

/**
 * Created by zhangzhiwei1 on 16-11-21.
 */

public class MemberConstant {

    /**
     * 生产环境
     */
    public static final String MEMBER_BASE_PATH = "member/";

    /**
     * 获取会员购买类型
     */
    public static final String MEMBER_GET_MEMBER_TYPE_LIST = MEMBER_BASE_PATH + "api/v1/member/list";

    /**
     * 获取banner
     * param: {@link #MEMBER_TYPE};
     */
    public static final String MEMBER_GET_BANNER_LIST = MEMBER_BASE_PATH + "api/v1/banner/list";

    /**
     * 可购买会员套餐列表
     * param: {@link #MEMBER_TOKEN};
     * param: {@link #MEMBER_TYPE};
     */
    public static final String MEMBER_GET_PRODUCT_LIST = MEMBER_BASE_PATH + "api/v1/product/list";

    /**
     * 查询可用优惠券
     * param: {@link #MEMBER_UID};
     * param: {@link #MEMBER_CATEGORY};
     * param: {@link #MEMBER_PRICE};
     * param: {@link #MEMBER_NUM};
     */
    public static final String MEMBER_GET_AVAILABLE_COUPON_LIST = MEMBER_BASE_PATH + "api/v1/coupon/available";

    /**
     * 获取用户优惠券
     * param: {@link #MEMBER_UID};
     * param: {@link #MEMBER_LAST_ID};
     * param: {@link #MEMBER_DIRECTION};
     * param: {@link #MEMBER_LIMIT};
     */
    public static final String MEMBER_GET_COUPON_LIST = MEMBER_BASE_PATH + "api/v1/coupon/lists";

    /**
     * 获取无效优惠券(已过期，已使用)
     * param: {@link #MEMBER_UID};
     * param: {@link #MEMBER_LAST_ID};
     * param: {@link #MEMBER_DIRECTION};
     * param: {@link #MEMBER_LIMIT};
     */
    public static final String MEMBER_GET_UNAVAILABLE_COUPON_LIST = MEMBER_BASE_PATH + "api/v1/coupon/unavailable";

    /**
     * 获取优惠券详情
     * param: {@link #MEMBER_COUPON_IDS};
     */
    public static final String MEMBER_GET_COUPON_DETAIL = MEMBER_BASE_PATH + "api/v1/coupon/detail";

    /**
     * 下单
     * param: {@link #MEMBER_TOKEN};
     * param: {@link #MEMBER_SKU_NO};
     * param: {@link #MEMBER_COUPON};
     */
    public static final String MEMBER_CREATE_ORDER = MEMBER_BASE_PATH + "api/v1/order/create";

    /**
     * 预支付信息
     * param: {@link #MEMBER_TOKEN};
     * param: {@link #MEMBER_CLIENT_TYPE};
     * param: {@link #MEMBER_ORDER_SN};
     * param: {@link #MEMBER_ORDER_PLATFORM};
     * param: {@link #MEMBER_ORDER_DBG};
     */
    public static final String MEMBER_PAY_ORDER = MEMBER_BASE_PATH + "api/v1/order/pay";

    /**
     * 订单详情
     * param: {@link #MEMBER_TOKEN};
     * param: {@link #MEMBER_ORDER_SN};
     */
    public static final String MEMBER_GET_ORDER_INFO = MEMBER_BASE_PATH + "api/v1/order/info";

    /**
     * 取消订单
     * param: {@link #MEMBER_TOKEN};
     * param: {@link #MEMBER_ORDER_SN};
     */
    public static final String MEMBER_CANCEL_ORDER = MEMBER_BASE_PATH + "api/v1/order/cancel";

    /**
     * 订单列表
     * param: {@link #MEMBER_TOKEN};
     * param: {@link #MEMBER_LIMIT};
     * param: {@link #MEMBER_OFFSET};
     * param: {@link #MEMBER_ORDER_TYPE};
     */
    public static final String MEMBER_GET_ORDER_LIST = MEMBER_BASE_PATH + "api/v1/order/list";


    /////////////////////////////////////////////////////////////////////////////////////////////////
    /**
     * 会员类型
     * TYPE:string
     */
    public static final String MEMBER_TYPE = "type";

    /**
     * 会员类型
     * TYPE:string
     */
    public static final String MEMBER_SPU_ID = "spu_id";

    /**
     * 用户sso token
     * MEMBER_TOKEN:string
     */
    public static final String MEMBER_TOKEN = "token";

    /**
     * 乐视网用户uid
     * MEMBER_UID:string
     */
    public static final String MEMBER_UID = "uid";

    /**
     * 会员商品分类字符串标识
     * MEMBER_PRICE:string
     */
    public static final String MEMBER_PRICE = "price";

    /**
     * 商品单价
     * MEMBER_CATEGORY:double
     */
    public static final String MEMBER_CATEGORY = "category";

    /**
     * 商品数量
     * MEMBER_NUM:int
     */
    public static final String MEMBER_NUM = "num";

    /**
     * 向后取数据：上次获取的最后一条记录的rank_id
     * 向前取数据：获取到的记录的最前面的一条记录rank_id
     * MEMBER_LAST_ID:int
     */
    public static final String MEMBER_LAST_ID = "last_id";

    /**
     * 1：标示向后取数据，-1：标识向前取数据
     * MEMBER_DIRECTION:int
     */
    public static final String MEMBER_DIRECTION = "direction";

    /**
     * 单次获取条数，取值范围：[1, 20]
     * MEMBER_LIMIT:int
     */
    public static final String MEMBER_LIMIT = "limit";

    /**
     * coupon id组成的字符串，以逗号隔开
     * MEMBER_COUPON_IDS:string
     */
    public static final String MEMBER_COUPON_IDS = "coupon_ids";

    /**
     * 产品sku号
     * MEMBER_SKU_NO:string
     */
    public static final String MEMBER_SKU_NO = "sku_no";

    /**
     * 使用的优惠券的ucoupon_id，多个优惠券可用逗号分隔
     * MEMBER_COUPON:string
     */
    public static final String MEMBER_COUPON = "coupon";

    /**
     * 客户端类型：1：mobile，2：pc，3：tv，4：mweb（移动端网站)
     * MEMBER_CLIENT_TYPE:string
     */
    public static final String MEMBER_CLIENT_TYPE = "client_type";

    /**
     * 产品订单号
     * MEMBER_ORDER_SN:string
     */
    public static final String MEMBER_ORDER_SN = "order_sn";

    /**
     * 产品订单号
     * MEMBER_ORDER_PLATFORM:int
     */
    public static final String MEMBER_ORDER_PLATFORM = "platform";

    /**
     * 产品订单号
     * MEMBER_ORDER_DBG:boolean
     */
    public static final String MEMBER_ORDER_DBG = "dbg";

    /**
     * 历史订单时可传
     * MEMBER_OFFSET:string
     */
    public static final String MEMBER_OFFSET = "offset";

    /**
     * 历史订单时可传
     * MEMBER_ORDER_TYPE:string
     */
    public static final String MEMBER_ORDER_TYPE = "type";
    public static final String EXTRA_MEMBER_FRAGMENT_TYPE = "";
    public static final String EXTRA_AGREEMENT_URL = "agreement_url";
    public static final String EXTRA_AGREEMENT_TITLE_NAME = "agreement_title_name";
    public static final int BANNER_TYPE_LINK = 2;
    public static final int BANNER_TYPE_APP = 3;
    public static final String MEMBER_PRODUCT_PRICE = "product_price";
    public static final String MEMBER_FROM_MAIN_PANEL = "from_main_panel";

    public class PARAM {
        public static final String COUPONLIST_KEY = "couponlist_key";
        public static final int CLIENT_TYPE = 1;
        public static final int PLATFORM = 1;
        public static final int PRODUCT_COUNT = 1;
        public static final String COUPON_LIST_COUNT_KEY = "coupon_list_count_key";
        public static final String COUPON_DATA_KEY = "coupon_data_key";
        public static final String COUPON_ID_KEY = "coupon_id_key";
        public static final String MEMBER_SKUS = "skus";
    }

    public static final class ORDER_STATUS {
        public static final int CREATED = 1;
        public static final int PAID = 2;
        public static final int REFUNDED = 3;
        public static final int CANCELLED = 4;
        public static final int REFUNDING = 5;
        public static final int COMPLISHED = 6;
    }
}
