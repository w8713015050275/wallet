package com.letv.wallet.common.http.client;

/**
 * http请求想关参数
 *
 * @author fengzihua
 */
public class RspConstants {

    public final static String HOSTPROTOCL = "https://";
    public final static String HOSTBASE = HOSTPROTOCL +  "wallet.scloud.letv.com";

    public final static String DOMAIN_LABEL = "wallet";

    public static final int CONNECT_TIMEOUT_TEN = 10000;


    public static final class _RESPONSE {
        /*{
            "errno": 10000, //10000标识成功，其它均为错误
            "errmsg": "error"   //错误信息
        }*/
        // 10000标识成功，其它均为错误
        public static final String ERRNO = "errno";
        // 错误信息
        public static final String ERROR_MSG = "errmsg";
        // 数据内容
        public static final String DATA = "data";
        // 更新成功code值
        public static final int RSP_OK = 10000;
    }
}