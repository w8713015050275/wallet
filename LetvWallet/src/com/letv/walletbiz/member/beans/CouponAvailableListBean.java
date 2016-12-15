package com.letv.walletbiz.member.beans;

/**
 * Created by zhangzhiwei1 on 16-11-21.
 */

import com.letv.wallet.common.http.beans.LetvBaseBean;

public class CouponAvailableListBean implements LetvBaseBean {

    public int num;
    public CouponBean[] list;
}
