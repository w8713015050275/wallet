package com.letv.walletbiz.base.util;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;

import com.google.gson.reflect.TypeToken;
import com.letv.wallet.common.http.RspDataException;
import com.letv.wallet.common.http.beans.BaseResponse;
import com.letv.wallet.common.http.client.RspConstants;
import com.letv.wallet.common.util.AccountHelper;
import com.letv.wallet.common.util.EnvUtil;
import com.letv.walletbiz.BuildConfig;
import com.letv.walletbiz.R;
import com.letv.walletbiz.base.activity.ActivityConstant;
import com.letv.walletbiz.base.activity.PayActivity;
import com.letv.walletbiz.base.activity.PayResultActivity;
import com.letv.walletbiz.base.http.client.BaseRequestParams;
import com.letv.walletbiz.base.pay.Constants;
import com.letv.walletbiz.base.pay.PrepayBean;
import com.letv.walletbiz.base.pay.Product;
import com.letv.walletbiz.member.pay.MemberProduct;
import com.letv.walletbiz.mobile.pay.MobileProduct;
import com.letv.walletbiz.movie.beans.MovieProduct;

import org.xutils.xmain;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;

/**
 * Created by linquan on 15-11-30.
 */
public class PayHelper implements Serializable {
    private final static String TAG = "PayHelper";


    public static void startPayActivity(Context context, Product product) {
        Intent intent = new Intent(context, PayActivity.class);
        Bundle b = new Bundle();
        b.putSerializable(ActivityConstant.PAY_PARAM.PAY_PRODUCT, product);
        intent.putExtras(b);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    public static void startPayResultActivity(Context context, Product product) {
        Intent intent = new Intent(context, PayResultActivity.class);
        Bundle b = new Bundle();
        b.putSerializable(ActivityConstant.PAY_PARAM.PAY_PRODUCT, product);
        if (product instanceof MobileProduct) {
            b.putInt(ActivityConstant.PAY_PARAM.PAY_PRODUCT_THEME, R.style.MobileTheme);
        } else if (product instanceof MovieProduct) {
            b.putInt(ActivityConstant.PAY_PARAM.PAY_PRODUCT_THEME, R.style.MovieTheme);
        } else if (product instanceof MemberProduct) {
            b.putInt(ActivityConstant.PAY_PARAM.PAY_PRODUCT_THEME, R.style.MemberTheme);
        }
        intent.putExtras(b);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    public static String getPrepayInfo(Context context, String path, Map<String, String> reqMap) throws Throwable {
        BaseResponse<PrepayBean> response;

        String uToken = AccountHelper.getInstance().getToken(context);
        BaseRequestParams reqParams = new BaseRequestParams(path);

        reqParams.addBodyParameter(Constants.INFO_PARAM.TOKEN, uToken);
        reqParams.addBodyParameter(Constants.INFO_PARAM.CLIENT, Constants.INFO_VALUE.CLIENT_ID);
        if (!reqMap.isEmpty()) {
            Set<String> keySet = reqMap.keySet();
            for (String key : keySet) {
                if (TextUtils.isEmpty(key)) continue;
                reqParams.addBodyParameter(key, reqMap.get(key));
            }
        }
        if (EnvUtil.getInstance().isTest()) {
            reqParams.addParameter(Constants.INFO_PARAM.DBG, true);
        }

        TypeToken typeToken = new TypeToken<BaseResponse<PrepayBean>>() {
        };
        response = xmain.http().postSync(reqParams, typeToken.getType());

        if (response != null) {
            if (response.errno == RspConstants._RESPONSE.RSP_OK && response.data != null) {
                PrepayBean payBean = response.data;
                return payBean.getPayinfo();
            }
            throw new RspDataException(response.errno + " : " + response.errmsg);
        }
        return null;
    }
}
