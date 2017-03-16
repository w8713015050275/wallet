package com.letv.walletbiz.main.recommend;

import android.content.Context;
import android.location.Address;
import android.os.Build;
import android.text.TextUtils;

import com.google.gson.reflect.TypeToken;
import com.letv.wallet.common.http.beans.BaseResponse;
import com.letv.wallet.common.util.AccountHelper;
import com.letv.wallet.common.util.AppUtils;
import com.letv.wallet.common.util.DeviceUtils;
import com.letv.walletbiz.base.http.client.BaseRequestParams;
import com.letv.walletbiz.main.recommend.bean.RecommendCardBean;

import org.xutils.xmain;

import java.util.List;

/**
 * Created by liuliang on 17-1-20.
 */

public class RecommendHelper {


    public static BaseResponse<List<RecommendCardBean>> getRecommendCardListFromNetWork(Context context, int limit, int offset, Address address) {
        BaseRequestParams params = getRecommendCardListRequestParams(context, limit, offset, address);
        BaseResponse<List<RecommendCardBean>> response;
        try {
            TypeToken typeToken = new TypeToken<BaseResponse<List<RecommendCardBean>>>() {};
            response = xmain.http().getSync(params, typeToken.getType());
        } catch (Throwable throwable) {
            response = null;
        }
        return response;
    }

    private static BaseRequestParams getRecommendCardListRequestParams(Context context, int limit, int offset, Address address) {
        BaseRequestParams params = new BaseRequestParams(RecommendConstant.PATH_RECOMMEND_CARD_LIST);
        params.addParameter(RecommendConstant.PARAM_SSO_TK, AccountHelper.getInstance().getToken(context));
        params.addParameter(RecommendConstant.PARAM_IMEI, DeviceUtils.getDeviceImei(context));

        String version = AppUtils.getAppFullVersionName(context);
        if (!TextUtils.isEmpty(version)) {
            params.addParameter(RecommendConstant.PARAM_V, version);
        }

        params.addParameter(RecommendConstant.PARAM_SYS_V, Build.ID);
        params.addParameter(RecommendConstant.PARAM_LE_MODEL, Build.MODEL);
        params.addParameter(RecommendConstant.PARAM_LIMIT, limit);
        params.addParameter(RecommendConstant.PARAM_OFFSET, offset);
        if (address != null) {
            params.addParameter(RecommendConstant.PARAM_LATITUDE, address.getLatitude());
            params.addParameter(RecommendConstant.PARAM_LONGITUDE, address.getLongitude());
            params.addParameter(RecommendConstant.PARAM_CITY_NAME, address.getLocality());
        }
        return params;
    }
}
