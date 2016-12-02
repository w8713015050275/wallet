package com.letv.walletbiz.base.activity;

/**
 * Created by linquan on 15-11-17.
 */
public class ActivityConstant {

    public static final class MOBILE_PARAM {
        public static final String TYPE = "Type";
        public static final String BASE_ACTION = "com.letv.wallet.mobile.";
        public static final String FEE = "fee";
        public static final String FLOW = "flow";

        public static final String MOBILENUMBER = "Number";
    }

    public static final class BUSINESS_ID {
        public static final int MAIN_ID = 0;
        public static final int MOBILE_FEE_ID = 1;
        public static final int MOBILE_FLOW_ID = 4;
    }

    public static final class PAY_PARAM {
        public static final String PAY_ACTION = "android.intent.action.LE_PAYENTRYACTIVITY";
        public static final String PAY_ORDER_SN = "Pay_Order_SN";
        public static final String PAY_PRODUCT_INFO = "Pay_Product_Info";
        public static final String PAY_PRODUCT = "Pay_Product";
        public static final String PAY_PRODUCT_THEME = "Pay_Theme";
    }

    public static final class NUMBER {
        public static final int ZERO = 0;
    }

    // 订单返回状态值 103：沒有網絡 104：网络链接错误 105：无记录
    public static final class RETURN_STATUS {
        public static final int FAIL_STATE_NO_NET = 103;
        public static final int FAIL_STATE_NET_CONNECTION_FIAL = 104;
        public static final int FAIL_STATE_N0_RECORD = 105;
        public static final int FAIL_STATE_TOAST_PROMPT = 106;
    }

    public static final class ORDER {

        public static final class PARAM {
            public final static String ORDER_LASTID = "last_id";
            public final static String ORDER_LIMIT = "limit";
            public final static String ORDER_MODEL = "model";
            public static final String ORDERRSPDATA = "OrderRSPData";
            public static final String ORDERDATA = "OrderData";
        }

        // 订单页常用常量
        public static final class LIST_CONSTANT {
            public static final long DEFAULT_LASTID = 0;
            public static final int DEFAULT_MOD = 1;
            public static final int MOD_MORE = 1;
            public static final int MOD_UPDATE = 2;
            public static final int MOD_PART_UPDATE = 3;
            public static final int MOD_REFRESH = -1;
            public static final int POSTION_OFFSET = 1;
            public static final int HAVE_PAGING_POSTION_OFFSET = 2;
        }

    }
}
