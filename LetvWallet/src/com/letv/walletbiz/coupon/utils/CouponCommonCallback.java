package com.letv.walletbiz.coupon.utils;

/**
 * Created by lijunying on 16-4-18.
 */
public interface CouponCommonCallback<T> {

    int NO_ERROR = -1;
    int ERROR_NO_NETWORK = 1;
    int ERROR_NETWORK = 2;
    int ERROR_PARAM = 4;
    int ERROR_PARSE_JSON = 5;
    int ERROR_NO_DATA = 6;

    void onLoadFinished(T result, int errorCode);

}
