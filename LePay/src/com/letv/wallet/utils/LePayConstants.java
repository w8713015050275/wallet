package com.letv.wallet.utils;

public class LePayConstants {

    public static final class PAY_RETURN_RESULT {
        public static final int PAY_SUCCESSED = 1;
        public static final int PAY_INFO_ERROR = 2;
        public static final int PAY_CANCLE = 3;
        public static final int PAY_FAILED = 4;
        public static final int PAY_WAITTING = 5;
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
    }
}
