package com.letv.walletbiz.coupon.utils;

import android.content.Context;

import com.google.gson.reflect.TypeToken;
import com.letv.wallet.common.http.beans.BaseResponse;
import com.letv.wallet.common.util.AccountHelper;
import com.letv.walletbiz.base.http.client.BaseRequestParams;
import com.letv.walletbiz.coupon.CouponConstant;
import com.letv.walletbiz.coupon.beans.CouponExpiredListResponseResult;
import com.letv.walletbiz.coupon.beans.CouponListRequestParams;
import com.letv.walletbiz.coupon.beans.CouponListResponseResult;

import org.xutils.xmain;

/**
 * Created by lijunying on 16-4-18.
 */
public class CouponListHelper {
    /**
     * 获取用户优惠券列表
     * @param context
     * @param reqParams
     * @return
     */

    public static BaseResponse<CouponListResponseResult> getCouponListOnlineSync(Context context, CouponListRequestParams reqParams) {
        BaseRequestParams params = new BaseRequestParams(CouponConstant.COUPON_LIST_PATH);
        String token = AccountHelper.getInstance().getToken(context);
        params.addParameter(CouponConstant.COUPON_PARAM_SSO_TK, token);
        if (reqParams.getLast_id() > 0) {
            params.addParameter(CouponConstant.COUPON_PARAM_LAST_ID, reqParams.getLast_id());
        }
        params.addParameter(CouponConstant.COUPON_PARAM_LIMIT, reqParams.getLimit() == 0 ? CouponConstant.COUPON_PARAM_LIMIT_DEFAULT : reqParams.getLimit());
        params.addParameter(CouponConstant.COUPON_PARAM_MODEL, reqParams.getModel() == 0 ? CouponConstant.COUPON_PARAM_MODEL_DEFAULT : reqParams.getModel());
        BaseResponse<CouponListResponseResult > response = null;
        try {
            TypeToken typeToken = new TypeToken<BaseResponse<CouponListResponseResult>>() {};
            response = xmain.http().getSync(params, typeToken.getType());
        } catch (Throwable throwable) {
            response = null;
        }
        return response;
    }

    /**
     * 获取已失效的优惠券列表
      * @param context
     * @param reqParams
     * @return
     */

    public static BaseResponse<CouponExpiredListResponseResult> getCouponExpiredListOnlineSync(Context context, CouponListRequestParams reqParams) {
        BaseRequestParams params = new BaseRequestParams(CouponConstant.COUPON_UNAVAILABLE_LIST_PATH);
        String token = AccountHelper.getInstance().getToken(context);
        params.addParameter(CouponConstant.COUPON_PARAM_SSO_TK, token);
        if (reqParams.getLast_id() != 0) {
            params.addParameter(CouponConstant.COUPON_PARAM_LAST_ID, reqParams.getLast_id());
        }
        params.addParameter(CouponConstant.COUPON_PARAM_LIMIT, reqParams.getLimit() == 0 ? CouponConstant.COUPON_PARAM_LIMIT_DEFAULT : reqParams.getLimit());
        params.addParameter(CouponConstant.COUPON_PARAM_MODEL, reqParams.getModel() == 0 ? CouponConstant.COUPON_PARAM_MODEL_DEFAULT : reqParams.getModel());
        BaseResponse<CouponExpiredListResponseResult > response = null;
        try {
            TypeToken typeToken = new TypeToken<BaseResponse<CouponExpiredListResponseResult>>() {};
            response = xmain.http().getSync(params, typeToken.getType());
        } catch (Throwable throwable) {
            response = null;
        }
        return response;
    }


}
