package com.letv.walletbiz.base.pay;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.Toast;

import com.google.gson.reflect.TypeToken;
import com.letv.wallet.common.http.RspDataException;
import com.letv.wallet.common.http.beans.BaseResponse;
import com.letv.wallet.common.http.client.RspConstants;
import com.letv.wallet.common.util.AccountHelper;
import com.letv.wallet.common.util.LogHelper;
import com.letv.walletbiz.BuildConfig;
import com.letv.walletbiz.R;
import com.letv.walletbiz.base.activity.ActivityConstant;
import com.letv.walletbiz.base.activity.PayResultActivity;
import com.letv.walletbiz.base.http.client.BaseRequestParams;
import com.letv.walletbiz.base.util.PayHelper;

import org.json.JSONException;
import org.xutils.xmain;

import java.io.Serializable;

/**
 * Created by linquan on 15-12-8.
 */
public abstract class Product implements Serializable {
    private final static String TAG = "Product";

    protected BasePayAdapter mPayAdapter;
    protected PayResultActivity.PayResultAdapter mResultAdapter;

    protected int mTitle;
    protected String mSN;
    protected int mProductId;
    protected String mName;
    protected long mTime;
    protected int mStatus;
    protected String mPrice;
    protected String mRealPrice;
    protected String mPrepayInfo;

    protected int mPayResult;
    protected String mPayStatus;
    private boolean mPaying = false;

    public Product() {
    }

    public int getProductId() {
        return mProductId;
    }

    public int getTitle() {

        return mTitle;
    }

    public String getSN() {

        return mSN;
    }

    public long getTime() {
        return mTime;
    }

    public int getStatus() {

        return mStatus;
    }

    public String getRealPrice() {
        return mRealPrice;
    }

    public void setRealPrice(String mRealPrice) {
        this.mRealPrice = mRealPrice;
    }

    public String getPrice() {

        return mPrice;
    }

    public void setPrice(String mPrice){
        this.mPrice = mPrice;
    }

    public String getProductName() {

        return mName;
    }


    public String getPayStatus() {

        return mPayStatus;
    }

    public int getPayResult() {

        return mPayResult;
    }

    protected void setPrepayInfo(String info) {
        mPrepayInfo = info;
    }

    public String getPrepayInfo() {
        return mPrepayInfo;
    }

    private boolean checkIsPayAdpater() {
        LogHelper.d("[%s] Check Caller's PayAdpater , found %s", TAG, mPayAdapter.getClass());
        return PayAdapter.class.isInstance(mPayAdapter);
    }


    public void pay(Context context) {
        if (checkIsPayAdpater()) {
            PayHelper.startPayActivity(context, this);

        } else {
            if (!mPaying) {
                mPaying = true;
                prePayTask task = new prePayTask(context);
                task.execute();
            }
        }
    }

    public void terminatePay() {
        if (checkIsPayAdpater()) {
        }
    }

    protected void payWithInfo(Context context, String payInfo) {
        LogHelper.d("[Product] : payWithInfo payInfo =" + payInfo);
        try {
            if (payInfo == null) return;
            Intent intent = new Intent(ActivityConstant.PAY_PARAM.PAY_ACTION);
            Bundle bundle = new Bundle();
            bundle.putString(Constants.INFO_PARAM.LEPAY_INFO, payInfo);
            intent.putExtras(bundle);
            ((Activity) context).startActivityForResult(intent, Constants.REQUEST.REQUEST_CODE);
        } catch (Exception e) {
        }
    }

    public BasePayAdapter getPayAdapter() {
        return mPayAdapter;
    }

    public PayResultActivity.PayResultAdapter getPayResultAdapter() {
        return mResultAdapter;
    }

    public void showPayResult(Context context, int result, String status) {
        mPayResult = result;
        mPayStatus = status;
        if (result == Constants.RESULT_STATUS.SUCCESS) {
            PayHelper.startPayResultActivity(context, this);
        }
    }

    private class prePayTask extends AsyncTask<Integer, Integer, Integer> implements Serializable {
        Context mContext;

        public prePayTask(Context context) {
            mContext = context;
        }

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected Integer doInBackground(Integer... params) {
            String payInfo;
            try {
                payInfo = getPrepayInfo(mContext, mPayAdapter.getPrepayInfoPath(), getSN());
                setPrepayInfo(payInfo);
            } catch (Throwable throwable) {
                if (throwable.getClass() == JSONException.class)
                    return Constants.PAY_STATUS.STATUS_PAYINFO_ERROR;
                return Constants.PAY_STATUS.STATUS_NETWORK_ERROR;
            }
            return Constants.PAY_STATUS.STATUS_PAY_STARTED;
        }

        @Override
        protected void onPostExecute(Integer result) {
            LogHelper.d("[%S] onPostExecute result %d", TAG, result);
            switch (result) {
                case Constants.PAY_STATUS.STATUS_PAY_STARTED:
                    payWithInfo(mContext, mPayAdapter.getPrepayInfo(mContext));
                    break;
                case Constants.PAY_STATUS.STATUS_PAYINFO_ERROR:
                    Toast.makeText(mContext, R.string.pay_info_response_error,
                            Toast.LENGTH_SHORT).show();
                    break;
                case Constants.PAY_STATUS.STATUS_NETWORK_ERROR:
                    Toast.makeText(mContext, R.string.pay_network_error,
                            Toast.LENGTH_SHORT).show();
                    break;
            }

            if (result != Constants.PAY_STATUS.STATUS_PAY_STARTED) {
                mPaying = false;
            }

        }
    }


    public String getPrepayInfo(Context context, String path, String order_no) throws Throwable {
        BaseResponse<PrepayBean> response;

        String uToken = AccountHelper.getInstance().getToken(context);
        BaseRequestParams reqParams = new BaseRequestParams(path);

        reqParams.addBodyParameter(Constants.INFO_PARAM.ORDER_SN, order_no);
        reqParams.addBodyParameter(Constants.INFO_PARAM.TOKEN, uToken);
        reqParams.addBodyParameter(Constants.INFO_PARAM.CLIENT, Constants.INFO_VALUE.CLIENT_ID);
        if("debug".equalsIgnoreCase(BuildConfig.BUILD_TYPE)){
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
