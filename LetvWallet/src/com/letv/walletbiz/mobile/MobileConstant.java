package com.letv.walletbiz.mobile;

/**
 * Created by linquan on 15-11-17.
 */
public class MobileConstant {
    public static final class PATH {
        private final static String _BASE = "charge/api/v1/phone/";
        public final static String PRODUCT = _BASE + "product";
        public final static String ORDER = _BASE + "order";
        public final static String ORDER_QUERY = _BASE + "orderlist";
        public final static String CHARGE = _BASE + "charge";
        public final static String SDKPAY = _BASE + "sdkpay";
        public final static String ORDER_DETAIL_QUERY = _BASE + "orderdetail";
        public final static String COUPON = "coupon/api/v1/available";

        public final static String MOBILE_MATTER_ATTENTION_BASE = "http://static.scloud.letv.com/htmlpage/";
        public final static String MOBILE_MATTER_ATTENTION_FEE = MOBILE_MATTER_ATTENTION_BASE + "1467017924-5770eac4cf88d.html";
        public final static String MOBILE_MATTER_ATTENTION_FLOW = MOBILE_MATTER_ATTENTION_BASE + "1467017962-5770eaeacbff0.html";
    }

    public static final class JPRODUCT {
        public final static String PROVINCE = "province";
        public final static String ISP = "isp";
        public final static String PRODUCTS = "product_list";
        public final static String ID = "product_id";
        public final static String RANK = "product_rank";
        public final static String NAME = "product_name";
        public final static String PRICE = "product_price";
    }

    public static final class PARAM {
        public final static String CLIENT = "client_type";
        public final static String SSO_TK = "sso_tk";
        public final static String TOKEN = "token";
        public final static String TYPE = "type";
        public final static String NUMBER = "number";
        public final static String PRODUCT_ID = "product_id";
        public final static String COUPON_ID = "coupon";
        public final static String SKU_SN = "skus";
        public final static String PRODUCT_UNIT = "product";
        public final static String ORDER_SN = "order_sn";
        public final static String CHARGE_PLATFORM = "platform";
        public final static String SKU = "sku";
        public final static String NUM = "num";

        public final static String COUPONLIST_KEY = "couponlist_key";
        public final static String COUPON_DATA_KEY = "coupon_data_key";
        public final static String COUPON_LIST_COUNT_KEY = "coupon_list_count_key";
        public final static String COUPON_ID_KEY = "coupon_id";
    }

    public static final class JPREPAY {
        public final static String TYPE = "type";
        public final static String RESPONSE = "response";
    }

    public static final class DPRODUCT {
        public final static String FORMAT_PRICE = "%1$.2f";

        public final static String NAME = "name";
        public final static String ID = "id";
        public final static String PRICE = "price";
    }

    public static final class PRODUCT_TYPE {

        public static final int MOBILE_FEE = 1;
        public static final int MOBILE_FLOW = 2;
    }

    public static final class BUSINESS_ID {
        public static final int MOBILE_FEE = 1;
        public static final int MOBILE_FLOW = 4;
    }

    //订单状态, 1:订单已创建, 2:支付中, 3:支付完成, 4:充值中, 5:充值完成, 6:订单取消
    public static final class ORDER_STATUS {

        public static final int CREATED = 1;
        public static final int PAY_ONGOING = 2;
        public static final int PAID = 3;
        public static final int DEPOSITING = 4;
        public static final int COMPLISHED = 5;
        public static final int CANCELLED = 6;
        public static final int MAX = 7;

    }

    //订单状态, 1:订单已创建, 2:支付中, 3:支付完成, 4:充值中, 5:充值完成
    public static final class ORDER_TODO {
        public static final int TOPAY = 1;
        public static final int REODER = 2;
        public static final int NONE = 3;
    }

    public static final class NUMBER {
        public static final float MTG = 1024;
    }

}
