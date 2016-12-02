package com.letv.walletbiz.mobile.pay;

import android.content.Context;

import com.letv.walletbiz.WalletApplication;
import com.letv.walletbiz.base.pay.BasePayAdapter;
import com.letv.walletbiz.base.pay.Constants;
import com.letv.walletbiz.mobile.MobileConstant;
import com.letv.walletbiz.mobile.beans.HistoryRecordNumberBean;
import com.letv.walletbiz.mobile.dbhelper.HistoryRecordHelper;

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
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        HistoryRecordNumberBean.RecordInfoBean recordInfoBean = new HistoryRecordNumberBean.RecordInfoBean();
                        recordInfoBean.setPhoneNum(mProduct.getNumber());
                        recordInfoBean.setTime(System.currentTimeMillis());
                        /*
                        TODO: Insert failure such as statistical work
                         */
                        boolean insertState = HistoryRecordHelper.insertContactToDBsync(WalletApplication.getApplication(), recordInfoBean);
                    }
                }).start();
                break;
            case Constants.RESULT_STATUS.FAIL:
            case Constants.RESULT_STATUS.PENDING:
                break;
        }
        return true;
    }
}