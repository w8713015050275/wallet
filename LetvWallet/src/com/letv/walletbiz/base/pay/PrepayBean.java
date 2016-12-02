package com.letv.walletbiz.base.pay;

import com.letv.wallet.common.http.beans.LetvBaseBean;

/**
 * Created by linquan on 15-11-26.
 */
public class PrepayBean implements LetvBaseBean {
    String pay_info;

    public String getPayinfo() {
        return pay_info;
    }
}

