package com.letv.walletbiz.base.http.beans.order;

import android.text.TextUtils;

import com.letv.walletbiz.base.http.client.BaseRequestParams;

import java.io.Serializable;

/**
 * Created by changjiajie on 16-1-21.
 */
public class OrderRequestBean implements Serializable {

    public BaseRequestParams reqParams;

    public OrderRequestBean(String path) {
        if (path == null || path.equals("")) return;
        reqParams = new BaseRequestParams(path);
    }

    public OrderRequestBean(String host, String path) {
        if (TextUtils.isEmpty(host) || TextUtils.isEmpty(path))
            return;
        reqParams = new BaseRequestParams(host, path);
    }

    public void addQueryStringParameter(String name, String value) {
        reqParams.addQueryStringParameter(name, value);
    }

}
