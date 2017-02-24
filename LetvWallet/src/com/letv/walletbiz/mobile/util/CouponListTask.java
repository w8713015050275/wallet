package com.letv.walletbiz.mobile.util;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;

import com.google.gson.reflect.TypeToken;
import com.letv.wallet.common.http.beans.BaseResponse;
import com.letv.wallet.common.util.CommonCallback;
import com.letv.walletbiz.base.http.client.BaseRequestParams;
import com.letv.walletbiz.base.util.JsonUitls;
import com.letv.walletbiz.base.util.JsonWrapper;
import com.letv.walletbiz.mobile.MobileConstant;
import com.letv.walletbiz.mobile.beans.CouponBean;
import com.letv.walletbiz.mobile.beans.CouponListBean;

import org.xutils.xmain;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by liuliang on 16-4-19.
 */
public class CouponListTask implements Runnable {

    private Context mContext;
    private CommonCallback<CouponListBean<CouponBean>> mCallback;

    private static int mNum = 1;
    private String mUtoken;
    private String mSkuSN;

    private int mErrorCode = CommonCallback.NO_ERROR;

    private static final int MSG_LOAD_FINISHED = 1;
    private Handler mHandler = new Handler(Looper.getMainLooper()) {

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_LOAD_FINISHED:
                    if (mCallback != null) {
                        mCallback.onLoadFinished((CouponListBean<CouponBean>) msg.obj, mErrorCode);
                    }
                    break;
            }
        }
    };

    public CouponListTask(Context context, CommonCallback<CouponListBean<CouponBean>> callback
            , String uToken, String skuSN) {
        mUtoken = uToken;
        mSkuSN = skuSN;
        mContext = context;
        mCallback = callback;
    }

    @Override
    public void run() {
        CouponListBean<CouponBean> couponList = getCouponListFromNetwork();
        Message msg = mHandler.obtainMessage(MSG_LOAD_FINISHED);
        msg.obj = couponList;
        mHandler.sendMessage(msg);
    }

    private CouponListBean<CouponBean> getCouponListFromNetwork() {
        BaseResponse<CouponListBean<CouponBean>> response = null;
        try {
            BaseRequestParams reqParams = new BaseRequestParams(MobileConstant.PATH.COUPON);
            reqParams.addQueryStringParameter(MobileConstant.PARAM.SSO_TK, mUtoken);
            List<JsonWrapper> jsons = new ArrayList<>();
            JsonWrapper obj = new JsonWrapper();
            obj.addData(MobileConstant.PARAM.SKU, mSkuSN);
            obj.addData(MobileConstant.PARAM.NUM, mNum);
            jsons.add(obj);
            reqParams.addQueryStringParameter(MobileConstant.PARAM.SKU_SN, JsonUitls.getJsonsString(jsons));
            TypeToken typeToken = new TypeToken<BaseResponse<CouponListBean<CouponBean>>>() {
            };
            response = xmain.http().postSync(reqParams, typeToken.getType());
        } catch (Exception e) {
            mErrorCode = CommonCallback.ERROR_NETWORK;
        } catch (Throwable throwable) {
            mErrorCode = CommonCallback.ERROR_NETWORK;
        }
        return response == null ? null : response.data;
    }
}
