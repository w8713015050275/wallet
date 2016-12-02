package com.letv.wallet.common.http.beans;

import com.letv.wallet.common.util.JsonResponseParser;

import org.xutils.http.annotation.HttpResponse;

/**
 * Created by linquan on 15-12-30.
 */
@HttpResponse(parser = JsonResponseParser.class)
public class BaseResponse<T> implements LetvBaseBean{
    public int errno = 10000;
    public String errmsg = null;

    public T data;
}