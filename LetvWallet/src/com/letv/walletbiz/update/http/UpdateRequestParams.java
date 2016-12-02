package com.letv.walletbiz.update.http;

import com.letv.walletbiz.base.http.client.BaseV2ParamBuilder;

import org.xutils.http.RequestParams;
import org.xutils.http.app.ParamsBuilder;

/**
 * Created by zhangzhiwei1 on 16-8-18.
 */
public class UpdateRequestParams extends RequestParams {

    public UpdateRequestParams( String uri) {
        this(uri, new UpdateParamBuilder());
    }

    public UpdateRequestParams(String uri, ParamsBuilder builder) {
        super(uri, builder, null, null);
    }
}
