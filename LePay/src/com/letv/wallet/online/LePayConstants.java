package com.letv.wallet.online;

public class LePayConstants {

    public static final class PATH {
        public final static String PAY_CHANNEL = "jrpay/api/v1/paychannel";
        public final static String GET_CASHIER_URL = "jrpay/api/v1/showcashier";
        public final static String ORDER_NO = "jrpay/api/v1/query";
    }

    public static final class PARAM {
        public final static String PAY_INFO = "pay_info";
        public final static String PAY_METHOD = "pay_method";
        public final static String ORDER_NO = "order_no";
    }

    // 10000 成功, 10001 参数错误, 10002 签名错误, 10005 用户级别的错误，错误信息可展示给用户, 10007 系统错误,10008 用户身份验证错误（比如sso_tk失效或者错误等）
    public static final class ERRNO {
        public static final int ERRNO_SUCCESS = 10000;
        public static final int ERRNO_PARAM = 10001;
        public static final int ERRNO_SIGN = 10002;
        public static final int ERRNO_USER = 10005;
        public static final int ERRNO_USER_AUTH_FAILED = 10008;
    }

    // 状态：int，3-未激活，2-余额不足，1-可用，0-不可用
    public static final class PAY_ACTIVE {
        public static final int UNAVAILABLE = 0;
        public static final int AVAILABLE = 1;
        public static final int INSUFFICIENT = 2;
        public static final int NOTACTIVATION = 3;
    }

    /**
     * 已激活，正常：0
     * 已激活，冻结：1
     * 未激活，不可再申请：2
     * 未激活，可申请：3
     * 激活申请中：4
     */
    public static final class YOUΠ_ACTIVE {
        public static final int NORMAL = 0;
        public static final int ACTIVATED_FROZEN = 1;
        public static final int NOTACTIVATION_CANNOT_APPLY = 2;
        public static final int NOTACTIVATION_CAN_APPLY = 3;
        public static final int ACTIVATION_IN = 4;
    }

    public static final class PAY_STATUS {
        public static final String FAIL = "1";
        public static final String SUCCESS = "2";
        public static final String UNAVAILABLE = "0";
        public static final String AVAILABLE = "1";
    }

    // 状态：string 1-未激活，2-已激活
    public static final class ACTIVE_STATUS {
        public static final String NO = "0";
        public static final String YES = "1";
    }

    public static final class PAY_CHANNEL {
        // 来源：int，1-boss，2-金融
        public static final int BOSS_PAY = 1;
        public static final int FINANCE_PAY = 2;

        public static final int CHANNEL_Π = 100;
        public static final int CHANNEL_ONLINE_BANK = 101;
        public static final int CHANNEL_QPAY = 102;
        public static final int CHANNEL_WXPAY = 103;
        public static final int CHANNEL_ALIPAY = 104;

        public static final String CHANNEL_Π_NAME = "lelehua";
        public static final String CHANNEL_ONLINE_BANK_NAME = "online_bank";
        public static final String CHANNEL_QPAY_NAME = "qpay";
        public static final String CHANNEL_WXPAY_NAME = "weixin";
        public static final String CHANNEL_ALIPAY_NAME = "alipay";
    }

    public static final class PAY_FROM {
        public static final String MOBILE_FEE = "1";
        public static final String MOBILE_FLOW = "2";
        public static final String OTHER = "3";
    }

    public static final class PAY_RETURN_RESULT {
        public static final int PAY_SUCCESSED = 1;
        public static final int PAY_CANCLE = 3;
        public static final int PAY_FAILED = 4;
        public static final int NONETWORK = 6;
    }

    public static class ApiReqeustCode {

        public static final int ReqeustCode_Cashier = 1;

        public ApiReqeustCode() {
        }
    }

    public static class ApiReqeustKey {
        public static String PAY_RETURN_RESULT = "pay_return_result";
    }

    public static class ApiIntentExtraKEY {
        public static String LEPAY_INFO = "lepayinfo";
        public static String INFO = "info";
        public static String PAY_STATUS = "pay_status";
        public static String ACTIVE_STATUS = "activeStatus";
        public static String PAYSTATUS = "payStatus";
        public static String JUMP_TYPE = "jump_type";
        public static String CHANNEL_DATA_KEY = "channel_data_key";
    }

    public static class JUMP_TYPE {
        public static String PAY = "pay";
        public static String ACTIVE = "active";
    }
}
