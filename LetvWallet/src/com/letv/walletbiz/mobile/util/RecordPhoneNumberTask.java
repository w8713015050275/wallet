package com.letv.walletbiz.mobile.util;

import android.text.TextUtils;

import com.letv.walletbiz.WalletApplication;
import com.letv.walletbiz.mobile.beans.HistoryRecordNumberBean;
import com.letv.walletbiz.mobile.dbhelper.HistoryRecordHelper;
import com.letv.walletbiz.mobile.pay.MobileProduct;

/**
 * Created by changjiajie on 16-12-28.
 */

public class RecordPhoneNumberTask implements Runnable {
    private MobileProduct mMobileProduct;

    public RecordPhoneNumberTask(MobileProduct mobileProduct) {
        mMobileProduct = mobileProduct;
    }

    @Override
    public void run() {
        if (!TextUtils.isEmpty(mMobileProduct.getNumber())) {
            HistoryRecordNumberBean.RecordInfoBean recordInfoBean = new HistoryRecordNumberBean.RecordInfoBean();
            recordInfoBean.setPhoneNum(mMobileProduct.getNumber());
            recordInfoBean.setTime(System.currentTimeMillis());
                        /*
                        TODO: Insert failure such as statistical work
                         */
            boolean insertState = HistoryRecordHelper.insertContactToDBsync(WalletApplication.getApplication(), recordInfoBean);
        }
    }
}
