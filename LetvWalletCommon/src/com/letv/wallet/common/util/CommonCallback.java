package com.letv.wallet.common.util;

/**
 * Created by changjiajie on 16-1-14.
 */
public interface CommonCallback<T> {

    int NO_ERROR = -1;
    int ERROR_NO_NETWORK = 1;
    int ERROR_NETWORK = 2;
    int ERROR_DB_QUERY = 3;
    int ERROR_PARAM = 4;
    int ERROR_PARSE_JSON = 5;


    void onLoadFinished(T result, int errorCode);
}
