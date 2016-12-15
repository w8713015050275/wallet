package com.letv.walletbiz.member.util;

/**
 * Created by zhangzhiwei1 on 16-11-21.
 */

public interface MemberCommonCallback<T> {

    int NO_ERROR            = -1;
    int ERROR_NO_NETWORK    = 1;
    int ERROR_NETWORK       = 2;
    int ERROR_DB_QUERY      = 3;
    int ERROR_PARAM         = 4;
    int ERROR_PARSE_JSON    = 5;


    void onLoadFinished(T result, int errorCode,boolean needUpdate);
}
