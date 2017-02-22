package com.letv.walletbiz.main.recommend;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.location.Address;
import android.os.Build;

import com.google.gson.reflect.TypeToken;
import com.letv.wallet.common.http.beans.BaseResponse;
import com.letv.wallet.common.util.AccountHelper;
import com.letv.wallet.common.util.CommonCallback;
import com.letv.wallet.common.util.DeviceUtils;
import com.letv.wallet.common.util.LogHelper;
import com.letv.walletbiz.base.http.client.BaseRequestParams;
import com.letv.walletbiz.base.http.client.BaseV2ParamBuilder;
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
    private Context mContext;
    private Address mAddress;

    public RecommendFooterTask(Context context, CommonCallback<List<CardFooter>> callback,
                               String url, Map<String, String> param, String httpMethod, Address address) {
        mCallback = callback;
        mUrl = url;
        mParam = param;
        mHttpMethod = httpMethod;
        mContext = context;
        mAddress = address;
    }

    @Override
    public void run() {
        BaseRequestParams params = new BaseRequestParams(mUrl, new BaseV2ParamBuilder(), null, null);
        if (mParam != null) {
            for (String key : mParam.keySet()) {
                params.addParameter(key, mParam.get(key));
            }
        }
        params.addParameter(RecommendConstant.PARAM_SSO_TK, AccountHelper.getInstance().getToken(mContext));
        params.addParameter(RecommendConstant.PARAM_IMEI, DeviceUtils.getDeviceImei(mContext));
        try {
            PackageInfo packageInfo = mContext.getPackageManager().getPackageInfo(mContext.getPackageName(), 0);
            if (packageInfo != null) {
                params.addParameter(RecommendConstant.PARAM_V, packageInfo.versionName);
            }
        } catch (PackageManager.NameNotFoundException e) {
            LogHelper.e(e);
        }

        params.addParameter(RecommendConstant.PARAM_SYS_V, Build.ID);
        params.addParameter(RecommendConstant.PARAM_LE_MODEL, Build.MODEL);
        if (mAddress != null) {
            params.addParameter(RecommendConstant.PARAM_LATITUDE, mAddress.getLatitude());
            params.addParameter(RecommendConstant.PARAM_LONGITUDE, mAddress.getLongitude());
            params.addParameter(RecommendConstant.PARAM_CITY_NAME, mAddress.getLocality());
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
