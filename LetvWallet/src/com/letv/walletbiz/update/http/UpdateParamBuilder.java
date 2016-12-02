package com.letv.walletbiz.update.http;

import android.os.Build;
import android.text.TextUtils;
import android.util.Log;

import com.letv.walletbiz.base.http.LeSignature;
import com.letv.walletbiz.base.http.UserAgent;
import com.letv.walletbiz.update.UpdateConstant;

import org.xutils.common.util.KeyValue;
import org.xutils.http.RequestParams;
import org.xutils.http.app.DefaultParamsBuilder;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;

/**
 * Created by zhangzhiwei1 on 16-8-18.
 */
public class UpdateParamBuilder  extends DefaultParamsBuilder {

    public UpdateParamBuilder() {
    }

    /**
     * 为请求添加通用参数等操作
     *
     * @param params
     */
    @Override
    public void buildParams(RequestParams params) {
        //set user-agent
        params.setHeader("user-agent", UserAgent.ensureUserAgent(null));

        params.setHeader("Content-Type","application/x-www-form-urlencoded");
        long timestamp = System.currentTimeMillis();
        String date = LeSignature.formatDate(timestamp);
        try {
            String sign = LeSignature.getSignature(UpdateConstant.ACCESS_KEY, UpdateConstant.SECRET_KEY, getParamMap(params),timestamp);
            params.addQueryStringParameter("_ak",UpdateConstant.ACCESS_KEY);
            params.addQueryStringParameter("_time",""+timestamp);
            params.addQueryStringParameter("_sign",""+sign);

        } catch (Exception e) {
            e.printStackTrace();
        }

        params.setHeader("Date", date);
    }

    private HashMap<String, String> getParam2Map(HashMap<String, String> mapParams,List<KeyValue> params) {

        if (params != null) {
            for (KeyValue kv : params) {
                String name = kv.key;
                String value = kv.getValueStr();
                mapParams.put(name, value);
            }
        }
        return mapParams;
    }

    private HashMap<String, String> getParamMap(RequestParams params) {
        HashMap<String, String> mapParams = new HashMap<String, String>();
        getParam2Map(mapParams, params.getQueryStringParams());
        return mapParams;
    }
}
