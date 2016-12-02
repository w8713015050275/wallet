package com.letv.walletbiz.base.pay;

/**
 * http请求想关参数
 *
 * @author linquan
 */
public class Constants {


    //支付平台：1：支付宝，2：微信支付
    public static final class PLATFORM {
        public static final int ALIPAY = 1;
        public static final int WECHAT = 2;
    }


    public static final class  RESULT_STATUS{
        public static final int SUCCESS = 1;
        public static final int PENDING = 0;
        public static final int FAIL = -1;
        public static final int CANCEL = -2;
    }

    public static final class PAY_STATUS {
        public static final int STATUS_OK = 0;
        public static final int STATUS_PAY_STARTED = 1;
        public static final int STATUS_NONETWORK_ERROR = -1;
        public static final int STATUS_NETWORK_ERROR = -2;
        public static final int STATUS_PAYINFO_ERROR = -3;
        public static final int STATUS_PAYSTART_ERROR = -4;
        public static final int STATUS_NOWECHAT_ERROR = -5;
        public static final int STATUS_WECHAT_NOPAY_ERROR = -6;
    }

    public static final class INFO_PARAM {
        public final static String TOKEN = "token";
        public final static String CLIENT = "client_type";
        public final static String NUMBER = "number";
        public final static String ORDER_SN = "order_sn";
        public static final String ORDER_NO = "orderNo";
        public final static String CHARGE_PLATFORM = "platform";
        public final static String DBG = "dbg";
        public final static String LEPAY_INFO = "lepayinfo";
        public final static String LEPAY_RETURN_RESULT = "pay_return_result";

    }

    public static final class INFO_VALUE {
        public final static String CLIENT_ID = "1";
    }

    public static final class REQUEST{
        public static final int REQUEST_CODE = 1001;
    }
}