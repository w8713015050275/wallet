package com.letv.walletbiz.coupon;

/**
 * Created by lijunying on 16-4-18.
 */
public class CouponConstant {
    private static final String COUPON_BASE_PATH = "coupon/";

    /**
     * 获取用户优惠券列表
     */
    public static final String COUPON_LIST_PATH = COUPON_BASE_PATH + "api/v1/home/list";
    /**
     * 获取已失效的优惠券列表
     */
    public static final String COUPON_UNAVAILABLE_LIST_PATH = COUPON_BASE_PATH + "api/v1/home/unavailable";
    /**
     * 获取可用优惠券列表
     */
    public static final String COUPON_AVAILABLE_LIST_PATH = COUPON_BASE_PATH + "api/v1/available";

    public static final String COUPON_CHECK_NEW_PATH = COUPON_BASE_PATH + "api/v1/checknew";


    /**
     * 乐视网用户token
     * Type:string
     */
    public static final String COUPON_PARAM_SSO_TK = "sso_tk";
    /**
     *向后取数据：上次获取的最后一条记录的rank_id
     向前取数据：获取到的记录的最前面的一条记录rank_id
     */
    public static final String COUPON_PARAM_LAST_ID = "last_id";
    /**
     *1：标示向后取数据，-1：标识向前取数据, 默认1，向后取数据
     */
    public static final String COUPON_PARAM_MODEL = "model";
    public static final int COUPON_PARAM_MODEL_DEFAULT = 1;
    /**
     *单次获取条数，取值范围：[1, 20],默认10
     */
    public static final String COUPON_PARAM_LIMIT = "limit";
    public static final int COUPON_PARAM_LIMIT_DEFAULT = 10;

    /**
     * 优惠券状态 state说明
     1	已领取（还未使用）
     5	已使用
     10	已过期
     */
    public static final int COUPON_STATE_UNCONSUMED = 1;
    public static final int COUPON_STATE_USED = 5;
    public static final int COUPON_STATE_EXPIRED = 10;


    //////////////////////////////////////////Bundle Extra//////////////////////////////////////////
    public static final String EXTRA_COUPON_EXPIRED_FRAGMENT_TYPE = "viewType";
    public static final int  REQUEST_CHECK_EXPIRED =1001;
    public static final String EXTRA_COUPON_BEAN ="CouponObj";
    public static final String EXTRA_COUPON_BEAN_ID ="CouponObjID";

    public static final String EXTRA_COUPON_EXPIRED_MSG_CHECK = "msg_check";



}
