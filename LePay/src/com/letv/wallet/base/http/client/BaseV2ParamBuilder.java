package com.letv.wallet.base.http.client;

import android.os.Build;
import android.text.TextUtils;

import com.letv.wallet.common.util.DeviceUtils;
import com.letv.wallet.PayApplication;
import com.letv.wallet.base.http.LeSignature;
import com.letv.wallet.base.http.UserAgent;
import com.letv.wallet.common.util.EnvUtil;

import org.xutils.common.util.KeyValue;
import org.xutils.http.RequestParams;
import org.xutils.http.app.DefaultParamsBuilder;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;

/**
 * Created by linquan on 15-12-31.
 */
public class BaseV2ParamBuilder extends DefaultParamsBuilder

{

    public BaseV2ParamBuilder() {

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

        // build query string parameter as API requested fields
        String APPKEY;
        if (EnvUtil.getInstance().isTest()) {
            APPKEY = "fertE2E4QcLjRAHGxcZy";
        } else {
            APPKEY = "hhBPZS1z9vizldPWHyct";
        }

        long timestamp = System.currentTimeMillis();
        String date = LeSignature.formatDate(timestamp);

        try {
            //get path
            URI u = new URI(params.getUri());
            String path = u.getPath();

            //to sign
            String sign = LeSignature.getSignature(params.getMethod().toString(),
                    path, null, date, getParamMap(params));

            //set to header Authorization
            params.setHeader("Authorization", String.format(" LETV %s %s", APPKEY, sign));
            params.setHeader("le_model", Build.MODEL);
            String imei = DeviceUtils.getDeviceImei(PayApplication.getApplication());
            if (!TextUtils.isEmpty(imei)) {
                params.setHeader("le_imei", imei);
            }
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        params.setHeader("Date", date);

    }

    private HashMap<String, String> getParam2Map(HashMap<String, String> mapParams, List<KeyValue> params) {

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
        getParam2Map(mapParams, params.getBodyParams());
        return mapParams;
    }

    /**
     * 自定义参数签名
     *
     * @param params
     * @param signs
     */
    @Override
    public void buildSign(RequestParams params, String[] signs) {

    }
    //public long timestamp = System.currentTimeMillis();
    //public File uploadFile;
}
