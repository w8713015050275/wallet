package com.letv.walletbiz.mobile.pay;

import android.content.Context;

import com.letv.wallet.common.util.ExecutorHelper;
import com.letv.walletbiz.base.pay.BasePayAdapter;
import com.letv.walletbiz.base.pay.Constants;
import com.letv.walletbiz.mobile.MobileConstant;
import com.letv.walletbiz.mobile.util.RecordPhoneNumberTask;

import java.util.HashMap;

/**
 * Created by linquan on 15-12-7.
 */
public class MobilePayAdapter implements BasePayAdapter {
    MobileProduct mProduct;

    public MobilePayAdapter(MobileProduct product) {
        mProduct = product;
    }

    @Override
    public String getPrepayInfo(Context context, int platform) {
        return null;
    }

    @Override
    public String getPrepayInfo(Context context) {
        return mProduct.getPrepayInfo();
    }

    @Override
    public String getPrepayInfoPath() {
        return MobileConstant.PATH.SDKPAY;
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
        //Todo: Add operation of save phonenumber to cache here
        switch (state) {
            case Constants.RESULT_STATUS.SUCCESS:
                /*
                再 PayActivity 中运行
                 */
                if (mProduct != null) {
                    ExecutorHelper.getExecutor().runnableExecutor(new RecordPhoneNumberTask(mProduct));
                }
                break;
            case Constants.RESULT_STATUS.FAIL:
            case Constants.RESULT_STATUS.PENDING:
                break;
        }
        return true;
    }
}