package com.letv.wallet.online.utils;

/**
 * Created by changjiajie on 17-1-9.
 */
public interface LePayOnlineCallback<T> {

    int NO_ERROR = -1;
    int ERROR_NETWORK = 1;
    int ERROR_DATA = 2;
    int ERROR_OTHER = 3;

    void onSuccess(T result);

    void onError(T result, int errorCode);
}
