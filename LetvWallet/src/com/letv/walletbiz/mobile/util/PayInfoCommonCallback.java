package com.letv.walletbiz.mobile.util;

/**
 * Created by liuliang on 16-1-14.
 */
public interface PayInfoCommonCallback<T> {

    int NO_ERROR            = -1;
    int ERROR_NO_NETWORK    = 1;
    int ERROR_NETWORK       = 2;
    int ERROR_DB_QUERY      = 3;
    int ERROR_PARAM         = 4;
    int ERROR_PARSE_JSON    = 5;


    void onLoadPayInfoFinished(T result, int errorCode);
}
