package com.letv.walletbiz.main.recommend;

import com.google.gson.reflect.TypeToken;
import com.letv.wallet.common.http.beans.BaseResponse;
import com.letv.wallet.common.util.CommonCallback;
import com.letv.wallet.common.util.LogHelper;
import com.letv.walletbiz.base.http.client.BaseRequestParams;
import com.letv.walletbiz.main.recommend.bean.RecommendCardBean.CardFooter;

import org.xutils.http.HttpMethod;
import org.xutils.xmain;

import java.util.List;
import java.util.Map;

/**
 * Created by liuliang on 17-2-13.
 */

public class RecommendFooterTask implements Runnable {

    private String mUrl;
    private Map<String, String> mParam;
    private String mHttpMethod;
    private int mErrorCode = CommonCallback.NO_ERROR;
    private CommonCallback<List<CardFooter>> mCallback;

    public RecommendFooterTask(CommonCallback<List<CardFooter>> callback,
                               String url, Map<String, String> param, String httpMethod) {
        mCallback = callback;
        mUrl = url;
        mParam = param;
        mHttpMethod = httpMethod;
    }

    @Override
    public void run() {
        BaseRequestParams params = new BaseRequestParams(mUrl);
        if (mParam != null) {
            for (String key : mParam.keySet()) {
                params.addParameter(key, mParam.get(key));
            }
        }
        TypeToken<BaseResponse<List<CardFooter>>> typeToken = new TypeToken<BaseResponse<List<CardFooter>>>() {};
        BaseResponse<List<CardFooter>> response = null;
        try {
            if (HttpMethod.GET.toString().equalsIgnoreCase(mHttpMethod)) {
                response = xmain.http().getSync(params, typeToken.getType());
            } else if (HttpMethod.POST.toString().equalsIgnoreCase(mHttpMethod)) {
                response = xmain.http().postSync(params, typeToken.getType());
            }
            if (response == null || response.errno != 10000) {
                mErrorCode = CommonCallback.ERROR_NETWORK;
            }
            if (mCallback != null) {
                mCallback.onLoadFinished(response.data, mErrorCode);
            }
        } catch (Throwable throwable) {
            LogHelper.e(throwable);
        }
    }
}
