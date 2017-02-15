package com.letv.wallet.common.util;

/**
 * Created by liuliang on 16-7-4.
 */
public class CommonConstants {

    public static int PHONENUMBER_LENGTH = 11;
    /**
     * phone number regex
     */
    public static String PHONENUMBER_SIMPLE_REGEX = "^[1][0-9]{10}$";
    public static String PHONENUMBER_RIGOROUS_REGEX = "^[1][3,4,5,7,8][0-9]{9}$";

    //.................................EXTRA................................
    /**
     * intent extra
     */
    public static final String EXTRA_URL = "extra_url";
    public static final String EXTRA_FROM = "from";
    public static final String EXTRA_TITLE_NAME = "title_name";
}
