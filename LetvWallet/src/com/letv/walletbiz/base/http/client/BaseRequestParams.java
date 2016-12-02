package com.letv.walletbiz.base.http.client;

import android.text.TextUtils;

import com.letv.wallet.common.util.DomainHelper;

import org.xutils.http.RequestParams;
import org.xutils.http.app.ParamsBuilder;


/**
 * Created by linquan on 15-12-30.
 */
public class BaseRequestParams  extends RequestParams {


    public BaseRequestParams( String path) {
        this(path, new BaseV2ParamBuilder());
    }

    public BaseRequestParams(String host, String path) {
        this(host,path, new BaseV2ParamBuilder());
    }

    public BaseRequestParams( String path,ParamsBuilder builder) {
        this(null, path , builder);
    }

    public BaseRequestParams(String host, String path, ParamsBuilder builder) {
        this(host + "/" + path, builder, null, null);
    }

    public BaseRequestParams(String uri, ParamsBuilder builder, String[] signs, String[] cacheKeys) {
        super(uri, builder, signs, cacheKeys);
    }

    @Override
    protected String checkDomain(String uri) {
        String host;
        if (!TextUtils.isEmpty(uri) && uri.startsWith("null")) {
            host = DomainHelper.getInstance().getHost();
            if (!TextUtils.isEmpty(host)) {
                uri = uri.replaceFirst("null", host);
            }
        }
        return uri;
    }
}
