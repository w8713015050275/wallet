package com.letv.walletbiz.movie.pay;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import com.google.gson.reflect.TypeToken;
import com.letv.wallet.common.http.beans.BaseResponse;
import com.letv.wallet.common.util.AccountHelper;
import com.letv.walletbiz.R;
import com.letv.walletbiz.base.http.client.BaseRequestParams;
import com.letv.walletbiz.base.pay.PayAdapter;
import com.letv.walletbiz.movie.MovieTicketConstant;
import com.letv.walletbiz.movie.beans.MovieProduct;

import org.xutils.xmain;

import java.util.HashMap;

/**
 * Created by liuliang on 16-2-16.
 */
public class MoviePayAdapter implements PayAdapter {

    private MovieProduct mProduct;

    public MoviePayAdapter(MovieProduct product) {
        mProduct = product;
    }
    @Override
    public int getTitle() {
        return R.string.movie_pay_order;
    }

    @Override
    public String getCost() {
        return mProduct.getPrice();
    }

    @Override
    public int getTimeLimitation() {
        return 0;
    }

    @Override
    public View createContentView(final Context context, ViewGroup parent) {
        return null;
    }

    @Override
    public View createFootNotedView(Context context, ViewGroup parent) {
        return null;
    }

    @Override
    public String getPrepayInfo(Context context, int platform) {
        AccountHelper accountHelper = AccountHelper.getInstance();
        BaseRequestParams params = new BaseRequestParams(MovieTicketConstant.MOVIE_PATH_ORDER_PAY);
        params.addParameter(MovieTicketConstant.MOVIE_PARAM_SSO_TK, accountHelper.getToken(context));
        params.addParameter(MovieTicketConstant.MOVIE_PARAM_PHONE, mProduct.getPhoneNumber());
        params.addParameter(MovieTicketConstant.MOVIE_PARAM_ORDER_NO, mProduct.getSN());
        params.addParameter(MovieTicketConstant.MOVIE_PARAM_PLATFORM, platform);
        TypeToken<BaseResponse<String>> typeToken = new TypeToken<BaseResponse<String>>(){};
        BaseResponse<String> response = null;
        try {
            response = xmain.http().postSync(params, typeToken.getType());
            if (response != null) {
                return response.data;
            }
        } catch (Throwable throwable) {
        }
        return null;
    }

    @Override
    public String getPrepayInfo(Context context) {
        return null;
    }

    @Override
    public String getPrepayInfoPath() {
        return null;
    }

    @Override
    public HashMap<String, String> getQueryParamMap() {
        return null;
    }

    @Override
    public HashMap<String, String> getBodyParamMap() {
        return null;
    }

    @Override
    public boolean addToken() {
        return false;
    }

    @Override
    public boolean onPayResult(int state) {
        return false;
    }

}
